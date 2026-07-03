package seda_project.control_alt_defeat.gamebox.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;

public class HexVBox extends VBox {
    private Polygon hexShape;

    public HexVBox() {
        super();
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
        setPadding(new Insets(10,getWidth()*.1,10,getWidth()*.1));
    }

    private void drawHexagon(double width, double height) {
        hexShape.getPoints().setAll(
                0.0 - width*.1, height * 0.5,
                0.0, 0.0,
                width, 0.0,
                width + width*.1, height * 0.5,
                width, height,
                0.0, height
        );
    }
}
