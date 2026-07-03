package seda_project.control_alt_defeat.gamebox.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class KeyCapController {

    @FXML
    private Label KeyCapText;

    public String getText() {
        return KeyCapText.getText();
    }

    public void setText(String s) {
        KeyCapText.setText(s);
    }
}
