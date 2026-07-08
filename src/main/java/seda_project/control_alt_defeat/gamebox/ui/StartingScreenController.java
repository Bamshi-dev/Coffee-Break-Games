package seda_project.control_alt_defeat.gamebox.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.BetterCallMain;

import java.net.URL;
import java.util.ResourceBundle;

public class StartingScreenController extends Controller implements Initializable {

    @FXML
    private VBox header;

    /**
     * Handles the action of exiting the application from the starting screen. It plays a button sound effect and calls the cleanExit method from the BetterCallMain class to perform any necessary cleanup and terminate the application gracefully.
     */
    @FXML
    public void onExitAction() {
        sC.play("button");
        BetterCallMain.cleanExit();
    }

    /**
     * Handles the action of starting the Memory game from the starting screen. It plays a button sound effect and transitions to the MemoryMenu scene, allowing the user to configure and start a new Memory game.
     */
    @FXML
    public void onMemoryAction() {
        sC.play("button");
        c.changeScene("/Views/Memory/MemoryMenu.fxml", header, vS);
    }

    /**
     * Handles the action of starting the Tetris game from the starting screen. It plays a button sound effect and transitions to the TetrisMenu scene, allowing the user to configure and start a new Tetris game.
     */
    @FXML
    public void onTetrisAction() {
        sC.play("button");
        c.changeScene("/Views/Tetris/TetrisMenu.fxml", header, vS);
    }

    /**
     * Handles the action of starting the HexChess game from the starting screen. It plays a button sound effect and transitions to the ChessMenu scene, allowing the user to configure and start a new HexChess game.
     */
    @FXML
    protected void onHexChessAction() {
        sC.play("button");
        c.changeScene("/Views/HexChess/ChessMenu.fxml", header, vS);
    }

    /**
     * Initializes the StartingScreenController by calling the superclass's initialize method. This method is part of the Initializable interface and is called to perform any necessary setup for the controller after its root element has been completely processed.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }
}