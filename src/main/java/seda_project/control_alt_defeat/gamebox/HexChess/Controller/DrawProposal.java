package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import java.awt.*;

public class DrawProposal extends Controller {
    private GameScreen screen;
    private int proposing;
    private Image event;

    /**
     * Handles the action when the draw proposal is accepted.
     */
    public void onAcceptAction() {
        ((Button) event.getSource()).setDisable(true);
        sC.play("button");
        screen.drawProposal();
        ((Stage) root.getScene().getWindow()).close();
    }

    /**
     * Handles the action when the draw proposal is declined.
     */
    public void onDeclineAction() {
        ((Button) event.getSource()).setDisable(true);
        sC.play("button");
        screen.declined(proposing);
        ((Stage) root.getScene().getWindow()).close();
    }

    /**
     * Injects the main GameScreen reference into this dialog.
     *
     * @param screen The active GameScreen instance.
     */
    public void sendGameScreen(GameScreen screen){
        this.screen = screen;

    }

    /**
     * Passes the ID of the player who proposed the draw.
     *
     * @param proposing 1 for Player 1, 2 for Player 2.
     */
    public void sendPlayer(int proposing) {
        this.proposing = proposing;
    }
}
