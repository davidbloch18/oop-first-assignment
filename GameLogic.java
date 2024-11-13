import java.util.ArrayList;
import java.util.List;

public class GameLogic extends Position implements PlayableLogic {
    private Disc[][] board;
    private List<Move> moveHistory;
    private boolean firstPlayerTurn;
    private Player player1;
    private Player player2;

    public GameLogic() {
        this(8, 0, 0); // Initialize an 8x8 board, example start at (0, 0)
    }

    public GameLogic(int boardSize, int row, int col) {
        super(row, col);
        this.board = new Disc[boardSize][boardSize];
        this.moveHistory = new ArrayList<>();
        this.firstPlayerTurn = true;

        // Initialize players' starting positions
        board[3][3] = new SimpleDisc(player1);
        board[4][4] = new SimpleDisc(player1);
        board[3][4] = new SimpleDisc(player2);
        board[4][3] = new SimpleDisc(player2);
    }

    @Override
    public boolean locate_disc(Position position, Disc disc) {
        if (isValidMove(position)) {
            List<Disc> flippedDiscs = applyMove(position, disc);
            moveHistory.add(new Move(firstPlayerTurn ? player1 : player2, disc, position, flippedDiscs));
            firstPlayerTurn = !firstPlayerTurn;
            return true;
        }
        return false;
    }

    private List<Disc> applyMove(Position position, Disc disc) {
        List<Disc> flippedDiscs = new ArrayList<>();
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
                    for (Disc flipDisc : tempFlipped)
                        flipDisc.setOwner(disc.getOwner());
                    break;
                } else
                    tempFlipped.add(neighborDisc);

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
        // Returns the count of flippable discs for the given position
        int flips = 0;
        Disc currentDisc = getDiscAtPosition(position);
        Player currentPlayer = currentDisc.getOwner();

        int[][] directions = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
        };

        for (int[] dir : directions) {
            int row = position.getRow();
            int col = position.getColumn();
            int tempFlips = 0;

            while (true) {
                row += dir[0];
                col += dir[1];

                if (row < 0 || row >= board.length || col < 0 || col >= board.length)
                    break;
                Disc neighborDisc = getDiscAtPosition(new Position(row, col));

                if (neighborDisc == null)
                    break;
                if (neighborDisc.getOwner().equals(currentPlayer)) {
                    flips += tempFlips;
                    break;
                }
                if (neighborDisc instanceof UnflippableDisc)
                    break;
                if (neighborDisc instanceof BombDisc) {
                    flips += countAdjacentFlips(new Position(row, col));
                    break;
                }
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
            board[lastMove.getPosition().getRow()][lastMove.getPosition().getColumn()] = null;
            for (Disc flippedDisc : lastMove.getFlippedDiscs()) {
                flippedDisc.setOwner(lastMove.getPlayer() == player1 ? player2 : player1);
            }
            firstPlayerTurn = !firstPlayerTurn;
        }
    }
}
