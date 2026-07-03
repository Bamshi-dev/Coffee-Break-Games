package seda_project.control_alt_defeat.gamebox.ui;

import javafx.scene.control.Button;
import javafx.scene.shape.Polygon;


public class HexButton extends Button {
    private Polygon hexShape;

    public HexButton() {
        super();
        initShape();
    }

    public HexButton(String text) {
        super(text);
        initShape();
    }

    private void initShape() {
        hexShape = new Polygon();
        setShape(hexShape);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double w = getWidth();
        double h = getHeight();
        if (w > 0 && h > 0) {
            drawHexagon(w, h);
        }
    }

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
