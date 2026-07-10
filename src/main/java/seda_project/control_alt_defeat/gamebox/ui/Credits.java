package seda_project.control_alt_defeat.gamebox.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class Credits extends Controller {

    /**
     * Method to get Back to the starting Screen
     */
    @FXML
    protected void onBackAction(){
        sC.play("button");
        c.backScene(header,vS);
    }

    /**
     * Method to open a Hyperlink
     * @param actionEvent the Event that is risen if clicked on a Hyperlink that is used to get the Hyperlink and
     *                    also the text
     */
    @FXML
    protected void onHyperlinkAction(ActionEvent actionEvent) {
        sC.play("button");
        Hyperlink clicked = (Hyperlink) actionEvent.getSource();
        c.getHostServices().showDocument(clicked.getText());
    }
}
