package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import seda_project.control_alt_defeat.gamebox.ui.Controller;


public class TetrisMenu extends Controller {

    /**
     * Handles the action of going back from the Tetris menu. It plays a button sound effect and transitions back to the previous scene.
     */
    @FXML
    protected void onLocalAction(){
        sC.play("button");
        c.changeScene("/Views/Tetris/LocalGameConfiguration.fxml",header,vS);
    }

    /**
     * Handles the action of going back from the Tetris menu. It plays a button sound effect and transitions back to the previous scene.
     */
    @FXML
    protected void onHostAction(){
        sC.play("button");
        c.changeScene("/Views/Tetris/HostLan.fxml",header,vS);
    }

    /**
     * Handles the action of going back from the Tetris menu. It plays a button sound effect and transitions back to the previous scene.
     */
    @FXML
    protected void onJoinAction(){
        sC.play("button");
        c.changeScene("/Views/Tetris/JoinLan.fxml",header,vS);
    }

    /**
     * Handles the action of going back from the Tetris menu. It plays a button sound effect and transitions back to the previous scene.
     */
    @FXML
    protected void onSettingsAction(){
        sC.play("button");
        c.changeScene("/Views/Tetris/SettingsMenu.fxml",header,vS);
    }

    /**
     * Handles the action of going back from the Tetris menu. It plays a button sound effect and transitions back to the previous scene.
     */
    @FXML
    protected void onPartDesignerAciton(){
        sC.play("button");
        c.changeScene("/Views/Tetris/BlockCreator.fxml",header,vS);
    }

    /**
     * Handles the action of exiting the Tetris menu. It plays a button sound effect and transitions back to the previous scene.
     */
    @FXML
    protected void onExitAction(){
        sC.play("button");
        c.backScene(header, vS);

    }
}
