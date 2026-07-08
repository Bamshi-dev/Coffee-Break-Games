package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.BlockRegistry;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.CustomBlock;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.HexButton;
import seda_project.control_alt_defeat.gamebox.ui.HexHBox;

public class BlockController extends Controller implements Initializable {
    private BlockEditorController editorController;
    private Color selectedColor = Color.CYAN;
    private int selectedListIndex = -1;
    private boolean update = false;

    private final int RECTANGLE_SIZE = 75;

    @FXML
    private GridPane gridPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private HexButton saveButton, loadButton, deleteButton, resetButton;

    @FXML
    private ColorPicker colorPicker;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        BlockRegistry registry = BlockRegistry.getInstance();
        editorController = new BlockEditorController(registry);
        colorPicker.setValue(selectedColor);
        buildGrid();
        refreshList();
        saveButton.setDisable(true);
        loadButton.setDisable(true);
        deleteButton.setDisable(true);
        resetButton.setDisable(true);

    }

    /**
     * Builds the grid of rectangles representing the block editor. Each rectangle corresponds to a cell in the 4x4 grid
     * and is initialized with a transparent fill and gray stroke. The rectangles are added to the gridPane, and mouse
     * click events are set up to handle toggling the cells and updating their visual representation.
     */
    private void buildGrid() {
        gridPane.getChildren().clear();

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                Rectangle cell = createCell(row, col);
                gridPane.add(cell, col, row);
            }
        }
    }

    /**
     * Creates a rectangle representing a cell in the block editor grid. The rectangle is initialized with a transparent
     * fill and gray stroke. Mouse click events are set up to handle toggling the cell's state, updating its visual
     * representation, and updating adjacent cells' visual representation based on the current state of the grid.
     */
    private Rectangle createCell(int row, int col) {
        Rectangle cell = new Rectangle(RECTANGLE_SIZE, RECTANGLE_SIZE);
        cell.setFill(Color.TRANSPARENT);
        cell.setStroke(Color.GRAY);
        cell.setStrokeWidth(0.5);

        cell.setOnMouseClicked(e -> {
            resetButton.setDisable(false);
            saveButton.setDisable(false);
            boolean[][] grid = editorController.getGrid();
            boolean alreadyActive = grid[row][col];
            if (alreadyActive) {
                if (!editorController.isRemovalSafe(row, col)) return;
                editorController.toggleCell(row, col);
                updateCellVisual(cell, row, col);
                updateAdjacent(row, col);
                updateAdjacentCellsVisual(row, col);
            }
            else if (editorController.checkEmpty() || isAdjacentToActive(row, col)) {
                editorController.toggleCell(row, col);
                updateCellVisual(cell, row, col);
                updateAdjacent(row, col);
                updateAdjacentCellsVisual(row, col);
            }
        });

        return cell;
    }

    /**
     * Updates the visual representation of adjacent cells in the block editor grid based on the current state of the
     * grid. It checks the four adjacent cells (up, down, left, right) and updates their visual representation accordingly.
     * If an adjacent cell is out of bounds or null, it catches the NullPointerException and prints the exception message
     * to the console.
     */
    private void updateAdjacent(int row, int col) {
        try{
            updateAdjacentCellsVisual(row-1,col);
        }
        catch(NullPointerException npe){
            System.out.println(npe.getMessage());
        }
        try{
            updateAdjacentCellsVisual(row+1,col);
        }
        catch(NullPointerException npe){
            System.out.println(npe.getMessage());
        }
        try{
            updateAdjacentCellsVisual(row,col+1);
        }
        catch(NullPointerException npe){
            System.out.println(npe.getMessage());
        }
        try{
            updateAdjacentCellsVisual(row,col-1);
        }
        catch(NullPointerException npe){
            System.out.println(npe.getMessage());
        }

    }

    /**
     * Updates the visual representation of a specific cell in the block editor grid based on its current state. It checks
     * if the cell is active (filled) or inactive (empty) and sets the fill color accordingly. If the cell is active, it
     * uses the selected color; otherwise, it sets the fill to transparent.
     */
    private void updateAdjacentCellsVisual(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) return;
        Rectangle cell = getCellAt(row,col);

        boolean[][] grid = editorController.getGrid();
        //Do not change already selected Cells
        if (grid[row][col]) return;

        boolean isAdjacentToActive = isAdjacentToActive(row, col);
        cell.setStroke(isAdjacentToActive ? Color.RED : Color.GRAY);
        cell.setStrokeWidth(isAdjacentToActive ? 1.5 : 0.5);
    }

    /**
     * Checks if a specific cell in the block editor grid is adjacent to any active (filled) cells. It checks the four
     * adjacent cells (up, down, left, right) and returns true if any of them are active; otherwise, it returns false.
     */
    private boolean isAdjacentToActive(int row, int col) {
        boolean[][] grid = editorController.getGrid();
        int[][] directions = {{-1,0},{1,0},{0,1},{0,-1}};
        for (int[] d : directions) {
            int r = row + d[0], c = col + d[1];
            if (r >= 0 && r < 4 && c >= 0 && c < 4 && grid[r][c]) return true;
        }
        return false;
    }

    /**
     * Retrieves the rectangle representing a specific cell in the block editor grid based on its row and column indices.
     * It iterates through the children of the gridPane and checks their row and column indices to find the matching rectangle.
     * If found, it returns the rectangle; otherwise, it returns null.
     */
    private Rectangle getCellAt(int row, int col) {
        for (var node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col)
                return (Rectangle) node;
        }
        return null;
    }

    /**
     * Updates the visual representation of a specific cell in the block editor grid based on its current state. It checks
     * if the cell is active (filled) or inactive (empty) and sets the fill color accordingly. If the cell is active, it
     * uses the selected color; otherwise, it sets the fill to transparent.
     */
    private void updateCellVisual(Rectangle cell, int row, int col) {
        boolean active = editorController.getGrid()[row][col];
        cell.setFill(active ? selectedColor : Color.TRANSPARENT);
    }

    /**
     * Refreshes the visual representation of the entire block editor grid based on the current state of the grid. It
     * iterates through all the rectangles in the gridPane and updates their fill color based on whether they are active
     * (filled) or inactive (empty). It also updates the visual representation of adjacent cells for each cell in the grid.
     */
    private void refreshGrid() {
        boolean[][] grid = editorController.getGrid();
        gridPane.getChildren().forEach(node -> {
            if (node instanceof Rectangle cell) {
                int col = GridPane.getColumnIndex(node);
                int row = GridPane.getRowIndex(node);
                cell.setFill(grid[row][col] ? selectedColor : Color.TRANSPARENT);
            }
        });
        for (int r = 0; r < 4; r++)
            for (int cc = 0; cc < 4; cc++)
                updateAdjacentCellsVisual(r, cc);
    }

    /**
     * Refreshes the list of custom blocks displayed in the scrollPane. It retrieves the list of custom pieces from the
     * BlockRegistry singleton instance and creates a VBox containing rows for each custom block. Each row consists of
     * a mini preview of the block and its name. The rows are added to the VBox, which is then set as the content of the
     * scrollPane. If there are no custom pieces, it sets the selectedListIndex to -1.
     */
    private void refreshList() {
        VBox list = new VBox(8);
        list.setStyle("-fx-padding: 8;");

        var customPieces = BlockRegistry.getInstance().getCustomPieces();

        for (int i = 0; i < customPieces.size(); i++) {
            final int index = i;
            CustomBlock piece = customPieces.get(i);

            HBox row = buildListRow(piece, index);
            list.getChildren().add(row);
        }
        if (customPieces.isEmpty()) selectedListIndex = -1;
        scrollPane.setContent(list);
    }

    /** 
     * Builds a row in the list of custom blocks displayed in the scrollPane. Each row consists of a mini preview of the
     * block and its name. The row is created as an HBox, and mouse click events are set up to handle selecting the row,
     * updating the selectedListIndex, and enabling/disabling the load and delete buttons based on the selection.
     */
    private HBox buildListRow(CustomBlock piece, int index) {
        Pane preview = buildMiniPreview(piece);

        Label name = new Label("Custom " + (index + 1));

        HexHBox row = new HexHBox();
        row.getChildren().addAll(preview,name);
        row.setAlignment(Pos.CENTER);
        row.getStyleClass().add("scrollItem");
        row.setStyle("-fx-cursor: hand;");

        row.setOnMouseClicked(e -> {
            selectedListIndex = index;
            ((VBox) scrollPane.getContent()).getChildren().forEach(
                    n -> n.getStyleClass().remove("selectedItem")
            );
            row.getStyleClass().add("selectedItem");
            deleteButton.setDisable(false);
            loadButton.setDisable(false);
        });
        return row;
    }

    /**
     * Builds a mini preview of a custom block for display in the list of custom blocks. It creates a GridPane and
     * populates it with rectangles representing the shape of the custom block. Each rectangle is filled with the color
     * of the block if it is part of the shape; otherwise, it is transparent. The rectangles are added to the GridPane,
     * which is then returned as a Pane for display.
     */
    private Pane buildMiniPreview(CustomBlock piece) {
        GridPane preview = new GridPane();
        boolean[][] shape = piece.getShape();
        int cellSize = 10;

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                cell.setFill(shape[r][c] ? piece.getColor() : Color.TRANSPARENT);
                cell.setStroke(Color.GRAY);
                cell.setStrokeWidth(0.3);
                preview.add(cell, c, r);
            }
        }
        return preview;
    }


    /**
     * Handles the action of going back to the previous scene. It plays a button click sound and navigates back to the
     * previous scene using the controller's backScene method.
     */
    @FXML
    protected void onBackAction(){
        sC.play("button");
        c.backScene(header,vS);
    }

    /**
     * Handles the action of loading a selected custom block from the list. It plays a button click sound and checks if
     * a valid custom block is selected. If a valid block is selected, it enables the save and reset buttons, retrieves
     * the selected custom block from the BlockRegistry, and loads it into the block editor for updating. It also updates
     * the visual representation of the grid and adjacent cells based on the loaded block's shape.
     */
    @FXML
    protected void onLoadAction(){
        sC.play("button");
        if (selectedListIndex < 0) return;

        saveButton.setDisable(false);
        resetButton.setDisable(false);

        var customPieces = BlockRegistry.getInstance().getCustomPieces();
        if (selectedListIndex >= customPieces.size()) return;

        CustomBlock piece = customPieces.get(selectedListIndex);
        update = true;
        saveButton.setText("Update");
        editorController.reset();
        editorController.loadForUpdate(piece);
        boolean[][] shape = piece.getShape();
        for (int r = 0; r < shape.length; r++)
            for (int c = 0; c < shape[r].length; c++)
                if (shape[r][c]) editorController.toggleCell(r, c);

        selectedColor = (Color) piece.getColor();
        refreshGrid();
        for (int r = 0; r < 4; r++)
            for (int cc = 0; cc < 4; cc++)
                updateAdjacentCellsVisual(r, cc);
    }

    /**
     * Handles the action of saving or updating a custom block in the block editor. It plays a button click sound and
     * checks if the editor is in update mode or not. If not in update mode, it saves the new custom block; otherwise,
     * it updates the existing custom block. After saving or updating, it resets the editor, refreshes the grid and list,
     * and disables the save and reset buttons. If an exception occurs during saving or updating, it displays an error
     * message on the save button and temporarily disables it before resetting its text and enabling it again.
     */
    @FXML
    protected void onSaveAction(){
        try {
            if (!update) {
                editorController.setColor(selectedColor);
                editorController.savePiece();
                editorController.reset();
                refreshGrid();
                refreshList();
            }
            else {
                editorController.setColor(selectedColor);
                editorController.updatePiece();
                editorController.reset();
                refreshGrid();
                refreshList();
                saveButton.setText("Save");
            }
            saveButton.setDisable(true);
            resetButton.setDisable(true);
        } catch (Exception ex) {
            saveButton.setText("Empty!");
            saveButton.setDisable(true);
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> {
                    saveButton.setText("Save");
                    saveButton.setDisable(false);
                });
            }).start();
        }
        update = false;
    }

    /**
     * Handles the action of deleting a selected custom block from the list. It plays a button click sound and checks if
     * a valid custom block is selected. If a valid block is selected, it disables the load and delete buttons, retrieves
     * the selected custom block from the BlockRegistry, removes it from the registry, and refreshes the list of custom
     * blocks displayed in the scrollPane. It also prints the index of the deleted block to the console.
     */
    @FXML
    protected void onDeleteAction(){
        sC.play("button");
        if (selectedListIndex < 0) return;
        loadButton.setDisable(true);
        deleteButton.setDisable(true);
        BlockRegistry pR = BlockRegistry.getInstance();
        CustomBlock cP = pR.getCustomPieces().get(selectedListIndex);
        pR.removeCustomPiece(cP);
        refreshList();
        System.out.println(selectedListIndex);
    }

    /**
     * Handles the action of selecting a new Color. It plays a button click sound, updates the selected color and
     * refreshes the Grid
     */
    @FXML
    protected void onColorAction(){
        sC.play("button");
        Color c = colorPicker.getValue();
        this.selectedColor = c;
        refreshGrid();
    }

    /**
     * Handles the action of resetting the block editor to its initial state. It plays a button click sound, resets the
     * editor, refreshes the grid and list of custom blocks, and disables the save, load, delete, and reset buttons.
     */
    @FXML
    protected void onResetAction(){
        sC.play("button");
        editorController.reset();
        refreshGrid();
        refreshList();
        resetButton.setDisable(true);
        saveButton.setDisable(true);
        loadButton.setDisable(true);
        deleteButton.setDisable(true);
    }
}
