package seda_project.control_alt_defeat.gamebox.ui;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Polygon;

public class HexHBox extends HBox {
    private Polygon hexShape;

    public HexHBox() {
        super();
        initShape();
    }

    /**
     * Initializes the hexagonal shape of the HBox by creating a Polygon and setting it as the shape of the HBox. The hexagon is defined by its vertices, which are calculated based on the HBox's width and height during layout. This method is called during construction to ensure that the HBox has a distinct hexagonal appearance in the user interface.
     */
    private void initShape() {
        hexShape = new Polygon();
        setShape(hexShape);
        setPadding(new Insets(10, 15, 10, 15));
    }

    /**
     * Overrides the layoutChildren method to update the hexagonal shape of the HBox based on its current width and height. This method is called during the layout phase of the HBox's lifecycle, allowing for dynamic resizing and ensuring that the hexagon maintains its proportions as the HBox's size changes. The vertices of the hexagon are recalculated to fit within the HBox's bounds.
     */
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double w = getWidth();
        double h = getHeight();
        if (w > 0 && h > 0) {
            drawHexagon(w, h);
        }
        setPadding(new Insets(10, getWidth() * .1, 10, getWidth() * .1));
    }

    /**
     * Draws a hexagonal shape based on the specified width and height. This method calculates the vertices of the hexagon and updates the points of the Polygon used as the shape of the HBox. The hexagon is defined by six points, which are positioned to create a visually distinct HBox that can be used in the user interface for various layout purposes.
     * @param width The width of the hexagon to be drawn.
     * @param height The height of the hexagon to be drawn.
     */
    private void drawHexagon(double width, double height) {
        hexShape.getPoints().setAll(
                0.0 - width * .1, height * 0.5,
                0.0, 0.0,
                width, 0.0,
                width + width * .1, height * 0.5,
                width, height,
                0.0, height
        );
    }
}
