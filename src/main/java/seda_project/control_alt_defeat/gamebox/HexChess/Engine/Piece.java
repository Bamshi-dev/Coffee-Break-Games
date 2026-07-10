package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import java.util.Objects;

public class Piece {
    private PieceType type;
    private final PlayerColor player;
    private boolean moved;
    private HexCoord position;

    public Piece(PieceType type, PlayerColor player){
        this.type = type;
        this.player = player;
        this.moved = false;
    }

    /**
     * Return type of this piece
     * @return
     */
    public PieceType getType() {
        return type;
    }

    /**
     * returns the player this Piece belings to
     * @return
     */
    public PlayerColor getPlayer() {
        return player;
    }

    /**
     * method to check whether this piece has been moved
     * @return
     */
    public boolean isMoved() {
        return moved;
    }

    /**
     * Method to set if a piece has been moved
     * @param moved
     */
    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    /**
     * Method to retrieve the current position of this piece
     * @return
     */
    public HexCoord getPosition() {
        return position;
    }


    /**
     * Method to set the current position of this piece
     * @param position
     */
    public void setPosition(HexCoord position) {
        this.position = Objects.requireNonNull(position, "Piece position cannot be null");
    }
}
