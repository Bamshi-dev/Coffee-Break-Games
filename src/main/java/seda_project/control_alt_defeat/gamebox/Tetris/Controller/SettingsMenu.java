package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisSettings;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class SettingsMenu extends Controller implements Initializable {
    TetrisSettings tS = TetrisSettings.getInstance();

    @FXML
    Label player1Left,player1Right,player1Down,player1Up,player2Left,player2Right,player2Down,player2Up;

    /**
     * Initializes the SettingsMenu controller by updating the displayed key bindings for both players based on the current settings in the TetrisSettings singleton instance.
     * @param url The location used to resolve relative paths for the root object, or null
     */
    @FXML
    protected void changePlayer1(ActionEvent event) {
        sC.play("button");
        Button clicked = (Button) event.getSource();
        int position = Integer.parseInt((String)clicked.getUserData());
        KeyCode input= tS.change(0,position);
        Label change = getPlayerLabel(0,position);
        change.setText(input.toString());

    }

    /**
     * Handles the action of changing the key binding for player 2. It retrieves the button that was clicked, determines the position of the key to be changed, and updates the corresponding key binding in the TetrisSettings singleton instance. It also updates the displayed label for the changed key binding.
     * @param event The ActionEvent triggered by clicking the button to change the key binding for player 2.
     */
    @FXML
    protected void changePlayer2(ActionEvent event) {
        sC.play("button");
        Button clicked = (Button) event.getSource();
        int position = Integer.parseInt((String)clicked.getUserData());
        KeyCode input= tS.change(1,position);
        Label change = getPlayerLabel(1,position);
        change.setText(input.toString());

    }

    /**
     * Handles the action of going back to the previous scene. It plays a button sound effect and transitions back to the previous scene.
     */
    @FXML
    protected void onBackAction(){
        sC.play("button");
        c.backScene(header, vS);
    }

    private Label getPlayerLabel(int player, int position){
        switch (player){
            case 0:
                switch (position){
                    case 0:
                        return player1Left;
                    case 1:
                        return player1Right;
                    case 2:
                        return player1Down;
                    case 3:
                        return player1Up;
                }
                break;
            case 1:
                switch (position){
                    case 0:
                        return player2Left;
                    case 1:
                        return player2Right;
                    case 2:
                        return player2Down;
                    case 3:
                        return player2Up;
                }
                break;
        }
        return null;
    }

    /**
     * Updates the displayed key bindings for both players based on the current settings in the TetrisSettings singleton instance. It retrieves the key codes for each player's controls and sets the corresponding labels to display the key names.
     */
    private void updateKeys() {
        ArrayList<KeyCode> p1keys = tS.getPlayer1Keys();
        player1Left.setText(p1keys.get(0).toString());
        player1Right.setText(p1keys.get(1).toString());
        player1Down.setText(p1keys.get(2).toString());
        player1Up.setText(p1keys.get(3).toString());
        ArrayList<KeyCode> p2keys = tS.getPlayer2Keys();
        player2Left.setText(p2keys.get(0).toString());
        player2Right.setText(p2keys.get(1).toString());
        player2Down.setText(p2keys.get(2).toString());
        player2Up.setText(p2keys.get(3).toString());
    }

    /**
     * Initializes the SettingsMenu controller by updating the displayed key bindings for both players based on the current settings in the TetrisSettings singleton instance. It also sets up any necessary event handlers or additional initialization logic for the settings menu.
     * @param url 
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        updateKeys();
    }
}
