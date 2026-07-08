package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

/**
 * Menu controller for the HexChess module.
 * Provides navigation to local games, LAN hosting, LAN joining, settings, and the board designer.
 */
public class ChessMenu extends Controller {
    @FXML
    private VBox header;

    /**
     * Handles the action of starting a local game in the HexChess menu. It plays a button sound effect and transitions
     * to the LocalGameConfiguration scene, allowing the user to configure settings for a local game.
     */
    @FXML
    protected void onLocalAction() {
        sC.play("button");
        c.changeScene("/Views/HexChess/LocalGameConfiguration.fxml",header,vS);
    }

    /**
     * Handles the action of joining a LAN game in the HexChess menu. It plays a button sound effect and transitions
     * to the JoinLan scene, allowing the user to connect to an existing LAN game as a client.
     */
    @FXML
    protected void onJoinAction() {
        sC.play("button");
        c.changeScene("/Views/HexChess/JoinLan.fxml",header,vS);
    }

    /**
     * Handles the action of hosting a LAN game in the HexChess menu. It plays a button sound effect and transitions
     * to the HostLanConfiguration scene, allowing the user to set up a LAN game as the host.
     */
    @FXML
    protected void onHostAction() {
        sC.play("button");
        c.changeScene("/Views/HexChess/HostLanConfiguration.fxml",header,vS);
    }

    /**
     * Handles the action of opening the settings menu in the HexChess menu. It plays a button sound effect and
     * transitions to the SettingsMenu scene, allowing the user to configure various settings for the game.
     */
    @FXML
    protected void onExitAction() {
        sC.play("button");
        c.backScene(header,vS);
    }

    /**
     * Handles the action of opening the settings menu in the HexChess menu. It plays a button sound effect and
     * transitions to the SettingsMenu scene, allowing the user to configure various settings for the game.
     */
    @FXML
    protected void onSettingsAction() {
        sC.play("button");
        c.changeScene("/Views/HexChess/ChessSettings.fxml",header,vS);
    }

    /**
     * Handles the action of opening the board designer in the HexChess menu. It plays a button sound effect,
     * transitions to the BoardDesigner scene, and disables the "Use" button in the designer, allowing the user to
     * create or edit custom board designs without immediately applying them.
     */
    @FXML
    public void onCustomBoardAction() {
        sC.play("button");
        BoardDesigner controller = (BoardDesigner)c.changeScene("/Views/HexChess/BoardDesigner.fxml",header,vS);
        controller.disableUse();
    }
}
