package seda_project.control_alt_defeat.gamebox.ui;

import javafx.scene.control.Button;
import javafx.scene.shape.Polygon;


public class HexButton extends Button {
    private Polygon hexShape;

    /**
     * Initializes a new HexButton instance with default settings. This constructor sets up the button's shape to be a hexagon by creating a Polygon and assigning it as the button's shape. The hexagonal shape is defined by its vertices, which are calculated based on the button's width and height during layout. This allows for a visually distinct button that can be used in the user interface for various actions.
     */
    public HexButton() {
        super();
        initShape();
    }

    /**
     * Initializes a new HexButton instance with the specified text label. This constructor sets up the button's shape to be a hexagon by creating a Polygon and assigning it as the button's shape. The provided text is displayed on the button, allowing for user interaction with labeled actions. The hexagonal shape is defined by its vertices, which are calculated based on the button's width and height during layout.
     * @param text The text label to be displayed on the button.
     */
    public HexButton(String text) {
        super(text);
        initShape();
    }

    /**
     * Initializes the hexagonal shape of the button by creating a Polygon and setting it as the button's shape. The hexagon is defined by its vertices, which are calculated based on the button's width and height during layout. This method is called during construction to ensure that the button has a distinct hexagonal appearance in the user interface.
     */
    private void initShape() {
        hexShape = new Polygon();
        setShape(hexShape);
    }

    /**
     * Overrides the layoutChildren method to update the hexagonal shape of the button based on its current width and height. This method is called during the layout phase of the button's lifecycle, allowing for dynamic resizing and ensuring that the hexagon maintains its proportions as the button's size changes. The vertices of the hexagon are recalculated to fit within the button's bounds.
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
     * Draws a hexagonal shape based on the specified width and height. This method calculates the vertices of the hexagon and updates the points of the Polygon used as the button's shape. The hexagon is defined by six points, which are positioned to create a visually distinct button that can be used in the user interface for various actions.
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
