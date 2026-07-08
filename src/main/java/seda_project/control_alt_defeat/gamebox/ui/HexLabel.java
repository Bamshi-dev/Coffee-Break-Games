package seda_project.control_alt_defeat.gamebox.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.shape.Polygon;

public class HexLabel extends Label {

    private Polygon hexShape;

    /**
     * Initializes a new HexLabel instance with default settings. This constructor sets up the label's shape to be a hexagon by creating a Polygon and assigning it as the label's shape. The hexagonal shape is defined by its vertices, which are calculated based on the label's width and height during layout. This allows for a visually distinct label that can be used in the user interface for displaying text.
     */
    public HexLabel() {
        super();
        initShape();
    }

    /**
     * Initializes a new HexLabel instance with the specified text label. This constructor sets up the label's shape to be a hexagon by creating a Polygon and assigning it as the label's shape. The provided text is displayed on the label, allowing for user interaction with labeled actions. The hexagonal shape is defined by its vertices, which are calculated based on the label's width and height during layout.
     * @param text The text label to be displayed on the label.
     */
    public HexLabel(String text) {
        super(text);
        initShape();
    }

    /**
     * Initializes the hexagonal shape of the label by creating a Polygon and setting it as the label's shape. The hexagon is defined by its vertices, which are calculated based on the label's width and height during layout. This method is called during construction to ensure that the label has a distinct hexagonal appearance in the user interface.
     */
    private void initShape() {
        hexShape = new Polygon();
        setShape(hexShape);
        setPadding(new Insets(5, 20,5,20));
    }

    /**
     * Overrides the layoutChildren method to update the hexagonal shape of the label based on its current width and height. This method is called during the layout phase of the label's lifecycle, allowing for dynamic resizing and ensuring that the hexagon maintains its proportions as the label's size changes. The vertices of the hexagon are recalculated to fit within the label's bounds.
     */
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double w = getWidth();
        double h = getHeight();
        if (w > 0 && h > 0) {
            drawHexagon(w, h);
        }
    }

    /**
     * Draws a hexagonal shape based on the specified width and height. This method calculates the vertices of the hexagon and updates the points of the Polygon used as the label's shape. The hexagon is defined by six points, which are positioned to create a visually distinct label that can be used in the user interface for displaying text.
     * @param width The width of the hexagon to be drawn.
     * @param height The height of the hexagon to be drawn.
     */
    private void drawHexagon(double width, double height) {
        hexShape.getPoints().setAll(
                0.0 - 20, height * 0.5,
                0.0, 0.0,
                width, 0.0,
                width + 20, height * 0.5,
                width, height,
                0.0, height
        );
    }

}
