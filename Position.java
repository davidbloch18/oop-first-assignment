public class Position implements Cloneable {
    private final int row;
    private final int  col;
    private Disc disc;
    private Player player1;
    private Player player2;

    public Position(int row, int col, Player player1, Player player2) {
        this.row = row;
        this.col = col;
        this.disc = null;
        this.player1 = player1;
        this.player2 = player2;
    }

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    //placing a new disc in the position
    public boolean setDisc(Disc disc) throws OccupiedPositionException {
        if(this.disc == null){
            try{
                this.disc = disc;
                return true;
            }catch (Exception e){System.out.println(e.getMessage());}
        }
        //if position is not empty, returns false
        throw new OccupiedPositionException("this position is not empty");
    }

    public boolean removeDisc(){
        try{
            this.disc = null;
        }catch(Exception e){System.out.println(e.getMessage());}
        return true;
    }

    public Disc getDisc(){return this.disc;}

    public boolean flipDisc() throws UnflippableDiscException{
        try{
            this.disc.setOwner(disc.getOwner().isPlayerOne? player2: player1);
            return true;
        }
        catch (Exception e){System.out.println(this.row +"," + this.col + "position: " + e.getMessage());}
        if (disc instanceof UnflippableDisc) {
            throw new UnflippableDiscException("Cannot change owner of an UnflippableDisc.");
        }
        return false;
    }

    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        String format = String.format("(%d,%d)", row, col);
        return format;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    @Override
    public Position clone() {
        try {
            Position clone = (Position) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
