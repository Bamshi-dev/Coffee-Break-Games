package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class MemoryMenu extends Controller {

    /**
     * Handles the action of starting a local game in the Memory menu. It plays a button sound effect and transitions to the LocalGameConfiguration scene, allowing the user to configure settings for a local game.
     */
    @FXML
    protected void onLocalAction(){
        sC.play("button");
        c.changeScene("/Views/Memory/LocalGameConfiguration.fxml",header,vS);
    }
    /**
     * Handles the action of hosting a LAN game in the Memory menu. It plays a button sound effect and transitions to the HostLanConfiguration scene, allowing the user to set up a LAN game as the host.
     */
    @FXML
    protected void onHostAction(){
        sC.play("button");
        c.changeScene("/Views/Memory/HostLanConfiguration.fxml",header,vS);
    }

    /**
     * Handles the action of joining a LAN game in the Memory menu. It plays a button sound effect and transitions to the JoinLanGame scene, allowing the user to connect to an existing LAN game as a client.
     */
    @FXML
    protected void onJoinAction(){
        sC.play("button");
        c.changeScene("/Views/Memory/JoinLanGame.fxml",header,vS);
    }

    /**
     * Handles the action of opening the settings menu in the Memory menu. It plays a button sound effect and transitions to the SettingsMenu scene, allowing the user to configure various settings for the game.
     */
    @FXML
    protected void onExitAction(){
        sC.play("button");
        c.backScene(header, vS);

    }
}
