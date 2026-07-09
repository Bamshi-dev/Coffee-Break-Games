package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;

import javafx.scene.paint.Color;

public class Block implements Serializable {
    private int x, y;
    private boolean[][] blocks;
    private String hexColor;

    /**
     * Creates a new Block instance with the specified shape and color. The shape is represented as a 2D boolean array,
     * where true indicates the presence of a block and false indicates an empty space. The color is represented as a
     * JavaFX Color object, which is converted to a hexadecimal string for storage.
     * The block is initialized at the default spawn position (x=3, y=0) on the Tetris board.
     * @param shape A 2D boolean array representing the shape of the block.
     */
    public Block(boolean[][] shape, Color fxColor) {
        // Deep copy the shape to prevent mutating the Enum's base array during rotation
        this.blocks = new boolean[shape.length][];
        for (int i = 0; i < shape.length; i++) {
            this.blocks[i] = shape[i].clone();
        }

        // Convert Color to String
        this.hexColor = fxColor.toString();

        this.x = 3; // Tetris spawn X middle of a 10-width board
        this.y = 0; // Spawn at the top
    }

    /**
     * Private constructor used for cloning a Block instance. It creates a new Block with the specified shape, color,
     * and position (x, y). This constructor is used internally when creating a snapshot of the block's current state.
     * @param clonedShape A 2D boolean array representing the shape of the block.
     * @param hexColor A hexadecimal string representing the color of the block.
     * @param x The x-coordinate of the block's position on the Tetris board.
     * @param y The y-coordinate of the block's position on the Tetris board.
     */
    private Block(boolean[][] clonedShape, String hexColor, int x, int y) {
        this.blocks = clonedShape;
        this.hexColor = hexColor;
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a deep copy of the current Block instance, including its shape, color, and position. This method is
     * useful for creating a snapshot of the block's state without affecting the original instance. The cloned block
     * can be manipulated independently of the original.
     * @return A new Block instance that is a deep copy of the current block.
     */
    public Block cloneForSnapshot() {
        boolean[][] clonedBlocks = new boolean[blocks.length][];
        for (int i = 0; i < blocks.length; i++) {
            clonedBlocks[i] = blocks[i].clone();
        }
        return new Block(clonedBlocks, this.hexColor, this.x, this.y);
    }

    public void moveDown() { y++; }
    public void moveUp() { y--; }
    public void moveLeft() { x--; }
    public void moveRight() { x++; }

    /**
     * Rotates the block's shape 90 degrees clockwise. The method creates a new 2D boolean array to hold the rotated
     * shape and updates the block's shape accordingly. This rotation is performed in-place, modifying the current
     * block instance.
     */
    public void rotateClockwise() {
        int n = blocks.length;
        boolean[][] newShape = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newShape[j][n - 1 - i] = blocks[i][j];
            }
        }
        blocks = newShape;
    }

    /**
     * Rotates the block's shape 90 degrees counter-clockwise. The method creates a new 2D boolean array to hold the
     * rotated shape and updates the block's shape accordingly. This rotation is performed in-place, modifying
     * the current block instance.
     */
    public void rotateCounterClockwise() {
        int n = blocks.length;
        boolean[][] newShape = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newShape[n - 1 - j][i] = blocks[i][j];
            }
        }
        blocks = newShape;
    }

    public boolean[][] getShape() { return blocks; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getHexColor() { return hexColor; }

    public void setX(int x){this.x = x;}
    public void setY(int y){this.y = y;}
    public void setShape(boolean[][] shape){this.blocks = shape;}
}
