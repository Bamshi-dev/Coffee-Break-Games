package seda_project.control_alt_defeat.gamebox.ui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class EasterEgg {

    @FXML
    private VBox spoilerBox;

    /**
     * Toggles the visibility of the spoiler content when the spoiler box is clicked. If the spoiler box is currently
     * in its default state, it will reveal the hidden content by removing the "hiddenText" style class from its
     * children. If the spoiler box is already revealed, it will hide the content again by adding the "hiddenText"
     * style class back to its children. This method provides an interactive way for users to reveal or hide spoiler
     * information in the UI.
     * @param mouseEvent The MouseEvent that triggered the action, typically a click event on the spoiler box.
     */
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
