import java.util.*;

public class GameLogic extends Position implements PlayableLogic {
    private Disc[][] board;
    private List<Move> moveHistory;
    private boolean firstPlayerTurn;
    private Player player1;
    private Player player2;
    private List<Disc> currentFlippedDiscs;
    private int tempCount;

    public GameLogic() {
        this(8, 0, 0, true, "GreedyAI"); // Default: Player 1 human, Player 2 is GreedyAI
    }

    public GameLogic(int boardSize, int row, int col, boolean isPlayerOneHuman, String player2AIType) {
        super(row, col);
        this.board = new Disc[boardSize][boardSize];
        this.moveHistory = new ArrayList<>();
        this.firstPlayerTurn = true;
        this.currentFlippedDiscs = new ArrayList<>();

        // Initialize players
        this.player1 = isPlayerOneHuman ? new HumanPlayer(true) : AIPlayer.createAIPlayer("GreedyAI", true);
        this.player2 = AIPlayer.createAIPlayer(player2AIType, false);

        // Initialize the board with starting positions
        initializeBoard();
    }

    private void initializeBoard() {
        board[3][3] = new SimpleDisc(player1);
        board[4][4] = new SimpleDisc(player1);
        board[3][4] = new SimpleDisc(player2);
        board[4][3] = new SimpleDisc(player2);
        locate_disc(new Position(3, 3), new SimpleDisc(player1));
        locate_disc(new Position(4, 4), new SimpleDisc(player1));
        locate_disc(new Position(3, 4), new SimpleDisc(player2));
        locate_disc(new Position(4, 3), new SimpleDisc(player2));
    }

    @Override
    public boolean locate_disc(Position position, Disc disc) {
        if (isValidMove(position)) {
            currentFlippedDiscs.clear();
            Stack<Disc> flippedDiscsStack = applyMove(position, disc, false);
            currentFlippedDiscs.addAll(flippedDiscsStack);

            moveHistory.add(new Move(firstPlayerTurn ? player1 : player2, disc, position));
            firstPlayerTurn = !firstPlayerTurn;
            return true;
        }
        return false;
    }

    private Stack<Disc> applyMove(Position position, Disc disc, boolean returnCountOnly) {
        Stack<Disc> flippedDiscs = new Stack<>();
        int flippedCount = 0; // To count flipped discs
        if (!returnCountOnly){
            board[position.getRow()][position.getColumn()] = disc;
            String format = String.format("Player 1 placed a %s in %s", disc.getType(), position.toString());
            System.out.println(format);
        }


        // Check in all directions
        int[][] directions = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
        };

        for (int[] dir : directions) {
            List<Disc> tempFlipped = new ArrayList<>();
            List<Position> bombPos = new ArrayList<>();
            int row = position.getRow() + dir[0];
            int col = position.getColumn() + dir[1];

            while (row >= 0 && row < board.length && col >= 0 && col < board[row].length) {
                Disc neighborDisc = getDiscAtPosition(new Position(row, col));
                if (neighborDisc == null || neighborDisc instanceof UnflippableDisc) {
                    break;
                }
                if (neighborDisc.getOwner() == (firstPlayerTurn ? player1 : player2)) {
                    for (Position pos : bombPos) {
                        Set<Disc> discFlipedByBomb = new HashSet<>();
                        bombFliped(pos, discFlipedByBomb);
                        tempFlipped.addAll(discFlipedByBomb);
                    }
                    flippedCount += tempFlipped.size();
                    if (!returnCountOnly) {  // Only flip discs if not counting
                        flippedDiscs.addAll(tempFlipped);
                        currentFlippedDiscs.addAll(tempFlipped);
                        for (Disc flipDisc : tempFlipped) {
                            flipDisc.setOwner(firstPlayerTurn ? player1 : player2);
                        }
                    }
                    break;
                }
                else {
                    tempFlipped.add(neighborDisc);
                    if (neighborDisc instanceof BombDisc){bombPos.add(new Position(row, col));}
                }

                row += dir[0];
                col += dir[1];
            }
        }

        if (returnCountOnly) {
            this.tempCount = flippedCount;
            return new Stack<>(); // Return an empty stack if counting
        }

        return flippedDiscs; // Return the stack of flipped discs
    }


    public void bombFliped(Position pos,Set<Disc> toFlip){
        int[][] directions = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
        };
        int row = pos.getRow();
        int col = pos.col();
        for (int[] dire: directions) {
            try{
                Disc fliped = getDiscAtPosition(new Position(row + dire[0], col + dire[1]));
                if ((fliped.getOwner() != (firstPlayerTurn ? player1 : player2)) && !(fliped instanceof UnflippableDisc)){
                    boolean added = toFlip.add(fliped);
                    if ((fliped instanceof BombDisc) && (added)){
                        bombFliped(new Position(row + dire[0], col + dire[1]), toFlip);
                    }
                }
            }catch (NullPointerException ignored){}

        }
    }

    @Override
    public Disc getDiscAtPosition(Position position) {
        return board[position.getRow()][position.getColumn()];
    }

    @Override
    public int getBoardSize() {
        return board.length;
    }

    @Override
    public List<Position> ValidMoves() {
        List<Position> validMoves = new ArrayList<>();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Position pos = new Position(row, col);
                if (isValidMove(pos))
                    validMoves.add(pos);
            }
        }
        return validMoves;
    }

    private boolean isValidMove(Position position) {
        if (getDiscAtPosition(position) != null)
            return false;
        applyMove(position, null, true);
        return (this.tempCount> 0);
    }

    @Override
    public int countFlips(Position position) {
        applyMove(position,null, true);
        return this.tempCount;
    }

    @Override
    public Player getFirstPlayer() {
        return player1;
    }

    @Override
    public Player getSecondPlayer() {
        return player2;
    }

    @Override
    public void setPlayers(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    public boolean isFirstPlayerTurn() {
        return firstPlayerTurn;
    }

    @Override
    public boolean isGameFinished() {
        return ValidMoves().isEmpty();
    }

    @Override
    public void reset() {
        // Reset the board and reinitialize it with the starting positions
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++)
                board[row][col] = null;
        }
        moveHistory.clear();
        firstPlayerTurn = true;
        initializeBoard(); // Reinitialize the board to the starting state
    }

    @Override
    public void undoLastMove() {
        if (!moveHistory.isEmpty()) {
            Move lastMove = moveHistory.remove(moveHistory.size() - 1);
            Position lastMovePosition = lastMove.position();
            board[lastMovePosition.getRow()][lastMovePosition.getColumn()] = null;

            // Restore the ownership of the flipped discs
            for (Disc flippedDisc : currentFlippedDiscs) {
                flippedDisc.setOwner(lastMove.getPlayer() == player1 ? player2 : player1);
            }

            currentFlippedDiscs.clear();
            firstPlayerTurn = !firstPlayerTurn;
        }
    }
}
