package seda_project.control_alt_defeat.gamebox.ui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class EasterEgg {

    @FXML
    private VBox spoilerBox;

    @FXML
    protected void onSpoilerClickAction(MouseEvent mouseEvent) {
        if (spoilerBox.getStyleClass().contains("spoilerBox")) {
            spoilerBox.getStyleClass().clear();
            for (Node s : spoilerBox.getChildren()) {
                s.getStyleClass().remove("hiddenText");
            }
        } else {
            spoilerBox.getStyleClass().addAll("spoilerBox", "box");
            for (Node s : spoilerBox.getChildren()) {
                s.getStyleClass().add("hiddenText");
            }
        }
    }
}
