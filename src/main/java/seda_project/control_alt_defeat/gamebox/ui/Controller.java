package seda_project.control_alt_defeat.gamebox.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Configuration;
import seda_project.control_alt_defeat.gamebox.SoundController;
import seda_project.control_alt_defeat.gamebox.ViewStack;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * The Controller class serves as a base class for all controllers in the gamebox application. It provides common
 * functionality for scene management, sound control, and user interface interactions. The class implements the
 * Initializable interface to allow for initialization of UI components and event handling.
 */
public class Controller implements Initializable {
    protected Configuration c = Configuration.getInstance();
    protected ViewStack vS = ViewStack.getInstance();
    protected SoundController sC = SoundController.getInstance();
    protected boolean flipped = false;
    protected boolean rainbowed = false;

    @FXML
    protected StackPane root;

    @FXML
    protected VBox header;

    /**
     * Flips the root StackPane by 180 degrees, creating a visual effect of flipping the entire scene. This method can
     * be used to create a unique user interface experience, such as revealing hidden content or providing a different
     * perspective on the current scene. The flipped state is tracked by the 'flipped' boolean variable.
     */
    public void flip() {
        flipped = true;
        root.rotateProperty().setValue(180);
    }

    /**
     * Applies a continuous rainbow color effect to the background of the root StackPane. The method creates a Timeline
     * animation that cycles through hues from 0 to 360 degrees, updating the background color of the root pane in
     * real-time. This creates a visually appealing effect that can be used for special events or themes within the
     * application. The rainbowed state is tracked by the 'rainbowed' boolean variable.
     */
    public void rainbow() {
        rainbowed = true;
        IntegerProperty hue = new SimpleIntegerProperty(0);

        hue.addListener((observable, oldValue, newValue) -> {
            root.setStyle(String.format("-fx-background-color: hsb(%d, 80%%, 90%%);", newValue.intValue()));
        });

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(hue, 0)),
                new KeyFrame(Duration.seconds(10), new KeyValue(hue, 360))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    /**
     * Initializes the Controller by setting up the user interface elements and event handlers. This method is called
     * automatically when the controller is created and allows for customization of the scene's appearance and behavior.
     * It also sets up a double-click event on the header to toggle between different themes
     * (MainTheme and CrimsonTheme) based on user interaction.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.header != null) {
            header.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        c.crimson = !c.crimson;
                        if (c.crimson) {
                            int pos = root.getStylesheets().indexOf(Objects.requireNonNull(Controller.class.getResource("/css/stylesheets/MainTheme.css")).toExternalForm());
                            root.getStylesheets().remove(Objects.requireNonNull(Controller.class.getResource("/css/stylesheets/MainTheme.css")).toExternalForm());
                            root.getStylesheets().add(pos, Objects.requireNonNull(Controller.class.getResource("/css/stylesheets/CrimsonTheme.css")).toExternalForm());
                        } else {
                            int pos = root.getStylesheets().indexOf(Objects.requireNonNull(Controller.class.getResource("/css/stylesheets/CrimsonTheme.css")).toExternalForm());
                            root.getStylesheets().remove(Objects.requireNonNull(Controller.class.getResource("/css/stylesheets/CrimsonTheme.css")).toExternalForm());
                            root.getStylesheets().add(pos, Objects.requireNonNull(Controller.class.getResource("/css/stylesheets/MainTheme.css")).toExternalForm());
                        }
                    }
                }
            });
        }
        if (c.crimson) {
            int pos = root.getStylesheets().indexOf(Objects.requireNonNull(Controller.class.getResource("/css/stylesheets/MainTheme.css")).toExternalForm());
            root.getStylesheets().remove(Objects.requireNonNull(Controller.class.getResource("/css/stylesheets/MainTheme.css")).toExternalForm());
            root.getStylesheets().add(pos, Objects.requireNonNull(Controller.class.getResource("/css/stylesheets/CrimsonTheme.css")).toExternalForm());
        }
        Rectangle clip = new Rectangle();

        clip.setArcWidth(32);
        clip.setArcHeight(32);
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        root.setClip(clip);
    }
}
