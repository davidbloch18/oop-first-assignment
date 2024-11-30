import java.util.*;

public class GameLogic extends Position implements PlayableLogic {
    private Position[][] board;
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
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board.length; col++) {
                board[row][col] = new Position(row, col, player1, player2);
            }
        }
        try {
            board[3][3].setDisc(new SimpleDisc(player1));
            board[4][4].setDisc(new SimpleDisc(player1));
            board[3][4].setDisc(new SimpleDisc(player2));
            board[4][3].setDisc(new SimpleDisc(player2));

        } catch (OccupiedPositionException ignored) {
        }
    }

    @Override
    public boolean locate_disc(Position position, Disc disc) {
        possibleNextMoves.clear();

        // Determine the current player
        Player currentPlayer = firstPlayerTurn ? player1 : player2;

        // Check if the special disc can be placed
        if (disc instanceof BombDisc) {
            if (currentPlayer.getNumber_of_bombs() <= 0) {
                System.out.println("No bombs remaining for the current player.");
                return false;
            }
        } else if (disc instanceof UnflippableDisc) {
            if (currentPlayer.getNumber_of_unflippedable() <= 0) {
                System.out.println("No unflippable discs remaining for the current player.");
                return false;
            }
        }

        // Validate the move
        if (!isValidMove(position)) {
            System.out.println("Invalid move: The position is either occupied or cannot flip any discs.");
            return false;
        }

        // Try placing the disc and updating the board
        try {
            // Assign ownership to the disc
            disc.setOwner(currentPlayer);

            // Calculate the move, including flipped positions
            Move nextMove = calculateNextMove(position, currentPlayer);

            // Place the disc
            board[position.row()][position.col()].setDisc(disc);
            System.out.printf("%s placed a %s at %s%n", currentPlayer.isPlayerOne() ? "Player 1" : "Player 2",
                    disc.getClass().getSimpleName(), position.toString());

            // Flip the discs affected by this move
            for (Position flipPos : nextMove.getFlips()) {
                System.out.printf("%s flipped the %s at %s%n",
                        currentPlayer.isPlayerOne() ? "Player 1" : "Player 2",
                        board[flipPos.row()][flipPos.col()].getDisc().getClass().getSimpleName(),
                        flipPos.toString());
                flipPos.flipDisc();
            }

            // Reduce special disc counters if applicable
            if (disc instanceof BombDisc) {
                currentPlayer.reduce_bomb();

            } else if (disc instanceof UnflippableDisc) {
                currentPlayer.reduce_unflippedable();

            }

            // Save the move in history and switch the turn
            moveHistory.push(nextMove);
            firstPlayerTurn = !firstPlayerTurn;

            return true;
        } catch (Exception e) {
            System.out.println("An error occurred while placing the disc: " + e.getMessage());
            return false;
        }
    }

    // creating a new move for position
    public Move calculateNextMove(Position newDiscPos, Player movePlayer) {
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
                        for (Position bomb : bombPos) {
                            tempFlipped.addAll(bombFlipped(bomb));
                        }
                        flippedPos.addAll(tempFlipped);
                        break;
                    } else {
                        if (!(neighborDisc instanceof UnflippableDisc)) {
                            tempFlipped.add(board[row][col]);
                            if (neighborDisc instanceof BombDisc) {
                                bombPos.add(board[row][col].clone());
                            }
                        }
                    }
                    row += dir[0];
                    col += dir[1];
                } catch (NullPointerException ignored) {
                }
            }
        }
        Move possibleNextMove = new Move(movePlayer, null, newDiscPos, flippedPos, this.board);
        this.tempCount = Math.max(possibleNextMove.getCount(), this.tempCount);
        return possibleNextMove;
    }

    public Set<Position> bombFlipped(Position pos) {
        Set<Position> toFlip = new HashSet<>();
        Queue<Position> toProcess = new LinkedList<>();
        Set<Position> visited = new HashSet<>();

        // Start with the initial bomb position
        toProcess.add(pos);

        while (!toProcess.isEmpty()) {
            Position current = toProcess.poll();

            // Skip already processed positions
            if (visited.contains(current))
                continue;

            visited.add(current);

            for (int[] dir : directions) {
                int row = current.row() + dir[0];
                int col = current.col() + dir[1];

                // Ensure the position is within bounds
                if (row >= 0 && row < board.length && col >= 0 && col < board[row].length) {
                    Position neighbor = board[row][col];
                    Disc neighborDisc = neighbor.getDisc();

                    if (neighborDisc != null
                            && neighborDisc.getOwner() != (firstPlayerTurn ? player1 : player2)
                            && !(neighborDisc instanceof UnflippableDisc)) {

                        // Add the neighbor to the set of discs to flip
                        toFlip.add(neighbor);

                        // If it's a BombDisc, add it to the processing queue for chain reaction
                        if (neighborDisc instanceof BombDisc) {
                            toProcess.add(neighbor);
                        }
                    }
                }
            }
        }

        return toFlip;
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

        try {
            Move nextMove = calculateNextMove(position, firstPlayerTurn ? player1 : player2);
            return (nextMove.getCount() > 0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public int countFlips(Position position) {
        try {
            Move nextMove = calculateNextMove(position, firstPlayerTurn ? player1 : player2);
            return nextMove.getCount();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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
        if (ValidMoves().isEmpty()) {
            int player1Discs = getNumberOfDiscs(player1);
            int player2Discs = getNumberOfDiscs(player2);
            if (player1Discs >= player2Discs) {
                player1.addWin();
                System.out.println(
                        "Player 1 wins with " + player1Discs + " discs! Player 2 had " + player2Discs + " discs.");
            } else {
                player2.addWin();
                System.out.println(
                        "Player 2 wins with " + player2Discs + " discs! Player 1 had " + player1Discs + " discs.");
            }
            return true;
        }
        return false;
    }

    private int getNumberOfDiscs(Player player) {
        int count = 0;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col].getDisc() != null && board[row][col].getDisc().getOwner() == player)
                    count++;
            }
        }
        return count;
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
            System.out.println("Undoing last move:");
            System.out.println(
                    "\tUndo: removing "
                            + board[lastMove.position().row()][lastMove.position().col()].getDisc().getType()
                            + " from (" + lastMove.position().row() + ", " + lastMove.position().col() + ")");
            board[lastMove.position().row()][lastMove.position().col()].removeDisc();
            lastMove.undo();
            firstPlayerTurn = !firstPlayerTurn;

        } else {
            reset();// very bad solution but is working. (the 4 pos of the start wont flipped back
                    // when undo called so reset instead)
            System.out.println("\tNo previous move available to undo ");
        }
    }

    public Position[][] getBoard() {
        return this.board;
    }

}
