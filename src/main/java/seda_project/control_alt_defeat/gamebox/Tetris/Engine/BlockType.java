package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import javafx.scene.paint.Color;
public enum BlockType implements TBlock {

        /**
        * Represents the I-shaped Tetris block, which is a straight line of four blocks. It is colored AQUA and has a vertical orientation in its default shape. 
        */    
    I(Color.AQUA, new boolean[][] {
            { false, false, true, false },
            { false, false, true, false },
            { false, false, true, false },
            { false, false, true, false }
    }),
        /**
         * Represents the O-shaped Tetris block, which is a square block consisting of four blocks arranged in a 2x2 grid. It is colored RED and has a fixed shape that does not change with rotation.
         */
    O(Color.RED, new boolean[][] {
            { true, true },
            { true, true }
    }),
        /**
         * Represents the T-shaped Tetris block, which consists of three blocks in a horizontal line with one block centered above them. It is colored BLUE and has a default orientation that resembles the letter "T".
         */
    T(Color.GREEN, new boolean[][] {
            { true, true,  true },
            { false, true,  false },
            {false,false,false}
    }),
        /**
         * Represents the L-shaped Tetris block, which consists of three blocks in a vertical line with one block extending to the right at the bottom. It is colored ORANGE and has a default orientation that resembles the letter "L".
         */
    L(Color.ORANGE, new boolean[][] {
            { true,  false, false },
            { true,  false, false },
            { true,  true,  false }
    }),
        /**
         * Represents the inverted L-shaped Tetris block, which consists of three blocks in a vertical line with one block extending to the left at the bottom. It is colored PINK and has a default orientation that resembles an inverted letter "L".
         */
    L_INV(Color.PINK, new boolean[][] {
            { false, false, true },
            { false, false, true },
            { false, true,  true }
    }),
        /**
         * Represents the Z-shaped Tetris block, which consists of two blocks in a horizontal line with two blocks extending diagonally down to the right. It is colored PURPLE and has a default orientation that resembles the letter "Z".
         */
    Z(Color.PURPLE, new boolean[][] {
            { false, false, false },
            { true,  true,  false },
            { false, true,  true  }
    }),
        /**
         * Represents the inverted Z-shaped Tetris block, which consists of two blocks in a horizontal line with two blocks extending diagonally down to the left. It is colored YELLOW and has a default orientation that resembles an inverted letter "Z".
         */
    Z_INV(Color.YELLOW, new boolean[][] {
            { false, false, false },
            { false, true,  true  },
            { true,  true,  false }
    });
    
    public final Color color;
    public final boolean[][] shape;

    /**
     * Constructs a BlockType enum constant with the specified color and shape. Each BlockType represents a unique Tetris block with its associated color and shape configuration.
     */
    BlockType(Color color, boolean[][] shape) {
        this.color = color;
        this.shape = shape;
    }
    
    /**
     * Creates a list of standard Tetris pieces based on the predefined BlockType enum constants. This method generates a new Block instance for each BlockType, allowing for easy access to the standard Tetris blocks in the game.
     * @return An ArrayList containing Block instances for each standard Tetris piece.
     */
    public static ArrayList<Block> createStandardPieces() {
        return Arrays.stream(values())
                .map(BlockType::toPiece)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns the shape of the Tetris block as a 2D boolean array, where true indicates the presence of a block and false indicates an empty space. This method is part of the TBlock interface implementation and provides access to the block's shape for game logic and rendering purposes.
     * @return A 2D boolean array representing the shape of the Tetris block.
     */
    @Override
    public boolean[][] getShape() {
        return shape;
    }

    /**
     * Returns the color of the Tetris block as a JavaFX Color object. This method is part of the TBlock interface implementation and provides access to the block's color for rendering purposes in the game.
     * @return A JavaFX Color object representing the color of the Tetris block.
     */
    @Override public Color getColor() {
        return color;
    }

    /**
     * Creates a new Block instance based on the current BlockType's shape and color. This method is part of the TBlock interface implementation and allows for the creation of a Block object that can be used in the game, representing the specific Tetris piece defined by the BlockType.
     * @return A new Block instance with the shape and color of the current BlockType.
     */
    @Override
    public Block toPiece() {
        return new Block(shape, color);
    }
}
