import java.util.List;

public class Move {
    private final Player player;
    private final Disc disc;
    private final Position position;
    private final List<Disc> flippedDiscs;

    public Move(Player player, Disc disc, Position position, List<Disc> flippedDiscs) {
        this.player = player;
        this.disc = disc;
        this.position = position;
        this.flippedDiscs = flippedDiscs;
    }

    public Player getPlayer() {
        return player;
    }

    public Disc getDisc() {
        return disc;
    }

    public Position getPosition() {
        return position;
    }

    public List<Disc> getFlippedDiscs() {
        return flippedDiscs;
    }
}
