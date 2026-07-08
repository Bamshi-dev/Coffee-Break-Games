package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.BlockRegistry;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisAdvancedSettings;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class LocalGameConfiguration extends Controller implements Initializable {

    protected TetrisAdvancedSettings advancedSettings = TetrisAdvancedSettings.getInstance();

    @FXML
    private TextField player1TF, player2TF;

    @FXML
    private Label statusLabel;

    @FXML
    private ComboBox<Integer> player1Level,player2Level;

    /**
     * Initializes the LocalGameConfiguration controller by setting up the user interface elements, including populating the level selection ComboBoxes for both players with levels from 1 to 20 and selecting the first level by default. It also hides the status label initially.
     * @param location The location used to resolve relative paths for the root object, or null
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        statusLabel.setVisible(false);

        player1Level.getItems().clear();
        player2Level.getItems().clear();

        for (int i = 0; i < 20; i++) {
            player1Level.getItems().add(i+1);
            player2Level.getItems().add(i+1);
        }
        player1Level.getSelectionModel().select(0);
        player2Level.getSelectionModel().select(0);
    }

    /**
     * Handles the action of going back to the previous scene. It plays a button sound effect and transitions back to the previous scene, passing along the player names and levels if the previous controller is an instance of HostLan or LocalGameConfiguration.
     */
    @FXML
    protected void onBackAction() {
        sC.play("button");
        c.backScene(header,vS);
    }

    /**
     * Handles the action of opening the advanced settings for the local game. It plays a button sound effect, retrieves the current player names and levels from the input fields, and transitions to the AdvancedSettings scene, passing along the player data to the new controller.
     */
    @FXML
    protected void onAdvancedSettingsAction(){
        sC.play("button");
        String p1Name = player1TF.getText();
        String p2Name = player2TF.getText();
        int p1Level =  player1Level.getSelectionModel().getSelectedItem();
        int p2Level =  player2Level.getSelectionModel().getSelectedItem();
        AdvancedSettings controller = (AdvancedSettings) c.changeScene("/Views/Tetris/AdvancedSettings.fxml",header,vS);
        controller.handLocalData(p1Name,p2Name,p1Level,p2Level);
    }

    /**
     * Handles the action of starting a local game. It retrieves the player names and levels from the input fields, validates the name lengths, and if valid, creates a new TetrisEngine instance with the provided data and advanced settings. It then transitions to the appropriate game screen based on the layout setting (vertical or horizontal) and initializes the game controller with the necessary data. If the name lengths are invalid, it plays an error sound effect.
     */
    @FXML
    protected void onStartAction() {
        sC.play("button");
        String player1Name = c.checkNameInput(player1TF.getText(),1);
        String player2Name = c.checkNameInput(player2TF.getText(),2);
        int p1Level =  player1Level.getSelectionModel().getSelectedItem();
        int p2Level =  player2Level.getSelectionModel().getSelectedItem();
        String address =  "";
        advancedSettings.setTwoBlocks(false);
        if (advancedSettings.isVertical()){
            address = "/Views/Tetris/GameScreen.fxml";
        }
        else {
            address = "/Views/Tetris/GameScreenHorizontal.fxml";
        }

        if (c.checkNameLength(player1Name,1,statusLabel) && c.checkNameLength(player2Name,2,statusLabel)){
            sC.stopLooping();
            sC.playLooping("tetris_background",.5);
            GameScreen controller = (GameScreen) c.changeScene(address,header,vS);
            TetrisEngine engine = new TetrisEngine(player1Name,player2Name, p1Level,p2Level, BlockRegistry.getInstance(),advancedSettings);
            controller.create(player1Name,player2Name,p1Level, p2Level, engine,header.getScene());
            controller.setInitialLevels(p1Level,p2Level);
            if (c.checkFlip(player1Name,player2Name)){
                controller.flip();
            }
            if (c.checkRainbow(player1Name,player2Name)){
                controller.rainbow();
            }
        }
        else {
            sC.play("error");
        }
    }

    /**
     * Handles the action of resetting the player names and levels to their default values. It clears the text fields for both players and resets the level selection ComboBoxes to the first level (1).
     * @param p1Name 
     * @param p2Name
     * @param p1Level
     * @param p2Level
     */
    public void handLocalData(String p1Name, String p2Name, int p1Level, int p2Level) {
        player1TF.setText(p1Name);
        player2TF.setText(p2Name);
        player1Level.getSelectionModel().select(p1Level-1);
        player2Level.getSelectionModel().select(p2Level-1);
    }
}
