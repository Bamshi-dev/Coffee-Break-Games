package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class LocalGameConfiguration extends Controller implements Initializable {

    @FXML
    private ComboBox<Integer> matchSize;

    @FXML
    private ToggleGroup DeckSizeGroup;

    @FXML
    private RadioButton smallGame,mediumGame,largeGame;

    @FXML
    private TextField player1TF, player2TF;

    @FXML
    private Label statusLabel;

    /**
     * Initializes the LocalGameConfiguration controller by setting up the user interface elements, including
     * populating the match size ComboBox with values from 1 to 45 and selecting the third value (3) by default. It
     * also sets the state of the ToggleGroup for deck size selection and hides the status label initially.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        statusLabel.setVisible(false);

        matchSize.getItems().clear();
        for(int i = 1; i <= 45; i++) {
            matchSize.getItems().add(i);
        }
        matchSize.getSelectionModel().select(2);
    }

    /**
     * Calculates the deck size based on the selected match size and updates the corresponding RadioButtons for small,
     * medium, and large game options. It retrieves the selected match size from the ComboBox and calls the deckSize
     * method of the Controller class to adjust the available deck size options accordingly. This method is called when
     * the user changes the match size selection, ensuring that the deck size options are dynamically updated to
     * reflect the new match size.
     */
    @FXML
    private void calcDeckSize(){
        int tupleSize = matchSize.getSelectionModel().getSelectedItem();
        c.deckSize(tupleSize,smallGame,mediumGame,largeGame);
    }

    /**
     * Handles the action of going back to the previous scene. It plays a button sound effect and transitions back to
     * the previous scene, passing along the player names and levels if the previous controller is an instance of
     * HostLan or LocalGameConfiguration.
     */
    @FXML
    protected void onBackAction(){
        sC.play("button");
        c.backScene(header,vS);
    }


    @FXML
    private void onStartGameAction(){
        sC.play("button");
        RadioButton selected = (RadioButton) DeckSizeGroup.getSelectedToggle();

        String player1Name = c.checkNameInput(player1TF.getText(),1);
        String player2Name = c.checkNameInput(player2TF.getText(),2);
        int tupleSize = matchSize.getSelectionModel().getSelectedItem();


        if (c.checkNameLength(player1Name,1, statusLabel) && c.checkNameLength(player2Name,2,statusLabel)) {
            if (selected != null) {
                sC.stopLooping();
                sC.playLooping("memory_background",.3);
                int deckSize = Integer.parseInt(selected.getText());
                GameScreen controller = (GameScreen) c.changeScene("/Views/Memory/GameScreen.fxml",header,vS);
                if (c.checkFlip(player1Name,player2Name)){
                    controller.flip();
                }
                if (c.checkRainbow(player1Name, player2Name)){
                    controller.rainbow();
                }

                controller.passMemoryData(player1Name, player2Name, tupleSize, deckSize);
                controller.startGame();
            }
            else {
                statusLabel.setVisible(true);
                statusLabel.setText("You need to select a deck Size!");
                sC.play("error");
            }
        }
        else{
            sC.play("error");
        }

    }
}
