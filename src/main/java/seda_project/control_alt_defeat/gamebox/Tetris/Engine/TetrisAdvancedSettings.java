package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;

public class TetrisAdvancedSettings implements Serializable {

    // Normal Configuration is with a vertical screen and the item that swaps the boards
    private boolean vertical = true;
    private long itemSpawnRate = 30000;
    private long itemDespawnRate = 7000;
    private int bombChance = 70;
    private boolean swapBoards = true;
    private boolean swapBlocks = false;
    private boolean portals = false;
    private boolean opponentSlowDown = false;
    private boolean opponentSpeedUp = false;
    private boolean opponentDelayRotation = false;
    private boolean selfDelayRotation = false;
    private boolean selfSpeedDown = false;
    private boolean radialBomb = false;
    private boolean columnBomb = false;
    private boolean twoBlocks = false;
    private boolean isBoardChange = false;


    private static TetrisAdvancedSettings instance;

    public static TetrisAdvancedSettings getInstance(){
        if (instance == null) instance = new TetrisAdvancedSettings();
        return instance;
    }

    private TetrisAdvancedSettings (){}

    /**
     * Copy every gameplay field from another settings object. Used in dual-engine LAN
     * so the client's engine is built from the HOST's advanced settings (power-up types,
     * spawn rates, bombs, two-blocks, board-change) rather than its own local defaults.
     */
    public void copyFrom(TetrisAdvancedSettings o) {
        this.vertical = o.vertical;
        this.itemSpawnRate = o.itemSpawnRate;
        this.itemDespawnRate = o.itemDespawnRate;
        this.bombChance = o.bombChance;
        this.swapBoards = o.swapBoards;
        this.swapBlocks = o.swapBlocks;
        this.portals = o.portals;
        this.opponentSlowDown = o.opponentSlowDown;
        this.opponentSpeedUp = o.opponentSpeedUp;
        this.opponentDelayRotation = o.opponentDelayRotation;
        this.selfDelayRotation = o.selfDelayRotation;
        this.selfSpeedDown = o.selfSpeedDown;
        this.radialBomb = o.radialBomb;
        this.columnBomb = o.columnBomb;
        this.twoBlocks = o.twoBlocks;
        this.isBoardChange = o.isBoardChange;
    }

    /**
     * Sets whether the game is played in vertical orientation or not. This setting affects the layout and display of the game screen, determining whether the game is presented in a vertical or horizontal format. The vertical parameter indicates whether the game should be displayed vertically (true) or horizontally (false).
     * @param vertical A boolean indicating whether the game should be displayed in vertical orientation (true) or horizontal orientation (false).
     */
    public void setVertical(boolean vertical) { this.vertical = vertical; }
    /**
     * Sets the item spawn rate for the game. This setting determines how frequently power-ups or special items appear on the game board during gameplay. The itemSpawnRate parameter specifies the time interval (in milliseconds) between the appearance of consecutive items, allowing for customization of the game's difficulty and pacing.
     * @param swapBoards A boolean indicating whether the game should be displayed in vertical orientation (true) or horizontal orientation (false).
     */
    public void setSwapBoards(boolean swapBoards) { this.swapBoards = swapBoards; }
    /**
     * Sets the item despawn rate for the game. This setting determines how long power-ups or special items remain on the game board before disappearing. The itemDespawnRate parameter specifies the time interval (in milliseconds) that an item will stay on the board before it is removed, allowing for customization of the game's difficulty and strategy.
     * @param swapBlocks A boolean indicating whether the game should be displayed in vertical orientation (true) or horizontal orientation (false).
     */
    public void setSwapBlocks(boolean swapBlocks) { this.swapBlocks = swapBlocks; }
    /**
     * Sets whether portals are enabled in the game. This setting determines whether players can use portals to teleport between different locations on the game board, adding an additional layer of strategy and complexity to the gameplay. The portals parameter indicates whether portals should be enabled (true) or disabled (false).
     * @param portals A boolean indicating whether portals should be enabled (true) or disabled (false) in the game.
     */
    public void setPortals(boolean portals) { this.portals = portals; }
    /**
     * Sets whether the opponent's blocks can slow down the player's blocks in the game. This setting affects the interaction between players in multiplayer mode, allowing for strategic gameplay where one player's actions can impact the other's performance. The opponentSlowDown parameter indicates whether this feature should be enabled (true) or disabled (false).
     * @param opponentSlowDown A boolean indicating whether the opponent's blocks can slow down the player's blocks (true) or not (false) in the game.
     */
    public void setOpponentSlowDown(boolean opponentSlowDown) { this.opponentSlowDown = opponentSlowDown; }
    /**
     * Sets whether the opponent's blocks can speed up the player's blocks in the game. This setting affects the interaction between players in multiplayer mode, allowing for strategic gameplay where one player's actions can impact the other's performance. The opponentSpeedUp parameter indicates whether this feature should be enabled (true) or disabled (false).
     */
    public void setOpponentSpeedUp(boolean opponentSpeedUp) { this.opponentSpeedUp = opponentSpeedUp; }
    /**
     * Sets whether the opponent's blocks can delay the rotation of the player's blocks in the game. This setting affects the interaction between players in multiplayer mode, allowing for strategic gameplay where one player's actions can impact the other's performance. The opponentDelayRotation parameter indicates whether this feature should be enabled (true) or disabled (false).
     * @param opponentDelayRotation A boolean indicating whether the opponent's blocks can delay the rotation of the player's blocks (true) or not (false) in the game.
     */
    public void setOpponentDelayRotation(boolean opponentDelayRotation) { this.opponentDelayRotation = opponentDelayRotation; }
    /**
     * Sets whether the player's blocks can delay their own rotation in the game. This setting affects the player's control over their blocks, allowing for strategic gameplay where the player can choose to delay the rotation of their blocks for better positioning. The selfDelayRotation parameter indicates whether this feature should be enabled (true) or disabled (false).
     * @param selfDelayRotation A boolean indicating whether the player's blocks can delay their own rotation (true) or not (false) in the game.
     */
    public void setSelfDelayRotation(boolean selfDelayRotation) { this.selfDelayRotation = selfDelayRotation; }
    /**
     * Sets whether the player's blocks can slow down their own speed in the game. This setting affects the player's control over their blocks, allowing for strategic gameplay where the player can choose to slow down their blocks for better positioning. The selfSpeedDown parameter indicates whether this feature should be enabled (true) or disabled (false).
     * @param selfSpeedDown A boolean indicating whether the player's blocks can slow down their own speed (true) or not (false) in the game.
     */
    public void setSelfSpeedDown(boolean selfSpeedDown) { this.selfSpeedDown = selfSpeedDown; }
    /**
     * Sets whether radial bombs are enabled in the game. This setting determines whether players can use radial bombs to clear blocks in a circular area on the game board, adding an additional layer of strategy and complexity to the gameplay. The radialBomb parameter indicates whether radial bombs should be enabled (true) or disabled (false).
     * @param radialBomb A boolean indicating whether radial bombs should be enabled (true) or disabled (false) in the game.
    */
    public void setRadialBomb(boolean radialBomb) { this.radialBomb = radialBomb; }

    /**
     * Sets whether column bombs are enabled in the game. This setting determines whether players can use column bombs to clear blocks in a vertical column on the game board, adding an additional layer of strategy and complexity to the gameplay. The columnBomb parameter indicates whether column bombs should be enabled (true) or disabled (false).
     * @param columnBomb A boolean indicating whether column bombs should be enabled (true) or disabled (false) in the game.
     */
    public void setColumnBomb(boolean columnBomb) { this.columnBomb = columnBomb; }
    /**
     * Sets whether the game should allow for board changes during gameplay. This setting affects the overall game dynamics, allowing for strategic gameplay where the game board can change based on certain conditions or player actions. The isBoardChange parameter indicates whether this feature should be enabled (true) or disabled (false).
     * @param isBoardChange A boolean indicating whether the game should allow for board changes during gameplay (true) or not (false).
     */
    public void setBoardChange(boolean isBoardChange) { this.isBoardChange = isBoardChange; }

    /**
     * Saves the integer settings for item spawn rate, item despawn rate, and bomb chance in the TetrisAdvancedSettings instance. This method allows for updating the gameplay parameters related to power-ups and special items in the game. The itemSpawnRate parameter specifies the time interval (in milliseconds) between the appearance of consecutive items, the itemDespawnRate parameter specifies the time interval (in milliseconds) that an item will stay on the board before it is removed, and the bombChance parameter specifies the probability (as a percentage) of a bomb appearing in the game.
     * @param itemSpawnRate the time interval (in milliseconds) between the appearance of consecutive items in the game.
     * @param itemDespawnRate the time interval (in milliseconds) that an item will stay on the board before it is removed in the game.
     * @param bombChance the probability (as a percentage) of a bomb appearing in the game.
     */
    public void saveIntSettings(int itemSpawnRate, int itemDespawnRate, int bombChance){
        this.itemDespawnRate = itemDespawnRate* 1000L;
        this.itemSpawnRate = itemSpawnRate* 1000L;
        this.bombChance = bombChance;
    }

    /**
     * Returns whether the game is played in vertical orientation or not. This method provides access to the current orientation setting of the game, allowing other parts of the application to determine how the game should be displayed. The return value indicates whether the game is currently set to be displayed vertically (true) or horizontally (false).
     * @return A boolean indicating whether the game is currently set to be displayed in vertical orientation (true) or horizontal orientation (false).
     */
    public boolean isVertical() {
        return vertical;
    }

    /**
     * Returns the item spawn rate for the game. This method provides access to the current setting for how frequently power-ups or special items appear on the game board during gameplay. The return value specifies the time interval (in milliseconds) between the appearance of consecutive items, allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A long value representing the time interval (in milliseconds) between the appearance of consecutive items in the game.
     */
    public long getItemSpawnRate() {
        return itemSpawnRate;
    }

    /**
     * Returns the item despawn rate for the game. This method provides access to the current setting for how long power-ups or special items remain on the game board before disappearing. The return value specifies the time interval (in milliseconds) that an item will stay on the board before it is removed, allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A long value representing the time interval (in milliseconds) that an item will stay on the board before it is removed in the game.
     */
    public long getItemDespawnRate() {
        return itemDespawnRate;
    }

    /**
     * Returns the bomb chance for the game. This method provides access to the current setting for the probability of a bomb appearing in the game. The return value specifies the probability as a percentage, allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return An integer representing the probability (as a percentage) of a bomb appearing in the game.
     */
    public int getBombChance() { return bombChance; }

    /**
     * Returns whether the game allows for board changes during gameplay. This method provides access to the current setting for whether the game board can change based on certain conditions or player actions. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether the game currently allows for board changes during gameplay (true) or not (false).
     */
    public boolean isSwapBoards() {
        return swapBoards;
    }

    /**
     * Returns whether the game allows for block swaps during gameplay. This method provides access to the current setting for whether players can swap blocks on the game board, adding an additional layer of strategy and complexity to the gameplay. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether the game currently allows for block swaps during gameplay (true) or not (false).
     */
    public boolean isSwapBlocks() {
        return swapBlocks;
    }

    /**
     * Returns whether portals are enabled in the game. This method provides access to the current setting for whether players can use portals to teleport between different locations on the game board, adding an additional layer of strategy and complexity to the gameplay. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether portals are currently enabled (true) or disabled (false) in the game.
     */
    public boolean isPortals() {
        return portals;
    }

    /**
     * Returns whether the opponent's blocks can slow down the player's blocks in the game. This method provides access to the current setting for whether one player's actions can impact the other's performance in multiplayer mode. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether the opponent's blocks can currently slow down the player's blocks (true) or not (false) in the game.
     */
    public boolean isOpponentSlowDown() {
        return opponentSlowDown;
    }

    /**
     * Returns whether the opponent's blocks can speed up the player's blocks in the game. This method provides access to the current setting for whether one player's actions can impact the other's performance in multiplayer mode. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether the opponent's blocks can currently speed up the player's blocks (true) or not (false) in the game.
     */
    public boolean isOpponentSpeedUp() {
        return opponentSpeedUp;
    }

    /**
     * Returns whether the opponent's blocks can delay the rotation of the player's blocks in the game. This method provides access to the current setting for whether one player's actions can impact the other's performance in multiplayer mode. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether the opponent's blocks can currently delay the rotation of the player's blocks (true) or not (false) in the game.
     */
    public boolean isOpponentDelayRotation() {
        return opponentDelayRotation;
    }

    /**
     * Returns whether the player's blocks can delay their own rotation in the game. This method provides access to the current setting for whether the player can choose to delay the rotation of their blocks for better positioning. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether the player's blocks can currently delay their own rotation (true) or not (false) in the game.
     */ 
    public boolean isSelfDelayRotation() {
        return selfDelayRotation;
    }

    /**
     * Returns whether the player's blocks can slow down their own speed in the game. This method provides access to the current setting for whether the player can choose to slow down their blocks for better positioning. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether the player's blocks can currently slow down their own speed (
     */
    public boolean isSelfSpeedDown() {
        return selfSpeedDown;
    }

    /**
     * Returns whether radial bombs are enabled in the game. This method provides access to the current setting for whether players can use radial bombs to clear blocks in a circular area on the game board, adding an additional layer of strategy and complexity to the gameplay. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether radial bombs are currently enabled (true) or disabled (false) in the game.
     */
    public boolean isRadialBomb() {
        return radialBomb;
    }

    /**
     * Returns whether column bombs are enabled in the game. This method provides access to the current setting for whether players can use column bombs to clear blocks in a vertical column on the game board, adding an additional layer of strategy and complexity to the gameplay. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether column bombs are currently enabled (true) or disabled (false) in the game.
     */
    public boolean isColumnBomb() {
        return columnBomb;
    }

    /**
     * Returns whether the game allows for two-block gameplay mode. This method provides access to the current setting for whether players can control two blocks simultaneously during gameplay, adding an additional layer of strategy and complexity to the game. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether the game currently allows for two-block gameplay mode (true) or not (false).
     */
    public boolean isTwoBlocks() {
        return twoBlocks;
    }

    /**
     * Sets whether the game allows for two-block gameplay mode. This method allows for updating the setting that determines whether players can control two blocks simultaneously during gameplay, adding an additional layer of strategy and complexity to the game. The twoBlocks parameter indicates whether this feature should be enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes. 
     * @param twoBlocks A boolean indicating whether the game should allow for two-block gameplay mode (true) or not (false).
     */
    public void setTwoBlocks(boolean twoBlocks) {
        this.twoBlocks = twoBlocks;
    }

    /**
     * Returns whether the game allows for board changes during gameplay. This method provides access to the current setting for whether the game board can change based on certain conditions or player actions. The return value indicates whether this feature is currently enabled (true) or disabled (false), allowing other parts of the application to retrieve and utilize this information for gameplay purposes.
     * @return A boolean indicating whether the game currently allows for board changes during gameplay (true) or not (false).
     */
    public boolean isBoardChange() {
        return isBoardChange;
    }
}
