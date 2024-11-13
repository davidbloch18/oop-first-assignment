import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Position;

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

            if (flipsForSimple > maxFlips) {
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

        return new Move(this, bestDisc, bestPosition, maxFlips);
    }

    // Helper method to calculate flips for neighbors in case of BombDisc
    private int calculateNeighborFlips(PlayableLogic gameStatus, Position position) {
        // Calculate additional flips due to neighboring discs being affected by a
        // BombDisc
        // Implement based on game rules for neighbors flipping around a BombDisc
        return 0; // Placeholder
    }

}

    }

    return null; // No valid move found
    }

    /**
     * Returns a list of available discs that the AI player can place.
     * This should return new instances of each disc type the AI can use.
     */
    private List<Disc> getPossibleDiscs() {
        List<Disc> discs = new ArrayList<>();
        if (getNumber_of_bombs() > 0) {
            discs.add(new BombDisc(this));
        }
        if (getNumber_of_unflippedable() > 0) {
            discs.add(new UnflippableDisc(this));
        }
        discs.add(new SimpleDisc(this)); // Simple discs are always available
        return discs;
    }
}
