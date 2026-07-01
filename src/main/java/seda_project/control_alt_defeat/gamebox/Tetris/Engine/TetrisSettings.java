package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import seda_project.control_alt_defeat.gamebox.Configuration;

import java.io.IOException;
import java.util.ArrayList;

public class TetrisSettings {
    private ArrayList<KeyCode> player1Keys;
    private ArrayList<KeyCode> player2Keys;

    private static TetrisSettings instance;

    public static TetrisSettings getInstance() {
        if (instance == null) {
            instance = new TetrisSettings();
        }
        return instance;
    }

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
