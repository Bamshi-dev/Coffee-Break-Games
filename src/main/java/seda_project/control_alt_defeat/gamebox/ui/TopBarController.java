package seda_project.control_alt_defeat.gamebox.ui;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.SoundController;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class TopBarController implements Initializable {
    private final Image mute = new Image(Objects.requireNonNull(Controller.class.getResource("/Images/others/mute.png")).toExternalForm());
    private final Image unmute = new Image(Objects.requireNonNull(Controller.class.getResource("/Images/others/unmute.png")).toExternalForm());
    private final SoundController sC = SoundController.getInstance();
    private double offsetX;
    private double offsetY;

    @FXML
    private HBox topBar;
    @FXML
    private ImageView muteButton;

    /**
     * Closes the application window when the close button is clicked. This method retrieves the current stage from the topBar's scene and calls the close() method to terminate the application window. It is typically bound to a close button in the user interface.
     */
    @FXML
    protected void closeWindow() {
        Stage s = (Stage) topBar.getScene().getWindow();
        s.close();
    }

    /**
     * Minimizes the application window when the minimize button is clicked. This method retrieves the current stage from the topBar's scene and sets its iconified property to true, effectively minimizing the window to the taskbar or dock. It is typically bound to a minimize button in the user interface.
     */
    @FXML
    protected void changeMute() {
        boolean next = sC.getMute();
        sC.setMute(!next);
        setMuteImageView(!next);
        changeSound(next);
    }

    /**
     * Minimizes the application window when the minimize button is clicked. This method retrieves the current stage from the topBar's scene and sets its iconified property to true, effectively minimizing the window to the taskbar or dock. It is typically bound to a minimize button in the user interface.
     */
    public void initMute() {
        boolean current = sC.getMute();
        setMuteImageView(current);
    }

    /**
     * 
     */
    public void setMuteImageView(boolean toSet) {
        if (toSet) {
            muteButton.setImage(mute);
        } else {
            muteButton.setImage(unmute);
        }
    }

    /**
     * Changes the sound state of the application based on the provided boolean value. If toSet is true, the sound is resumed; if false, the sound is paused. This method allows for dynamic control of the application's audio output, enabling or disabling sound effects and music as needed. It is typically called in response to user interactions with the mute button or other sound-related controls in the user interface.
     * @param toSet A boolean value indicating whether to resume (true) or pause (false) the sound. When set to true, the application's audio output will be enabled; when set to false, the audio output will be disabled.
     */
    public void changeSound(boolean toSet) {
        if (toSet) {
            sC.resume();
        } else {
            sC.pause();
        }
    }

    /**
     * Initializes the TopBarController by checking if the muteButton is not null and calling the initMute() method to set the initial mute state of the application. This method is part of the Initializable interface and is called to perform any necessary setup for the controller after its root element has been completely processed.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (muteButton != null) {
            initMute();
        }
    }

    /**
     * Handles the drag movement of the application window when the user clicks and drags the top bar. It calculates the new position of the window based on the mouse's screen coordinates and updates the stage's X and Y properties accordingly. This method allows for a custom draggable window experience, enabling users to move the application window around the screen by interacting with the top bar.
     * @param mouseEvent The MouseEvent object containing information about the mouse event, including the current screen coordinates of the mouse cursor. This parameter is used to determine the new position of the application window during the drag operation.
     */
    @FXML
    public void onDragMovement(MouseEvent mouseEvent) {

        Stage stage = (Stage) topBar.getScene().getWindow();

        stage.setX(mouseEvent.getScreenX() - offsetX);
        stage.setY(mouseEvent.getScreenY() - offsetY);
    }

    /**
     * Handles the initial click event on the top bar to start the drag operation. It records the initial X and Y offsets of the mouse cursor relative to the top bar, which are later used in the onDragMovement method to calculate the new position of the application window. This method is typically bound to a mouse press event on the top bar in the user interface.
     * @param mouseEvent The MouseEvent object containing information about the mouse event, including the current coordinates of the mouse cursor relative to the top bar. This parameter is used to determine the initial offsets for the drag operation.
     */
    @FXML
    public void onDragStart(MouseEvent mouseEvent) {
        offsetX = mouseEvent.getX();
        offsetY = mouseEvent.getY();
    }

    /**
     * Handles the action of toggling the visibility of the Easter Egg panel when the corresponding button is clicked. If the panel is not currently displayed, it loads the EasterEggs.fxml view, adds it to the root StackPane, and animates it sliding in from the left. If the panel is already displayed, it animates it sliding out to the left and removes it from the root StackPane. This method provides a fun and interactive way for users to discover hidden content within the application.
     * @param mouseEvent The MouseEvent object containing information about the mouse event, including the source of the click event. This parameter is used to determine when the Easter Egg button is clicked and to trigger the appropriate action for showing or hiding the Easter Egg panel.
     */
    @FXML
    public void onEggAction(MouseEvent mouseEvent) {

        StackPane root = (StackPane) topBar.getScene().getRoot();
        Node existingEggPanel = root.lookup("#easterEggPanel");

        if (existingEggPanel == null) {
            try {
                FXMLLoader loader = new FXMLLoader(TopBarController.class.getResource("/Views/EasterEggs.fxml"));
                VBox easterEggView = loader.load();
                easterEggView.setId("easterEggPanel");
                StackPane.setAlignment(easterEggView, Pos.CENTER_LEFT);
                root.getChildren().add(easterEggView);
                easterEggView.setTranslateX(-250.0);

                TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), easterEggView);
                slideIn.setToX(0);
                slideIn.play();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            TranslateTransition slideout = new TranslateTransition(Duration.millis(500), existingEggPanel);
            slideout.setToX(-250.0);
            slideout.play();
            slideout.setOnFinished(_ -> root.getChildren().remove(existingEggPanel));
        }

    }
}
