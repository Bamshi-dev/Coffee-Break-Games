package seda_project.control_alt_defeat.gamebox.Memory.engine;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

// this part is AI generated
public class SymbolLoader {

    private static final String SYMBOL_PATH = "src/main/resources/Images/Memory";
    private static final String CARD_BACK_PATH = SYMBOL_PATH + "/backface.png";
    private static final Map<Integer, BufferedImage> symbolCache = new HashMap<>();
    private static BufferedImage cardBackCache = null;

    /**
     * Returns the BufferedImage corresponding to the given symbol ID. If the image is already cached, it retrieves it from the cache; otherwise, it loads the image from the resource path and caches it for future use. The symbol images are expected to be located in the "src/main/resources/Images/Memory" directory with filenames corresponding to their symbol IDs (e.g., "1.png", "2.png", etc.).
     * @param symbolId The ID of the symbol for which the image is requested. It should correspond to a valid image file in the resource path.
     * @return A BufferedImage representing the symbol image for the given ID, or null if the image could not be loaded. The method handles caching to improve performance by avoiding repeated loading of the same image.
     */
    public static BufferedImage getSymbolImage(int symbolId) {
        if (symbolCache.containsKey(symbolId)) {
            return symbolCache.get(symbolId);
        }

        String resourceName = SYMBOL_PATH + symbolId + ".png";
        BufferedImage img = loadImage(resourceName);
        if (img != null) {
            symbolCache.put(symbolId, img);
        }
        return img;
    }

    /**
     * Returns the BufferedImage representing the back face of a card. If the image is already cached, it retrieves it from the cache; otherwise, it loads the image from the resource path and caches it for future use. The back face image is expected to be located in the "src/main/resources/Images/Memory" directory with the filename "backface.png".
     * @return A BufferedImage representing the back face of a card, or null if the image could not be loaded. The method handles caching to improve performance by avoiding repeated loading of the same image.
     */
    public static BufferedImage getCardBackImage() {
        if (cardBackCache == null) {
            cardBackCache = loadImage(CARD_BACK_PATH);
        }
        return cardBackCache;
    }

    /**
     * Loads an image from the specified resource path and returns it as a BufferedImage. If the image cannot be found or loaded, it returns null. This method is used internally by the SymbolLoader class to load symbol and card back images from the resource directory.
     * @param path The resource path of the image to be loaded. It should correspond to a valid image file in the resource directory.
     * @return A BufferedImage representing the loaded image, or null if the image could not be found or loaded. The method handles exceptions and prints error messages to the standard error stream for debugging purposes.
     */
    private static BufferedImage loadImage(String path) {
        try (InputStream is = SymbolLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Resource not found: " + path);
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Error loading image at " + path + ": " + e.getMessage());
            return null;
        }
    }
}
