import java.util.List;
import java.util.Random;

public class RandomAI extends AIPlayer {
    private final Random random = new Random();

    public RandomAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        List<Position> validPositions = gameStatus.ValidMoves();
        if (validPositions.isEmpty()) {
            return null; // No move possible
        }

        Position chosenPosition = validPositions.get(random.nextInt(validPositions.size()));
        Disc chosenDisc;
        // Randomly select a disc type, respecting the limits for bombs and unflippable
        // discs
        int discChoice = random.nextInt(3); // 0: Simple, 1: Bomb, 2: Unflippable
        if (discChoice == 0) {
            chosenDisc = new SimpleDisc(this);
        } else if (discChoice == 1 && getNumber_of_bombs() > 0) {
            chosenDisc = new BombDisc(this);
            reduce_bomb();
        } else if (discChoice == 2 && getNumber_of_unflippedable() > 0) {
            chosenDisc = new UnflippableDisc(this);
            reduce_unflippedable();
        } else {
            // Default to SimpleDisc if limits reached
            chosenDisc = new SimpleDisc(this);
        }
        return new Move(null, chosenDisc, chosenPosition);
    }
}
