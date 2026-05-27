package fes.aragon.modelo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Fondo {
    public void pintar(GraphicsContext graficos) {
        graficos.setStroke(Color.LIGHTGRAY);
        graficos.setLineWidth(1.0);

        for (int i = 0; i <= 20; i++) {
            graficos.strokeLine(i * 30, 0, i * 30, 600);
            graficos.strokeLine(0, i * 30, 600, i * 30);
        }
    }
}