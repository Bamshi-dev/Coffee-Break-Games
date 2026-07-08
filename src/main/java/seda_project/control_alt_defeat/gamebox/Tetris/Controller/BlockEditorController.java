package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import java.util.Arrays;

import javafx.scene.paint.Color;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.BlockRegistry;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.CustomBlock;

public class BlockEditorController {
    private static final int MAX_GRID_SIZE = 4;

    private final boolean[][] grid = new boolean[MAX_GRID_SIZE][MAX_GRID_SIZE];
    private Color selectedColor = Color.WHITE;
    private final BlockRegistry registry;
    private CustomBlock pieceToUpdate = null    ;

    public BlockEditorController(BlockRegistry registry) {
        this.registry = registry;
    }

    /**
     * Toggles the state of a cell in the grid at the specified row and column. If the cell is currently active (true), it will be deactivated (set to false), and if it is inactive (false), it will be activated (set to true).
     */
    public void toggleCell(int row, int col) {
        grid[row][col] = !grid[row][col];
    }

    /**
     * Sets the selected color for the custom block being edited. This color will be used when saving or updating the custom block in the BlockRegistry.
     */
    public void setColor(Color color) {
        this.selectedColor = color;
    }

    /**
     * Saves the current state of the grid as a new custom block in the BlockRegistry. It trims the grid to remove any empty rows and columns, checks if the resulting piece is not empty, and then adds it to the registry with the selected color. If the piece is empty, an IllegalStateException is thrown.
     */
    public void savePiece() {
        boolean[][] trimmed = trimGrid(grid);
        if (isEmpty(trimmed)) throw new IllegalStateException("Piece cannot be empty");
        registry.addCustomPiece(trimmed, selectedColor);
    }

    /**
     * Loads a custom block into the editor for updating. It sets the pieceToUpdate field to the provided CustomBlock instance, allowing the user to modify the existing block's shape and color in the editor.
     * @param customBlock
     */
    public void loadForUpdate(CustomBlock customBlock) {
        this.pieceToUpdate = customBlock;
    }

    /**
     * Updates the existing custom block in the BlockRegistry with the current state of the grid. It trims the grid to remove any empty rows and columns, checks if the resulting piece is not empty, and then updates the registry with the new shape and selected color for the pieceToUpdate. If the piece is empty, an IllegalStateException is thrown.
     */
    public void updatePiece(){
        boolean[][] trimmed = trimGrid(grid);
        if (isEmpty(trimmed)) throw new IllegalStateException("Piece cannot be empty");
        registry.updateCustomPiece(trimmed, selectedColor, pieceToUpdate);
        pieceToUpdate = null;
    }

    /**
     * Resets the grid to its initial state by setting all cells to false (inactive). This method is typically called when the user wants to clear the editor and start creating a new custom block from scratch.
     */
    public void reset() {
        for (boolean[] row : grid) Arrays.fill(row, false);
    }

    /**
     * Trims the provided grid to remove any empty rows and columns, returning a new grid that contains only the active cells. The method calculates the minimum and maximum row and column indices that contain active cells, creates a new grid of the appropriate size, and copies the active cells from the original grid to the new trimmed grid.
     * @param g
     * @return
     */
    private boolean[][] trimGrid(boolean[][] g) {
        int minRow = MAX_GRID_SIZE;
        int maxRow = 0;
        int minCol = MAX_GRID_SIZE;
        int maxCol = 0;
        for (int r = 0; r < MAX_GRID_SIZE; r++)
            for (int c = 0; c < MAX_GRID_SIZE; c++)
                if (g[r][c]) {
                    minRow = Math.min(minRow, r);
                    maxRow = Math.max(maxRow, r);
                    minCol = Math.min(minCol, c);
                    maxCol = Math.max(maxCol, c);
                }
        int rows = maxRow - minRow + 1;
        int cols = maxCol - minCol + 1;
        int size = Math.max(rows, cols);
        boolean[][] trimmed = new boolean[size][size];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                trimmed[r][c] = g[r + minRow][c + minCol];
        return trimmed;
    }

    /**
     * Checks if the provided grid is empty, meaning that all cells are inactive (false). It iterates through each cell in the grid and returns false if any cell is found to be active (true). If all cells are inactive, it returns true, indicating that the grid is empty.
     */
    private boolean isEmpty(boolean[][] g) {
        for (boolean[] row : g)
            for (boolean cell : row)
                if (cell) return false;
        return true;
    }

    /**
     * Returns the current state of the grid as a 2D boolean array. Each cell in the array represents whether the corresponding cell in the editor is active (true) or inactive (false). This method allows other parts of the application to access the current configuration of the custom block being edited.
     */
    public boolean[][] getGrid() {
        return grid;
    }

    /**
     * Checks if the current grid is empty, meaning that all cells are inactive (false). It calls the isEmpty method with the current grid and returns the result. This method can be used to determine if the user has created any active cells in the editor or if the grid is still in its initial empty state.
     */
    public boolean checkEmpty() {
        return isEmpty(this.grid);
    }

    /**
     * Checks if removing a cell at the specified row and column from the grid would result in a disconnected configuration. It creates a simulated copy of the grid with the specified cell removed and then checks if the remaining active cells are still connected. If the removal would cause disconnection, it returns false; otherwise, it returns true, indicating that the removal is safe.
     */
    public boolean isRemovalSafe(int row, int col) {
        if (!grid[row][col]) return true;

        boolean[][] simulated = copyGrid(grid);
        simulated[row][col] = false;

        return isConnected(simulated);
    }

    /**
     * Creates a copy of the provided 2D boolean array (grid) and returns the new copy. This method is used to create a simulated version of the grid for checking connectivity without modifying the original grid. It iterates through each row of the source array and creates a new array with the same values, effectively duplicating the grid structure.
     * @param source
     * @return
     */
    private boolean[][] copyGrid(boolean[][] source) {
        boolean[][] copy = new boolean[source.length][];
        for (int r = 0; r < source.length; r++)
            copy[r] = Arrays.copyOf(source[r], source[r].length);
        return copy;
    }

    /**
     * Checks if the active cells in the provided grid are connected, meaning that there is a path between any two active cells. It uses a breadth-first search (BFS) algorithm to traverse the grid starting from the first active cell found. The method counts the number of visited active cells and compares it to the total number of active cells in the grid. If all active cells are reachable from the starting cell, it returns true; otherwise, it returns false, indicating that the grid is disconnected.
     */
    private boolean isConnected(boolean[][] g) {
        int startRow = -1, startCol = -1;
        int activeCount = 0;

        for (int r = 0; r < MAX_GRID_SIZE; r++) {
            for (int c = 0; c < MAX_GRID_SIZE; c++) {
                if (g[r][c]) {
                    activeCount++;
                    if (startRow == -1) {
                        startRow = r;
                        startCol = c;
                    }
                }
            }
        }

        if (activeCount <= 1) return true;

        boolean[][] visited = new boolean[MAX_GRID_SIZE][MAX_GRID_SIZE];
        java.util.Deque<int[]> queue = new java.util.ArrayDeque<>();
        queue.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;
        int visitedCount = 1;

        int[][] directions = {{-1, 0}, {1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            for (int[] d : directions) {
                int r = current[0] + d[0];
                int c = current[1] + d[1];
                if (r >= 0 && r < MAX_GRID_SIZE && c >= 0 && c < MAX_GRID_SIZE
                        && g[r][c] && !visited[r][c]) {
                    visited[r][c] = true;
                    visitedCount++;
                    queue.add(new int[]{r, c});
                }
            }
        }

        return visitedCount == activeCount;
    }
}
