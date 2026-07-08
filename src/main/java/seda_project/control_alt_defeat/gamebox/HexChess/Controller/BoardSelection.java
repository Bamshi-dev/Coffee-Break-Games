package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.BoardDesignState;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.JsonHandler;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.HexButton;
import seda_project.control_alt_defeat.gamebox.ui.HexLabel;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BoardSelection extends Controller implements Initializable {
    private List<HexLabel> availableStates = new ArrayList<>();
    private HexLabel selectedState;
    private BoardDesignState selectedboard;
    private List<BoardDesignState> boards;
    private BoardDesigner designer;

    @FXML
    private VBox scrollContent;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private HexButton useButton, deleteButton;

    /**
     * Receives the list of available board states from the BoardDesigner.
     *
     * @param designer     The BoardDesigner controller instance.
     * @param listofBoards The list of currently saved custom board designs.
     */
    public void passBoards(BoardDesigner designer, List<BoardDesignState> listofBoards) {
        this.designer = designer;
        this.boards = listofBoards;
        createList();
    }

    /**
     * Populates the scrollable UI list with the available board designs.
     * Sets up click listeners for selecting specific board states.
     */
    private void createList(){
        if (boards != null) {
            for (BoardDesignState board : boards) {
                String fenNotation = board.getFENState().replaceAll("\"", "");
                HexLabel l = new HexLabel(fenNotation);
                l.getStyleClass().add("scrollItem");
                availableStates.add(l);
                l.setOnMouseClicked(mouseEvent -> {
                    useButton.setDisable(false);
                    deleteButton.setDisable(false);
                    deselectAll();
                    selectedState = l;
                    selectedboard = board;
                    l.getStyleClass().add("selectedItem");
                    l.getStyleClass().forEach(System.out::println);
                });
                scrollContent.getChildren().add(l);
            }
        }
    }

    /**
     * Removes the "selectedItem" styling from all listed board state labels.
     */
    private void deselectAll() {
        for (HexLabel l : availableStates) {
            l.getStyleClass().remove("selectedItem");
        }
    }

    /**
     * Closes the board selection window and returns to the previous screen.
     */
    @FXML
    protected void onBackAction(){
        sC.play("button");
        Stage stage = (Stage) scrollPane.getScene().getWindow();
        stage.close();
    }

    /**
     * Deletes the currently selected board state from the list and the saved JSON file.
     */
    @FXML
    protected void onDeleteAction(){
        sC.play("button");
        if (selectedboard == null) return;
        int position = boards.indexOf(selectedboard);
        availableStates.remove(position);
        scrollContent.getChildren().remove(position);
        boards.remove(position);
        JsonHandler handler = new  JsonHandler();
        handler.writeBoardStates(boards);
        selectedboard = null;
    }

    /**
     * Loads the currently selected board state into the BoardDesigner and closes the selection window.
     */
    @FXML
    protected void onUseAction(){
        sC.play("button");
        if (selectedboard == null) return;
        designer.loadBoard(selectedboard);
        onBackAction();
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
        deleteButton.setDisable(true);
        useButton.setDisable(true);
    }
}
