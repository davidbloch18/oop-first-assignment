import java.util.List;

public class Move {
    private final Player player;
    private final Disc disc;
    private final Position position;
    private final List<Disc> flippedDiscs;

    /**
     * Constructs a Move object representing a player's move in the game.
     * 
     * @param player       The player making the move.
     * @param disc         The type of disc being placed.
     * @param position     The position on the board where the disc is placed.
     * @param flippedDiscs The list of discs that were flipped as a result of this
     *                     move.
     */
    public Move(Player player, Disc disc, Position position, List<Disc> flippedDiscs) {
        this.player = player;
        this.disc = disc;
        this.position = position;
        this.flippedDiscs = flippedDiscs;
    }

    /**
     * Gets the player who made the move.
     * 
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the disc placed in this move.
     * 
     * @return The disc.
     */
    public Disc disc() {
        return disc;
    }

    /**
     * Gets the position of this move.
     * 
     * @return The position.
     */
    public Position position() {
        return position;
    }

    /**
     * Gets the list of discs that were flipped by this move.
     * 
     * @return The list of flipped discs.
     */
    public List<Disc> getFlippedDiscs() {
        return flippedDiscs;
    }

    @Override
    public String toString() {
        return "Move by " + player + " at " + position + " with " + disc +
                ", flipping " + flippedDiscs.size() + " discs.";
    }

}