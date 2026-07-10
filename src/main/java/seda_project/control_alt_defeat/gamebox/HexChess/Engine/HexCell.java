package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

public class HexCell {
    private HexCoord coords;
    private Piece piece;

    public HexCell (HexCoord coord){
        this.coords = coord;
    }

    /**
     * @return this HexCoord
     */
    public HexCoord getCoords() {
        return coords;
    }

    /**
     * Set the piece on this Cell
     * @param newPiece
     */
    public void setPiece (Piece newPiece){
        this.piece = newPiece;
    }

    /**
     * @return the piece on this field, or null if empty.
     */
    public Piece getPiece(){
        return this.piece;
    }

    /**
     * @return true if this field has a piece
     */
    public boolean hasPiece(){
        return this.piece != null;
    }

    public boolean noPiece(){
        return this.piece == null;
    }

}
