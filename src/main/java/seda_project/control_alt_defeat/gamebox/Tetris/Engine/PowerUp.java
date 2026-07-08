package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;

public class PowerUp implements Serializable {

    private final int playerNum;
    private final int row;
    private final int col;
    private final long spawnTime;
    private final PowerUpType type;

    private boolean drawn;

    /**
     * Creates a new PowerUp instance with the specified parameters. The PowerUp represents a special item in the Tetris game that can be collected by a player to gain certain advantages or effects. The playerNum indicates which player the power-up belongs to, while row and col specify its position on the game board. The spawnTime records when the power-up was created, and the type defines the specific effect of the power-up. The drawn parameter indicates whether the power-up has been rendered on the screen.
     * @param playerNum The player number (1 or 2) that the power-up belongs to.
     * @param row The row position of the power-up on the game board.
     * @param col The column position of the power-up on the game board.
     * @param spawnTime The time (in milliseconds) when the power-up was spawned.
     * @param type The PowerUpType that defines the specific effect of the power-up.
     * @param drawn A boolean indicating whether the power-up has been drawn on the screen.
     */
    public PowerUp(int playerNum, int row, int col, long spawnTime, PowerUpType type, boolean drawn) {
        this.playerNum = playerNum;
        this.row = row;
        this.col = col;
        this.spawnTime = spawnTime;
        this.type = type;
        this.drawn = drawn;
    }

    /**
     * Returns whether the power-up has been drawn on the screen. This method can be used to check if the power-up is currently visible to the player or if it has already been rendered in the game. The drawn state can be updated when the power-up is rendered or collected by a player.
     * @return A boolean indicating whether the power-up has been drawn on the screen (true) or not (false).
     */
    public boolean isDrawn() {
        return drawn;
    }

    /**
     * Marks the power-up as drawn, indicating that it has been rendered on the screen. This method can be called when the power-up is displayed to the player, allowing the game logic to track its visibility state. Once marked as drawn, the power-up can be treated as visible in the game until it is collected or removed.
     */
    public void draw(){
        this.drawn = true;
    }

    /**
     * Returns the player number associated with this power-up. The player number indicates which player the power-up belongs to, allowing the game logic to determine which player can collect or activate the power-up. This information is essential for managing player-specific effects and interactions in the game.
     * @return An integer representing the player number (1 or 2) that the power-up belongs to.
     */
    public int getPlayerNum() {
        return playerNum;
    }

    /**
     * Returns the row position of the power-up on the game board. The row value indicates the vertical position of the power-up, allowing the game logic to determine its location and interactions with other blocks or players. This information is essential for rendering the power-up in the correct position and managing its behavior during gameplay.
     * @return An integer representing the row position of the power-up on the game board.
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column position of the power-up on the game board. The column value indicates the horizontal position of the power-up, allowing the game logic to determine its location and interactions with other blocks or players. This information is essential for rendering the power-up in the correct position and managing its behavior during gameplay.
     */
    public int getCol() {
        return col;
    }

    /**
     * Returns the time (in milliseconds) when the power-up was spawned. The spawn time can be used to track the duration of the power-up's existence in the game, allowing for time-based effects or expiration. This information is essential for managing the lifecycle of the power-up and determining when it should be removed or deactivated.
     * @return A long value representing the time (in milliseconds) when the power-up was spawned.
     */
    public long getSpawnTime() {
        return spawnTime;
    }

    /**
     * Returns the PowerUpType associated with this power-up. The PowerUpType defines the specific effect or ability that the power-up provides to the player, allowing the game logic to determine how it should be applied during gameplay. This information is essential for managing the behavior and interactions of the power-up within the game.
     * @return The PowerUpType that defines the specific effect of the power-up.
     */
    public PowerUpType getType() {
        return type;
    }
}
