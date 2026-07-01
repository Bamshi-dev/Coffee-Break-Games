package seda_project.control_alt_defeat.gamebox.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class KeyCapController {

    @FXML
    private Label KeyCapText;

    public void setText(String s){
        KeyCapText.setText(s);
    }

    public String getText(){
        return KeyCapText.getText();
    }
}
