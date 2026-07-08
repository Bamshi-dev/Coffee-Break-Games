package seda_project.control_alt_defeat.gamebox.Memory.engine;

import java.util.ArrayList;
import java.util.List;

public class GameEngineImpl implements GameEngine {

    private GameConfig config;
    private List<Card> board;
    private int score1;
    private int score2;
    private String activePlayer;
    private List<Integer> flippedIds;
    private boolean gameOver;

    // UI and Network listeners
    private final List<GameEventListener> listeners = new ArrayList<>();

    /**
     * Initializes the game engine with the provided configuration and setup. It sets up the game board, scores, active
     * player, and other necessary state variables based on the initial deck and first player specified in the GameSetup.
     * This method is called at the start of a new game or when resetting the game with a new setup.
     * @param config The GameConfig object containing the game settings, including player names, match size, and deck size.
     * @param setup The GameSetup object containing the initial deck of cards and the first player to take a turn.
     *              It provides the necessary information to initialize the game state.
     */
    @Override
    public void start(GameConfig config, GameSetup setup) {
        this.config = config;
        this.board = new ArrayList<>(setup.initialDeck());
        this.activePlayer = setup.firstPlayer();
        this.flippedIds = new ArrayList<>();
        this.score1 = 0;
        this.score2 = 0;
        this.gameOver = false;
    }

    /**
     * Returns a snapshot of the current game state, including the board configuration, scores, active player, flipped
     * card IDs, and game over status. This snapshot can be used for saving the game state, displaying the current game
     * status in the user interface, or for other purposes where a representation of the current game state is needed.
     * @return A GameSnapshot object representing the current state of the game, including the board, scores, active
     *          player, flipped card IDs, and game over status.
     */
    @Override
    public GameSnapshot getSnapshot() {
        return new GameSnapshot(
                List.copyOf(board),
                score1,
                score2,
                activePlayer,
                List.copyOf(flippedIds),
                gameOver
        );
    }

    /**
     * Returns the current game configuration, including player names, match size, and deck size. This method provides
     * access to the settings that define the rules and parameters of the current game session.
     * @return The GameConfig object representing the current game configuration.
     */
    @Override
    public void addListener(GameEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Checks if a card can be flipped based on its ID. It verifies that the card ID is within the valid range, that
     * the game is not over, and that the card is neither removed nor already face-up. This method is used to determine
     * whether a player can flip a specific card during their turn.
     * @param cardId The ID of the card to check for flipping. It should be a valid index within the board list.
     * @return True if the card can be flipped; false otherwise. A card can be flipped if it is within the valid range,
     *          the game is not over, and the card is neither removed nor already face-up.
     */
    public boolean canFlip(int cardId) {
        if (cardId < 0 || cardId >= board.size() || gameOver) return false;
        Card card = board.get(cardId);
        return !card.removed() && !card.faceUp();
    }

    /**
     * Flips a card with the specified ID and handles the game logic for matching cards, scoring, and turn changes. It
     * checks if the flip is valid, updates the card state, notifies listeners of the flip, and determines if a match
     * has occurred. If a match is found, it updates the scores and removes the matched cards; otherwise, it switches
     * the active player. The method also checks for game over conditions and notifies listeners accordingly.
     * @param cardId The ID of the card to flip. It should be a valid index within the board list.
     * @return A FlipResult object representing the outcome of the flip action, including whether it resulted in a match,
     *          mismatch, or game over, along with relevant information such as flipped card IDs, active player, and points scored. If the flip is invalid, it returns null.
     */
    @Override
    public FlipResult flip(int cardId) {
        // Check if flip is possible
        if (!canFlip(cardId)) {
            return null;
        }

        // Mark card face-up and add to flippedIds
        Card oldCard = board.get(cardId);
        board.set(cardId, new Card(oldCard.id(), oldCard.symbolId(), true, false));
        flippedIds.add(cardId);

        // Notify listeners that a card was flipped
        GameSnapshot snap = getSnapshot();
        for (GameEventListener listener : listeners) {
            listener.onCardFlipped(cardId, snap);
        }

        // Check if flipped exactly K cards
        if (flippedIds.size() == config.k()) {

            // Check if all flipped cards share the same symbol
            boolean isMatch = true;
            int firstSymbol = board.get(flippedIds.getFirst()).symbolId();

            for (int id : flippedIds) {
                if (board.get(id).symbolId() != firstSymbol) {
                    isMatch = false;
                    break;
                }
            }

            // Save the IDs before clearing
            List<Integer> idsToResolve = List.copyOf(flippedIds);

            if (isMatch) {
                // Add 10 points to the active player
                int points = GameConstants.POINTS_PER_MATCH;
                if (activePlayer.equals(config.player1Name())) {
                    score1 += points;
                } else {
                    score2 += points;
                }

                // Remove the matched cards
                for (int id : flippedIds) {
                    Card c = board.get(id);
                    board.set(id, new Card(c.id(), c.symbolId(), c.faceUp(), true));
                }

                flippedIds.clear();

                // Check for Game Over
                gameOver = true;
                for (Card c : board) {
                    if (!c.removed()) {
                        gameOver = false;
                        break;
                    }
                }

                // Notify listeners of the match
                snap = getSnapshot();
                for (GameEventListener listener : listeners) {
                    listener.onMatch(idsToResolve, activePlayer, 10, snap);
                    if (gameOver) {
                        listener.onGameOver(score1, score2, getOutcome(config.player1Name()), getOutcome(config.player2Name()));
                    }
                }

                return new FlipResult(
                        gameOver ? TurnResult.GAME_OVER : TurnResult.MATCH,
                        idsToResolve,
                        activePlayer,
                        10
                );

            } else {
                // Switch player
                if (activePlayer.equals(config.player1Name())) {
                    activePlayer = config.player2Name();
                } else {
                    activePlayer = config.player1Name();
                }

                // Notify listeners of the mismatch before flipping cards down
                snap = getSnapshot();
                for (GameEventListener listener : listeners) {
                    listener.onMismatch(idsToResolve, snap);
                }

                // Face down
                for (int id : flippedIds) {
                    Card c = board.get(id);
                    board.set(id, new Card(c.id(), c.symbolId(), false, false));
                }

                flippedIds.clear();

                // Notify listeners that cards flipped back and turn changed
                snap = getSnapshot();
                for (GameEventListener listener : listeners) {
                    listener.onMismatchFlipback(idsToResolve, snap);
                    listener.onTurnChanged(activePlayer, snap);
                }

                return new FlipResult(
                        TurnResult.MISMATCH,
                        idsToResolve,
                        activePlayer,
                        0
                );
            }
        }
        return null;
    }

    /**
     * Returns the outcome of the game for a specific player based on their name. It checks if the game is over and
     * compares the scores of both players to determine if the specified player has won, lost, or if the game ended in
     * a draw. This method is used to provide feedback to players about their performance at the end of the game.
     * @param playerName The name of the player for whom the game outcome is being queried. It should match one of the
     *                   player names specified in the GameConfig.
     * @return A GameOutcome enum value indicating whether the specified player has won, lost, or if the game ended in
     *          a draw. If the game is not over yet, it throws an IllegalStateException.
     */
    @Override
    public GameOutcome getOutcome(String playerName) {
        if (!gameOver) throw new IllegalStateException("Game is not over yet.");

        if (score1 == score2) return GameOutcome.DRAW;
        boolean isP1 = playerName.equals(config.player1Name());
        if (isP1) {
            return score1 > score2 ? GameOutcome.WIN : GameOutcome.LOSE;
        } else {
            return score2 > score1 ? GameOutcome.WIN : GameOutcome.LOSE;
        }
    }

    /**
     * Removes a previously added GameEventListener from the list of listeners. This allows for dynamic management of
     * listeners, enabling the removal of listeners that are no longer needed or relevant to the current game session.
     * If the specified listener is not found in the list, the method does nothing.
     * @param listener The GameEventListener to be removed from the list of listeners. If the listener is not present,
     *                 no action is taken.
     */
    @Override
    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns the name of the currently active player whose turn it is to make a move. This method provides access to
     * the current turn information, allowing other components of the game to determine which player is currently taking their turn.
     * @return A string representing the name of the active player. It corresponds to one of the player names specified
     *          in the GameConfig.
     */
    @Override
    public String getActivePlayer() {
        return activePlayer;
    }

    /**
     * Checks if the game is over, indicating that all matches have been completed and no further moves can be made.
     * This method provides a way to determine the end state of the game, allowing other components to take appropriate
     * actions based on whether the game has concluded.
     * @return True if the game is over; false otherwise. A game is considered over when all cards have been matched and
     *          removed from the board.
     */
    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Resets the game engine with a new GameSetup, allowing for a fresh start with the same configuration. It
     * reinitializes the game state, including the board, scores, active player, and other necessary variables
     * based on the new setup provided. This method is typically called when starting a new game or when resetting
     * the current game with a different deck or initial conditions.
     * @param newSetup The GameSetup object containing the new initial deck of cards and the first player to take a turn.
     *                 It provides the necessary information to reinitialize the game state for a new game session.
     */
    @Override
    public void resetWith(GameSetup newSetup) {
        start(this.config, newSetup);
    }
}
