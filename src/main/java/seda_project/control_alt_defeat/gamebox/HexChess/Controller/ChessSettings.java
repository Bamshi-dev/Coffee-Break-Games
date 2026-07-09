package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.PieceSettings;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.Toast;

public class ChessSettings extends Controller implements Initializable {

    PieceSettings settings = PieceSettings.getInstance();

    @FXML
    protected VBox header;

    @FXML
    protected StackPane stackPane;

    @FXML
    private ImageView p1Pawn, p1Rook, p1Knight, p1Bishop, p1Queen, p1King, p1Duck;
    @FXML
    private ImageView p2Pawn, p2Rook, p2Knight, p2Bishop, p2Queen, p2King, p2Duck;

    @FXML
    private ColorPicker p1ColorPicker, p2ColorPicker, darkTilesColorPicker, normalTilesColorPicker, lightTilesColorPicker;

    @FXML
    private Polygon darkPolygon, normalPolygon, lightPolygon;

    private Color p1Color = Color.WHITE;
    private Color p2Color = Color.BLACK;
    private Color darkTileColor = Color.web("#5C4033");
    private Color normalTileColor = Color.web("#8B5A2B");
    private Color lightTileColor = Color.web("#C19A6B");

    private final Color p1DefaultColor = Color.WHITE;
    private final Color p2DefaultColor = Color.BLACK;
    private final Color darkTileDefaultColor = Color.web("#5C4033");
    private final Color normalTileDefaultColor = Color.web("#8B5A2B");
    private final Color lightTileDefaultColor = Color.web("#C19A6B");


    private List<ImageView> p1Pieces;
    private List<ImageView> p2Pieces;
    private List<Image> p1OriginalImages;
    private List<Image> p2OriginalImages;


    /**
     * Handles the action of returning to the previous scene in the ChessSettings menu. It plays a button sound effect
     * and navigates back to the previous scene, allowing the user to exit the settings menu without saving changes.
     */
    @FXML
    protected void onBackAction() {
        sC.play("button");
        c.backScene(header,vS);
    }

    /**
     * Handles the action of opening the board designer in the HexChess menu. It plays a button sound effect,
     * transitions to the BoardDesigner scene, and disables the "Use" button in the designer, allowing the user to
     * create or edit custom board designs without immediately applying them.
     */
    @FXML
    protected void onSaveAction() {
        sC.play("button");
        settings.setP1Color(p1Color);
        settings.setP2Color(p2Color);
        settings.setP1Pieces(p1Pieces);
        settings.setP2Pieces(p2Pieces);
        settings.setDarkTiles(darkTileColor);
        settings.setNormalTiles(normalTileColor);
        settings.setLightTiles(lightTileColor);

        String toastMsg = "Saved Settings";
        Toast.makeText(stackPane, toastMsg);
    }

    /**
     * Handles the action of resetting the chess settings to their default values. It plays a button sound effect,
     * restores the original images for both players' pieces, resets the colors for both players and the tile colors
     * to their default values, and updates the ColorPickers and preview polygons accordingly. This allows the user
     * to quickly revert any changes made in the settings menu back to the original configuration.
     */
    @FXML
    protected void onDefaultAction() {
        sC.play("button");
        for (int i = 0; i < p1Pieces.size(); i++) {
            p1Pieces.get(i).setImage(p1OriginalImages.get(i));
        }
        for (int i = 0; i < p2Pieces.size(); i++) {
            p2Pieces.get(i).setImage(p2OriginalImages.get(i));
        }
        p1Color = p1DefaultColor;
        p2Color = p2DefaultColor;

        p1ColorPicker.setValue(p1Color);
        p2ColorPicker.setValue(p2Color);

        darkTileColor = darkTileDefaultColor;
        normalTileColor = normalTileDefaultColor;
        lightTileColor = lightTileDefaultColor;

        darkTilesColorPicker.setValue(darkTileColor);
        normalTilesColorPicker.setValue(normalTileColor);
        lightTilesColorPicker.setValue(lightTileColor);

        darkPolygon.setFill(darkTileColor);
        normalPolygon.setFill(normalTileColor);
        lightPolygon.setFill(lightTileColor);
    }

    /**
     * Triggers the recoloring of Player 1's pieces based on the selected color. It retrieves the color from the
     * ColorPicker, calls the redraw method to apply the new color to Player 1's pieces, and updates the p1Color
     * variable to reflect the new selection.
     */
    @FXML
    protected void onP1ColorAction(){
        Color c = p1ColorPicker.getValue();
        redraw(c, p1Pieces,p1OriginalImages, p1DefaultColor);
        p1Color = c;
    }

    /**
     * Triggers the recoloring of Player 2's pieces based on the selected color. It retrieves the color from the
     * ColorPicker, calls the redraw method to apply the new color to Player 2's pieces, and updates the p2Color
     * variable to reflect the new selection.
     */
    @FXML
    protected void onP2ColorAction(){
        Color c = p2ColorPicker.getValue();
        redraw(c, p2Pieces, p2OriginalImages,p2DefaultColor);
        p2Color = c;
    }

    /**
     * Redraws the pieces with the specified color. It iterates through the list of ImageView pieces, creates a
     * writable image based on the original image, and replaces the pixels of the specified old color with the new
     * color. This method allows for dynamic recoloring of chess pieces based on user selection, providing a visual
     * preview of the changes in the settings menu.
     * @param c The new color to apply to the pieces.
     * @param pieces The list of ImageView pieces to be recolored.
     * @param originals The list of original Image objects corresponding to the pieces, used for pixel comparison
     *                  and recoloring.
     * @param oldColor The color in the original images that should be replaced with the new color.
     *                 This is typically the default color of the pieces that needs to be changed based on user selection.
     */
    private void redraw(Color c, List<ImageView> pieces,List<Image> originals, Color oldColor) {
        for (int i = 0; i < pieces.size(); i++) {
            Image original = originals.get(i);
            Image helper = pieces.get(i).getImage();
            WritableImage writable = new WritableImage(helper.getPixelReader(), (int) helper.getWidth(), (int) helper.getHeight());
            PixelWriter writer = writable.getPixelWriter();
            PixelReader reader = original.getPixelReader();
            for (int x = 0; x < helper.getWidth(); x++) {
                for (int y = 0; y < helper.getHeight(); y++) {
                    Color color = reader.getColor(x, y);
                    if (color.equals(oldColor)) {
                        writer.setColor(x, y, c);
                    }
                }
            }
            pieces.get(i).setImage(writable);
        }
    }


    /**
     * Initializes the ChessSettings controller by setting up the original images for both players' pieces, configuring
     * the ColorPickers with the current settings, and updating the preview polygons to reflect the selected tile colors.
     * This method is called automatically when the controller is created and ensures that the user interface starts
     * in a consistent state, displaying the current configuration of pieces and tile colors.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        p1OriginalImages = Stream.of(p1Pawn, p1Rook, p1Knight, p1Bishop, p1Queen, p1King, p1Duck).map(ImageView::getImage).toList();
        p2OriginalImages = Stream.of(p2Pawn, p2Rook, p2Knight, p2Bishop, p2Queen, p2King, p2Duck).map(ImageView::getImage).toList();
        p1Pieces = List.of(p1Pawn, p1Rook, p1Knight, p1Bishop, p1Queen, p1King, p1Duck);
        p2Pieces = List.of(p2Pawn, p2Rook, p2Knight, p2Bishop, p2Queen, p2King, p2Duck);

        if (settings.getP1Pieces() != null){
            p1Color = settings.getP1Color();
            p1ColorPicker.setValue(p1Color);
            redraw(p1Color,p1Pieces,p1OriginalImages,p1DefaultColor);
        }
        if (settings.getP2Pieces() != null){
            p2Color = settings.getP2Color();
            p2ColorPicker.setValue(p2Color);
            redraw(p2Color,p2Pieces,p2OriginalImages,p2DefaultColor);
        }

        if (settings.getDarkTiles() != null){
            darkTileColor = settings.getDarkTiles();
            darkTilesColorPicker.setValue(darkTileColor);
        }
        else {
            darkTilesColorPicker.setValue(darkTileColor);
        }
        if (settings.getNormalTiles() != null) {
            normalTileColor = settings.getNormalTiles();
            normalTilesColorPicker.setValue(normalTileColor);
        }
        else {
            normalTilesColorPicker.setValue(normalTileColor);
        }

        if (settings.getLightTiles() != null){
            lightTileColor = settings.getLightTiles();
            lightTilesColorPicker.setValue(lightTileColor);
        }
        else {
            lightTilesColorPicker.setValue(lightTileColor);
        }
        darkPolygon.setFill(darkTileColor);
        normalPolygon.setFill(normalTileColor);
        lightPolygon.setFill(lightTileColor);

    }

    /**
     * Updates the dark tile preview polygon to match the ColorPicker selection. It retrieves the selected color from
     * the darkTilesColorPicker, applies it to the darkPolygon for visual feedback, and updates the darkTileColor
     * variable to reflect the new selection.
     */
    @FXML
    protected void onDarkTileAction() {
        darkTileColor = changeTileColor(darkTilesColorPicker,darkPolygon);
    }

    /**
     * Updates the light tile preview polygon to match the ColorPicker selection. It retrieves the selected color from
     * the lightTilesColorPicker, applies it to the lightPolygon for visual feedback, and updates the lightTileColor
     * variable to reflect the new selection.
     */
    @FXML
    protected void onLightTileaAction() {
        lightTileColor = changeTileColor(lightTilesColorPicker,lightPolygon);
    }

    /**
     * Updates the normal tile preview polygon to match the ColorPicker selection. It retrieves the selected color
     * from the normalTilesColorPicker, applies it to the normalPolygon for visual feedback, and updates the
     * normalTileColor variable to reflect the new selection.
     */
    @FXML
    protected void onNormalTileAction() {
        normalTileColor = changeTileColor(normalTilesColorPicker,normalPolygon);
    }

    /**
     * Changes the color of the specified polygon based on the selected color from the provided ColorPicker.
     * It retrieves the new color from the ColorPicker, applies it to the polygon for visual feedback, and returns the
     * new color. This method is used to update the preview of tile colors in the chess settings menu.
     * @param colorPicker The ColorPicker from which to retrieve the new color selection.
     * @param polygon The Polygon whose fill color will be updated to reflect the new color selection.
     * @return The new color selected from the ColorPicker, which has been applied to the specified polygon for
     * visual feedback in the settings menu.
     */
    private Color changeTileColor(ColorPicker colorPicker, Polygon polygon){
        Color newColor = colorPicker.getValue();
        polygon.setFill(newColor);
        return newColor;
    }
}
