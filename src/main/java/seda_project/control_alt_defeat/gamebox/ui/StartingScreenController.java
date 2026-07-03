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

    @FXML
    public void onExitAction() {
        sC.play("button");
        BetterCallMain.cleanExit();
    }

    @FXML
    public void onMemoryAction() {
        sC.play("button");
        c.changeScene("/Views/Memory/MemoryMenu.fxml", header, vS);
    }

    @FXML
    public void onTetrisAction() {
        sC.play("button");
        c.changeScene("/Views/Tetris/TetrisMenu.fxml", header, vS);
    }

    @FXML
    protected void onHexChessAction() {
        sC.play("button");
        c.changeScene("/Views/HexChess/ChessMenu.fxml", header, vS);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }
}