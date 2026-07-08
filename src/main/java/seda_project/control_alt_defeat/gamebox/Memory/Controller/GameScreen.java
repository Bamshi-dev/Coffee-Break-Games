package seda_project.control_alt_defeat.gamebox.Memory.Controller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Card;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Decks;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEngineImpl;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEventListener;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSnapshot;
import seda_project.control_alt_defeat.gamebox.network.GameMessage;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.HexHBox;
import seda_project.control_alt_defeat.gamebox.ui.MCard;
import seda_project.control_alt_defeat.gamebox.ui.Toast;

public class GameScreen extends Controller {
    GameEngine engine;
    int matchSize;
    int deckSize;
    ArrayList<MCard> flippedCards = new ArrayList<>();
    boolean canClick = true;
    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
    private final java.util.Set<Integer> remoteFlipIds = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private boolean disconnected = false;
    private String myName;
    private final Map<Integer, MCard> cardsById = new HashMap<>();
    private final Map<MCard, Integer> cardIdOf = new HashMap<>();
    private GameConfig config;
    private GameSetup setup;

    @FXML
    private Label sboardP1, sboardP2, sboardScoreP1, sboardScoreP2, activePlayerLabel;

    @FXML
    private HexHBox activePlayerGrid;

    @FXML
    private StackPane gamePane;

    /**
     * Handles the action of exiting the game and returning to the main menu. It plays a button sound effect, clears the current session, and transitions to the StartingScreen scene. This method is typically invoked when the player chooses to exit the game, ensuring that any ongoing game state is properly cleared and the user is returned to the main menu for further actions.
     */
    @FXML
    private void onExitGameAction() {
        sC.play("button");
        sC.playLooping("lobby_background",.2);
        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }

    /**
     * Passes the necessary data for a single-machine memory game to the GameScreen controller. It sets up the player names, match size, deck size, and initializes the game configuration and setup. The method also highlights the active player and creates the game board based on the provided parameters. This method is typically called when starting a new game session, ensuring that the GameScreen is properly configured with the relevant game data.
     * @param player1 The name of the first player.
     * @param player2 The name of the second player.
     * @param tupleSize The size of the tuples (number of cards in a match).
     * @param deckSize The size of the deck (total number of cards).
     */
    public void passMemoryData(String player1, String player2, int tupleSize, int deckSize) {
        sboardP1.setText(player1);
        sboardP2.setText(player2);

        this.matchSize = tupleSize;
        this.deckSize = deckSize;
        this.myName = player1;

        // Build a fresh GameConfig + GameSetup for a single-machine game.
        this.config = new GameConfig(tupleSize, deckSize, player1, player2);
        this.setup  = Decks.prepare(config);

        highlightActivePlayer(setup.firstPlayer());

        createBoard();
    }

    /**
     * Passes the necessary data for a LAN memory game to the GameScreen controller. It retrieves the current session's configuration and setup, sets up the player names, match size, deck size, and initializes the game configuration and setup. The method also highlights the active player and creates the game board based on the provided parameters. This method is typically called when starting a new LAN game session, ensuring that the GameScreen is properly configured with the relevant game data from the networked session.
     */
    public void passLanData() {
        Session s = Session.current();
        sboardP1.setText(s.config.player1Name());
        sboardP2.setText(s.config.player2Name());

        this.matchSize = s.config.k();
        this.deckSize  = s.config.deckSize();
        this.myName    = s.myName;
        this.config    = s.config;
        this.setup     = s.setup;

        highlightActivePlayer(setup.firstPlayer());

        createBoard();
    }

    /**
     * Highlights the active player in the user interface by updating the visual representation of the active player's grid. It checks the provided player's name against the names of both players and applies appropriate CSS classes to indicate which player is currently active. This method is typically called when the turn changes or when the game starts, ensuring that players can easily identify whose turn it is during gameplay.
     */
    private void createBoard() {
        GridPane playingGrid = new GridPane();
        playingGrid.setHgap(5);
        playingGrid.setVgap(5);
        playingGrid.setPadding(new Insets(5));

        List<Card> deck = setup.initialDeck();
        int n = deck.size();

        int row = 5;
        int col = (int) Math.ceil((double) n / row);

        if (row > col){
            int tmp = col;
            col = row;
            row = tmp;
        }

        double height = 361;

        double size = height /row;

        playingGrid.setPrefSize(size*col, size*row);
        playingGrid.setMaxSize(size*col, size*row);
        playingGrid.setMinSize(size*col, size*row);

        for (int i = 0; i < col; i++) {
            playingGrid.getColumnConstraints().add(new ColumnConstraints(size));
        }
        for (int i = 0; i < row; i++) {
            playingGrid.getRowConstraints().add(new RowConstraints(size));
        }

        int placed = 0;
        int overhang = n % row;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int helper = 0;
                if (i == row - 1 && overhang != 0) {
                    helper = (row - overhang) / 2;
                }
                if (placed < n) {
                    Card c = deck.get(placed);
                    int cardId = c.id();
                    int symbolId = c.symbolId();
                    MCard cell = new MCard(i, j + helper, symbolId);
                    cell.setHeightProporties(size,size);
                    cell.setPrefSize(size, size);
                    cell.setMinSize(size, size);
                    cell.setMaxSize(size, size);

                    // Track the MCard in both directions so listeners can find it.
                    cardsById.put(cardId, cell);
                    cardIdOf.put(cell, cardId);

                    cell.setOnAction(_ -> {
                        if (!canClick) return;
                        // In LAN mode, only the active player can flip.
                        if (Session.current().network != null
                                && !engine.getActivePlayer().equals(myName)) {
                            return;
                        }
                        cell.setDisable(true);
                        flipmotion(cell, cardId);
                    });
                    playingGrid.add(cell, j + helper, i);
                    placed++;
                }
            }
        }

        gamePane.getChildren().add(playingGrid);
    }

    /**
     * Sets the status label to indicate whether a match was found or not. It plays a corresponding sound effect and displays a toast message on the game pane to inform the players of the match result. This method is typically called after a player flips cards and the game engine determines whether the flipped cards form a match, providing immediate feedback to the players about their actions.
     * @param match A boolean indicating whether a match was found (true) or not (false). If true, the method plays a match sound and displays a "Match" message; if false, it plays a mismatch sound and displays a "Mismatch" message.
     */
    public void setStatusLabel(boolean match) {
        if (match){
            sC.play("memory_match");
            Toast.makeText(gamePane,"Match");
        }
        else {
            sC.play("memory_missmatch");
            Toast.makeText(gamePane,"Missmatch");
        }
    }

    /**
     * Sets the active player label in the user interface to display the name of the currently active player. This method updates the text of the activePlayerLabel to reflect the provided player's name, allowing players to easily identify whose turn it is during gameplay. It is typically called when the turn changes or when the game starts, ensuring that the user interface accurately represents the current game state.
     * @param name The name of the currently active player. This string is used to update
     */
    public void setActivePlayerLabel(String name) {
        activePlayerLabel.setText(name);
    }

    /**
     * Starts the memory game by initializing the game engine, setting up event listeners for game events, and handling network messages for LAN multiplayer. It configures the game engine with the prepared setup and begins processing player actions, updating the user interface based on game events such as matches, mismatches, and turn changes. This method is typically called when the game is ready to begin, ensuring that all necessary components are initialized and ready for gameplay.
     */
    public void startGame() {
        // Build the engine and start it with our prepared setup.
        engine = new GameEngineImpl();
        engine.start(config, setup);

        // Drive the UI from engine events.
        engine.addListener(new GameEventListener() {
            @Override
            public void onMatch(List<Integer> matchedIds, String scoringPlayer,
                                int scoreAwarded, GameSnapshot snapshot) {
                Platform.runLater(() -> {
                    int activeIdx = scoringPlayer.equals(config.player1Name()) ? 1 : 0;
                    awardPoints(activeIdx);
                    setStatusLabel(true);
                    removeMatch();
                    if (snapshot.gameOver()) gameEnd();
                });
            }

            @Override
            public void onMismatch(List<Integer> flippedIds, GameSnapshot snapshot) {
                canClick = false;
                Platform.runLater(() -> {
                    turnCardsBack();
                    setStatusLabel(false);
                });
            }

            @Override
            public void onTurnChanged(String newActivePlayer, GameSnapshot snapshot) {
                Platform.runLater(() -> {
                    setActivePlayerLabel(newActivePlayer);
                    highlightActivePlayer(newActivePlayer);
                });
            }
        });

        NetworkLayer net = Session.current().network;
        if (net != null) {
            net.addListener(new NetworkListener() {
                @Override
                public void onMessage(Message msg) {
                    if (msg instanceof GameMessage.Flip f) {
                        Platform.runLater(() -> applyRemoteFlip(f.cardId()));
                    }
                }
                @Override
                public void onDisconnected(String reason) {
                    Platform.runLater(() -> handleDisconnect(reason));
                }
            });
        }

        setActivePlayerLabel(engine.getActivePlayer());
    }

    /**
     * Highlights the active player in the user interface by updating the visual representation of the active player's grid. It checks the provided player's name against the names of both players and applies appropriate CSS classes to indicate which player is currently active. This method is typically called when the turn changes or when the game starts, ensuring that players can easily identify whose turn it is during gameplay.
     * @param newActivePlayer The name of the currently active player. This string is used to determine which player's grid should be highlighted in the user interface, allowing players to easily identify whose turn it is during gameplay.
     */
    private void highlightActivePlayer(String newActivePlayer) {
        if (newActivePlayer.equals(sboardP1.getText())){
            activePlayerGrid.getStyleClass().add("ready");
            activePlayerGrid.getStyleClass().remove("error");
        }
        else {
            activePlayerGrid.getStyleClass().add("error");
            activePlayerGrid.getStyleClass().remove("ready");
        }
    }

    /**
     * Turns the flipped cards back to their face-down state after a mismatch. It uses a pause transition to delay the action, allowing players to see the flipped cards before they are turned back. The method clears the list of flipped cards and re-enables clicking on the cards after the transition is complete. This method is typically called when a mismatch occurs, providing visual feedback to players and maintaining the game flow.
     */
    public void turnCardsBack() {
        pause.setOnFinished(_ -> {
            for (MCard c : flippedCards) {
                flipmotion(c, cardIdOf.get(c));
            }
            flippedCards.clear();
            canClick = true;
        });
        pause.play();
    }

    /**
     * Removes the matched cards from the game board after a successful match. It uses a pause transition to delay the action, allowing players to see the matched cards before they are removed. The method sets the matched cards to be invisible, clears the list of flipped cards, and re-enables clicking on the remaining cards after the transition is complete. This method is typically called when a match occurs, providing visual feedback to players and maintaining the game flow.
     */
    public void removeMatch() {
        canClick = false;
        pause.setOnFinished(_ -> {
            for (MCard c : flippedCards) {
                c.setVisible(false);
            }
            flippedCards.clear();
            canClick = true;
        });
        pause.play();
    }

    /**
     * Awards points to the active player based on the number of active players. If the number of active players is even, points are awarded to player 2; if odd, points are awarded to player 1. The method updates the score labels for the respective players in the user interface, reflecting the new scores after a successful match. This method is typically called when a match occurs, ensuring that players receive immediate feedback on their performance and progress in the game.
     * @param active An integer representing the number of active players. This parameter is used to determine which player should receive points based on the parity of the number of active players, allowing for dynamic scoring in multiplayer settings.
     */
    public void awardPoints(int active) {
        if (active % 2 == 0) {
            int current = Integer.parseInt(sboardScoreP2.getText());
            sboardScoreP2.setText((current + 10) + "");
        } else {
            int current = Integer.parseInt(sboardScoreP1.getText());
            sboardScoreP1.setText((current + 10) + "");
        }
    }

    /**
     * Ends the game and determines the winner based on the scores of both players. It compares the scores of player 1 and player 2, identifies the winner (or a tie), and transitions to the ResultScreen scene to display the match results. The method passes relevant match data, including player names, scores, match size, deck size, and the winner's identifier, to the ResultScreen controller for display. This method is typically called when the game concludes, ensuring that players are informed of the final outcome and results of their gameplay.
     */
    public void gameEnd() {
        int points1 = Integer.parseInt(sboardScoreP1.getText());
        int points2 = Integer.parseInt(sboardScoreP2.getText());
        int winner;
        if (points1 == points2) {
            winner = 0;
        } else if (points1 > points2) {
            winner = 1;
        } else {
            winner = 2;
        }
        ResultScreen controller = (ResultScreen) c.changeScene("/Views/Memory/ResultScreen.fxml",header,vS);
        controller.passMatchData(sboardP1.getText(), sboardP2.getText(),
                    sboardScoreP1.getText(), sboardScoreP2.getText(),
                    matchSize, deckSize, winner);
        if(flipped){
            controller.flip();
        }
        if(rainbowed){
            controller.rainbow();
        }
    }

    /**
     * Flips a card with a smooth animation, handling both the visual transition and the game logic associated with flipping a card. If the card is being flipped face-up, it adds the card to the list of flipped cards and checks if the number of flipped cards has reached the match size, disabling further clicks if necessary. It also communicates the flip action to the game engine and, in LAN mode, sends a network message to inform the opponent of the flip. If the card is being flipped face-down, it simply performs the visual transition without affecting game logic. This method ensures that card flipping is visually appealing and functionally integrated with the game's rules and multiplayer mechanics.
     * @param card The MCard instance representing the card to be flipped. This parameter is used to identify which card is being flipped and to perform the visual transition associated with the flip action.
     * @param cardId An integer representing the unique identifier of the card being flipped. This parameter is used to track the card within the game engine and to communicate the flip action in LAN multiplayer settings, ensuring that the correct card is processed during the flip operation.
     */
    private void flipmotion(MCard card, int cardId) {
        ScaleTransition firstHalf = new ScaleTransition(Duration.millis(300), card);
        firstHalf.setFromX(1);
        firstHalf.setToX(0);

        ScaleTransition secondHalf = new ScaleTransition(Duration.millis(300), card);
        secondHalf.setFromX(0);
        secondHalf.setToX(1);

        if (!card.getFaceUp()) {
            flippedCards.add(card);
            if (flippedCards.size() >= matchSize) {
                canClick = false;
            }
            firstHalf.setOnFinished(_ -> {
                card.setFaceUp(true);
                boolean wasRemote = remoteFlipIds.remove(cardId);
                engine.flip(cardId);
                NetworkLayer net = Session.current().network;
                if (net != null && !wasRemote) {
                    net.send(new GameMessage.Flip(cardId));
                }
            });
        } else {
            firstHalf.setOnFinished(_ -> card.faceDown());
        }

        SequentialTransition flip = new SequentialTransition(firstHalf, secondHalf);
        flip.play();
    }

    /**
     * Applies a remote flip action to a card based on its unique identifier. This method is typically called when a flip action is received from the opponent in a LAN multiplayer setting. It retrieves the corresponding MCard instance using the provided cardId, checks if the card is valid and face-down, and then adds the cardId to the set of remote flips to prevent duplicate processing. Finally, it invokes the flipmotion method to perform the visual flip animation for the card. This ensures that remote flip actions are properly handled and visually represented in the game interface.
     * @param cardId An integer representing the unique identifier of the card to be flipped remotely. This parameter is used to locate the corresponding MCard instance and to track the remote flip action, ensuring that the correct card is processed and visually flipped in response to the opponent's action in a LAN multiplayer setting.
     */
    private void applyRemoteFlip(int cardId) {
        MCard card = cardsById.get(cardId);
        if (card == null || card.getFaceUp() || !canClick) return;
        remoteFlipIds.add(cardId);
        flipmotion(card, cardId);
    }

    /**
     * Handles the disconnection of a player from the LAN multiplayer game. It checks if the disconnection has already been processed to avoid duplicate handling, sets the disconnected flag to true, and stops any ongoing pause transitions. The method then displays an alert to inform the player that the connection to the opponent has been lost, providing the reason for the disconnection. After acknowledging the alert, it clears the current session, empties the view stack, and transitions back to the StartingScreen scene. This method ensures that players are properly informed of disconnections and that the game state is reset appropriately.
     * @param reason A string providing the reason for the disconnection. This parameter is used to inform the player of the cause of the disconnection, allowing them to understand why the game session has been interrupted and what actions they may need to take next.
     */
    private void handleDisconnect(String reason) {
        if (disconnected) return;
        disconnected = true;

        canClick = false;
        pause.stop();

        Alert alert = new Alert(Alert.AlertType.WARNING,
                "Connection to opponent lost: " + reason + "\n\nReturning to the main menu.",
                ButtonType.OK);
        alert.setTitle("Disconnected");
        alert.setHeaderText("Opponent disconnected");
        alert.showAndWait();

        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }
}
