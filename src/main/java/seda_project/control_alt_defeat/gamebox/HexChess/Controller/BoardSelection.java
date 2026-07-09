package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.BoardDesignState;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.JsonHandler;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.HexButton;
import seda_project.control_alt_defeat.gamebox.ui.HexLabel;

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
     * Passes the BoardDesigner instance and the list of available board designs to the BoardSelection controller. It
     * initializes the internal state of the controller with the provided designer and board states, allowing the
     * user to select and manage board designs.
     * @param designer The BoardDesigner instance that will be used to load the selected board design.
     * @param listofBoards The list of available BoardDesignState instances representing the saved board designs that
     *                     can be selected and managed in the BoardSelection controller.
     */
    public void passBoards(BoardDesigner designer, List<BoardDesignState> listofBoards) {
        this.designer = designer;
        this.boards = listofBoards;
        createList();
    }

    /**
     * Creates a list of available board designs and populates the scrollable content area with HexLabel instances
     * representing each board design. It sets up event handlers for selecting and managing the board designs, allowing
     * the user to choose a board design to use or delete from the list. The method is called during initialization to
     * display the available board designs in the user interface.
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
     * Deselects all HexLabel instances in the availableStates list by removing the "selectedItem" style class from each
     * label. This method is called when a new board design is selected, ensuring that only the currently selected board
     * design is visually highlighted in the user interface.
     */
    private void deselectAll() {
        for (HexLabel l : availableStates) {
            l.getStyleClass().remove("selectedItem");
        }
    }

    /**
     * Handles the action of going back to the previous scene. It plays a button sound effect, retrieves the current
     * stage from the scroll pane, and closes the stage, effectively returning the user to the previous screen.
     * This method is typically called when the user clicks the "Back" button in the user interface, allowing them to
     * exit the board selection screen without making any changes or selections.
     */
    @FXML
    protected void onBackAction(){
        sC.play("button");
        Stage stage = (Stage) scrollPane.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the action of deleting the currently selected board design from the list of available board designs.
     * It plays a button sound effect, removes the selected board design from the internal list and the user interface,
     * updates the JSON file containing the saved board designs, and clears the selection.
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
     * Handles the action of using the currently selected board design. It plays a button sound effect, checks if a
     * board design is selected, loads the selected board design into the BoardDesigner instance, and then calls the
     * onBackAction method to return to the previous scene.
     */
    @FXML
    protected void onUseAction(){
        sC.play("button");
        if (selectedboard == null) return;
        designer.loadBoard(selectedboard);
        onBackAction();
    }

    /**
     * Initializes the BoardSelection controller by disabling the "Delete" and "Use" buttons, preventing the user
     * from performing actions until a board design is selected. This method is called automatically when the controller
     * is created and ensures that the user interface starts in a consistent state, with the action buttons disabled
     * until a valid selection is made.
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
        deleteButton.setDisable(true);
        useButton.setDisable(true);
    }
}
