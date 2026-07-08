package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Tetris.Controller.GameScreen;

public class KeyHandler {
    private final TetrisEngine engine;
    private final TetrisSettings tS;
    private final GameScreen gameScreen;

    /**
     * Which player this keyboard drives:
     *  0 = local hot-seat (WASD -> player 1, arrows -> player 2)
     *  1 = LAN host: every key drives player 1 only
     *  2 = LAN dual-engine client: every key drives player 2 only
     */
    private final int lanPlayer;

    private int p1LocalFocus = 0;
    private int p2LocalFocus = 0;

    // bypass OS limitations
    private final Map<KeyCode, Integer> activeKeys = new HashMap<>();
    private Timeline repeatTimer;

    /**
     * Creates a new KeyHandler instance that manages keyboard input for the Tetris game. It takes in the TetrisEngine, TetrisSettings, and GameScreen instances to handle player input and game state. The lanPlayer parameter determines which player the keyboard input will control in a LAN multiplayer setting.
     * @param engine The TetrisEngine instance that manages the game logic and state.
     * @param tS The TetrisSettings instance that holds the key bindings and settings for the game.
     * @param gameScreen The GameScreen instance that represents the user interface for the game.
     */
    public KeyHandler(TetrisEngine engine, TetrisSettings tS, GameScreen gameScreen) {
        this(engine, tS, gameScreen, 0);
    }

    /**
     * Creates a new KeyHandler instance that manages keyboard input for the Tetris game, specifically for LAN multiplayer settings. It takes in the TetrisEngine, TetrisSettings, and GameScreen instances to handle player input and game state. The p1Only parameter determines whether the keyboard input will control only player 1 (true) or both players (false) in a LAN multiplayer setting.
     * @param engine The TetrisEngine instance that manages the game logic and state.
     * @param tS The TetrisSettings instance that holds the key bindings and settings for the game.
     * @param gameScreen The GameScreen instance that represents the user interface for the game.
     * @param p1Only A boolean indicating whether the keyboard input will control only player 1 (true) or both players (false) in a LAN multiplayer setting.
     */
    public KeyHandler(TetrisEngine engine, TetrisSettings tS, GameScreen gameScreen, boolean p1Only) {
        this(engine, tS, gameScreen, p1Only ? 1 : 0);
    }

    /**
     * Creates a new KeyHandler instance that manages keyboard input for the Tetris game, specifically for LAN multiplayer settings. It takes in the TetrisEngine, TetrisSettings, and GameScreen instances to handle player input and game state. The lanPlayer parameter determines which player the keyboard input will control in a LAN multiplayer setting (1 for player 1, 2 for player 2).
     * @param engine The TetrisEngine instance that manages the game logic and state.
     * @param tS The TetrisSettings instance that holds the key bindings and settings for the game.
     * @param gameScreen The GameScreen instance that represents the user interface for the game.
     * @param lanPlayer An integer indicating which player the keyboard input will control in a LAN multiplayer setting (1 for player 1, 2 for player 2).
     */
    public KeyHandler(TetrisEngine engine, TetrisSettings tS, GameScreen gameScreen, int lanPlayer) {
        this.engine = engine;
        this.tS = tS;
        this.gameScreen = gameScreen;
        this.lanPlayer = lanPlayer;
    }

    /**
     * Stops the key handling process by stopping the internal repeat timer. This method is typically called when the game is paused or ended to prevent further processing of keyboard input. It ensures that no additional key events are handled after the game state has changed.
     */
    public void stop() {
        if (repeatTimer != null) repeatTimer.stop();
    }

    /**
     * Handles the keyboard input for the Tetris game based on the provided KeyCode. It checks the key against the configured key bindings for both players and processes the corresponding game actions (left, right, drop, rotate) through the TetrisEngine. The method also takes into account whether the game is in LAN mode and which player the keyboard input is controlling.
     * @param key The KeyCode representing the key that was pressed on the keyboard. This key is checked against the configured key bindings for both players to determine the appropriate game action to process.
     */
    public void handle(KeyCode key) {
        ArrayList<KeyCode> p1 = tS.getPlayer1Keys();
        ArrayList<KeyCode> p2 = tS.getPlayer2Keys();
        boolean twoBlocks = TetrisAdvancedSettings.getInstance().isTwoBlocks();

        // LAN: this keyboard drives exactly one player (host = 1, dual client = 2).
        // Both keysets map to that single player, so a player at one machine can use
        // either WASD or the arrows; the secondary set targets block 1 in two-block mode.
        if (lanPlayer == 1 || lanPlayer == 2) {
            int target = lanPlayer;
            if (key == p1.get(0)) engine.processInput(target, "LEFT", 0);
            else if (key == p1.get(1)) engine.processInput(target, "RIGHT", 0);
            else if (key == p1.get(2)) engine.processInput(target, "DROP", 0);
            else if (key == p1.get(3)) engine.processInput(target, "ROTATE", 0);

            if (twoBlocks) {
                if (key == p2.get(0)) engine.processInput(target, "LEFT", 1);
                else if (key == p2.get(1)) engine.processInput(target, "RIGHT", 1);
                else if (key == p2.get(2)) engine.processInput(target, "DROP", 1);
                else if (key == p2.get(3)) engine.processInput(target, "ROTATE", 1);
            } else {
                if (key == p2.get(0)) engine.processInput(target, "LEFT", 0);
                else if (key == p2.get(1)) engine.processInput(target, "RIGHT", 0);
                else if (key == p2.get(2)) engine.processInput(target, "DROP", 0);
                else if (key == p2.get(3)) engine.processInput(target, "ROTATE", 0);
            }
            /* in lan each machine drives a single player so we stop here.
             if we dont, the code below runs too and moves the other player
            and also rotates the block twice
            */
            return;
        }

        if (key == p1.get(0)) engine.processInput(1, "LEFT", p1LocalFocus);
        else if (key == p1.get(1)) engine.processInput(1, "RIGHT", p1LocalFocus);
        else if (key == p1.get(2)) engine.processInput(1, "DROP", p1LocalFocus);
        else if (key == p1.get(3)) engine.processInput(1, "ROTATE", p1LocalFocus);

        if (key == p2.get(0)) engine.processInput(2, "LEFT", p2LocalFocus);
        else if (key == p2.get(1)) engine.processInput(2, "RIGHT", p2LocalFocus);
        else if (key == p2.get(2)) engine.processInput(2, "DROP", p2LocalFocus);
        else if (key == p2.get(3)) engine.processInput(2, "ROTATE", p2LocalFocus);
    }

    /**
     * Attaches the KeyHandler to a JavaFX Scene, enabling it to listen for keyboard input events. It sets up event filters for key presses and releases, allowing the KeyHandler to track active keys and manage their states. The method also initializes an internal timer that processes key repeats at a fixed interval, simulating the behavior of holding down keys for continuous input. This setup ensures that the game responds appropriately to player input during gameplay.
     * @param scene The JavaFX Scene to which the KeyHandler will be attached, allowing it to listen for keyboard input events and manage player actions in the Tetris game.
     */
    public void attach(Scene scene) {
        // Track Physical Press
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = ((KeyEvent) event).getCode();
            if (!activeKeys.containsKey(code)) {
                activeKeys.put(code, 0); // 0 ticks held
                handle(code); // Trigger immediately
            }
            event.consume(); // Prevent OS repeat
        });

        // Track Physical Release
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            activeKeys.remove(((KeyEvent) event).getCode());
        });

        // Internal 60fps Key Engine
        repeatTimer = new Timeline(new KeyFrame(Duration.millis(20), e -> {
            ArrayList<KeyCode> p1 = tS.getPlayer1Keys();
            ArrayList<KeyCode> p2 = tS.getPlayer2Keys();

            for (Map.Entry<KeyCode, Integer> entry : new ArrayList<>(activeKeys.entrySet())) {
                KeyCode key = entry.getKey();
                int ticks = entry.getValue();
                ticks++;
                activeKeys.put(key, ticks);

                boolean isLeftRight = (key == p1.get(0) || key == p1.get(1) || key == p2.get(0) || key == p2.get(1));
                boolean isDrop = (key == p1.get(2) || key == p2.get(2));

                if (isDrop) {
                    // Soft drop repeats very fast (40ms) immediately
                    if (ticks % 2 == 0) handle(key);
                } else if (isLeftRight) {
                    // Standard Tetris DAS: Wait 160ms before auto-repeating, then move every 60ms
                    if (ticks > 8 && (ticks - 8) % 3 == 0) handle(key);
                }
            }
        }));
        repeatTimer.setCycleCount(Timeline.INDEFINITE);
        repeatTimer.play();
    }
}
