package seda_project.control_alt_defeat.gamebox.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.shape.Polygon;

public class HexLabel extends Label {

    private Polygon hexShape;

    public HexLabel() {
        super();
        initShape();
    }

    public HexLabel(String text) {
        super(text);
        initShape();
    }

    private void initShape() {
        hexShape = new Polygon();
        setShape(hexShape);
        setPadding(new Insets(0,15,0,15));
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
                0.0-20, height * 0.5,
                0.0, 0.0,
                width, 0.0,
                width+20, height * 0.5,
                width, height,
                0.0, height
        );
    }

}
