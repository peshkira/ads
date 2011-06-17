package at.tuwien.ads11.remote;

import java.io.Serializable;

import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;


public class Movement implements Serializable {
    
    private static final long serialVersionUID = 2385091442718309135L;

    private Player player;
    
    private Prisoner prisoner;
    
    private int rowOrCol;
    
    private int row;
    
    private int col;
    
    public Movement(Player p, Prisoner pr, int rOc, int row, int col) {
        this.setPlayer(p);
        this.setPrisoner(pr);
        this.setRowOrCol(rOc);
        this.setRow(row);
        this.setCol(col);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPrisoner(Prisoner prisoner) {
        this.prisoner = prisoner;
    }

    public Prisoner getPrisoner() {
        return prisoner;
    }

    public void setRowOrCol(int rowOrCol) {
        this.rowOrCol = rowOrCol;
    }

    public int getRowOrCol() {
        return rowOrCol;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getRow() {
        return row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getCol() {
        return col;
    }
}
