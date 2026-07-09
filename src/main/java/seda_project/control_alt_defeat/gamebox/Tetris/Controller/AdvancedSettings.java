package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisAdvancedSettings;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.IntField;
import seda_project.control_alt_defeat.gamebox.ui.Toast;
import seda_project.control_alt_defeat.gamebox.ui.ToggleSwitch;


public class AdvancedSettings extends Controller implements Initializable {
    private final TetrisAdvancedSettings advancedSettings = TetrisAdvancedSettings.getInstance();
    String p1Name, p2Name;
    int p1Level, p2Level;
    @FXML
    private CheckBox swapBoards, swapBlocks,portals, opponentSlowDown, opponentSpeedUp, opponentDelayRotation,selfSlowDown,selfDelayRotation,radialBomb,columnBomb;

    @FXML
    private IntField itemSpawnRate, itemDespawnTime, bombChance;

    @FXML
    private ToggleGroup Layout;

    @FXML
    private ToggleSwitch toggleSwitch;

    /**
     * Method that is called when clicking on the "Back" Button. The user is brought back to the screen they came from
     * and restores the before set values
     */
    @FXML
    protected void onBackAction(){
        sC.play("button");
        var controller = c.backScene(header,vS);
        if (controller instanceof HostLan hL){
            hL.handPlayerData(p1Name,p1Level);
        } else if (controller instanceof LocalGameConfiguration lGC){
            lGC.handLocalData(p1Name,p2Name,p1Level,p2Level);
        }
    }
    /**
     * Saves the advanced settings to the TetrisAdvancedSettings singleton instance and displays a toast message indicating
     * that the settings have been saved. If the bomb chance exceeds 100%, it sets the bomb chance to 100% and displays
     * a specific message.
     */
    @FXML
    protected void onSaveAction(){
        sC.play("button");
        advancedSettings.saveIntSettings(itemSpawnRate.getValue(), itemDespawnTime.getValue(),bombChance.getValue());
        advancedSettings.setSwapBoards(swapBoards.isSelected());
        advancedSettings.setSwapBlocks(swapBlocks.isSelected());
        advancedSettings.setPortals(portals.isSelected());
        advancedSettings.setOpponentDelayRotation(opponentDelayRotation.isSelected());
        advancedSettings.setOpponentSpeedUp(opponentSpeedUp.isSelected());
        advancedSettings.setOpponentSlowDown(opponentSlowDown.isSelected());
        advancedSettings.setSelfDelayRotation(selfDelayRotation.isSelected());
        advancedSettings.setSelfSpeedDown(selfSlowDown.isSelected());
        advancedSettings.setRadialBomb(radialBomb.isSelected());
        advancedSettings.setColumnBomb(columnBomb.isSelected());
        advancedSettings.setBoardChange(toggleSwitch.switchOnProperty().getValue());
        advancedSettings.setVertical(((RadioButton)Layout.getSelectedToggle()).getText().equals("Vertical"));
        if (bombChance.getValue() >100){
            Toast.makeText(root, "Settings saved!\nBomb Chance set to 100%");
            bombChance.setText(String.valueOf(100));
            advancedSettings.saveIntSettings(itemSpawnRate.getValue(), itemDespawnTime.getValue(),100);
        }
        else {
            Toast.makeText(root, "Settings saved!");
        }
    }

    /**
     * Initializes the advanced settings controller by setting the initial values of the UI components based on the
     * current settings in the TetrisAdvancedSettings singleton instance. It also sets up the toggle group for layout
     * selection and initializes the toggle switch state.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        ObservableList<Toggle> toggels = Layout.getToggles();
        if (!advancedSettings.isVertical()){
            toggels.get(1).setSelected(true);
        }
        toggleSwitch.setSwitchedOn(advancedSettings.isBoardChange());
        swapBoards.setSelected(advancedSettings.isSwapBoards());
        swapBlocks.setSelected(advancedSettings.isSwapBlocks());
        portals.setSelected(advancedSettings.isPortals());
        opponentSlowDown.setSelected(advancedSettings.isOpponentSlowDown());
        opponentSpeedUp.setSelected(advancedSettings.isOpponentSpeedUp());
        opponentDelayRotation.setSelected(advancedSettings.isOpponentDelayRotation());
        selfSlowDown.setSelected(advancedSettings.isSelfSpeedDown());
        selfDelayRotation.setSelected(advancedSettings.isSelfDelayRotation());
        radialBomb.setSelected(advancedSettings.isRadialBomb());
        columnBomb.setSelected(advancedSettings.isColumnBomb());

        itemSpawnRate.setText((advancedSettings.getItemSpawnRate()/1000)+"");
        itemDespawnTime.setText((advancedSettings.getItemDespawnRate()/1000)+"");
        bombChance.setText(String.valueOf(advancedSettings.getBombChance()));
    }

    /**
     * Method to store the data (e.g. player name and player leve) form the Host Screen
     */
    public void handHostData(String yourName, int hostLevel) {
        this.p1Name = yourName;
        this.p1Level = hostLevel;
    }

    /**
     * Handles the data received from a local game configuration. It sets the player names and levels for both players
     * based on the provided parameters.
     */
    public void handLocalData(String p1Name, String p2Name, int p1Level, int p2Level) {
        this.p1Name = p1Name;
        this.p1Level = p1Level;
        this.p2Name = p2Name;
        this.p2Level = p2Level;
    }
}
