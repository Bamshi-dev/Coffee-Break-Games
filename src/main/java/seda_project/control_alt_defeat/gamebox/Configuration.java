package seda_project.control_alt_defeat.gamebox;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class Configuration {

    private static Configuration instance;
    public boolean crimson = false;
    private HostServices hostServices;

    private Configuration() {
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }


    /**
     * Calculates the available DeckSizes for the Memory Game and changes the appearance of the UI.
     * @param tupleSize the amount of cards one tuple is build of
     * @param smallGame the RadioButton for the small Game (few Cards and Tuples)
     * @param mediumGame the RadioButton for a medium Game (normal Cards and Tuples)
     * @param largeGame the RadioButton for a large Game (many Cards and Tuples)
     */
    public void deckSize(int tupleSize, RadioButton smallGame, RadioButton mediumGame, RadioButton largeGame) {
        int max = 45 / tupleSize;
        int stepsize = max / 3;
        System.out.println("tuple size: " + tupleSize);
        System.out.println("max: " + max);
        System.out.println("stepsize: " + stepsize);
        if (max == 2) {
            smallGame.setText(String.valueOf(tupleSize));
            mediumGame.setDisable(false);
            mediumGame.setText(String.valueOf((max - stepsize) * tupleSize));
            largeGame.setDisable(true);
            largeGame.setText("---");
            if (largeGame.isSelected()) {
                largeGame.setSelected(false);
            }
        } else if (max == 1) {
            smallGame.setText(String.valueOf(tupleSize));
            mediumGame.setDisable(true);
            mediumGame.setText("---");
            largeGame.setDisable(true);
            largeGame.setText("---");
            if (largeGame.isSelected()){
                largeGame.setSelected(false);
            }
            if (mediumGame.isSelected()){
                mediumGame.setSelected(false);
            }
        } else {
            largeGame.setText(String.valueOf(max * tupleSize));
            mediumGame.setDisable(false);
            mediumGame.setText(String.valueOf((max - stepsize) * tupleSize));
            largeGame.setDisable(false);
            smallGame.setText(String.valueOf((max - 2 * stepsize) * tupleSize));
        }
    }

    /**
     * Cheks if a name has more than 16 characters
     * @param name the Name to be checked
     * @param player the player Number (eiter 1 or 2)
     * @param statusLabel the label to show the error message in case of one being there
     * @return if the name has less character this methods does return ture otherwise it will return false
     */
    public boolean checkNameLength(String name, int player, Label statusLabel) {
        int max = 16;
        int length = name.length();
        if (length > max) {
            statusLabel.setVisible(true);
            statusLabel.setText("The name of player " + player + " may not be longer than " + max + " characters!");
            return false;
        }
        return true;
    }


    /**
     * If a User does not Input Name replace it with "Player" and the player number
     * @param name the input of the User
     * @param player the number of the player
     * @return
     */
    public String checkNameInput(String name, int player) {
        if (name.equals("")) {
            name = "Player " + player;
        }
        return name;
    }


    /**
     * This method does select the first Player
     * @return the integer that is the first player
     */
    public int ActivePlayer() {
        return new Random().nextInt(2) + 1;
    }

    /**
     * This is a universal method to trigger a scene change
     * @param address is the location of the fxml file of the new scene
     * @param header is a VBox element with the fx:id header to get the stage
     * @param vS is the viewStack to store the scene history. Manly used for the backwards movement
     * @return returns the controller for the next scene
     */
    public Object changeScene(String address, VBox header, ViewStack vS) {
        try {
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            vS.addFxmlLoaders(address);
            var controller = loader.getController();
            Stage stage = (Stage) header.getScene().getWindow();
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

            if (controller != null) {
                return controller;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * This method takes the last element of the ViewStack and displays it. So it can be used to navigate backwars
     * @param header is a VBox element with the fx:id header to get the stage
     * @param vS is the viewStack to store the scene history. Manly used for the backwards movement
     * @return the Controller of the next scene
     */
    public Object backScene(VBox header, ViewStack vS) {
        try {
            vS.popFxmlLoader();
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(vS.getFxmlLoader()));
            Parent root = loader.load();
            var controller = loader.getController();

            Stage stage = (Stage) header.getScene().getWindow();
            Scene currentScene = stage.getScene();

            currentScene.setRoot(root);

            if (controller != null) {
                return controller;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * This method checks whether the Game- and Resultscreens should be displayed upside down
     * @param player1Name the Name of the first Player
     * @param player2Name the Name of the second Player
     * @return if a name matches one of the strings true is returned
     */
    public boolean checkFlip(String player1Name, String player2Name) {
        if (player1Name.equals("Upsidedown")) {
            return true;
        }
        if (player2Name.equals("Upsidedown")) {
            return true;
        }
        if (player1Name.equals("Dinnerbone")) {
            return true;
        }
        if (player2Name.equals("Dinnerbone")) {
            return true;
        }
        if (player1Name.equals("Grumm")) {
            return true;
        }
        return player2Name.equals("Grumm");
    }

    /**
     * This method checks whether the background of the Gamae- and Resultscreen should have rainbow color
     * @param player1Name the Name of the first Player
     * @param player2Name the Name of the second Player
     * @return if a name matches one of the strings true is returned
     */
    public boolean checkRainbow(String player1Name, String player2Name) {
        if (player1Name.equals("jeb_")) {
            return true;
        }
        if (player2Name.equals("jeb_")) {
            return true;
        }
        if (player1Name.equals("Rainbow")) {
            return true;
        }
        return player2Name.equals("Rainbow");
    }

    public HostServices getHostServices() {
        return hostServices;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
}
