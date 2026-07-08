package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import javafx.scene.paint.Color;

public class BlockRegistry {
    private static final Random RANDOM = new Random();

    private final List<TBlock> pieces = new ArrayList<>();

    private Stack<TBlock> player1Pieces = new Stack<>();
    private Stack<TBlock> player2Pieces = new Stack<>();

    private static BlockRegistry instance;

    /**
     * Returns the singleton instance of the BlockRegistry. If the instance does not exist, it creates a new one. This method ensures that only one instance of BlockRegistry is used throughout the application, providing a centralized registry for Tetris blocks.
     * @return The singleton instance of BlockRegistry.
     */
    public static BlockRegistry getInstance() {
        if (instance == null) instance = new BlockRegistry();
        return instance;
    }

    /**
     * Private constructor for the BlockRegistry singleton. It initializes the registry with all predefined Tetris block types by adding them to the pieces list. This constructor is called only once when the singleton instance is created.
     */
    private BlockRegistry() {
        pieces.addAll(Arrays.asList(BlockType.values()));
    }

    /**
     * Adds a new custom block to the registry. The custom block is created with the specified shape and color, and it is added to the list of pieces. This method allows users to create and register their own custom Tetris blocks.
     * @param shape A 2D boolean array representing the shape of the custom block, where true indicates the presence of a block and false indicates an empty space.
     * @param color The JavaFX Color object representing the color of the custom block.
     */
    public void addCustomPiece(boolean[][] shape, Color color){
        pieces.add(new CustomBlock(shape,color));
    }

    /**
     * Updates an existing custom block in the registry with a new shape and color. The method finds the position of the specified custom block in the pieces list, and if found, it replaces it with a new CustomBlock instance created with the provided shape and color. If the custom block is not found in the registry, an IllegalArgumentException is thrown.
     * @param shape A 2D boolean array representing the new shape of the custom block, where true indicates the presence of a block and false indicates an empty space.
     * @param color The JavaFX Color object representing the new color of the custom block.
     * @param customBlock The existing CustomBlock instance to be updated in the registry.
     */
    public void updateCustomPiece(boolean[][] shape, Color color, CustomBlock customBlock){
        int position = pieces.indexOf(customBlock);
        if (position == -1 ) throw new IllegalArgumentException("Piece not found in registry");
        pieces.set(position, new CustomBlock(shape, color));
    }

    /**
     * Removes a custom block from the registry. The method takes a CustomBlock instance as a parameter and removes it from the pieces list. This allows users to delete custom blocks they have previously added to the registry.
     * @param customBlock The CustomBlock instance to be removed from the registry.
     */
    public void removeCustomPiece(CustomBlock customBlock){
        pieces.remove(customBlock);
    }

    /**
     * Returns a list of all Tetris blocks currently registered in the BlockRegistry, including both predefined and custom blocks. This method provides access to the complete set of available blocks for use in the game.
     * @return A list of all TBlock instances in the registry.
     */
    public List<TBlock> getAllPieces(){
        return pieces;
    }

    /**
     * Returns a list of all custom blocks currently registered in the BlockRegistry. This method filters the pieces list to include only instances of CustomBlock, allowing users to retrieve their custom-created blocks separately from the predefined block types.
     * @return A list of all CustomBlock instances in the registry.
     */
    public List<CustomBlock> getCustomPieces() {
        return pieces.stream()
                .filter(p -> p instanceof CustomBlock)
                .map(p -> (CustomBlock) p)
                .toList();
    }
}
