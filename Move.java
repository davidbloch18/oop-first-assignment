import java.util.HashSet;
import java.util.Set;

public class Move extends GameLogic {
    private final Player player;
    private final Position position;
    private final Set<Position> effectedPos;
    private final int count;
    private Disc disc;
    private Position[][] board;
    /**
     * Constructs a Move object representing a player's move in the game.
     * 
     * @param player   The player making the move.
     * @param disc     The type of disc being placed.
     * @param position The position on the board where the disc is placed.
     * 
     */
    public Move(Player player, Disc disc, Position position, Position[][] board) {
        this.player = player;
        this.position = position;
        this.effectedPos = new HashSet<>();
        this.count = 0;
        this.disc = position.getDisc();
        this.board = board;

    }
    public Move(Player player,Disc disc, Position position, Set<Position> effectedPos, Position[][] board) {
        this.player = player;
        this.position = position;
        this.effectedPos= new HashSet<>();
        this.effectedPos.addAll(effectedPos);
        this.count = effectedPos.size();
        this.disc = disc;
        this.board = board;

    }

    public Move(Player player,Disc disc, Position position){
        this.player = player;
        this.position = position;
        this.effectedPos = new HashSet<>();
        this.count = 0;
        this.disc = disc;
        this.board = getBoard();
    }

    public int getCount(){return this.count;}

    public Set<Position> getFlips(){
        return this.effectedPos;
    }
    public boolean undo(){
        this.position.removeDisc();
        try{
            for (Position pos: this.effectedPos){
                board[pos.row()][pos.col()].flipDisc();
            }
        }catch (UnflippableDiscException ignored){}
        catch (Exception e){System.out.println(e.getMessage());}
        return true;
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
     * Gets the position of this move.
     * 
     * @return The position.
     */
    public Position position() {
        return position;
    }

    @Override
    public String toString() {
        return "Move by " + player + " at " + position + " with "  + " disc";
    }


    public Disc disc() {
        return this.disc;
    }
}