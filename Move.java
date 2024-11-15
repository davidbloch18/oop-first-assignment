import java.util.List;

public class Move {
    private final Player player;
    private final Disc disc;
    private final Position position;

    /**
     * Constructs a Move object representing a player's move in the game.
     * 
     * @param player   The player making the move.
     * @param disc     The type of disc being placed.
     * @param position The position on the board where the disc is placed.
     * 
     */
    public Move(Player player, Disc disc, Position position) {
        this.player = player;
        this.disc = disc;
        this.position = position;

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

    @Override
    public String toString() {
        return "Move by " + player + " at " + position + " with " + disc + " disc";
    }

}