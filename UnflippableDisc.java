public class UnflippableDisc implements Disc, Cloneable {
    private final Player owner;
    private final String type;

    public UnflippableDisc(Player owner) {
        this.owner = owner;
        this.type = "⭕";
    }

    @Override
    public Player getOwner() {return owner;}

    @Override
    public void setOwner(Player player){};
    @Override
    public String getType() {
        return type;
    }
    @Override
    public UnflippableDisc clone() {
        try {
            return (UnflippableDisc) super.clone(); // Call super.clone()
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // Handle the exception
        }
    }

}

