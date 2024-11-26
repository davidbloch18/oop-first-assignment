import java.util.ArrayList;
import java.util.List;

public class GreedyAI extends AIPlayer {

    public GreedyAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        List<Position> validPositions = gameStatus.ValidMoves();
        if (validPositions.isEmpty()) {
            return null; // No move possible
        }

        Position bestPosition = null;
        Disc bestDisc = null;
        int maxFlips = -1;

        // Iterate over all valid positions and choose the one with the highest flips
        for (Position position : validPositions) {
            int flipsForSimple = gameStatus.countFlips(position);

            if (flipsForSimple >= maxFlips) { // added the "=" sign to make the AI go in default to lowest right (if
                                              // more than 2 positions have the same flips count)
                                              // position
                maxFlips = flipsForSimple;
                bestPosition = position;
                bestDisc = new SimpleDisc(this);
            }

            // Check BombDisc if available
            if (getNumber_of_bombs() > 0) {
                int flipsForBomb = gameStatus.countFlips(position) + calculateNeighborFlips(gameStatus, position);
                if (flipsForBomb > maxFlips) {
                    maxFlips = flipsForBomb;
                    bestPosition = position;
                    bestDisc = new BombDisc(this);
                }
            }

            // Check UnflippableDisc if available (flipsForUnflippable should be 0)
            if (getNumber_of_unflippedable() > 0 && flipsForSimple == 0) {
                bestPosition = position;
                bestDisc = new UnflippableDisc(this);
            }
        }

        // Update counters if a Bomb or Unflippable disc was chosen
        if (bestDisc instanceof BombDisc) {
            reduce_bomb();
        } else if (bestDisc instanceof UnflippableDisc) {
            reduce_unflippedable();
        }

        return new Move(this, bestDisc, bestPosition);
    }

    /**
     * Calculates the flips for a BombDisc by considering adjacent discs.
     *
     * @param gameStatus The current game state.
     * @param position   The position where the BombDisc is placed.
     * @return The number of adjacent discs that would be flipped.
     */
    private int calculateNeighborFlips(PlayableLogic gameStatus, Position position) {
        // Implement logic to count adjacent flips (based on the game rules for
        // BombDisc).
        // Placeholder example
        return 0;
    }
}
