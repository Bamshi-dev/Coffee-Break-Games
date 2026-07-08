package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Decks;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.network.GameMessage;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class ResultScreen extends Controller {

    String player1Name,player2Name, pointsPlayer1, pointsPlayer2;
    int tupleSize,deckSize, winner;
    private boolean disconnected = false;
    @FXML
    private Label matchSizeLabel, deckSizeLabel,looserLabel,winnerLabel, positionWinnerLabel, positionLooserLabel, winnerPointsLabel,looserPointsLabel;

    @FXML private Button playAgainButton;

    /**
     * Handles the action of exiting the game from the ResultScreen. It plays a button sound effect, clears the current
     * session, and transitions back to the StartingScreen scene, allowing the user to exit the game and return to the main menu.
     */
    @FXML
    private void onExitGameAction(){
        sC.play("button");
        sC.playLooping("lobby_background",.2);
        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }

    /**
     * Handles the action of playing again from the ResultScreen. It plays a button sound effect and checks if the
     * current session is a LAN game or a local game. If it's a local game, it starts a new local game with the same
     * player names and settings. If it's a LAN game, it checks if the current player is the host and, if so, sends a
     * NewGame message to the opponent and starts a new game with the specified configuration and setup.
     */
    @FXML
    private void onPlayAgainAction() {
        sC.play("button");
        NetworkLayer net = Session.current().network;
        if (net == null) {
            // Local play: same as before — fresh game with same names + K + deck.
            startNewLocalGame();
            return;
        }

        // LAN: only the host can start a new game.
        if (!Session.current().isHost) return;     // safeguard; button is also disabled

        GameConfig cfg = new GameConfig(tupleSize, deckSize, player1Name, player2Name);
        GameSetup setup = Decks.prepare(cfg);

        Session.current().config = cfg;
        Session.current().setup  = setup;

        net.send(new GameMessage.NewGame(cfg, setup));
        startNewGameWithSetup(cfg, setup);
    }

    /**
     * Starts a new local game with the same player names and settings as the previous game. It transitions to the
     * GameScreen scene, passing along the player data and starting the game. If the flipped or rainbowed settings were
     * enabled in the previous game, it applies those settings to the new game as well.
     */
    private void startNewLocalGame() {
        GameScreen controller = (GameScreen) c.changeScene("/Views/Memory/GameScreen.fxml",header,vS);
        controller.passMemoryData(player1Name, player2Name, tupleSize, deckSize);
        controller.startGame();
        if (flipped){
            controller.flip();
        }
        if (rainbowed){
            controller.rainbow();
        }
    }

    /**
     * Starts a new game with the specified configuration and setup. It updates the current session with the new game
     * configuration and setup, transitions to the GameScreen scene, and starts the game. If the flipped or rainbowed
     * settings were enabled in the previous game, it applies those settings to the new game as well.
     * @param cfg The GameConfig object containing the configuration for the new game, including match size, deck size,
     *            and player names.
     * @param setup The GameSetup object containing the prepared setup for the new game, including the deck of cards
     *              and any other necessary game data.
     */
    private void startNewGameWithSetup(GameConfig cfg, GameSetup setup) {
        Session.current().config = cfg;
        Session.current().setup  = setup;
        GameScreen controller = (GameScreen) c.changeScene("/Views/Memory/GameScreen.fxml",header,vS);
        controller.passLanData();
        controller.startGame();
        if (flipped){
            controller.flip();
        }
        if (rainbowed){
            controller.rainbow();
        }

    }

    /**
     * Handles the disconnection of the opponent during a LAN game. It checks if the disconnection has already been
     * handled to avoid duplicate alerts. If not, it sets the disconnected flag to true, displays an alert to inform
     * the user about the lost connection, clears the current session, and transitions back to the StartingScreen scene.
     * This method is typically called when the network layer detects that the opponent has disconnected from the game.
     * @param reason The reason for the disconnection, which is displayed in the alert message to inform the user about
     *               the cause of the lost connection.
     */
    private void handleDisconnect(String reason) {
        if (disconnected) return;
        disconnected = true;
        Alert alert = new Alert(Alert.AlertType.WARNING,
                "Connection to opponent lost: " + reason,
                ButtonType.OK);
        alert.setTitle("Disconnected");
        alert.setHeaderText("Opponent disconnected");
        alert.showAndWait();
        Session.clear();
        onExitGameAction();
    }

    /**
     * Passes the match data from the previous game to the ResultScreen controller, including player names, points,
     * match size, deck size, and the winner. It updates the user interface elements to display the match results and
     * sets up a network listener for LAN games to handle new game requests or disconnections. This method is typically
     * called when transitioning to the ResultScreen after a game has ended.
     * @param player1Name The name of the first player, which is displayed in the ResultScreen to indicate the winner
     *                    or loser of the match.
     * @param player2Name The name of the second player, which is displayed in the ResultScreen to indicate the winner
     *                    or loser of the match.
     * @param pointsPlayer1 The points scored by the first player in the match, which is displayed in the ResultScreen
     *                      to show the player's performance.
     * @param pointsPlayer2 The points scored by the second player in the match, which is displayed in the ResultScreen
     *                      to show the player's performance.
     * @param tupleSize The match size (number of pairs) used in the game, which is displayed in the ResultScreen to
     *                  indicate the difficulty level of the match.
     * @param deckSize The deck size (number of cards) used in the game, which is displayed in the ResultScreen to
     *                 indicate the complexity of the match.
     * @param winner The winner of the match, represented as an integer (0 for draw, 1 for player 1, 2 for player 2),
     *               which is used to determine the display of the winner and loser labels in the ResultScreen.
     */
    public void passMatchData(String player1Name, String player2Name, String pointsPlayer1, String pointsPlayer2, int tupleSize, int deckSize, int winner){
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.tupleSize = tupleSize;
        this.deckSize = deckSize;
        this.winner = winner;
        this.pointsPlayer1 = pointsPlayer1;
        this.pointsPlayer2 = pointsPlayer2;

        deckSizeLabel.setText(String.valueOf(deckSize));
        matchSizeLabel.setText(String.valueOf(tupleSize));

        switch (winner){
            case 0:
                positionLooserLabel.setText("Draw");
                positionWinnerLabel.setText("Draw");
                break;
            case 1:
                winnerLabel.setText(player1Name);
                winnerPointsLabel.setText(pointsPlayer1);
                looserLabel.setText(player2Name);
                looserPointsLabel.setText(pointsPlayer2);
                break;
            case 2:
                winnerLabel.setText(player2Name);
                looserPointsLabel.setText(pointsPlayer2);
                looserLabel.setText(player1Name);
                winnerPointsLabel.setText(pointsPlayer1);
                break;
        }

        // LAN mode: register listener for NewGame / Disconnect
        NetworkLayer net = Session.current().network;
        if (net != null) {
            if (!Session.current().isHost && playAgainButton != null) {
                playAgainButton.setText("Waiting for host...");
                playAgainButton.setDisable(true);
            }
            net.addListener(new NetworkListener() {
                @Override
                public void onMessage(Message msg) {
                    if (msg instanceof GameMessage.NewGame ng) {
                        Platform.runLater(() -> startNewGameWithSetup(ng.config(), ng.setup()));
                    }
                }
                @Override
                public void onDisconnected(String reason) {
                    Platform.runLater(() -> handleDisconnect(reason));
                }
            });
        }

    }
}
