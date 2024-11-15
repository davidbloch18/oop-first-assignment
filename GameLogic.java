import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameLogic extends Position implements PlayableLogic {
    private Disc[][] board;
    private List<Move> moveHistory;
    private boolean firstPlayerTurn;
    private Player player1;
    private Player player2;
    private List<Disc> currentFlippedDiscs;

    public GameLogic() {
        this(8, 0, 0, true, "GreedyAI"); // Default: Player 1 human, Player 2 is GreedyAI
    }

    public GameLogic(int boardSize, int row, int col, boolean isPlayerOneHuman, String player2AIType) {
        super(row, col);
        this.board = new Disc[boardSize][boardSize];
        this.moveHistory = new ArrayList<>();
        this.firstPlayerTurn = true;
        this.currentFlippedDiscs = new ArrayList<>(); // Initialize the flipped discs list

        // Initialize player1 based on whether they are human or AI
        if (isPlayerOneHuman) {
            player1 = new HumanPlayer(true); // Player 1 is human
        } else {
            player1 = AIPlayer.createAIPlayer("GreedyAI", true); // Player 1 is AI (e.g., GreedyAI)
        }

        // Initialize player2 as an AI (or human if you want to make both players human)
        player2 = AIPlayer.createAIPlayer(player2AIType, false); // Player 2 AI (e.g., RandomAI)

        // Set the "isPlayerOne" flag for player1
        player1.isPlayerOne = true;

        // Set the "isPlayerOne" flag for player2
        player2.isPlayerOne = false;

        // Initialize the board with the starting positions
        board[3][3] = new SimpleDisc(player1);
        board[4][4] = new SimpleDisc(player1);
        board[3][4] = new SimpleDisc(player2);
        board[4][3] = new SimpleDisc(player2);
    }

    @Override
    public boolean locate_disc(Position position, Disc disc) {
        if (isValidMove(position)) {
            // Clear the current flipped discs list for the new move
            currentFlippedDiscs.clear();

            Stack<Disc> flippedDiscsStack = applyMove(position, disc);
            currentFlippedDiscs.addAll(flippedDiscsStack);

            // Add move to history, without flipped discs in Move class
            moveHistory.add(new Move(firstPlayerTurn ? player1 : player2, disc, position));

            // Print each flipped disc
            for (Disc flippedDisc : currentFlippedDiscs) {
                System.out.printf("Player %d flipped a disc at (%d, %d)%n",
                        flippedDisc.getOwner() == player1 ? 1 : 2,
                        flippedDisc.getClass().getSimpleName(),
                        position.getRow(),
                        position.getColumn());
            }

            firstPlayerTurn = !firstPlayerTurn;
            return true;
        }
        return false;
    }

    private Stack<Disc> applyMove(Position position, Disc disc) {
        Stack<Disc> flippedDiscs = new Stack<>();
        board[position.getRow()][position.getColumn()] = disc;

        // Check in all directions
        int[][] directions = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
        };

        for (int[] dir : directions) {
            List<Disc> tempFlipped = new ArrayList<>();
            int row = position.getRow() + dir[0];
            int col = position.getColumn() + dir[1];

            while (row >= 0 && row < board.length && col >= 0 && col < board.length) {
                Disc neighborDisc = getDiscAtPosition(new Position(row, col));
                if (neighborDisc == null || neighborDisc instanceof UnflippableDisc)
                    break;
                if (neighborDisc.getOwner() == disc.getOwner()) {
                    flippedDiscs.addAll(tempFlipped);
                    currentFlippedDiscs.addAll(tempFlipped); // Add flipped discs to the global list
                    for (Disc flipDisc : tempFlipped) {
                        flipDisc.setOwner(disc.getOwner());
                    }
                    break;
                } else {
                    tempFlipped.add(neighborDisc);
                }

                row += dir[0];
                col += dir[1];
            }
        }
        return flippedDiscs;
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
        return countFlips(position) > 0;
    }

    @Override
    public int countFlips(Position position) {
        int flips = 0;
        Disc currentDisc = getDiscAtPosition(position);

        // Ensure currentDisc is not null before proceeding
        if (currentDisc == null) {
            return flips; // Return 0 flips if there's no disc at the position
        }

        Player currentPlayer = currentDisc.getOwner();

        // Direction vectors for checking in all 8 possible directions
        int[][] directions = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
        };

        // Check each direction for valid flips
        for (int[] dir : directions) {
            int row = position.getRow();
            int col = position.getColumn();
            int tempFlips = 0;

            while (true) {
                row += dir[0];
                col += dir[1];

                // Check if the position is out of bounds
                if (row < 0 || row >= board.length || col < 0 || col >= board.length) {
                    break; // Exit the loop if out of bounds
                }

                Disc neighborDisc = getDiscAtPosition(new Position(row, col));

                // If the neighbor is null, break out of the loop
                if (neighborDisc == null) {
                    break;
                }

                // If the neighbor's owner is the same as the current player's, count flips
                if (neighborDisc.getOwner().equals(currentPlayer)) {
                    flips += tempFlips;
                    break; // Found a valid flip sequence, break out of the loop
                }

                // If the neighbor is an UnflippableDisc, stop
                if (neighborDisc instanceof UnflippableDisc) {
                    break;
                }

                // If the neighbor is a BombDisc, consider adjacent flips
                if (neighborDisc instanceof BombDisc) {
                    flips += countAdjacentFlips(new Position(row, col));
                    break; // Exit after handling BombDisc
                }

                // Otherwise, continue counting potential flips
                tempFlips++;
            }
        }
        return flips;
    }

    private int countAdjacentFlips(Position position) {
        int flips = 0;
        int[][] directions = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
        };

        for (int[] dir : directions) {
            int row = position.getRow() + dir[0];
            int col = position.getColumn() + dir[1];

            if (row >= 0 && row < board.length && col >= 0 && col < board.length) {
                Disc neighborDisc = getDiscAtPosition(new Position(row, col));
                if (neighborDisc != null && neighborDisc.getOwner() != null)
                    flips++;
            }
        }
        return flips;
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
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++)
                board[row][col] = null;
        }
        moveHistory.clear();
        firstPlayerTurn = true;
    }

    @Override
    public void undoLastMove() {
        if (!moveHistory.isEmpty()) {
            Move lastMove = moveHistory.remove(moveHistory.size() - 1);
            Position lastMovePosition = lastMove.position();
            board[lastMovePosition.getRow()][lastMovePosition.getColumn()] = null;

            // Restore the flipped discs from the GameLogic's list
            for (Disc flippedDisc : currentFlippedDiscs) {
                flippedDisc.setOwner(lastMove.getPlayer() == player1 ? player2 : player1);
            }

            // Reset flipped discs list after undo
            currentFlippedDiscs.clear();

            firstPlayerTurn = !firstPlayerTurn;
        }
    }

}