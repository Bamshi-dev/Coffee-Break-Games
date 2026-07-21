package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TetrisEngine {
    private Board p1Board;
    private Board p2Board;

    private Block[] p1ActiveBlocks = new Block[2];
    private Block[] p2ActiveBlocks = new Block[2];
    private boolean isTwoBlockMode = false;

    private ArrayList<Block> p1NextActiveBlock;
    private ArrayList<Block> p2NextActiveBlock;

    private int bombChance;

    private int p1Score;
    private int p2Score;

    private final String p1Name;
    private final String p2Name;

    private boolean p1Lost;
    private boolean p2Lost;
    private boolean isGameOver;

    private int p1Level = 1;
    private int p2Level = 1;

    private List<TBlock> p1Blocks;
    private List<TBlock> p2Blocks;

    private final BlockRegistry blockRegistry;
    private final TetrisAdvancedSettings advancedSettings;

    private final List<TetrisEventListener> listeners = new CopyOnWriteArrayList<>();

    private int p1LinesCleared = 0;
    private int p2LinesCleared = 0;

    private static final long INITIAL_TICK_INTERVAL_MS = 800;
    private static final long MIN_TICK_INTERVAL_MS     = 100;

    private long p1TickInterval = INITIAL_TICK_INTERVAL_MS;
    private long p2TickInterval = INITIAL_TICK_INTERVAL_MS;

    private long p1BaseTickInterval;
    private long p2BaseTickInterval;

    private final List<Integer> p1SlowEffects = new ArrayList<>();
    private final List<Integer> p2SlowEffects = new ArrayList<>();
    private final List<Integer> p1FastEffects = new ArrayList<>();
    private final List<Integer> p2FastEffects = new ArrayList<>();

    private static final long ROTATION_DELAY_MS = 200;

    private long p1RotationDelayExpiry;
    private long p2RotationDelayExpiry;

    // Power-up mechanics
    private final List<PowerUp> activePowerUps = new CopyOnWriteArrayList<>();
    private long lastPowerUpSpawnTime;
    private static long POWERUP_INTERVAL_MS = 10000;
    private static long POWERUP_LIFESPAN_MS  = 7000;
    private static final Random RANDOM = new Random();

    private List<BombType> possibleBombs;
    private List<PowerUpType> possiblePowerUps;

    private boolean isStopped = false;

    // Dual-engine LAN: when soloPlayer != 0 this engine simulates only that player's
    // board. Power-ups spawn only on that board, opponent-targeted effects are sent
    // over the network instead of mutating the (locally absent) other board.
    private int soloPlayer = 0;
    private boolean dualMode = false;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "tetris-rotation-delay");
                t.setDaemon(true);
                return t;
            });

    /**
     * Creates a new TetrisEngine instance that manages the game logic and state for a Tetris game. It takes in the player names, initial levels, block registry, and advanced settings to configure the game environment. The engine initializes the game boards, active blocks, scores, and other necessary components to facilitate gameplay. The constructor also sets up the power-up mechanics and dual-engine LAN mode if applicable.
     * @param p1Name The name of player 1.
     * @param p2Name The name of player 2.
     * @param p1Level The initial level for player 1.
     * @param p2Level The initial level for player 2.
     * @param registry The BlockRegistry instance that provides access to the available Tetris blocks.
     * @param advancedSettings The TetrisAdvancedSettings instance that holds the advanced game settings and configurations.
     */
    public TetrisEngine(String p1Name, String p2Name, int p1Level, int p2Level, BlockRegistry registry, TetrisAdvancedSettings advancedSettings) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;

        this.p1NextActiveBlock = new ArrayList<>();
        this.p2NextActiveBlock = new ArrayList<>();

        this.blockRegistry = registry;
        this.advancedSettings = advancedSettings;

        this.p1Board = new Board(false);
        this.p2Board = new Board(true);

        this.p1Score = 0;
        this.p2Score = 0;

        this.p1Level = p1Level;
        this.p2Level = p2Level;

        this.isTwoBlockMode = advancedSettings.isTwoBlocks();

        this.bombChance = advancedSettings.getBombChance();

        POWERUP_INTERVAL_MS = advancedSettings.getItemSpawnRate();
        POWERUP_LIFESPAN_MS = advancedSettings.getItemDespawnRate();

        possibleBombs = new ArrayList<>();
        if (advancedSettings.isRadialBomb()) possibleBombs.add(BombType.RADIUS);
        if (advancedSettings.isColumnBomb()) possibleBombs.add(BombType.CLEAR_BELOW);

        possiblePowerUps = new ArrayList<>();
        if (advancedSettings.isOpponentDelayRotation()) possiblePowerUps.add(PowerUpType.OPPONENTROTATIONDELAY);
        if (advancedSettings.isOpponentSlowDown()) possiblePowerUps.add(PowerUpType.OPPONENTSPEEDDOWN);
        if (advancedSettings.isOpponentSpeedUp()) possiblePowerUps.add(PowerUpType.OPPONENTSPEEDUP);
        if (advancedSettings.isSelfDelayRotation()) possiblePowerUps.add(PowerUpType.SELFROTATIONDELAY);
        if (advancedSettings.isSelfSpeedDown()) possiblePowerUps.add(PowerUpType.SELFSPEEDDOWN);
        if (advancedSettings.isPortals()) possiblePowerUps.add(PowerUpType.PORTAL);
        if (advancedSettings.isSwapBlocks()) possiblePowerUps.add(PowerUpType.SWAPACTIVEBLOCKS);
        if (advancedSettings.isSwapBoards()) possiblePowerUps.add(PowerUpType.SWAPBOARDS);

        if (p1Level > 1){
            p1TickInterval = (long) Math.max(
                    MIN_TICK_INTERVAL_MS,
                    INITIAL_TICK_INTERVAL_MS * Math.pow(0.85, p1Level - 1)
            );
        }
        p1BaseTickInterval = p1TickInterval;

        if (p2Level > 1){
            p2TickInterval = (long) Math.max(
                    MIN_TICK_INTERVAL_MS,
                    INITIAL_TICK_INTERVAL_MS * Math.pow(0.85, p2Level - 1)
            );
        }
        p2BaseTickInterval = p2TickInterval;

        this.p1Lost = false;
        this.p2Lost = false;
        this.isGameOver = false;
        this.lastPowerUpSpawnTime = System.currentTimeMillis();

        p1Blocks = generateBlocks();
        p2Blocks = generateBlocks();

        spawnNewBlock(1, 0);
        spawnNewBlock(2, 0);

        if (isTwoBlockMode) {
            spawnNewBlock(1, 1);
            spawnNewBlock(2, 1);
        }
    }

    /**
     * Generates a shuffled list of Tetris blocks from the block registry. This method retrieves all available Tetris blocks from the block registry, shuffles them randomly, and returns the shuffled list. The generated list is used to provide a randomized sequence of blocks for each player during gameplay, ensuring that the game remains unpredictable and challenging.
     * @return A list of TBlock instances representing the shuffled Tetris blocks available for gameplay.
     */
    private List<TBlock> generateBlocks() {
        List<TBlock> allPieces = new ArrayList<>(blockRegistry.getAllPieces());
        Collections.shuffle(allPieces);
        return allPieces;
    }

    /**
     * Stops the TetrisEngine, preventing further game updates and notifying listeners of the stopped state. This method can be called to halt the game logic and freeze the current state of the game. It sets the isStopped flag to true and creates a snapshot of the current game state, which is then passed to all registered listeners through their onStopped method. This allows other parts of the application to respond to the engine being stopped, such as updating the UI or handling end-of-game scenarios.
     */
    public synchronized void stop(){
        if (isStopped) return;
        isStopped = true;
        GameState snapStopped = getSnapshot();
        listeners.forEach(l -> l.onStopped(snapStopped));
    }

    /**
     * Returns the current tick interval in milliseconds for the specified player. The tick interval determines how frequently the game updates for that player, affecting the speed at which blocks fall and the overall pace of gameplay. This method can be used to retrieve the current tick interval for either player 1 or player 2, allowing other parts of the application to access this information for gameplay logic or UI updates.
     * @param player An integer representing the player number (1 or 2) for which the tick interval is requested.
     * @return The current tick interval in milliseconds for the specified player.
     */
    public synchronized long getTickIntervalMs(int player) {
        return player == 1 ? p1TickInterval : p2TickInterval;
    }

    /**
     * Adds a TetrisEventListener to the list of listeners that will be notified of game events. This method allows other parts of the application to register for updates on the game state, such as when a tick occurs, when the game is stopped, or when other significant events happen during gameplay. The listener will receive notifications through its defined methods, enabling it to respond appropriately to changes in the game state.
     * @param listener The TetrisEventListener instance to be added to the list of listeners.   
     */
    public synchronized void addListener(TetrisEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a TetrisEventListener from the list of listeners that will be notified of game events. This method allows other parts of the application to unregister from receiving updates on the game state, preventing further notifications to the specified listener. Once removed, the listener will no longer receive notifications about ticks, game stops, or other significant events during gameplay.
     * @param listener The TetrisEventListener instance to be removed from the list of listeners.   
     */
    public synchronized void removeListener(TetrisEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Switch this engine into dual-engine LAN mode, simulating only {@code player}'s
     * board. Power-ups then spawn only on that board and opponent-targeted effects are
     * emitted as {@link AttackType}s for the network layer to deliver. Cross-board
     * power-ups that still need their own handshake (portal, swaps) and bombs are kept
     * out of the spawn pool until their respective phases.
     */
    public synchronized void setSoloPlayer(int player) {
        this.soloPlayer = player;
        this.dualMode = player != 0;
        // All power-ups are now networked for dual mode (self/opponent effects, portal,
        // the two swaps), and bombs are self-contained, so nothing is excluded here.
    }

    /**
     * Dual-engine LAN: apply an opponent-sent effect to THIS machine's own board.
     * Mirrors the receiving side of the OPPONENT* power-ups from the single-engine model.
     */
    public synchronized void applyIncomingAttack(AttackType type) {
        if (isGameOver || isStopped || soloPlayer == 0) return;
        int me = soloPlayer;
        switch (type) {
            case SPEED_UP -> {
                if (me == 1) p1FastEffects.add(10); else p2FastEffects.add(10);
                bumpInterval(me);
            }
            case SPEED_DOWN -> {
                if (me == 1) p1SlowEffects.add(10); else p2SlowEffects.add(10);
                bumpInterval(me);
            }
            case ROTATION_DELAY -> {
                long expiry = System.currentTimeMillis() + 10_000;
                if (me == 1) p1RotationDelayExpiry = expiry; else p2RotationDelayExpiry = expiry;
            }
        }
    }

    /**
     * Recomputes and updates the tick interval for the specified player based on the current active speed effects. This method calculates the new tick interval by considering any active slow or fast effects that may be affecting the player's gameplay. It then updates the player's tick interval and notifies all registered listeners of the change in tick speed, allowing other parts of the application to respond to the updated game state.
     * @param player An integer representing the player number (1 or 2) for which the tick interval is being updated.   
     */
    private void bumpInterval(int player) {
        long iv = recomputeInterval(player);
        if (player == 1) p1TickInterval = iv; else p2TickInterval = iv;
        listeners.forEach(l -> l.changeTickSpeed(player, iv));
    }

    /**
     * Recomputes the tick interval for the specified player based on the current active speed effects. This method calculates the new tick interval by considering the number of active slow and fast effects that may be affecting the player's gameplay. It uses a base speed value and applies a multiplier based on the difference between the number of slow and fast effects to determine the final tick interval. The computed interval is returned as a long value representing the new tick interval in milliseconds.
     * @param player An integer representing the player number (1 or 2) for which the tick interval is being recomputed.
     * @return A long value representing the recomputed tick interval in milliseconds for the specified player.
     */
    private long recomputeInterval(int player) {
        long baseSpeed = (player == 1) ? p1BaseTickInterval : p2BaseTickInterval;
        List<Integer> slows = (player == 1) ? p1SlowEffects : p2SlowEffects;
        List<Integer> fasts = (player == 1) ? p1FastEffects : p2FastEffects;
        int exponent = slows.size() - fasts.size();
        if (exponent >= 0) {
            return (long) (baseSpeed * (Math.pow(2,exponent)));
        } else {
            return (long) (baseSpeed / (Math.pow(2,(-exponent))));
        }
    }


    /**
     * Processes the active speed effects for the specified player, updating their tick interval accordingly. This method checks for any active slow or fast effects that may be affecting the player's gameplay and decrements their remaining duration. If any effects have expired, it recomputes the player's tick interval and notifies all registered listeners of the change in tick speed. This allows other parts of the application to respond to the updated game state and adjust gameplay accordingly.
     * @param player An integer representing the player number (1 or 2) for which the active speed effects are being processed.   
     */
    private void processSpeedEffects(int player) {
        List<Integer> slows = (player == 1) ? p1SlowEffects : p2SlowEffects;
        List<Integer> fasts = (player == 1) ? p1FastEffects : p2FastEffects;

        boolean changed = tickDown(slows) | tickDown(fasts);

        if (changed) {
            long newInterval = recomputeInterval(player);
            if (player == 1) {
                p1TickInterval = newInterval;
            }
            else {
                p2TickInterval = newInterval;
            }
            listeners.forEach(l -> l.changeTickSpeed(player, newInterval));
        }
    }

    /**
     * Decrements the remaining duration of active speed effects for a player and checks for any expired effects. This method iterates through the list of active effects, removing any that have expired (i.e., their remaining duration is less than or equal to 1) and decrementing the remaining duration of the others. It returns a boolean indicating whether any effects have expired, allowing the calling method to determine if further actions, such as recomputing the tick interval, are necessary.
     * @param effects A list of integers representing the remaining duration of active speed effects for a player.
     * @return A boolean indicating whether any active speed effects have expired (true) or not
     */
    private boolean tickDown(List<Integer> effects) {
        boolean expired = effects.removeIf(t -> t <= 1);
        effects.replaceAll(t -> t - 1);
        return expired;
    }

    /**
     * Processes a game tick for the specified player, updating the game state and notifying listeners of the changes. This method handles the game logic for each tick, including applying gravity to active blocks, checking for win conditions, managing power-up spawning, and notifying listeners of the current game state. It ensures that the game progresses smoothly and that all relevant updates are communicated to registered listeners for both players.
     * @param player An integer representing the player number (1 or 2) for which the game tick is being processed.
     */
    public synchronized void tick(int player) {
        if (isGameOver || isStopped) return;

        if (player == 1) {
            processSpeedEffects(1);
        } else {
            processSpeedEffects(2);
        }

        if (!p1Lost && player == 1) {
            if (p1ActiveBlocks[0] != null) applyGravity(1, 0, p1Board);
            if (isTwoBlockMode && p1ActiveBlocks[1] != null) applyGravity(1, 1, p1Board);
        }

        if (!p2Lost && player == 2) {
            if (p2ActiveBlocks[0] != null) applyGravity(2, 0, p2Board);
            if (isTwoBlockMode && p2ActiveBlocks[1] != null) applyGravity(2, 1, p2Board);
        }

        checkWinCondition();
        managePowerUpSpawning();

        GameState snap = getSnapshot();
        listeners.forEach(l -> l.onTick(snap, player));
    }

    /**
     * Manages the spawning and expiration of power-ups in the game. This method checks for any active power-ups that have expired based on their lifespan and removes them from the active list, notifying listeners of their expiration. It also checks if it's time to spawn a new power-up based on the defined interval and randomly selects a power-up type to spawn on the appropriate player's board. The method ensures that power-ups are managed effectively, adding an element of strategy and excitement to the gameplay.
     */
    private void managePowerUpSpawning() {
        long now = System.currentTimeMillis();

        List<PowerUp> expired = new ArrayList<>();
        for (PowerUp p : activePowerUps) {
            if (now - p.getSpawnTime() >= POWERUP_LIFESPAN_MS) {
                expired.add(p);
            }
        }
        if (!expired.isEmpty()){
            activePowerUps.removeAll(expired);
            for (PowerUp p : expired){
                GameState snapExpired = getSnapshot();
                listeners.forEach(l -> l.onPowerUpTriggered(snapExpired,p));
            }
        }

        if (now - lastPowerUpSpawnTime >= POWERUP_INTERVAL_MS && !possiblePowerUps.isEmpty()) {
            spawnRandomPowerUp(now);
            lastPowerUpSpawnTime = now;
        }
    }

    /**
     * Spawns a random power-up on the appropriate player's board based on the current game state and settings. This method randomly selects a player (or uses the solo player in dual-engine LAN mode) and attempts to place a power-up at a random location on their board. It checks for valid placement, ensuring that the selected cell is empty and not already occupied by another power-up. If a valid location is found, the power-up is created, added to the active list, and listeners are notified of its spawn. The method ensures that power-ups are introduced into the game in a fair and strategic manner.
     * @param now The current system time in milliseconds, used to determine the spawn time of the power-up and manage its lifespan.
     */
    private void spawnRandomPowerUp(long now ) {
        int playerNum = RANDOM.nextBoolean() ? 1 : 2;
        if (p1Lost) {playerNum = 2;}
        if (p2Lost) {playerNum = 1;}
        // Dual-engine LAN: this engine only owns its own board, so only spawn there.
        if (dualMode) {playerNum = soloPlayer;}

        Board board = (playerNum == 1) ? p1Board : p2Board;
        String[][] grid = board.getGrid();
        PowerUpType selected = possiblePowerUps.get(RANDOM.nextInt(possiblePowerUps.size()));
        int attempts = 0;
        while (attempts < 50) {
            int r = RANDOM.nextInt(board.getHeight()-3);
            int c = RANDOM.nextInt(board.getWidth());
            if (playerNum == 1 && grid[r+3][c] == null && !isPowerUpAt(playerNum, r+3, c)) {
                PowerUp p = new PowerUp(playerNum, r+3, c,now,selected,false);
                activePowerUps.add(p);
                listeners.forEach(l -> l.onPowerUpSpawned(this.getSnapshot()));
                p.draw();
                break;
            }
            else if (playerNum == 2 && grid[r][c] == null && !isPowerUpAt(playerNum, r, c)) {
                PowerUp p = new PowerUp(playerNum, r, c,now,selected,false);
                activePowerUps.add(p);
                listeners.forEach(l -> l.onPowerUpSpawned(this.getSnapshot()));
                p.draw();
                break;
            }
            attempts++;
        }
    }

    /**
     * Checks if there is an active power-up at the specified row and column for the given player. This method iterates through the list of active power-ups and compares their player number, row, and column with the provided parameters. If a matching power-up is found, it returns true; otherwise, it returns false. This check is used to ensure that power-ups are not spawned in locations that are already occupied by another power-up, maintaining fair gameplay and preventing overlap.
     * @param playerNum An integer representing the player number (1 or 2) for which the check is being performed.
     * @param row An integer representing the row index on the player's board where the check is being performed.
     * @param col An integer representing the column index on the player's board where the check is being performed.
     * @return A boolean indicating whether there is an active power-up at the specified row and column for the given player (true) or not (false).
     */
    private boolean isPowerUpAt(int playerNum, int row, int col) {
        for (PowerUp p : activePowerUps) {
            if (p.getPlayerNum() == playerNum && p.getRow() == row && p.getCol() == col) {
                return true;
            }
        }
        return false;
    }

    /**
     * Processes the input action for the specified player and block index, updating the game state accordingly. This method handles player inputs such as moving blocks left or right, rotating blocks, and dropping blocks. It checks for valid actions based on the current game state, including whether the game is over, if the player has lost, and if the block index is valid. Depending on the action, it updates the position or rotation of the active block and notifies listeners of any changes in block movement. The method ensures that player inputs are processed correctly and that the game state remains consistent.
     * @param playerNum An integer representing the player number (1 or 2) for which the input action is being processed.
     * @param action A string representing the action to be performed, such as "LEFT", "RIGHT", "ROTATE", or "DROP".
     * @param blockIndex An integer representing the index of the active block (0 or 1) for which the action is being applied. In two-block mode, this allows players to control multiple blocks simultaneously.
     */
    public synchronized void processInput(int playerNum, String action, int blockIndex) {
        if (isGameOver || isStopped) return;
        if (playerNum == 1 && p1Lost) return;
        if (playerNum == 2 && p2Lost) return;
        if (!isTwoBlockMode && blockIndex > 0) return;

        Block block = (playerNum == 1) ? p1ActiveBlocks[blockIndex] : p2ActiveBlocks[blockIndex];
        Block otherBlock = isTwoBlockMode ? ((playerNum == 1) ? p1ActiveBlocks[(blockIndex + 1) % 2] : p2ActiveBlocks[(blockIndex + 1) % 2]) : null;
        Board board = (playerNum == 1) ? p1Board : p2Board;

        if (block == null) return;

        switch (action) {
            case "LEFT" -> {
                block.moveLeft();
                if (!board.isValidPosition(block, otherBlock)) block.moveRight();
            }
            case "RIGHT" -> {
                block.moveRight();
                if (!board.isValidPosition(block, otherBlock)) block.moveLeft();
            }
            case "ROTATE" -> {
                boolean delayActive = (playerNum == 1)
                        ? System.currentTimeMillis() < p1RotationDelayExpiry
                        : System.currentTimeMillis() < p2RotationDelayExpiry;

                if (delayActive) {
                    scheduler.schedule(
                            () -> performRotation(playerNum, blockIndex),
                            ROTATION_DELAY_MS, TimeUnit.MILLISECONDS
                    );
                } else {
                    performRotation(playerNum, blockIndex);
                }
            }
            case "DROP" -> applyGravity(playerNum, blockIndex, board);
        }
        listeners.forEach(l -> l.onBlockMovement(getSnapshot(), playerNum));
    }

    /**
     * Performs the rotation of the active block for the specified player and block index, updating the game state accordingly. This method rotates the block clockwise and checks if the new position is valid on the board. If the rotation results in an invalid position (e.g., overlapping with other blocks or going out of bounds), it reverts the rotation by rotating the block counter-clockwise. After processing the rotation, it notifies all registered listeners of the block movement, allowing other parts of the application to respond to the change in game state.
     * @param playerNum An integer representing the player number (1 or 2) for which the rotation is being performed.
     * @param blockIndex An integer representing the index of the active block (0 or 1) for which the rotation is being applied. In two-block mode, this allows players to control multiple blocks simultaneously.
     */
    private synchronized void performRotation(int playerNum, int blockIndex) {
        if (isGameOver || isStopped) return;
        Block[] activeBlocks = (playerNum == 1) ? p1ActiveBlocks : p2ActiveBlocks;
        Block block = activeBlocks[blockIndex];
        if (block == null) return;
        Block other = isTwoBlockMode ? activeBlocks[(blockIndex + 1) % 2] : null;
        Board board = (playerNum == 1) ? p1Board : p2Board;

        block.rotateClockwise();
        if (!board.isValidPosition(block, other)) block.rotateCounterClockwise();

        listeners.forEach(l -> l.onBlockMovement(getSnapshot(), playerNum));
    }

    /**
     * Applies gravity to the active block for the specified player and block index, moving it down (or up if inverted) on the board. This method checks if the new position is valid and handles locking the block in place if it reaches the ground or collides with other blocks. If the block is a BombBlock, it triggers an explosion instead of locking. The method also manages line clearing, score updates, and spawning new blocks as necessary. After processing gravity, it notifies all registered listeners of any changes in block movement or game state.
     * @param playerNum An integer representing the player number (1 or 2) for which gravity is being applied.
     * @param blockIndex An integer representing the index of the active block (0 or 1) for which gravity is being applied. In two-block mode, this allows players to control multiple blocks simultaneously.
     * @param board The Board instance representing the player's game board on which gravity is being applied
     */
    private void applyGravity(int playerNum, int blockIndex, Board board) {
        Block activeBlock = (playerNum == 1) ? p1ActiveBlocks[blockIndex] : p2ActiveBlocks[blockIndex];
        Block otherBlock = isTwoBlockMode ? ((playerNum == 1) ? p1ActiveBlocks[(blockIndex + 1) % 2] : p2ActiveBlocks[(blockIndex + 1) % 2]) : null;

        if (board.isInverted()) {
            activeBlock.moveUp();
        } else {
            activeBlock.moveDown();
        }

        if (!board.isValidPosition(activeBlock, otherBlock)) {
            // FIX: Hard Lock to avoid collision at the top
            boolean hardLock = !board.isValidPosition(activeBlock, null);

            // Revert the movement to avoid overlap
            if (board.isInverted()) {
                activeBlock.moveDown();
            } else {
                activeBlock.moveUp();
            }

            // Lock the block when it hits the ground
            if (hardLock) {
                if (activeBlock instanceof BombBlock bb){
                    explode(playerNum, bb, board);
                    GameState snapExplode = getSnapshot();
                    listeners.forEach(l -> l.onBlockMovement(snapExplode,playerNum));
                }
                else {
                    board.lockBlock(activeBlock);
                    GameState snapLock = getSnapshot();
                    listeners.forEach(l ->l.lockSound());
                    listeners.forEach(l -> l.onBlockLocked(playerNum, snapLock));
                }

                int linesCleared = board.clearLines();
                if (linesCleared > 0) {
                    if (playerNum == 1) {
                        p1LinesCleared +=  linesCleared;
                        p1Score += calculateScore(linesCleared);
                        if (advancedSettings.isBoardChange() && !p1Lost && !p2Lost) {
                            if (dualMode) {
                                p1Board.expand(linesCleared);
                                final int n = linesCleared;
                                listeners.forEach(l -> l.onBoardShrinkOut(n));
                                listeners.forEach(l -> l.onBoardSizeChange(1, n, getSnapshot()));
                            } else {
                                p1Board.expand(linesCleared);
                                p2Board.shrink(linesCleared);
                                for(int i = 0; i < (isTwoBlockMode ? 2 : 1); i++) {
                                    if (p2ActiveBlocks[i] != null) p2ActiveBlocks[i].setY(p2ActiveBlocks[i].getY() - linesCleared);
                                }
                                listeners.forEach(l -> l.onBoardSizeChange(playerNum,linesCleared,getSnapshot()));
                            }
                        }
                    }
                    else {
                        p2LinesCleared +=  linesCleared;
                        p2Score += calculateScore(linesCleared);
                        if( advancedSettings.isBoardChange() && !p1Lost && !p2Lost) {
                            if (dualMode) {
                                p2Board.expand(linesCleared);
                                final int n = linesCleared;
                                listeners.forEach(l -> l.onBoardShrinkOut(n));
                                listeners.forEach(l -> l.onBoardSizeChange(2, n, getSnapshot()));
                            } else {
                                p2Board.expand(linesCleared);
                                p1Board.shrink(linesCleared);
                                for(int i = 0; i < (isTwoBlockMode ? 2 : 1); i++) {
                                    if (p1ActiveBlocks[i] != null) p1ActiveBlocks[i].setY(p1ActiveBlocks[i].getY() + linesCleared);
                                }
                                listeners.forEach(l -> l.onBoardSizeChange(playerNum,linesCleared,getSnapshot()));
                            }
                        }
                    }

                    GameState snapLines = getSnapshot();
                    final int finalLinesCleared = linesCleared;
                    listeners.forEach(l -> l.onLinesCleared(playerNum, finalLinesCleared, snapLines));

                    handleLevelProgression(playerNum);
                }
                spawnNewBlock(playerNum, blockIndex);
            }
        }
        else{
            PowerUp hit = checkPowerUpTrigger(playerNum, activeBlock);
            if (hit != null) {
                switch (hit.getType()) {
                    case PORTAL : {
                        if (dualMode) { executePortalDual(playerNum, activeBlock, blockIndex); break; }
                        executePortal(playerNum,activeBlock, blockIndex);
                        break;
                    }
                    case SELFSPEEDDOWN:{
                        if (playerNum == 1){
                            p1SlowEffects.add(10);
                            p1TickInterval = recomputeInterval(1);
                            listeners.forEach(l -> l.changeTickSpeed(playerNum, p1TickInterval));
                        }
                        else {
                            p2SlowEffects.add(10);
                            p2TickInterval = recomputeInterval(2);
                            listeners.forEach(l -> l.changeTickSpeed(playerNum, p2TickInterval));
                        }
                        break;
                    }
                    case OPPONENTSPEEDUP: {
                        if (dualMode) { listeners.forEach(l -> l.onOpponentAttack(AttackType.SPEED_UP)); break; }
                        if (playerNum == 1){
                            p2FastEffects.add(10);
                            p2TickInterval = recomputeInterval(2);
                            listeners.forEach(l -> l.changeTickSpeed(2, p2TickInterval));;
                        }
                        else {
                            p1FastEffects.add(10);
                            p1TickInterval = recomputeInterval(1);
                            listeners.forEach(l -> l.changeTickSpeed(1, p1TickInterval));
                        }
                        break;
                    }
                    case SWAPACTIVEBLOCKS: {
                        if (dualMode) { initiateSwapActive(); break; }
                        swapActiveBlocks();
                        listeners.forEach(l -> l.onBlockSwap(getSnapshot()));
                        break;
                    }
                    case OPPONENTSPEEDDOWN: {
                        if (dualMode) { listeners.forEach(l -> l.onOpponentAttack(AttackType.SPEED_DOWN)); break; }
                        if (playerNum == 1){
                            p2SlowEffects.add(10);
                            p2TickInterval = recomputeInterval(2);
                            listeners.forEach(l -> l.changeTickSpeed(2, p2TickInterval));
                        }
                        else {
                            p1SlowEffects.add(10);
                            p1TickInterval = recomputeInterval(1);
                            listeners.forEach(l -> l.changeTickSpeed(1, p1TickInterval));
                        }
                        break;
                    }
                    case SELFROTATIONDELAY: {
                        long expiry = System.currentTimeMillis() + 10_000;
                        if (playerNum == 1) p1RotationDelayExpiry = expiry;
                        else p2RotationDelayExpiry = expiry;
                        break;
                    }
                    case OPPONENTROTATIONDELAY: {
                        if (dualMode) { listeners.forEach(l -> l.onOpponentAttack(AttackType.ROTATION_DELAY)); break; }
                        long expiry = System.currentTimeMillis() + 10_000;
                        if (playerNum == 1) p2RotationDelayExpiry = expiry;
                        else p1RotationDelayExpiry = expiry;
                        break;
                    }
                    case SWAPBOARDS:{
                        if (dualMode) { initiateSwapBoards(); break; }
                        executePlayerSwap();
                        break;
                    }
                }
                GameState snapSwap = getSnapshot();
                listeners.forEach(l -> l.onPowerUpTriggered(snapSwap,hit));
            }
        }
    }

    /**
     * Swaps the active blocks between player 1 and player 2, updating their positions on the respective boards. This method exchanges the references of the active blocks for both players and performs error correction to ensure that the blocks are positioned correctly within the bounds of their respective boards. It checks if the new positions of the swapped blocks are valid and adjusts them if necessary to prevent any overlap or out-of-bounds placement. After swapping, it notifies all registered listeners of the block swap event, allowing other parts of the application to respond to the change in game state.
     */
    private void swapActiveBlocks() {
        Block[] temp = p1ActiveBlocks;
        p1ActiveBlocks = p2ActiveBlocks;
        p2ActiveBlocks = temp;

        for (int i = 0; i < 2; i++) {
            if (p1ActiveBlocks[i] != null) swapBlockErrorCorrection(p1ActiveBlocks[i].getX(), p1ActiveBlocks[i].getY(), p1ActiveBlocks[i], p1Board);
            if (p2ActiveBlocks[i] != null) swapBlockErrorCorrection(p2ActiveBlocks[i].getX(), p2ActiveBlocks[i].getY(), p2ActiveBlocks[i], p2Board);
        }
    }

    private void swapBlockErrorCorrection(int posX, int posY, Block p1ActiveBlock, Board p1Board) {
        if (posX + p1ActiveBlock.getShape().length > p1Board.getWidth()){
            posX = posX+(p1Board.getWidth() - posX - p1ActiveBlock.getShape().length);

        }
        if (posY + p1ActiveBlock.getShape().length > p1Board.getHeight()){
            posY = posY + (p1Board.getHeight() - posY - p1ActiveBlock.getShape().length);
        }
        p1ActiveBlock.setX(posX);
        p1ActiveBlock.setY(posY);
    }

    /**
     * Dual-engine PORTAL: the block leaves my board and travels to the opponent's board
     * on THEIR machine. We position it like {@link #executePortal} would for the
     * destination, emit it for the network layer to deliver, and spawn our replacement.
     */
    private void executePortalDual(int playerNum, Block activeBlock, int sourceIndex) {
        if (playerNum == 1) {
            activeBlock.setY(p1Board.getHeight() - activeBlock.getShape().length);
            activeBlock.setX(3);
        } else {
            activeBlock.setX(3);
            activeBlock.setY(0);
        }
        listeners.forEach(l -> l.onPortalOut(activeBlock));
        spawnNewBlock(playerNum, sourceIndex);
    }

    /** Dual-engine PORTAL receiver: queue an incoming block onto my own board. */
    public synchronized void receivePortalBlock(Block block) {
        if (isGameOver || isStopped || soloPlayer == 0) return;
        if (soloPlayer == 1) p1NextActiveBlock.add(block);
        else p2NextActiveBlock.add(block);
    }

    // ----- Dual-engine board-change (shared boundary) -----

    /** Dual-engine: I cleared lines and expanded; shrink my own board on the receiver. */
    public synchronized void applyBoardShrink(int rows) {
        if (isGameOver || isStopped || soloPlayer == 0) return;
        if ((soloPlayer == 1) ? p1Lost : p2Lost) return;
        Board myBoard = (soloPlayer == 1) ? p1Board : p2Board;
        int safe = Math.min(rows, myBoard.getHeight() - 1);
        if (safe <= 0) return;
        myBoard.shrink(safe);
        Block[] myActive = (soloPlayer == 1) ? p1ActiveBlocks : p2ActiveBlocks;
        int dy = (soloPlayer == 1) ? -safe : safe;
        for (int i = 0; i < (isTwoBlockMode ? 2 : 1); i++) {
            if (myActive[i] != null) myActive[i].setY(myActive[i].getY() + dy);
        }
        listeners.forEach(l -> l.onBoardSizeChange(soloPlayer, safe, getSnapshot()));
    }

    // ----- Dual-engine SWAPACTIVEBLOCKS (two-way handshake) -----

    /**
     * Clones the active blocks of the current player for snapshot purposes. This method creates a deep copy of the active blocks for the player specified by the soloPlayer variable, allowing for safe manipulation and transmission of block data without affecting the original blocks. The cloned blocks are returned as an array, with each block being cloned using the cloneForSnapshot method to ensure that all relevant properties are preserved. This is particularly useful in dual-engine LAN mode, where active blocks may need to be shared or swapped between players.
     * @return An array of Block objects representing the cloned active blocks of the current player.
     */
    private Block[] cloneMyActive() {
        Block[] mine = (soloPlayer == 1) ? p1ActiveBlocks : p2ActiveBlocks;
        return new Block[]{ mine[0] != null ? mine[0].cloneForSnapshot() : null,
                            mine[1] != null ? mine[1].cloneForSnapshot() : null };
    }

    /**
     * Sets the active blocks of the current player to the incoming blocks received from the opponent. This method updates the active blocks for the player specified by the soloPlayer variable, replacing them with the provided incoming blocks. It also performs error correction to ensure that the new active blocks are positioned correctly within the bounds of the player's board, preventing any overlap or out-of-bounds placement. After updating the active blocks, it notifies all registered listeners of the block swap event, allowing other parts of the application to respond to the change in game state.
     * @param incoming An array of Block objects representing the incoming active blocks received from the opponent. These blocks will replace the current player's active blocks, and error correction will be applied to ensure valid positioning on the board.
     */
    private void setSwappedActive(Block[] incoming) {
        Board myBoard = (soloPlayer == 1) ? p1Board : p2Board;
        Block[] myActive = (soloPlayer == 1) ? p1ActiveBlocks : p2ActiveBlocks;
        for (int i = 0; i < 2; i++) {
            myActive[i] = incoming[i];
            if (myActive[i] != null) swapBlockErrorCorrection(myActive[i].getX(), myActive[i].getY(), myActive[i], myBoard);
        }
    }

    /** I hit a swap-active power-up: offer my active blocks to the opponent. */
    private void initiateSwapActive() {
        Block[] copy = cloneMyActive();
        listeners.forEach(l -> l.onSwapActiveRequest(copy));
    }

    /**
     * Receives a swap-active request from the opponent, updating the current player's active blocks with the incoming blocks. This method is called when the opponent initiates a swap-active action, and it replaces the current player's active blocks with the provided senderBlocks. It also creates a copy of the current player's active blocks before the swap for snapshot purposes. After updating the active blocks, it notifies all registered listeners of the swap-active response and block swap event, allowing other parts of the application to respond to the change in game state.
     * @param senderBlocks An array of Block objects representing the active blocks sent by the opponent. These blocks will replace the current player's active blocks, and a copy of the current player's active blocks will be created for snapshot purposes.
     */
    public synchronized void receiveSwapActiveRequest(Block[] senderBlocks) {
        if (isGameOver || isStopped || soloPlayer == 0) return;
        Block[] myCopy = cloneMyActive();
        setSwappedActive(senderBlocks);
        listeners.forEach(l -> l.onSwapActiveResponse(myCopy));
        listeners.forEach(l -> l.onBlockSwap(getSnapshot()));
    }

    public synchronized void receiveSwapActiveResponse(Block[] oppBlocks) {
        if (isGameOver || isStopped || soloPlayer == 0) return;
        setSwappedActive(oppBlocks);
        listeners.forEach(l -> l.onBlockSwap(getSnapshot()));
    }

    // ----- Dual-engine SWAPBOARDS (two-way handshake) -----

    /** I hit a swap-boards power-up: offer my whole board + active blocks to the opponent. */
    private void initiateSwapBoards() {
        Board myBoard = (soloPlayer == 1) ? p1Board : p2Board;
        listeners.forEach(l -> l.onSwapBoardsRequest(deepCopy(myBoard.getGrid()), cloneMyActive()));
    }

    /**
     * Receives a swap-boards request from the opponent, updating the current player's board and active blocks with the incoming data. This method is called when the opponent initiates a swap-boards action, and it replaces the current player's board grid and active blocks with the provided senderGrid and senderBlocks. It also creates a copy of the current player's board grid and active blocks before the swap for snapshot purposes. After updating the board and active blocks, it notifies all registered listeners of the swap-boards response, allowing other parts of the application to respond to the change in game state.
     * @param senderGrid A 2D array of strings representing the grid of the opponent's board. This grid will replace the current player's board grid, and a copy of the current player's board grid will be created for snapshot purposes.
     * @param senderBlocks An array of Block objects representing the active blocks sent by the opponent. These blocks will replace the current player's active blocks, and a copy of the current player's active blocks will be created for snapshot purposes. 
     */
    public synchronized void receiveSwapBoardsRequest(String[][] senderGrid, Block[] senderBlocks) {
        if (isGameOver || isStopped || soloPlayer == 0) return;
        Board myBoard = (soloPlayer == 1) ? p1Board : p2Board;
        String[][] myGrid = deepCopy(myBoard.getGrid());
        Block[] myCopy = cloneMyActive();
        applySwappedBoard(senderGrid, senderBlocks);
        listeners.forEach(l -> l.onSwapBoardsResponse(myGrid, myCopy));
    }

    /**
     * Receives a swap-boards response from the opponent, updating the current player's board and active blocks with the incoming data. This method is called when the opponent responds to a swap-boards request, and it replaces the current player's board grid and active blocks with the provided oppGrid and oppBlocks. After updating the board and active blocks, it notifies all registered listeners of the block swap event, allowing other parts of the application to respond to the change in game state.
     * @param oppGrid A 2D array of strings representing the grid of the opponent's board. This grid will replace the current player's board grid.
     * @param oppBlocks An array of Block objects representing the active blocks sent by the opponent. These blocks will replace the current player's active blocks.
     */
    public synchronized void receiveSwapBoardsResponse(String[][] oppGrid, Block[] oppBlocks) {
        if (isGameOver || isStopped || soloPlayer == 0) return;
        applySwappedBoard(oppGrid, oppBlocks);
    }

    /** Overwrite my board (rotated to my orientation) with the opponent's, swap in their blocks. */
    private void applySwappedBoard(String[][] incomingGrid, Block[] incomingBlocks) {
        Board myBoard = (soloPlayer == 1) ? p1Board : p2Board;
        myBoard.overwriteGrid(rotateGrid180(incomingGrid));
        Block[] myActive = (soloPlayer == 1) ? p1ActiveBlocks : p2ActiveBlocks;
        for (int i = 0; i < 2; i++) {
            myActive[i] = incomingBlocks[i];
            if (myActive[i] != null) rotateBlock180(myBoard, myActive[i]);
        }
        activePowerUps.removeIf(p -> p.getPlayerNum() == soloPlayer);
        listeners.forEach(l -> l.clearPowerUps());
        listeners.forEach(l -> l.onBlockSwap(getSnapshot()));
        checkValidPosition(soloPlayer, 0);
        if (isTwoBlockMode) checkValidPosition(soloPlayer, 1);
    }

    /**
     * Executes the portal power-up effect for the specified player, transferring the active block to the opponent's board. This method adjusts the position of the active block based on the player's number, ensuring that it is placed correctly on the opponent's board. After positioning the block, it adds it to the opponent's next active block queue and spawns a new block for the current player. The method allows players to strategically use the portal power-up to disrupt their opponent's gameplay by sending blocks across boards.
     * @param playerNum An integer representing the player number (1 or 2) for which the portal effect is being executed.
     * @param activeBlock The Block object representing the active block that is being transferred to the opponent's board.
     * @param sourceIndex An integer representing the index of the active block (0 or 1) for which the portal effect is being applied. In two-block mode, this allows players to control multiple blocks simultaneously.
     */
    private void executePortal(int playerNum, Block activeBlock, int sourceIndex) {
        if (playerNum == 1) {
            activeBlock.setY(p1Board.getHeight() - activeBlock.getShape().length);
            activeBlock.setX(3);
            p2NextActiveBlock.add(activeBlock);
            spawnNewBlock(1, sourceIndex);
        } else {
            activeBlock.setX(3);
            activeBlock.setY(0);
            p1NextActiveBlock.add(activeBlock);
            spawnNewBlock(2, sourceIndex);
        }
    }

    /**
     * Handles the explosion of a BombBlock on the specified player's board, clearing blocks in a defined pattern based on the bomb type. This method checks the type of the BombBlock (RADIUS or CLEAR_BELOW) and applies the corresponding explosion effect. For RADIUS bombs, it clears blocks in a square area around the bomb's position, while for CLEAR_BELOW bombs, it clears all blocks below the bomb's position. After processing the explosion, it notifies all registered listeners of the bomb effect, allowing other parts of the application to respond to the change in game state.
     * @param playerNum An integer representing the player number (1 or 2) for which the bomb explosion is being processed.
     * @param bb The BombBlock object representing the bomb that is exploding on the board.
     * @param board The Board instance representing the player's game board on which the bomb explosion is being applied.
     */
    private void explode(int playerNum, BombBlock bb, Board board) {
        BombType type = bb.getType();
        int posX = bb.getX();
        int posY = bb.getY();
        String[][] grid = board.getGrid();
        if (type == BombType.RADIUS) {
            for (int i = 0; i <= 3; i++) {
                for (int j = 0; j <= 3; j++) {
                    int posXminus = posX - i;
                    int posXplus  = posX + i;
                    int posYminus = posY - j;
                    int posYplus  = posY + j;

                    if (posXminus >= 0 && posYminus >= 0)
                        grid[posYminus][posXminus] = null;
                    if (posXminus >= 0 && posYplus < grid.length)
                        grid[posYplus][posXminus] = null;
                    if (posXplus < grid[0].length && posYplus < grid.length)
                        grid[posYplus][posXplus] = null;
                    if (posXplus < grid[0].length && posYminus >= 0)
                        grid[posYminus][posXplus] = null;
                }
            }
            listeners.forEach(TetrisEventListener::radialBomb);
        }
        if (type == BombType.CLEAR_BELOW){
            int i = posY;
            while (playerNum == 1 ? i < grid.length : i >= 0) {
                grid[i][posX] = null;
                if (playerNum == 1) i++; else i--;
            }
            listeners.forEach(TetrisEventListener::columnBomb);
        }
    }

    /**
     * Checks if the active block of the specified player has triggered a power-up on the board. This method iterates through the shape of the active block and checks if any part of it overlaps with an active power-up on the player's board. If a power-up is found at the same position as the block, it is removed from the list of active power-ups, and the method returns the triggered power-up. This check allows for the activation of power-ups when a player successfully positions their block over a power-up on the board.
     * @param playerNum An integer representing the player number (1 or 2) for which the power-up trigger check is being performed.
     * @param block The Block object representing the active block that is being checked for power-up triggers on the player's board.
     * @return The PowerUp object that was triggered by the active block, or null if no power-up was triggered. If a power-up is triggered, it is removed from the list of active power-ups to prevent further activation.
     */
    private PowerUp checkPowerUpTrigger(int playerNum, Block block) {
        PowerUp hit = null;
        boolean[][] shape = block.getShape();
        int bx = block.getX();
        int by = block.getY();

        List<PowerUp> toRemove = new ArrayList<>();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    int boardX = bx + col;
                    int boardY = by + row;
                    for (PowerUp p : activePowerUps) {
                        if (p.getPlayerNum() == playerNum && p.getCol() == boardX && p.getRow() == boardY) {
                            hit = p;
                            toRemove.add(p);
                        }
                    }
                }
            }
        }
        activePowerUps.removeAll(toRemove);
        return hit;
    }

    /**
     * Swaps the entire game boards and active blocks between player 1 and player 2, updating their positions and orientations accordingly. This method creates a deep copy of player 1's board grid, overwrites player 1's board with a rotated version of player 2's board, and vice versa. It also adjusts the heights of the boards if they differ, ensuring that the active blocks are repositioned correctly. The method handles the swapping of active blocks, rotating them to match the new board orientation, and checks for any invalid positions that may result from the swap. Finally, it notifies all registered listeners of the board size changes and block movements, allowing other parts of the application to respond to the change in game state.
     */
    private void executePlayerSwap() {
        String[][] tempGrid = deepCopy(p1Board.getGrid());
        p1Board.overwriteGrid(rotateGrid180(p2Board.getGrid()));
        p2Board.overwriteGrid(rotateGrid180(tempGrid));

        int change = Math.abs(p1Board.getHeight()-p2Board.getHeight());
        if (change > 0 && p1Board.getHeight() > p2Board.getHeight()) {
            listeners.forEach(l -> l.onBoardSizeChange(1,change,getSnapshot()));
            for(int i=0; i<2; i++) {
                if(p2ActiveBlocks[i] != null) p2ActiveBlocks[i].setY(p2ActiveBlocks[i].getY()+change);
            }
        }
        else if(change > 0 && p2Board.getHeight() > p1Board.getHeight()) {
            listeners.forEach(l -> l.onBoardSizeChange(2,change,getSnapshot()));
            for(int i=0; i<2; i++) {
                if(p1ActiveBlocks[i] != null) p1ActiveBlocks[i].setY(p1ActiveBlocks[i].getY()-change);
            }
        }

        Block[] tempBlocks = p1ActiveBlocks;
        p1ActiveBlocks = p2ActiveBlocks;
        p2ActiveBlocks = tempBlocks;

        for (int i=0; i<2; i++) {
            if(p1ActiveBlocks[i] != null) rotateBlock180(p1Board,p1ActiveBlocks[i]);
            if(p2ActiveBlocks[i] != null) rotateBlock180(p2Board,p2ActiveBlocks[i]);
        }

        boolean swapped = false;
        if (p1Lost && !p2Lost) {
            p1Lost = false;
            p2Lost = true;
            swapped = true;
        }
        if (!p1Lost && p2Lost && !swapped) {
            p1Lost = true;
            p2Lost = false;
        }

        List<PowerUp> swappedPowerUps = new ArrayList<>();
        for (PowerUp p : activePowerUps) {
            PowerUp temp = null;
            if (p.getPlayerNum() == 1){
                temp = new PowerUp(1,Math.abs(p1Board.getHeight()-p.getRow()), Math.abs(p1Board.getWidth()-p.getCol()),p.getSpawnTime(),p.getType(),false);
            }
            else {
                temp = new PowerUp(1,Math.abs(p2Board.getHeight()-p.getRow()), Math.abs(p2Board.getWidth()-p.getCol()),p.getSpawnTime(),p.getType(),false);
            }
            swappedPowerUps.add(temp);
        }
        activePowerUps.clear();
        listeners.forEach( l -> l.clearPowerUps());
        activePowerUps.addAll(swappedPowerUps);
        listeners.forEach(l -> {
            l.onPowerUpSpawned(getSnapshot());
            l.onBlockMovement(getSnapshot(),1);
            l.onBlockMovement(getSnapshot(),2);
        });

        for(int i=0; i<2; i++) {
            if (p1ActiveBlocks[i] != null && !p1Board.isValidPosition(p1ActiveBlocks[i], isTwoBlockMode ? p1ActiveBlocks[(i + 1) % 2] : null)) {
                p1Lost = true;
                GameState snapLost = getSnapshot();
                listeners.forEach(l -> l.onPlayerLost(1, snapLost));
            }
            if (p2ActiveBlocks[i] != null && !p2Board.isValidPosition(p2ActiveBlocks[i], isTwoBlockMode ? p2ActiveBlocks[(i + 1) % 2] : null)) {
                p2Lost = true;
                GameState snapLost = getSnapshot();
                listeners.forEach(l -> l.onPlayerLost(2, snapLost));
            }
        }
    }

    /**
     * Rotates the specified active block by 180 degrees on the given board, adjusting its position and shape accordingly. This method calculates the new position of the block based on the board's dimensions and the block's current position, ensuring that it remains within the bounds of the board after rotation. It then creates a new shape for the block by reversing both its rows and columns, effectively rotating it by 180 degrees. The updated position and shape are then set for the active block, allowing it to be displayed correctly on the board after rotation.
     * @param board The Board instance representing the game board on which the active block is located. This is used to determine the dimensions of the board for calculating the new position of the block after rotation.
     * @param activeBlock The Block object representing the active block that is being rotated. Its position and shape will be updated to reflect the 180-degree rotation on the board.
     */
    private void rotateBlock180(Board board,Block activeBlock) {
        activeBlock.setX((board.getWidth() - 1) - (activeBlock.getX() + activeBlock.getShape()[0].length - 1));
        activeBlock.setY( (board.getHeight() - 1) - (activeBlock.getY() + activeBlock.getShape().length - 1));

        boolean[][] shape = activeBlock.getShape();
        int rows = shape.length;
        int cols = shape[0].length;

        boolean[][] rotated = new boolean[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                rotated[row][col] = shape[rows-1-row][cols-1-col];
            }
        }
        activeBlock.setShape(rotated);
    }

    /**
     * Rotates a 2D grid of strings by 180 degrees, effectively flipping it both horizontally and vertically. This method creates a new grid with the same dimensions as the source grid and populates it by reversing the order of rows and columns from the original grid. The resulting rotated grid can be used for various purposes, such as displaying the board from a different perspective or preparing data for network transmission in a multiplayer game. The method ensures that the original grid remains unchanged while providing a new representation of the data.
     * @param src The source 2D array of strings representing the original grid to be rotated. This grid will be used as the basis for creating the rotated version.
     * @return A new 2D array of strings representing the grid rotated by 180 degrees. The original grid remains unchanged, and the rotated grid can be used for further processing or display.
     */
    private static String[][] rotateGrid180(String[][] src) {
        int rows = src.length;
        int cols = src[0].length;
        String[][] rotated = new String[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[r][c] = src[rows - 1 - r][cols - 1 - c];
            }
        }
        return rotated;
    }

    /**
     * Handles the level progression for the specified player based on the number of lines cleared. This method calculates the new level for the player by dividing the total lines cleared by 10 and adding 1. If the new level exceeds the current level, it updates the player's level and recalculates the tick interval for block movement based on a predefined formula. The method then notifies all registered listeners of the level change, allowing other parts of the application to respond to the player's progression in the game.
     * @param player An integer representing the player number (1 or 2) for which the level progression is being handled. The method uses this parameter to determine which player's level and lines cleared to evaluate and update.
     */
    private void handleLevelProgression(int player) {
        int level = player == 1 ? p1Level : p2Level;
        int playerLines = player == 1 ? p1LinesCleared : p2LinesCleared;

        int newLevel = (playerLines / 10) + 1;
        if (newLevel > level) {
            long newTickInterval = (long) Math.max(
                    MIN_TICK_INTERVAL_MS,
                    INITIAL_TICK_INTERVAL_MS * Math.pow(0.85, level - 1)
            );
            if (player == 1){
                p1Level = newLevel;
                p1TickInterval = newTickInterval;
            }
            else if (player == 2){
                p2Level = newLevel;
                p2TickInterval = newTickInterval;
            }

            GameState snapLevel = getSnapshot();
            listeners.forEach(l -> l.onLevelChanged(newTickInterval, snapLevel, player));
        }
    }

    /**
     * Spawns a new block for the specified player at the given index, either as a regular block or a bomb block based on the bomb chance. This method checks if a bomb should be spawned and selects a random bomb type from the list of possible bombs. If a bomb is spawned, it is positioned appropriately on the board to prevent collisions in two-block mode. If a regular block is spawned, it is taken from the player's block queue or generated if the queue is empty. The method ensures that the new block is placed in a valid position on the board and checks for any potential collisions with existing blocks. After spawning the new block, it notifies all registered listeners of the change in game state.
     * @param playerNum An integer representing the player number (1 or 2) for which the new block is being spawned. This parameter determines which player's block queue and board are used for spawning the block.
     * @param index An integer representing the index of the active block (0 or 1) for which the new block is being spawned. In two-block mode, this allows players to control multiple blocks simultaneously.
     */
    private void spawnNewBlock(int playerNum, int index) {
        boolean bomb = !possibleBombs.isEmpty() && RANDOM.nextInt(1, 100) > 100-bombChance;
        if (bomb){
            BombType selectedType = possibleBombs.get(RANDOM.nextInt(0, possibleBombs.size()));
            BombBlock newBombBlock = new BombBlock(selectedType);

            // Prevent collision in two block mode
            if (isTwoBlockMode) {
                newBombBlock.setX(index == 0 ? 1 : 6);
            } else {
                newBombBlock.setX(3); // Middle spawn
            }

            if (playerNum == 1){
                p1ActiveBlocks[index] = newBombBlock;
                checkValidPosition(playerNum, index);
            } else {
                while (newBombBlock.getY() < p2Board.getHeight() - newBombBlock.getShape()[0].length) {
                    newBombBlock.moveDown();
                }
                p2ActiveBlocks[index] = newBombBlock;
                checkValidPosition(playerNum, index);
            }
        } else {
            refillBlocks(playerNum);
            Block newBlock = null;
            List<Block> queue = (playerNum == 1) ? p1NextActiveBlock : p2NextActiveBlock;
            List<TBlock> blocks = (playerNum == 1) ? p1Blocks : p2Blocks;

            if (queue.isEmpty()) {
                newBlock = blocks.getFirst().toPiece();
                blocks.removeFirst();
            } else {
                newBlock = queue.getFirst();
                if (playerNum == 2) {
                    newBlock.setY(0);
                }
                queue.removeFirst();
            }

            // Adjust spawn X to prevent collision in Two Block Mode
            if (isTwoBlockMode) {
                newBlock.setX(index == 0 ? 1 : 6);
            } else {
                newBlock.setX(3);
            }

            if (playerNum == 1) {
                p1ActiveBlocks[index] = newBlock;
                checkValidPosition(playerNum, index);
            } else {
                while (newBlock.getY() < p2Board.getHeight() - newBlock.getShape()[0].length) {
                    newBlock.moveDown();
                }
                p2ActiveBlocks[index] = newBlock;
                checkValidPosition(playerNum, index);
            }
        }
    }

    /**
     * Refills the block queue for the specified player if it is empty. This method checks the player's block queue and generates a new set of blocks if the queue is empty, ensuring that the player always has blocks available for spawning. The refill process is triggered based on the player number, with player 1 and player 2 having separate block queues. This mechanism helps maintain a continuous flow of blocks during gameplay, preventing situations where a player runs out of blocks to play with.
     * @param i An integer representing the player number (1 or 2) for which the block queue is being refilled. The method checks the corresponding block queue and generates new blocks if necessary.
     */
    private void refillBlocks(int i) {
        if (i == 1 && p1Blocks.isEmpty()) {
            p1Blocks = generateBlocks();
        } else if (i != 1 && p2Blocks.isEmpty()) {
            p2Blocks = generateBlocks();
        }
    }

    /**
     * Checks if the active block of the specified player is in a valid position on the board, considering any other active blocks in two-block mode. If the position is invalid, it marks the player as having lost and notifies all registered listeners of the loss event. This method ensures that players cannot continue playing if their active block is placed in an invalid position, maintaining the integrity of the game state. It takes into account the player's number and the index of the active block being checked, allowing for proper validation in both single and two-block modes.
     * @param playerNum An integer representing the player number (1 or 2) for which the active block position is being checked. This parameter determines which player's board and active blocks are used for validation.
     * @param index An integer representing the index of the active block (0 or 1) for which the position is being checked. In two-block mode, this allows for validation of multiple active blocks simultaneously.
     */
    private void checkValidPosition(int playerNum, int index){
        if (playerNum == 1) {
            Block other = isTwoBlockMode ? p1ActiveBlocks[(index + 1) % 2] : null;
            if (!p1Board.isValidPosition(p1ActiveBlocks[index], other)) {
                p1Lost = true;
                listeners.forEach(l -> l.onPlayerLost(1, getSnapshot()));
            }
        } else {
            Block other = isTwoBlockMode ? p2ActiveBlocks[(index + 1) % 2] : null;
            if (!p2Board.isValidPosition(p2ActiveBlocks[index], other)) {
                p2Lost = true;
                listeners.forEach(l -> l.onPlayerLost(2, getSnapshot()));
            }
        }
    }

    /**
     * Calculates the score based on the number of lines cleared in a single move. This method uses a switch expression to determine the score awarded for clearing 1 to 4 lines, with higher scores for clearing more lines at once. If no lines are cleared, the score is zero. The scoring system incentivizes players to clear multiple lines simultaneously, rewarding strategic placement and planning during gameplay.
     * @param linesCleared An integer representing the number of lines cleared in a single move. The method uses this value to determine the corresponding score based on predefined scoring rules.
     * @return An integer representing the score awarded for the specified number of lines cleared. The score is calculated according to the following rules: 100 points for 1 line, 300 points for 2 lines, 500 points for 3 lines, and 800 points for 4 lines. If no lines are cleared, the score is zero.    
     */
    private int calculateScore(int linesCleared) {
        return switch (linesCleared) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            case 4 -> 800;
            default -> 0;
        };
    }

    /**
     * Checks the win condition for the game by evaluating whether both players have lost and the game is not already over or stopped. If both players have lost, it sets the game state to over and notifies all registered listeners of the game over event, providing a snapshot of the final game state. This method ensures that the game concludes appropriately when both players are unable to continue, allowing for proper handling of end-of-game scenarios in multiplayer gameplay.
     */
    private void checkWinCondition() {
        if (!isGameOver && !isStopped && p1Lost && p2Lost) {
            isGameOver = true;
            GameState snapOver = getSnapshot();
            listeners.forEach(l -> l.onGameOver(snapOver));
        }
    }

    /**
     * Creates a snapshot of the current game state, including the grids of both players' boards, their active blocks, scores, levels, names, loss status, game over status, active power-ups, and whether two-block mode is enabled. This method generates a deep copy of the relevant data to ensure that the snapshot accurately reflects the current state of the game without being affected by subsequent changes. The snapshot can be used for various purposes, such as saving the game state, transmitting it over a network, or displaying it in a user interface.
     * @return A GameState object representing the current state of the game, including all relevant information about both players and the game environment. The snapshot provides a comprehensive view of the game at the moment it is created, allowing for accurate representation and analysis of the game state.
     */
    public synchronized GameState getSnapshot() {
        Block[] p1Clones = new Block[2];
        Block[] p2Clones = new Block[2];
        if (p1ActiveBlocks[0] != null) p1Clones[0] = p1ActiveBlocks[0].cloneForSnapshot();
        if (p1ActiveBlocks[1] != null) p1Clones[1] = p1ActiveBlocks[1].cloneForSnapshot();
        if (p2ActiveBlocks[0] != null) p2Clones[0] = p2ActiveBlocks[0].cloneForSnapshot();
        if (p2ActiveBlocks[1] != null) p2Clones[1] = p2ActiveBlocks[1].cloneForSnapshot();

        return new GameState(
                deepCopy(p1Board.getGrid()), deepCopy(p2Board.getGrid()),
                p1Clones, p2Clones,
                p1Score, p2Score, p1Level, p2Level, p1Name, p2Name, p1Lost, p2Lost, isGameOver, new ArrayList<>(activePowerUps), isTwoBlockMode
        );
    }

    /**
     * Creates a deep copy of a 2D array of strings, ensuring that the original array remains unchanged while providing a new representation of the data. This method iterates through each row of the source array and clones it, creating a new 2D array with the same dimensions and content as the original. The deep copy allows for safe manipulation of the copied data without affecting the original array, which is particularly useful in scenarios where the game state needs to be preserved or transmitted without risk of unintended modifications.
     * @param src The source 2D array of strings to be copied. This array serves as the basis for creating the new deep copy, and its content will be replicated in the resulting array.
     * @return A new 2D array of strings that is a deep copy of the source array. The returned array has the same dimensions and content as the original, allowing for independent manipulation without affecting the source data.
     */
    private static String[][] deepCopy(String[][] src) {
        String[][] dst = new String[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i].clone();
        }
        return dst;
    }

    /**
     * Represents the state of the game at a specific point in time, encapsulating the grids of both players' boards, their active blocks, scores, levels, names, loss status, game over status, active power-ups, and whether two-block mode is enabled. This record provides a structured way to capture and transmit the current state of the game, allowing for easy access to relevant information for both players and the overall game environment. The GameState record is serializable, enabling it to be saved or transmitted as needed.
     */
    public record GameState(
            String[][] p1Grid,
            String[][] p2Grid,
            Block[]    p1ActiveBlocks,
            Block[]    p2ActiveBlocks,
            int        p1Score,
            int        p2Score,
            int        p1Level,
            int        p2Level,
            String     p1Name,
            String     p2Name,
            boolean    p1Lost,
            boolean    p2Lost,
            boolean    gameOver,
            List<PowerUp> powerUps,
            boolean    isTwoBlockMode
    ) implements Serializable {}

    /**
     * Resets the game state to its initial configuration, clearing both players' boards, resetting scores and levels, and preparing for a new game session. This method clears the grids of both players' boards, resets their scores and levels to the starting values, and sets the loss status and game over flag to false. It also clears any active power-ups and resets the tick intervals for block movement. After resetting the game state, it spawns new blocks for both players and notifies all registered listeners of the reset event, allowing other parts of the application to respond to the change in game state.
     */
    public synchronized void reset() {
        p1Board.clear();
        p2Board.clear();
        p1Score = 0;
        p2Score = 0;
        p1Level = 1;
        p2Level = 1;
        p1Lost = false;
        p2Lost = false;
        isGameOver = false;

        p1LinesCleared = 0;
        p2LinesCleared = 0;

        p1TickInterval = INITIAL_TICK_INTERVAL_MS;
        p2TickInterval = INITIAL_TICK_INTERVAL_MS;

        activePowerUps.clear();
        lastPowerUpSpawnTime = System.currentTimeMillis();

        spawnNewBlock(1, 0);
        spawnNewBlock(2, 0);

        if (isTwoBlockMode) {
            spawnNewBlock(1, 1);
            spawnNewBlock(2, 1);
        }

        GameState snapReset = getSnapshot();
        listeners.forEach(l -> l.onReset(snapReset));
    }
}
