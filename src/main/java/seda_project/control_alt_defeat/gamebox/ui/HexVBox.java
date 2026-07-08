package seda_project.control_alt_defeat.gamebox.ui;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;

public class HexVBox extends VBox {
    private Polygon hexShape;

    /**
     * Initializes a new HexVBox instance with default settings. This constructor sets up the VBox's shape to be a hexagon by creating a Polygon and assigning it as the VBox's shape. The hexagonal shape is defined by its vertices, which are calculated based on the VBox's width and height during layout. This allows for a visually distinct VBox that can be used in the user interface for layout purposes.
     */
    public HexVBox() {
        super();
        initShape();
    }

    /**
     * Initializes the hexagonal shape of the VBox by creating a Polygon and setting it as the shape of the VBox. The hexagon is defined by its vertices, which are calculated based on the VBox's width and height during layout. This method is called during construction to ensure that the VBox has a distinct hexagonal appearance in the user interface.
     */
    private void initShape() {
        hexShape = new Polygon();
        setShape(hexShape);
    }

    /**
     * Overrides the layoutChildren method to update the hexagonal shape of the VBox based on its current width and height. This method is called during the layout phase of the VBox's lifecycle, allowing for dynamic resizing and ensuring that the hexagon maintains its proportions as the VBox's size changes. The vertices of the hexagon are recalculated to fit within the VBox's bounds.
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
     * Draws a hexagonal shape based on the specified width and height. This method calculates the vertices of the hexagon and updates the points of the Polygon used as the shape of the VBox. The hexagon is defined by six points, which are positioned to create a visually distinct VBox that can be used in the user interface for various layout purposes.
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
