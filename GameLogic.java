import java.util.*;

public class GameLogic extends Position implements PlayableLogic {
    private Position[][]  board;
    // Check in all directions
    private final int[][] directions = {
            { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
            { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
    };
    private Stack<Move> moveHistory;
    private boolean firstPlayerTurn;
    private Player player1;
    private Player player2;
    private List<Position> possibleNextMoves;
    private int tempCount;
    private Set<Position> bombPos;

    public GameLogic() {
        this(8, 0, 0, true, "GreedyAI"); // Default: Player 1 human, Player 2 is GreedyAI
    }

    public GameLogic(int boardSize, int row, int col, boolean isPlayerOneHuman, String player2AIType) {
        super(row, col);
        this.board = new Position[boardSize][boardSize];
        this.moveHistory = new Stack<>();
        this.firstPlayerTurn = true;
        this.possibleNextMoves = new ArrayList<>();
        this.bombPos = new HashSet<>();

        // Initialize players
        this.player1 = isPlayerOneHuman ? new HumanPlayer(true) : AIPlayer.createAIPlayer("GreedyAI", true);
        this.player2 = AIPlayer.createAIPlayer(player2AIType, false);



        // Initialize the board with starting positions
        initializeBoard();
    }

    private void initializeBoard() {
        for(int row= 0; row<board.length; row++){
            for(int col= 0; col<board.length; col++){
                board[row][col] = new Position(row, col, player1, player2);
            }
        }
        try{
            board[3][3].setDisc(new SimpleDisc(player1));
            board[4][4].setDisc(new SimpleDisc(player1));
            board[3][4].setDisc(new SimpleDisc(player2));
            board[4][3].setDisc(new SimpleDisc(player2));

        }catch (OccupiedPositionException ignored){}
    }

    @Override
    public boolean locate_disc(Position position, Disc disc) {
            possibleNextMoves.clear();
            if (isValidMove(position)){
                try{
                    disc.setOwner(firstPlayerTurn ? player1 : player2);
                    Move nextMove = calculateNextMove(position, firstPlayerTurn ? player1 : player2);
                    board[position.row()][position.col()].setDisc(disc);
                    System.out.printf("%s placed a %s in %s\n", firstPlayerTurn ? "player1" : "player2", disc.getType(),position.toString());
                    for (Position pos:nextMove.getFlips()) {
                        System.out.printf("%s flliped the %s in %s\n", firstPlayerTurn ? "player1" : "player2",board[pos.row()][pos.col()].getDisc().getType(), board[pos.row()][pos.col()].toString() );
                        pos.flipDisc();
                    }
                    moveHistory.push(nextMove);
                    System.out.println();
                }catch (Exception e){System.out.println(e.getMessage());}
                firstPlayerTurn = !firstPlayerTurn;
                return true;
            }
            return false;
    }

    //creating a new move for position
    public Move calculateNextMove(Position newDiscPos, Player movePlayer){
        Set<Position> flippedPos = new HashSet<>();
        for (int[] dir : directions) {
            Set<Position> tempFlipped = new HashSet<>();
            this.bombPos.clear();
            int row = newDiscPos.row() + dir[0];
            int col = newDiscPos.col() + dir[1];

            while (row >= 0 && row < board.length && col >= 0 && col < board[row].length) {
                try {
                    Disc neighborDisc = getDiscAtPosition(new Position(row, col));
                    if (board[row][col].getDisc() == null) {
                        break;
                    }
                    if (neighborDisc.getOwner() == movePlayer) {
                            for (Position bomb: bombPos) {
                                tempFlipped.addAll(bombFlipped(bomb));
                            }
                        flippedPos.addAll(tempFlipped);
                        break;
                    }
                    else {
                        if(!(neighborDisc instanceof UnflippableDisc)){
                            tempFlipped.add(board[row][col]);
                            if (neighborDisc instanceof BombDisc){
                                bombPos.add(board[row][col].clone());
                            }
                        }
                    }
                    row += dir[0];
                    col += dir[1];
                }catch (NullPointerException ignored){}
            }
        }
        Move possibleNextMove = new Move(movePlayer,null, newDiscPos, flippedPos, this.board);
        this.tempCount = Math.max(possibleNextMove.getCount(), this.tempCount);
        return possibleNextMove;
    }

    public Set<Position> bombFlipped(Position pos) {
        Set<Position> toFlip = new HashSet<>();

        for (int[] dir : directions) {
            int row = pos.row() + dir[0];
            int col = pos.col() + dir[1];

            // Check if the new row and column are within the bounds of the board
            try {
                // Get the disc at the position
                Disc flipped = board[row][col].getDisc();

                // Check for null and validate ownership
                if ((flipped != null) && (flipped.getOwner() != (firstPlayerTurn ? player1 : player2)) &&
                        !(flipped instanceof UnflippableDisc)) {
                    boolean added = toFlip.add(board[row][col]); // Clone the position to flip
                    if (flipped instanceof BombDisc && added) {
                        // If it is a BombDisc, add it to bombPos
                        this.bombPos.add(board[row][col]); // Clone the position again
                    }
                }
            }catch (IndexOutOfBoundsException ignored){}
        }
        return toFlip; // Return the set of positions to be flipped
    }

    @Override
    public Disc getDiscAtPosition(Position position) {
        return board[position.getRow()][position.getCol()].getDisc();
    }

    @Override
    public int getBoardSize() {
        return board.length;
    }

    @Override
    public List<Position> ValidMoves() {
        this.possibleNextMoves.clear();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (isValidMove(board[row][col])) {
                    this.possibleNextMoves.add(board[row][col]);
                }
            }
        }
        return this.possibleNextMoves;
    }

    private boolean isValidMove(Position position) {
        if (getDiscAtPosition(position) != null)
            return false;

        try{
            Move nextMove = calculateNextMove(position, firstPlayerTurn ? player1 : player2);
            return (nextMove.getCount() > 0);
        }catch (Exception e){System.out.println(e.getMessage());}
        return false;
    }

    @Override
    public int countFlips(Position position) {
        try{
            Move nextMove = calculateNextMove(position, firstPlayerTurn ? player1 : player2);
            return nextMove.getCount();
        }catch (Exception e){System.out.println(e.getMessage());}
        return 0;
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
        if (!this.moveHistory.isEmpty()) {

            Move lastMove = this.moveHistory.pop();
            board[lastMove.position().row()][lastMove.position().col()].removeDisc();
            lastMove.undo();
            firstPlayerTurn = !firstPlayerTurn;

        }
    }
    public Position[][] getBoard(){
        return this.board;
    }

}
