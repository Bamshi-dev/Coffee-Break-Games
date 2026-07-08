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

    @FXML
    protected void closeWindow() {
        Stage s = (Stage) topBar.getScene().getWindow();
        s.close();
    }

    @FXML
    protected void changeMute() {
        boolean next = sC.getMute();
        sC.setMute(!next);
        setMuteImageView(!next);
        changeSound(next);
    }

    public void initMute() {
        boolean current = sC.getMute();
        setMuteImageView(current);
    }

    public void setMuteImageView(boolean toSet) {
        if (toSet) {
            muteButton.setImage(mute);
        } else {
            muteButton.setImage(unmute);
        }
    }

    public void changeSound(boolean toSet) {
        if (toSet) {
            sC.resume();
        } else {
            sC.pause();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (muteButton != null) {
            initMute();
        }
    }

    @FXML
    public void onDragMovement(MouseEvent mouseEvent) {

        Stage stage = (Stage) topBar.getScene().getWindow();

        stage.setX(mouseEvent.getScreenX() - offsetX);
        stage.setY(mouseEvent.getScreenY() - offsetY);
    }

    @FXML
    public void onDragStart(MouseEvent mouseEvent) {
        offsetX = mouseEvent.getX();
        offsetY = mouseEvent.getY();
    }

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
