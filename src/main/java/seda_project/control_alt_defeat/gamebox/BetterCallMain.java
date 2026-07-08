package seda_project.control_alt_defeat.gamebox;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seda_project.control_alt_defeat.gamebox.ui.ResizeHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BetterCallMain extends Application {
    private static final Logger logger = LoggerFactory.getLogger(BetterCallMain.class);
    private static final ViewStack vS = ViewStack.getInstance();

    private final SoundController sC = SoundController.getInstance();
    private final List<KeyCode> konamiList = new ArrayList<>();
    private final List<KeyCode> comparisionList = List.of(KeyCode.UP, KeyCode.UP, KeyCode.DOWN, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.B, KeyCode.A);
    /**
     * Exit method to shut down the whole software
     */
    public static void cleanExit() {
        logger.debug("Shutting down");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Start of the Application. This methods sets the scene to the starting Menu and attaches the ResizeListener and also konamiListener to the Stage.
     * In here the music is started
     * @param stage
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Coffee Break Game");
        stage.centerOnScreen();
        stage.show();
        stage.setMinHeight(600);
        stage.setMinWidth(800);
        stage.getIcons().add(new Image(BetterCallMain.class.getResource("/Images/others/Application_Icon.png").toExternalForm()));
        String address = "/Views/StartingScreen.fxml";
        FXMLLoader loader = new FXMLLoader(BetterCallMain.class.getResource(address));
        vS.addFxmlLoaders(address);
        sC.playLooping("lobby_background", .2);
        var mainMenuScene = new Scene(loader.load());
        mainMenuScene.setFill(Color.TRANSPARENT);
        konamiListener(stage);
        stage.setScene(mainMenuScene);
        stage.sizeToScene();
        stage.centerOnScreen();
        ResizeHelper.addResizeListener(stage);
        logger.debug("Startup completed");
    }


    /**
     * Attaches the konamiListener to the Stage.
     * When UP, UP, DOWN, DOWN, LEFT, RIGHT, LEFT, RIGHT, A, B is pressed the music is changed
     * @param stage
     */
    private void konamiListener(Stage stage) {
        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (konamiList.size() == 1 && !konamiList.getFirst().equals(KeyCode.UP)) {
                konamiList.clear();
            } else if (konamiList.size() == 2) {
                KeyCode first = konamiList.getFirst();
                KeyCode second = konamiList.get(1);
                if (first.equals(KeyCode.UP) && second.equals(KeyCode.UP)) {
                    if (event.getCode().equals(KeyCode.UP)) {
                        konamiList.removeFirst();
                    }
                }
            } else if (konamiList.size() >= 3 && event.getCode().equals(KeyCode.UP)) {
                konamiList.clear();
            }
            konamiList.add(event.getCode());
            if (konamiList.equals(comparisionList)) {
                System.out.println("Is the same");
                sC.activateKonami();
            } else if (konamiList.size() >= 10) {
                System.out.println("Clear");
                konamiList.clear();
            } else if (konamiList.stream().anyMatch(keyCode -> !comparisionList.contains(keyCode))) {
                System.out.println("Clear");
                konamiList.clear();
            }
        });
    }
}