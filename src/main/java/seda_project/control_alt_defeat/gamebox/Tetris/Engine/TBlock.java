package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.scene.paint.Color;

/**
 * The TBlock interface defines the structure and behavior of a Tetris block in the game. It provides methods to retrieve the shape and color of the block, as well as a method to convert the block into a standard Block instance. Implementing classes must provide concrete implementations for these methods, allowing for different types of blocks with varying shapes and colors to be used in the game.
 * The getShape() method returns a 2D boolean array representing the block's shape,
 */
public interface TBlock {
    boolean[][] getShape();
    Color getColor();
    Block toPiece();
}
