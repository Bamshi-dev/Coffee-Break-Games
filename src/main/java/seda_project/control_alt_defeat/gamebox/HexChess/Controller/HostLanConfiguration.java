package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.ToggleSwitch;

public class HostLanConfiguration  extends Controller {
    private String boardState;

    @FXML
    private TextField hostNameTF;

    @FXML
    private Label statusLabel;

    /**
     * Initiates the hosting process. Validates the username and transitions
     * to the waiting screen if conditions are met.
     */
    @FXML
    protected void onSearchAction() {
        sC.play("button");
        String yourName = c.checkNameInput(hostNameTF.getText(),1);
        if (c.checkNameLength(yourName,1,statusLabel)) {
            Session s = Session.current();
            s.myName = yourName;
            s.isHost = true;
            if (boardState != null) {
                s.boardState = boardState;
            }
            WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/HexChess/WaitForOpponent.fxml", header, vS);
            controller.passHostData(yourName, boardState);
        }
        else{
            sC.play("error");
        }
    }

    /**
     * Cancels the hosting setup and returns to the previous menu.
     * Clears any existing network session data.
     */
    @FXML
    protected void onBackAction() {
        sC.play("button");
        Session.clear();
        c.backScene(header,vS);
    }

    /**
     * Pre-fills the host's name field.
     *
     * @param name The name to fill in.
     */
    public void handData(String name){
        hostNameTF.setText(name);
    }

    /**
     * Assigns a custom board state notation to be used for the hosted game.
     *
     * @param notation FEN-like notation string of the board state.
     * @param p1Name   Optional player 1 name to prefill.
     */
    public void boardSelection(String notation, String p1Name) {
        this.boardState = notation;
        if (p1Name != null){
            hostNameTF.setText(p1Name);
        }
    }

    /**
     * Navigates to the custom board designer to create or select a custom board configuration.
     */
    public void onCustomBoardAction() {
        BoardDesigner controller = (BoardDesigner) c.changeScene("/Views/HexChess/BoardDesigner.fxml",header,vS);

        controller.handNames(hostNameTF.getText(),null);
    }


}
