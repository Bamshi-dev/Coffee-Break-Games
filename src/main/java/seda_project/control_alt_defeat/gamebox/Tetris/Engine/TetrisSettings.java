package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import seda_project.control_alt_defeat.gamebox.Configuration;

public class TetrisSettings {
    private ArrayList<KeyCode> player1Keys;
    private ArrayList<KeyCode> player2Keys;

    private static TetrisSettings instance;

    /**
     * Returns the singleton instance of TetrisSettings. If the instance does not exist, it creates a new one. This
     * method ensures that there is only one instance of TetrisSettings throughout the application, providing a
     * centralized point for managing game settings such as player controls. The singleton pattern is used to maintain
     * consistency and avoid conflicts in settings across different parts of the application.
     * @return The singleton instance of TetrisSettings, which holds the key bindings and settings for the Tetris game.
     * If the instance does not exist, it is created and returned.
     */
    public static TetrisSettings getInstance() {
        if (instance == null) {
            instance = new TetrisSettings();
        }
        return instance;
    }

    /**
     * Initializes the TetrisSettings instance with default key bindings for both players. Player 1 is assigned the
     * WASD keys for movement, while Player 2 is assigned the arrow keys. This constructor sets up the initial
     * configuration for player controls, allowing for immediate gameplay without requiring additional setup. The key
     * bindings can be changed later through the provided methods, but this constructor establishes a standard starting
     * point for the game settings.
     */
    private TetrisSettings() {
        // Player 1 Primary (WASD)
        player1Keys = new ArrayList<>();
        player1Keys.add(KeyCode.A);
        player1Keys.add(KeyCode.D);
        player1Keys.add(KeyCode.S);
        player1Keys.add(KeyCode.W);

        // Player 2 Primary (Arrows)
        player2Keys = new ArrayList<>();
        player2Keys.add(KeyCode.LEFT);
        player2Keys.add(KeyCode.RIGHT);
        player2Keys.add(KeyCode.DOWN);
        player2Keys.add(KeyCode.UP);
    }

    /**
     * Changes the key binding for a specific player and position. This method opens a modal window to capture the new
     * key input from the user. It checks if the new key is already assigned to either player, and if not, updates the
     * key binding for the specified player and position. The method returns the new KeyCode that was set, allowing for
     * confirmation of the change. This functionality enables players to customize their controls according to their
     * preferences while ensuring that key conflicts are avoided.
     * @param player An integer representing the player number (0 for Player 1, 1 for Player 2) whose key binding is
     *               being changed. This parameter determines which player's key bindings will be modified.
     * @param position An integer representing the position of the key binding to be changed (0 for left, 1 for right,
     *                 2 for down, 3 for up). This parameter specifies which specific control of the player is being
     *                 updated with a new key input.
     * @return The new KeyCode that was set for the specified player and position. If the key change was successful,
     * this method returns the newly assigned KeyCode; otherwise, it may return null if the key was already in use or
     * if the input was invalid.
     */
    public KeyCode change(int player, int position) {
        try {
            Stage inputStage = new Stage();
            inputStage.initStyle(StageStyle.TRANSPARENT);
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource("/Views/Tetris/KeyInput.fxml"));
            Parent root = loader.load();
            Scene s = new Scene(root);
            s.setFill(Color.TRANSPARENT);
            inputStage.initModality(Modality.APPLICATION_MODAL);
            KeyCode[] result = new KeyCode[1];

            s.setOnKeyPressed(e -> {
                KeyCode key = e.getCode();
                if (!player1Keys.contains(key) && !player2Keys.contains(key)) {
                    result[0] = key;
                    if (position < 4) {
                        switch (player) {
                            case 0:
                                player1Keys.set(position, key);
                                break;
                            case 1:
                                player2Keys.set(position, key);
                                break;
                        }
                    }
                    inputStage.close();
                }
            });

            inputStage.setScene(s);
            inputStage.showAndWait();
            return result[0];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public ArrayList<KeyCode> getPlayer1Keys() { return player1Keys; }
    public ArrayList<KeyCode> getPlayer2Keys() { return player2Keys; }
}
