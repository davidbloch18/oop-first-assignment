public class HumanPlayer extends Player {
    public HumanPlayer(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public boolean isHuman() {
        return true;
    }

    // Since the HumanPlayer doesn't require any special AI for making moves,
    // the game interface would handle user input to decide the move.
}
