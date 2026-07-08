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
import seda_project.control_alt_defeat.gamebox.Memory.engine.Decks;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class HostLan extends Controller implements Initializable {
    @FXML
    private RadioButton smallGame,mediumGame,largeGame;

    @FXML
    private ComboBox<Integer> matchSize;

    @FXML
    private ToggleGroup DeckSizeGroup;

    @FXML
    private TextField hostNameTF;

    @FXML
    private Label statusLabel;

    /**
     * Initializes the HostLan controller by setting up the user interface elements, including populating the match size ComboBox with values from 1 to 45 and selecting the third value (3) by default. It also sets the state of the ToggleGroup for deck size selection and hides the status label initially.
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
     * Calculates the deck size based on the selected match size and updates the corresponding RadioButtons for small, medium, and large game options. It retrieves the selected match size from the ComboBox and calls the deckSize method of the Controller class to adjust the available deck size options accordingly. This method is typically called when the user changes the match size selection, ensuring that the deck size options are dynamically updated to reflect the new match size.
     */
    @FXML
    private void calcDeckSize(){
        int tupleSize = matchSize.getSelectionModel().getSelectedItem();
        c.deckSize(tupleSize,smallGame,mediumGame,largeGame);
    }

    /**
     * Handles the action of going back to the previous scene. It plays a button sound effect, clears the current session, and transitions back to the previous scene. This method is typically called when the user clicks the "Back" button in the user interface, allowing them to return to the previous screen without starting a new game session.
     */
    @FXML
    protected void onBackAction(){
        sC.play("button");
        Session.clear();
        c.backScene(header,vS);
    }

    /**
     * Handles the action of searching for an opponent in a LAN game. It retrieves the player's name, match size, and deck size from the input fields, validates the name length, and if valid, sets up the session with the player's data, marks the player as the host, and transitions to the WaitForOpponent scene. If the name is invalid or no deck size is selected, it plays an error sound and displays an appropriate message in the status label. This method is typically called when the user clicks the "Search" button in the user interface, initiating the process of finding an opponent for a LAN game.
     */
    @FXML
    protected void onSearchAction(){
        sC.play("button");
        RadioButton selected = (RadioButton) DeckSizeGroup.getSelectedToggle();

        String yourName = c.checkNameInput(hostNameTF.getText(),1);

        int tupleSize = matchSize.getSelectionModel().getSelectedItem();

        if (c.checkNameLength(yourName, 1,statusLabel)) {
            if (selected != null) {

                int deckSize = Integer.parseInt(selected.getText());

                GameConfig config = new GameConfig(tupleSize, deckSize, yourName, "Opponent");
                GameSetup setup = Decks.prepare(config);

                Session s = Session.current();
                s.myName = yourName;
                s.isHost = true;
                s.config = config;
                s.setup  = setup;

                WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/Memory/WaitForOpponent.fxml",header,vS);
                controller.passHostData(yourName, tupleSize, deckSize);
            } else {
                statusLabel.setVisible(true);
                statusLabel.setText("You need to select a deck Size!");
                sC.play("error");
            }
        }
        else{
            sC.play("error");
        }

    }

    /**
     * Transfers data back to the HostLan controller when returning from another scene, such as the AdvancedSettings scene. It sets the host name, match size, and deck size based on the provided parameters, ensuring that the user interface reflects the previously selected values. This method is typically called when navigating back to the HostLan scene, allowing users to see their previous selections and make adjustments if necessary.
     * @param name The name of the host player, which is set in the hostNameTF TextField to reflect the player's input or previous selection.
     * @param tupleSize The selected match size, which is used to set the selected item in the matchSize ComboBox, allowing users to see their previous selection when returning to the scene.
     * @param deckSize The selected deck size, which is used to determine which RadioButton (smallGame, mediumGame, or largeGame) should be selected based on the provided deck size value. This ensures that the user's previous selection is accurately reflected in the user interface when returning to the HostLan scene.
     */
    public void backTransfer(String name, int tupleSize, int deckSize){
        hostNameTF.setText(name);
        matchSize.getSelectionModel().select(tupleSize-1);
        c.deckSize(tupleSize,smallGame,mediumGame,largeGame);

        if (smallGame.getText().equals(String.valueOf(deckSize))) {
            smallGame.setSelected(true);
        }
        if (mediumGame.getText().equals(String.valueOf(deckSize))) {
            mediumGame.setSelected(true);
        }
        if (largeGame.getText().equals(String.valueOf(deckSize))) {
            largeGame.setSelected(true);
        }
    }
}
