package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.network.GameMessage;
import seda_project.control_alt_defeat.gamebox.network.Lan;
import seda_project.control_alt_defeat.gamebox.network.LanHost;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class WaitForOpponent extends Controller {
    private String hostName;
    private String joinName;
    private int tupleSize;
    private int deckSize;
    private Timeline timeline;
    private String ipAddress;
    private LanHost.Pending pendingHost;
    private boolean countdownStarted;
    private boolean leftLobby;
    // Pristine config/setup as built by HostLan, BEFORE any joiner's name was
    // patched in. Restored when a joiner leaves the lobby so the next joiner
    // doesn't inherit the previous one's name (e.g. as firstPlayer).
    private GameConfig originalConfig;
    private GameSetup originalSetup;


    @FXML
    private Button startGameButton;

    @FXML
    private Label yourNameLabel, opponentNameLabel, deckSizeLabel, matchSizeLabel, statusLabel, hostIpAddressLabel;

    /**
     * Handles the action of going back from the WaitForOpponent scene. It plays a button sound effect, clears the
     * current session, and transitions back to the previous scene. If the previous scene is HostLan or JoinLan, it
     * passes the relevant data (host name, tuple size, deck size, join name, and IP address) back to that controller
     * to restore the state of the previous scene.
     */
    @FXML
    protected void onBackAction() {
        sC.play("button");
        // Session.clear() below fires our own onDisconnected ("local close");
        // this flag stops it from restarting hosting on a screen we just left.
        leftLobby = true;
        cancelPendingHost(); // otherwise the port stays bound and the next host fails
        Session.clear();
        Object controller = c.backScene(header,vS);
        if (controller instanceof HostLan con) {
            con.backTransfer(hostName, tupleSize, deckSize);
        }
        if (controller instanceof JoinLan con) {
            con.backTransfer(joinName, ipAddress);
        }
    }

    /**
     * Handles the action of starting the game from the WaitForOpponent scene. It plays a button sound effect, toggles
     * the local ready state in the current session, sends a Ready message to the opponent, updates the start game button
     * text, and updates the local status label to reflect the current ready state. If both players are ready, it
     * initiates a countdown to start the game.
     */
    @FXML
    protected void onStartGameAction() {
        sC.play("button");
        NetworkLayer layer = Session.current().network;
        if (layer == null) return;

        Session.current().localReady = !Session.current().localReady;
        boolean myReady = Session.current().localReady;

        layer.send(new GameMessage.Ready(myReady));
        startGameButton.setText(myReady ? "Not Ready" : "Ready");

        updateLocalStatus(myReady);
        maybeStartCountdown();
    }

    /**
     * Updates the local status label in the WaitForOpponent scene based on the current ready state of the local player
     * and the opponent. It checks if both players are ready, and if so, it updates the status label to indicate that
     * all players are ready. If only the local player is ready, it displays a message indicating that it is waiting
     * for the opponent to be ready. If neither player is ready, it displays a message indicating that the local player is not ready.
     */
    private void updateLocalStatus(boolean myReady) {
        if (myReady && Session.current().peerReady) {
            statusLabel.getStyleClass().clear();
            statusLabel.getStyleClass().add("ready");
            statusLabel.getStyleClass().add("box");
            statusLabel.setText("All ready!");
        } else if (myReady) {
            statusLabel.setText("Waiting for opponent to be ready...");
        } else {
            statusLabel.setText("You are not ready.");
        }
    }

    /**
     * Checks if both players are ready to start the game and initiates a countdown to start the game if both players are
     * ready. It verifies that the current player is the host and that both the local player and the opponent are marked
     * as ready in the current session. If these conditions are met, it sends a StartCountdown message to the opponent
     * and schedules the game to start after a specified delay.
     */
    private void maybeStartCountdown() {
        Session s = Session.current();
        if (!s.isHost) return;                          // only the host triggers
        if (!(s.localReady && s.peerReady)) return;     // need both sides ready

        long delayMs = 3000;
        s.network.send(new GameMessage.StartCountdown(delayMs));
        scheduleStartGame(delayMs);
    }

    /**
     * Schedules the start of the game after a specified delay. It updates the status label to indicate the countdown
     * time, disables the start game button, and sets up a PauseTransition to trigger the start of the game after the
     * specified delay. When the transition finishes, it calls the startGameNow method to initiate the game.
     * @param delayMs The delay in milliseconds before the game starts. This parameter is used to determine the
     *                countdown time displayed in the status label and the duration of the PauseTransition before starting the game.
     */
    private void scheduleStartGame(long delayMs) {
        countdownStarted = true;
        statusLabel.setText("Starting in " + (delayMs / 1000) + "s...");
        startGameButton.setDisable(true);
        PauseTransition pt = new PauseTransition(Duration.millis(delayMs));
        pt.setOnFinished(_ -> startGameNow());
        pt.play();
    }

    /**
     * Starts the game immediately by transitioning to the GameScreen scene and passing the necessary data for the LAN
     * game. It plays a new background music, retrieves the current session's configuration and setup, and
     * initializes the GameScreen controller with the LAN data. If the flipped or rainbowed settings were enabled in the
     * previous game, it applies those settings to the new game as well.
     */
    private void startGameNow() {
        sC.playLooping("memory_background",.3);
        GameScreen controller = (GameScreen) c.changeScene("/Views/Memory/GameScreen.fxml",header,vS);
        controller.passLanData();
        controller.startGame();
        if (c.checkFlip(Session.current().config.player1Name(), Session.current().config.player2Name())){
            controller.flip();
        }
        if (c.checkRainbow(Session.current().config.player1Name(), Session.current().config.player2Name())){
            controller.rainbow();
        }
    }

    /**
     * Passes the host data to the WaitForOpponent controller, including the host name, match size, and deck size. It
     * updates the user interface elements to display the host's name, match size, and deck size, and sets up a
     * loading-dots animation while waiting for an opponent to join. It also opens a server socket to listen for incoming
     * connections from clients and attaches a network listener to handle messages from the connected client.
     * This method is typically called when transitioning to the WaitForOpponent scene as the host of a LAN game.
     * @param hostName The name of the host player, which is displayed in the WaitForOpponent scene to indicate the host's identity.
     * @param tupleSize The match size (number of pairs) for the game, which is displayed in the WaitForOpponent scene to
     *                  indicate the difficulty level of the game.
     * @param deckSize The deck size (number of cards) for the game, which is displayed in the WaitForOpponent scene to
     *                 indicate the complexity of the game.
     */
    public void passHostData(String hostName, int tupleSize, int deckSize) {
        this.hostName = hostName;
        this.tupleSize = tupleSize;
        this.deckSize = deckSize;

        yourNameLabel.setText(hostName);
        deckSizeLabel.setText(Integer.toString(deckSize));
        matchSizeLabel.setText(Integer.toString(tupleSize));
        startGameButton.setText("Ready");

        // Loading-dots animation while waiting for someone to join
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),   _ -> opponentNameLabel.setText("")),
                new KeyFrame(Duration.seconds(0.5), _ -> opponentNameLabel.setText("o")),
                new KeyFrame(Duration.seconds(1),   _ -> opponentNameLabel.setText("oo")),
                new KeyFrame(Duration.seconds(1.5), _ -> opponentNameLabel.setText("ooo")),
                new KeyFrame(Duration.seconds(2),   _ -> opponentNameLabel.setText("ooo"))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        statusLabel.setText("Waiting for an opponent to Join!");

        // Keep the pristine lobby data so a later joiner starts from a clean
        // slate if the first one leaves (handleHostMessage patches these).
        originalConfig = Session.current().config;
        originalSetup  = Session.current().setup;

        startHosting();
        hostIpAddressLabel.setText(Lan.localIp());
    }

    /** Start (or restart) the TCP accept for one opponent. */
    private void startHosting() {
        // Open the server socket. When the client connects, hop back to JavaFX
        // and attach the network listener so we can react to messages.
        pendingHost = LanHost.hostAsync(Lan.DEFAULT_PORT,
                layer -> Platform.runLater(() -> {
                    if (leftLobby) { // accept raced the Back button: don't adopt the connection
                        try { layer.close(); } catch (Exception ignored) {}
                        return;
                    }
                    pendingHost = null; // accepted: nothing left to cancel
                    Session.current().network = layer;
                    attachHostListener(layer);
                }),
                err -> Platform.runLater(() -> statusLabel.setText("Hosting failed: " + err.getMessage()))
        );
    }

    /**
     * The joined opponent left before the game started: reset the lobby state
     * (including the config/setup that were patched with their name) and start
     * accepting again so a new opponent can join.
     */
    private void onOpponentLeftLobby(String reason) {
        // leftLobby: WE closed the connection by leaving — never restart hosting
        // from a dead screen. countdownStarted: too late to rewind the lobby.
        if (leftLobby || countdownStarted) {
            statusLabel.setText("Opponent disconnected: " + reason);
            return;
        }
        Session s = Session.current();
        if (s.network != null) {
            try { s.network.close(); } catch (Exception ignored) {}
            s.network = null;
        }
        s.localReady = false;
        s.peerReady = false;
        s.config = originalConfig;
        s.setup  = originalSetup;

        startGameButton.setText("Ready");
        startGameButton.setDisable(false);
        statusLabel.getStyleClass().removeAll("ready");
        statusLabel.setText("Opponent left. Waiting for an opponent to Join!");
        opponentNameLabel.setText("");
        if (timeline != null) timeline.play();

        startHosting();
    }

    /**
     * Passes the join data to the WaitForOpponent controller, including the player's name and the IP address of the
     * host. It updates the user interface elements to display the player's name, sets the start game button text to
     * "Ready," and displays a status message indicating that it is waiting for game information from the host. It also
     * sets up a network listener to handle messages from the host, such as receiving the lobby configuration and
     * readiness status. This method is typically called when transitioning to the WaitForOpponent scene as a client
     * joining a LAN game.
     * @param playerName The name of the player joining the LAN game, which is displayed in the WaitForOpponent scene
     *                   to indicate the player's identity.
     * @param ipAddress The IP address of the host to which the player is connecting, which is used to establish a
     *                  network connection and receive game information from the host.
     */
    public void passJoinData(String playerName, String ipAddress) {
        this.joinName = playerName;
        this.ipAddress = ipAddress;

        yourNameLabel.setText(playerName);
        startGameButton.setText("Ready");
        statusLabel.setText("Waiting for game info from host...");

        NetworkLayer layer = Session.current().network;
        if (layer == null) {
            statusLabel.setText("No connection to host.");
            return;
        }

        // Listen for the host to send us the lobby config
        layer.addListener(new NetworkListener() {
            @Override
            public void onMessage(Message msg) {
                Platform.runLater(() -> handleJoinMessage(msg));
            }
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> statusLabel.setText("Disconnected: " + reason));
            }
        });

        // Greet the host
        layer.send(new GameMessage.Hello(playerName));
    }

    /**
     * Attaches a network listener to the host's network layer to handle incoming messages from the connected client.
     * It listens for messages such as Hello and Ready, and updates the session and user interface accordingly. If the
     * client disconnects, it updates the status label to indicate that the opponent has disconnected. This method is
     * called when the host successfully establishes a connection with a client in a LAN game.
     * @param layer The NetworkLayer instance representing the established connection between the host and the client.
     *              This object is used for communication between the host and the client in the LAN game, allowing the
     *              host to receive messages from the client and respond accordingly.
     */
    private void attachHostListener(NetworkLayer layer) {
        layer.addListener(new NetworkListener() {
            @Override
            public void onMessage(Message msg) {
                Platform.runLater(() -> handleHostMessage(layer, msg));
            }
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> onOpponentLeftLobby(reason));
            }
        });
    }

    /**
     * Handles incoming messages from the connected client when the current player is the host. It processes messages
     * such as Hello and Ready, updating the session and user interface accordingly. When a Hello message is received,
     * it updates the game configuration and setup to reflect the client's name and sends the updated lobby configuration
     * back to the client. When a Ready message is received, it updates the peer's ready status and checks if both
     * players are ready to start the game. If both players are ready, it initiates a countdown to start the game.
     * @param layer The NetworkLayer instance representing the established connection between the host and the client.
     *              This object is used for communication between the host and the client in the LAN game, allowing the
     *              host to receive messages from the client and respond accordingly.
     * @param msg The incoming message from the client, which can be of type Hello or Ready. The method processes the
     *            message based on its type and updates the session and user interface accordingly.
     */
    private void handleHostMessage(NetworkLayer layer, Message msg) {
        if (msg instanceof GameMessage.Hello h) {
            // Now we know the joiner's name. Patch config + setup so they
            // reflect reality (instead of the "Opponent" placeholder).
            GameConfig oldCfg = Session.current().config;
            GameConfig newCfg = new GameConfig(
                    oldCfg.k(), oldCfg.deckSize(),
                    oldCfg.player1Name(), h.playerName());

            GameSetup oldSetup = Session.current().setup;
            String first = oldSetup.firstPlayer().equals("Opponent")
                    ? h.playerName()
                    : oldSetup.firstPlayer();
            GameSetup newSetup = new GameSetup(oldSetup.initialDeck(), first);

            Session.current().config = newCfg;
            Session.current().setup  = newSetup;

            // Tell the joiner everything they need to start the same engine
            layer.send(new GameMessage.LobbyConfig(newCfg, newSetup));

            // Update host UI
            playerJoin(h.playerName());
        } else if (msg instanceof GameMessage.Ready r) {
            Session.current().peerReady = r.ready();
            updatePeerStatus();
            maybeStartCountdown();
        }
    }

    /**
     * Handles incoming messages from the connected host when the current player is a client joining a LAN game. It
     * processes messages such as LobbyConfig, Ready, and StartCountdown, updating the session and user interface
     * accordingly. When a LobbyConfig message is received, it updates the session's configuration and setup to reflect
     * the host's game settings and updates the user interface to display the host's name, match size, and deck size.
     * When a Ready message is received, it updates the peer's ready status and checks if both players are ready to start
     * the game. If both players are ready, it initiates a countdown to start the game.
     * @param msg The incoming message from the host, which can be of type LobbyConfig, Ready, or StartCountdown. The
     *            method processes the message based on its type and updates the session and user interface accordingly.
     */
    private void handleJoinMessage(Message msg) {
        if (msg instanceof GameMessage.LobbyConfig lc) {
            Session.current().config = lc.config();
            Session.current().setup  = lc.setup();

            opponentNameLabel.setText(lc.config().player1Name());
            deckSizeLabel.setText(Integer.toString(lc.config().deckSize()));
            matchSizeLabel.setText(Integer.toString(lc.config().k()));
            statusLabel.setText("Press Ready to start!");
        } else if (msg instanceof GameMessage.Ready r) {
            Session.current().peerReady = r.ready();
            updatePeerStatus();
        } else if (msg instanceof GameMessage.StartCountdown sc) {
            scheduleStartGame(sc.delayMs());
        }
    }

    /**
     * Updates the user interface to reflect that the opponent has joined the game. It stops the loading-dots animation,
     * sets the opponent's name in the opponentNameLabel, and updates the status label to indicate that it is waiting
     * for the opponent to be ready. This method is called when a Hello message is received from the client, indicating
     * that the client has successfully connected to the host.
     * @param joinN The name of the opponent who has joined the game, which is displayed in the opponentNameLabel to
     *              indicate the opponent's identity in the WaitForOpponent scene.
     */
    public void playerJoin(String joinN) {
        timeline.stop();
        joinName = joinN;
        opponentNameLabel.setText(joinName);
        statusLabel.setText("Waiting for " + joinName + " to be ready!");
    }

    /**
     * Updates the user interface to reflect the current readiness status of the opponent. It checks if both the local
     * player and the opponent are marked as ready in the current session. If both players are ready, it updates the
     * status label to indicate that all players are ready. If only the opponent is ready, it displays a message
     * indicating that it is waiting for the local player to be ready. If neither player is ready, it displays a message
     * indicating that it is waiting for the opponent to be ready.
     */
    private void updatePeerStatus() {
        Session s = Session.current();
        if (s.localReady && s.peerReady) {
            statusLabel.getStyleClass().clear();
            statusLabel.getStyleClass().add("ready");
            statusLabel.getStyleClass().add("box");
            statusLabel.setText("All ready!");
        } else if (s.peerReady) {
            statusLabel.setText("Opponent is ready. Press Ready when you are.");
        } else if (s.localReady) {
            statusLabel.setText("Waiting for opponent to be ready...");
        } else {
            statusLabel.setText("Press Ready to start!");
        }
    }

    private void cancelPendingHost() {
        if (pendingHost != null) {
            pendingHost.close(); // unblocks accept() and frees the port
            pendingHost = null;
        }
    }
}