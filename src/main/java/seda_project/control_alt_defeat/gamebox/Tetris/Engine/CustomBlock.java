package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.scene.paint.Color;

public record CustomBlock(boolean[][] shape, Color color) implements TBlock {
    
    /**
     * Returns the shape of the custom block as a 2D boolean array, where each element indicates whether a cell is occupied (true) or empty (false). This method provides access to the block's configuration, allowing other parts of the application to retrieve and manipulate the block's shape for gameplay purposes.
     * @return A 2D boolean array representing the shape of the custom block.
     */
    @Override
    public boolean[][] getShape() {
        return shape;
    }
    /**
     * Returns the color of the custom block as a JavaFX Color object. This method provides access to the block's color for rendering purposes in the game, allowing other parts of the application to retrieve and display the block's color.
     * @return A JavaFX Color object representing the color of the custom block.
     */
    @Override
    public Color getColor() {
        return color;
    }
    /**
     * Creates a new Block instance based on the shape and color of the custom block. This method allows for the conversion of a CustomBlock into a standard Block object, which can be used in the game logic and rendering processes. The resulting Block will have the same shape and color as the CustomBlock.
     * @return A new Block instance with the shape and color of the custom block.
     */
    @Override
    public Block toPiece() {
        return new Block(shape, color);
    }
}
