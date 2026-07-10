package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Configuration;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.PlayerColor;
import seda_project.control_alt_defeat.gamebox.HexChess.Network.ChessMessage;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import javafx.scene.control.Button;

public class ResultScreen extends Controller{
    @FXML
    private Label p1NameLabel, p2NameLabel, p1Score, p2Score, p1Awarded, p2Awarded, resultLabel, statusLabel;


    /**
     * Initializes network listeners for the result screen
     */
    public void initNetwork() {
        Session s = Session.current();
        if (s.network != null) {
            // Reset ready states for the new lobby phase
            s.localReady = false;
            s.peerReady = false;

            // Drop the GameScreen's stale bridge listener so its mid-game disconnect
            // alert doesn't fire when the opponent simply exits the result screen.
            s.network.clearListeners();

            s.network.addListener(new NetworkListener() {
                @Override
                public void onMessage(Message msg) {
                    if (msg instanceof ChessMessage.Ready r) {
                        Platform.runLater(() -> {
                            s.peerReady = r.ready();
                            checkStart();
                        });
                    } else if (msg instanceof ChessMessage.StartCountdown sc) {
                        Platform.runLater(() -> scheduleStartGame(sc.delayMs()));
                    }
                }

                @Override
                public void onDisconnected(String reason) {
                    Platform.runLater(() -> {
                        statusLabel.setVisible(true);
                        statusLabel.setText("Opponent disconnected.");
                    });
                }
            });
        }
    }

    /**
     * Evaluates whether both players are ready to restart.
     */
    private void checkStart() {
        Session s = Session.current();
        if (s.isHost && s.localReady && s.peerReady) {
            long delayMs = 3000;
            s.network.send(new ChessMessage.StartCountdown(delayMs));
            scheduleStartGame(delayMs);
        }
    }

    /**
     * Schedules a visual countdown leading to a fresh game restart.
     * @param delayMs
     */
    private void scheduleStartGame(long delayMs) {
        statusLabel.setVisible(true);
        statusLabel.setText("Restarting in " + (delayMs / 1000) + "s...");
        PauseTransition pt = new PauseTransition(Duration.millis(delayMs));
        pt.setOnFinished(e -> startGameNow());
        pt.play();
    }

    /**
     * Updates the text labels for player names and their base scores.
     * @param p1Name
     * @param p2Name
     * @param p1Points
     * @param p2Points
     */
    public void handData(String p1Name, String p2Name, String p1Points, String p2Points){
        p1NameLabel.setText(p1Name);
        p2NameLabel.setText(p2Name);
        p1Score.setText(""+p1Points);
        p2Score.setText(""+p2Points);
    }

    /**
     * Processes a definitive checkmate victory scenario.
     * @param winner
     */
    public void winner(int winner){
        resultLabel.setText("Checkmate");
        onePoint(winner);
    }

    /**
     * Processes a mutual tie (Remis) scenario, awarding half points evenly.
     */
    public void remis(){
        resultLabel.setText("Remis");
        halfPoint();
    }

    /**
     * Process a stalemate scenario
     * @param winner
     */
    public void stalemate(int winner){
        resultLabel.setText("Stalemate");
        if ((winner == 1)) {
            p1Score.setText(String.valueOf(Double.parseDouble(p1Score.getText()) + .75));
            p2Score.setText(String.valueOf(Double.parseDouble(p2Score.getText()) + .25));
            p1Awarded.setText("+0.75");
            p2Awarded.setText("+0.25");
        } else {
            p2Score.setText(String.valueOf(Double.parseDouble(p2Score.getText()) + .75));
            p1Score.setText(String.valueOf(Double.parseDouble(p1Score.getText()) + .25));
            p1Awarded.setText("+0.25");
            p2Awarded.setText("+0.75");
        }
    }

    /**
     * Method to bring a user back to the Main Menu
     */
    public void onExitAction() {
        sC.play("button");
        sC.stopLooping();
        sC.playLooping("lobby_background",.2);
        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }


    /**
     * Method to start a new round. If in a Network Game just set tise player ready
     */
    public void onPlayAgainAction(ActionEvent event) {
        // prevent double clicking
        ((Button) event.getSource()).setDisable(true);
        sC.play("button");
        Session s = Session.current();

        if (s.network != null) {
            s.localReady = true;
            s.network.send(new ChessMessage.Ready(true));
            statusLabel.setVisible(true);
            statusLabel.setText("Waiting for opponent...");

            checkStart();
        } else {
            startGameNow();
        }
    }

    /**
     * Start a new Game Enginge and move all Player to the GameScreen that is linked to this Engine
     */
    private void startGameNow() {
        Session s = Session.current();

        if (s.network != null) {
            s.network.clearListeners();
        }

        GameEngine engine = new GameEngine();
        s.chessEngine = engine;

        StackPane parent = (StackPane) root.getParent();
        parent.getChildren().remove(root);
        GameScreen controller;
        try {
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource("/Views/HexChess/GameScreen.fxml"));
            Parent root = loader.load();
            vS.addFxmlLoaders("/Views/HexChess/GameScreen.fxml");
            controller = loader.getController();
            Stage stage = (Stage) parent.getScene().getWindow();
            Scene currentScene = stage.getScene();

            Platform.runLater(() -> {
                double height = stage.getHeight();
                double width = stage.getWidth();

                currentScene.setFill(Color.TRANSPARENT);
                currentScene.setRoot(root);

                stage.sizeToScene();

                stage.setWidth(Math.max(width, stage.getWidth()));
                stage.setHeight(Math.max(height, stage.getHeight()));
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        controller.setGameEngine(engine);


        String p1 = p1NameLabel.getText();
        String p2 = p2NameLabel.getText();

        if (p1.equalsIgnoreCase("Bot")) {
            controller.setBotMode(true, PlayerColor.WHITE);
        } else if (p2.equalsIgnoreCase("Bot")) {
            controller.setBotMode(true, PlayerColor.BLACK);
        } else {
            controller.setBotMode(false, null);
        }

        if (p1.equals("Duck")) {
            controller.p1Duck();
        }
        if (p2.equals("Duck")){
            controller.p2Duck();
        }
        controller.init(parent.getScene());
        if (flipped){
            controller.flip();
        }
        if (rainbowed){
            controller.rainbow();
        }
        if (s.network != null) {
            if (s.isHost) {
                controller.attachHostBridge(s.network, engine);
            } else {
                controller.attachClientBridge(s.network, engine);
            }
        }

        controller.setNames(p1, p2);
        controller.setPoints(Double.parseDouble(p1Score.getText()), Double.parseDouble(p2Score.getText()));

        if (s.network == null && p1.equalsIgnoreCase("Bot")) {
            controller.activePlayer(PlayerColor.WHITE);
        }
    }

    /**
     * Process a Game that has ended in a Draw
     */
    public void draw() {
        resultLabel.setText("Draw Proposal");
        halfPoint();
    }

    /**
     * Process a Game that has ended with a resign
     * @param winner
     */
    public void resign(int winner) {
        resultLabel.setText("Resigned");
        onePoint(winner);
    }

    /**
     * Method to award one Point
     * @param winner
     */
    private void onePoint(int winner) {
        if ((winner == 1)) {
            p1Score.setText(String.valueOf(Double.parseDouble(p1Score.getText()) + 1.0));
            p1Awarded.setText("+1.0");
            p2Awarded.setText("+0.0");
        } else {
            p2Score.setText(String.valueOf(Double.parseDouble(p2Score.getText()) + 1.0));
            p1Awarded.setText("+0.0");
            p2Awarded.setText("+1.0");
        }
    }

    /**
     * Method to award half a Point
     */
    private void halfPoint() {
        p1Score.setText(String.valueOf(Double.parseDouble(p1Score.getText()) + .5));
        p2Score.setText(String.valueOf(Double.parseDouble(p2Score.getText()) + .5));
        p1Awarded.setText("+0.5");
        p2Awarded.setText("+0.5");
    }

    public void setGameScreen(GameScreen gameScreen) {
    }
}
