package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;
import java.util.Arrays;

public class Board implements Serializable {
    private int width = 10;
    private int height = 20;
    private String[][] grid; // null means empty, String holds hex color
    private final boolean inverted;

    /**
     * Creates a new Board instance with the specified orientation. The board is initialized with a grid of the default
     * width (10) and height (20), and all cells are set to null, indicating that they are empty. The inverted parameter
     * determines whether the board is inverted (blocks fall upward) or standard (blocks fall downward).
     * @param inverted
     */
    public Board(boolean inverted) {
        this.grid = new String[height][width];
        this.inverted = inverted;
    }

    /**
     * Checks if the specified block is in a valid position on the board. A valid position means that the block does not
     * collide with the boundaries of the board, does not overlap with any locked blocks in the grid, and does not
     * collide with another active block if provided. This method is used to determine whether a block can be placed or
     * moved to a specific location on the board.
     * @param block The Block instance to check for a valid position on the board.
     * @return true if the block is in a valid position; false otherwise.
     */
    public boolean isValidPosition(Block block) {
        return isValidPosition(block, null);
    }

    /**
     * Checks if the specified moving block is in a valid position on the board, considering another active block for
     * potential collisions. A valid position means that the moving block does not collide with the boundaries of the
     * board, does not overlap with any locked blocks in the grid, and does not collide with the other active block if
     * provided. This method is used to determine whether a block can be placed or moved to a specific location on the
     * board while considering interactions with another active block.
     * @param movingBlock The Block instance that is being moved or placed on the board.
     * @param otherActiveBlock An optional Block instance representing another active block on the board. If provided,
     *                         the method checks for potential collisions between the moving block and this other
     *                         active block.
     * @return true if the moving block is in a valid position; false otherwise.
     */
    public boolean isValidPosition(Block movingBlock, Block otherActiveBlock) {
        boolean[][] shape = movingBlock.getShape();
        int bx = movingBlock.getX();
        int by = movingBlock.getY();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    int boardX = bx + col;
                    int boardY = by + row;

                    // 1. Check Board Boundaries
                    if (boardX < 0 || boardX >= width || boardY < 0 || boardY >= height) {
                        return false;
                    }

                    // 2. Check Locked Grid
                    if (grid[boardY][boardX] != null) {
                        return false;
                    }

                    // 3. Check Inter-Block Collision
                    if (otherActiveBlock != null) {
                        if (collidesWithOtherBlock(boardX, boardY, otherActiveBlock)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    // Map global board coordinates to the local space of the other block.
    private boolean collidesWithOtherBlock(int boardX, int boardY, Block otherBlock) {
        boolean[][] otherShape = otherBlock.getShape();
        int localRow = boardY - otherBlock.getY();
        int localCol = boardX - otherBlock.getX();

        // If the coordinate falls within the other block's grid, check if that specific cell is solid
        if (localRow >= 0 && localRow < otherShape.length &&
                localCol >= 0 && localCol < otherShape[0].length) {
            return otherShape[localRow][localCol];
        }

        return false;
    }

    /**
     * Locks the specified block into the board's grid, marking the cells occupied by the block as filled with the
     * block's color. This method updates the board's state to reflect that the block is now part of the locked grid
     * and can no longer be moved or manipulated. It is typically called when a block has reached its final position
     * on the board and needs to be integrated into the existing locked blocks.
     * @param block The Block instance to be locked into the board's grid.
     */
    public void lockBlock(Block block) {
        boolean[][] shape = block.getShape();
        int bx = block.getX();
        int by = block.getY();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    grid[by + row][bx + col] = block.getHexColor();
                }
            }
        }
    }

    /**
     * Overwrites the current grid of the board with a new grid. The new grid is provided as a 2D array of strings,
     * where each string represents the color of a block or null for empty cells. This method updates the board's state
     * to reflect the new configuration of blocks, allowing for dynamic changes to the board's layout during gameplay
     * or for loading saved game states.
     * @param newGrid A 2D array of strings representing the new grid configuration, where each string is a hexadecimal color code or null for empty cells.
     */
    public void overwriteGrid(String[][] newGrid) {
        int newHeight = newGrid.length;
        int newWidth  = newGrid[0].length;
        this.grid   = new String[newHeight][newWidth];
        this.height = newHeight;
        this.width  = newWidth;
        for (int i = 0; i < newHeight; i++) {
            this.grid[i] = newGrid[i].clone();
        }
    }

    /**
     * Clears any full lines in the board's grid and shifts the remaining blocks down (or up if inverted) to fill the
     * cleared space. This method checks each row of the grid for completeness, removes any full rows, and updates
     * the grid accordingly. It returns the number of lines that were cleared during this operation, which can be used
     * for scoring or game progression purposes.
     * @return The number of lines cleared from the board's grid.
     */
    public int clearLines() {
        int linesCleared = 0;
        int startRow = inverted ? 0 : height - 1;
        int endRow = inverted ? height : -1;
        int step = inverted ? 1 : -1;

        for (int row = startRow; row != endRow; row += step) {
            if (isLineFull(row)) {
                linesCleared++;
                shiftGrid(row);
                row -= step;
            }
        }
        return linesCleared;
    }

    /**
     * Checks if a specific row in the board's grid is completely filled with blocks (i.e., no empty cells). A full
     * line is defined as a row where every cell contains a block (non-null value). This method is used to determine
     * which rows need to be cleared during gameplay.
     * @param row The index of the row to check for completeness.
     * @return true if the specified row is full; false otherwise.
     */
    private boolean isLineFull(int row) {
        for (int col = 0; col < width; col++) {
            if (grid[row][col] == null) return false;
        }
        return true;
    }

    /**
     * Shifts the rows of the board's grid down (or up if inverted) starting from a specified target row. This method
     * is called after a line has been cleared to move all rows above (or below if inverted) the cleared line down
     * (or up) by one position, effectively filling the space left by the cleared line. The topmost (or bottommost if
     * inverted) row is then filled with empty cells (null values).
     * @param targetRow The index of the row from which to start shifting the rows down (or up if inverted).
     */
    private void shiftGrid(int targetRow) {
        if (inverted) {
            for (int row = targetRow; row < height - 1; row++) {
                System.arraycopy(grid[row + 1], 0, grid[row], 0, width);
            }
            Arrays.fill(grid[height - 1], null);
        } else {
            for (int row = targetRow; row > 0; row--) {
                System.arraycopy(grid[row - 1], 0, grid[row], 0, width);
            }
            Arrays.fill(grid[0], null);
        }
    }

    /**
     * Clears the entire board's grid, setting all cells to null, which indicates that they are empty. This method is
     * typically used to reset the board to its initial state, removing all locked blocks and preparing it for a new
     * game or round of gameplay.
     */
    public void clear() {
        for (int row = 0; row < height; row++) {
            Arrays.fill(grid[row], null);
        }
    }

    public void expand(int rows) {
        int newHeight = height + rows;
        String[][] newGrid = new String[newHeight][width];

        if (inverted) {
            // blocks fall upward, active space is at the bottom — preserve existing rows at top
            for (int i = 0; i < height; i++) {
                newGrid[i] = grid[i].clone();
            }
            // fill new empty rows at the bottom
            for (int i = height; i < newHeight; i++) {
                newGrid[i] = new String[width];
            }
        } else {
            // blocks fall downward, active space is at the top — preserve existing rows at bottom
            int offset = newHeight - height;
            for (int i = 0; i < height; i++) {
                newGrid[i + offset] = grid[i].clone();
            }
            // fill new empty rows at the top
            for (int i = 0; i < offset; i++) {
                newGrid[i] = new String[width];
            }
        }
        this.grid = newGrid;
        this.height = newHeight;
    }

    /**
     * Shrinks the board's grid by removing a specified number of rows from either the top or bottom, depending on the
     * board's orientation. If the board is inverted, rows are removed from the bottom; otherwise, they are removed from
     * the top. This method updates the board's state to reflect the new height and configuration of the grid after the
     * specified rows have been removed.
     * @param rows The number of rows to remove from the board's grid. Must be less than the current height of the board.
     * @throws IllegalArgumentException if the number of rows to remove is greater than or equal to the current height
     * of the board.
     */
    public void shrink(int rows) {
        if (rows >= height) throw new IllegalArgumentException("Cannot shrink below 1 row");
        int newHeight = height - rows;
        String[][] newGrid = new String[newHeight][width];

        if (inverted) {
            // drop rows from the bottom
            for (int i = 0; i < newHeight; i++) {
                newGrid[i] = grid[i].clone();
            }
        } else {
            // drop rows from the top
            int offset = height - newHeight;
            for (int i = 0; i < newHeight; i++) {
                newGrid[i] = grid[i + offset].clone();
            }
        }


        this.grid = newGrid;
        this.height = newHeight;
    }

    public String[][] getGrid() { return grid; }
    public boolean isInverted() { return inverted; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
