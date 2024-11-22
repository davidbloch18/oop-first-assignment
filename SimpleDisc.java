public class SimpleDisc implements Disc {
    private Player owner;
    private String type;

    public SimpleDisc(Player owner) {
        this.owner = owner;
        this.type = "⬤";
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Player player) {
        this.owner = player;
    }

    @Override
    public String getType() {
        return type;
    }



}
