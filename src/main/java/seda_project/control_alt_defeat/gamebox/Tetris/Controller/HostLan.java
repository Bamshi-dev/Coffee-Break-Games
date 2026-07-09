package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisAdvancedSettings;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.ToggleSwitch;

public class HostLan extends Controller implements Initializable {
    private TetrisAdvancedSettings advancedSettings = TetrisAdvancedSettings.getInstance();

    @FXML
    TextField hostNameTF;

    @FXML
    Label statusLabel;

    @FXML
    private ComboBox<Integer> yourLevel;

    @FXML
    private ToggleSwitch toggleSwitch;

    /**
     * Initializes the HostLan controller by setting up the user interface elements, including populating the level s
     * election ComboBox with levels from 1 to 20 and selecting the first level by default. It also sets the state of
     * the ToggleSwitch based on the current advanced settings for two blocks.
     */
    @FXML
    protected void onBackAction(){
        sC.play("button");
        Session.clear();
        c.backScene(header,vS);
        advancedSettings.setTwoBlocks(toggleSwitch.switchOnProperty().getValue());
    }

    /**
     * Handles the action of searching for an opponent in a LAN game. It retrieves the player's name and level from
     * the input fields, validates the name length, and if valid, sets up the session with the player's data, marks
     * the player as the host, and transitions to the WaitForOpponent scene. If the name is invalid, it plays an error sound.
     */
    @FXML
    private void onSearchAction(){
        sC.play("button");
        String yourName = c.checkNameInput(hostNameTF.getText(),1);
        int hostLevel = yourLevel.getSelectionModel().getSelectedItem();
        if (c.checkNameLength(yourName,1,statusLabel)){
            Session s = Session.current();
            s.myName = yourName;
            s.myLevel = hostLevel;
            s.isHost = true;
            advancedSettings.setTwoBlocks(toggleSwitch.switchOnProperty().getValue());
            WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/Tetris/WaitForOpponent.fxml",header,vS);
            controller.passHostData(yourName,hostLevel);
        }
        else{
            sC.play("error");
        }
    }

    @FXML
    private void onAdvancedSettingsAction(){
        AdvancedSettings controller = (AdvancedSettings) c.changeScene("/Views/Tetris/AdvancedSettings.fxml",header,vS);

        String yourName = hostNameTF.getText();
        int hostLevel = yourLevel.getSelectionModel().getSelectedItem();
        advancedSettings.setTwoBlocks(toggleSwitch.switchOnProperty().getValue());

        controller.handHostData(yourName,hostLevel);
        advancedSettings.setTwoBlocks(toggleSwitch.switchOnProperty().getValue());
    }

    /**
     * Initializes the HostLan controller by setting up the user interface elements, including populating the level selection
     * ComboBox with levels from 1 to 20 and selecting the first level by default. It also sets the state of the ToggleSwitch
     * based on the current advanced settings for two blocks.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        statusLabel.setVisible(false);

        yourLevel.getItems().clear();

        for (int i = 0; i < 20; i++) {
            yourLevel.getItems().add(i+1);
        }
        yourLevel.getSelectionModel().select(0);

        toggleSwitch.setSwitchedOn(advancedSettings.isTwoBlocks());
    }

    /**
     * Handles the consistency of data over multiple screens. Resets the player name and player level to the values
     * set before.
     * @param p1Name the player name set before
     * @param p1Level the player level set before
     */
    public void handPlayerData(String p1Name, int p1Level) {
        hostNameTF.setText(p1Name);
        yourLevel.getSelectionModel().select(p1Level-1);
    }
}
