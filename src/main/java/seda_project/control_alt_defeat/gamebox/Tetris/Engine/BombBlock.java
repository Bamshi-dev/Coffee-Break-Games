package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;

import javafx.scene.paint.Color;

public class BombBlock extends Block implements Serializable {
    private BombType type;

    /**
     * Creates a new BombBlock instance with the specified BombType. The BombBlock is initialized with a single block
     * shape and a white color, representing its unique functionality in the game. The BombType determines the specific
     * behavior of the bomb when it is activated during gameplay.
     * @param type The BombType that defines the behavior of the bomb block.
     */
    public BombBlock(BombType type) {
        super(new boolean[][] {{true}}, Color.WHITE);
        this.type = type;
    }

    /**
     * Creates a deep copy of the current BombBlock instance, including its shape, color, position, and BombType. This
     * method is useful for creating a snapshot of the bomb block's state without affecting the original instance. The
     * cloned bomb block can be manipulated independently of the original.
     * @return A new BombBlock instance that is a deep copy of the current bomb block
     */
    @Override
    public Block cloneForSnapshot() {
        BombBlock clone = new BombBlock(this.type);
        clone.setX(this.getX());
        clone.setY(this.getY());
        clone.setShape(this.getShape().clone());
        return clone;
    }

    /**
     * Returns the BombType of this BombBlock, which defines its specific behavior when activated in the game. The
     * BombType can be used to determine how the bomb interacts with other blocks and the game board.
     * @return The BombType of this BombBlock.
     */
    public BombType getType(){
        return this.type;
    }

}
