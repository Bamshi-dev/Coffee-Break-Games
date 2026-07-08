package seda_project.control_alt_defeat.gamebox.Memory;

import java.util.ArrayList;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.Controller.GameScreen;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Player;

public class LocalGame {
    private Configuration c = Configuration.getInstance();
    private Player player1;
    private Player player2;
    private GameScreen gameScreen;
    private int matchSize;
    private int activePlayer;
    private ArrayList<Integer> flippedCards;
    private int deckSize;
    private int possibleMatches;
    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));

    /**
     * Initializes a new instance of the LocalGame class with the specified players, game screen, match size, and deck
     * size. It sets up the game state, including the active player, flipped cards list, and possible matches based on
     * the provided configuration. The constructor also updates the game screen to display the name of the active player.
     * @param player1 The first player participating in the game.
     * @param player2 The second player participating in the game.
     * @param gameScreen The GameScreen instance responsible for displaying the game interface and handling user interactions.
     * @param matchSize The number of cards that need to be matched in a single turn.
     * @param deckSize The total number of cards in the game deck, which determines the number of possible matches.
     */
    public LocalGame(Player player1, Player player2, GameScreen gameScreen, int matchSize, int deckSize) {
        this.player1 = player1;
        this.player2 = player2;
        this.gameScreen = gameScreen;
        this.activePlayer = c.ActivePlayer();
        this.matchSize = matchSize;
        this.flippedCards = new ArrayList<>();
        this.deckSize = deckSize;
        possibleMatches = deckSize/matchSize;
        gameScreen.setActivePlayerLabel(getActivePlayerName());
    }

    /**
     * Returns the name of the currently active player whose turn it is to make a move. It determines the active player
     * based on the current turn index and returns the corresponding player's name. This method is used to update the
     * game interface with the name of the player who is currently taking their turn.
     * @return A string representing the name of the active player. It corresponds to either player1 or player2 based on
     *          the current turn index, allowing the game interface to display the correct player's name during their turn.
     */
    public String getActivePlayerName() {
        if (activePlayer % 2 == 0) {
            return player2.getName();
        } else {
            return player1.getName();
        }
    }


}
