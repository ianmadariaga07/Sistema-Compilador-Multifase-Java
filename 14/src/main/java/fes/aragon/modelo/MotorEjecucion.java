package fes.aragon.modelo;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MotorEjecucion {
    private int x = 0;
    private int y = 0;
    private String direccion = "derecha";
    private final int TAMANO_CELDA = 30;
    private final int LIMIT = 20;
    private double hue = 0.0;

    private Image imgDerecha, imgIzquierda, imgArriba, imgAbajo;

    public MotorEjecucion(Image d, Image i, Image a, Image ab) {
        this.imgDerecha = d;
        this.imgIzquierda = i;
        this.imgArriba = a;
        this.imgAbajo = ab;
    }

    public SequentialTransition procesar(ArrayList<Comando> comandos, ImageView nave, Canvas hoja,
                                         TableView<VariableFila> tabla, ObservableList<VariableFila> datos) {
        SequentialTransition secuencia = new SequentialTransition();
        Map<String, Integer> variables = new HashMap<>();
        GraphicsContext gc = hoja.getGraphicsContext2D();

        int i = 0;
        while (i < comandos.size()) {
            Comando c = comandos.get(i);
            String accion = c.getAccion();

            if (accion.equals("inicio")) {
                x = c.getParametroX();
                y = c.getParametroY();
                TranslateTransition t = new TranslateTransition(Duration.millis(10), nave);
                t.setToX(x * TAMANO_CELDA);
                t.setToY(y * TAMANO_CELDA);
                secuencia.getChildren().add(t);
            } else if (accion.equals("derecha") || accion.equals("izquierda") || accion.equals("arriba") || accion.equals("abajo")) {
                direccion = accion;
                PauseTransition cambioImagen = new PauseTransition(Duration.millis(1));
                cambioImagen.setOnFinished(e -> {
                    if (accion.equals("derecha")) nave.setImage(imgDerecha);
                    else if (accion.equals("izquierda")) nave.setImage(imgIzquierda);
                    else if (accion.equals("arriba")) nave.setImage(imgArriba);
                    else if (accion.equals("abajo")) nave.setImage(imgAbajo);
                });
                secuencia.getChildren().add(cambioImagen);
            } else if (accion.equals("mover")) {
                int pasos = c.getParametroX();
                for (int p = 0; p < pasos; p++) {
                    int prevX = x;
                    int prevY = y;
                    double hueActual = hue;

                    if (direccion.equals("derecha")) x = (x + 1) % LIMIT;
                    else if (direccion.equals("izquierda")) x = (x - 1 + LIMIT) % LIMIT;
                    else if (direccion.equals("abajo")) y = (y + 1) % LIMIT;
                    else if (direccion.equals("arriba")) y = (y - 1 + LIMIT) % LIMIT;

                    hue = (hue + 12.0) % 360.0;

                    TranslateTransition t = new TranslateTransition(Duration.millis(300), nave);
                    t.setToX(x * TAMANO_CELDA);
                    t.setToY(y * TAMANO_CELDA);

                    t.setOnFinished(e -> {
                        gc.setFill(Color.hsb(hueActual, 0.75, 0.9));
                        gc.fillRect((prevX * TAMANO_CELDA) + 1, (prevY * TAMANO_CELDA) + 1, TAMANO_CELDA - 2, TAMANO_CELDA - 2);
                    });

                    secuencia.getChildren().add(t);
                }
            } else if (accion.startsWith("asignacion_")) {
                String var = accion.replace("asignacion_", "");
                int val = c.getParametroX();
                variables.put(var, val);

                PauseTransition actualizador = new PauseTransition(Duration.millis(1));
                actualizador.setOnFinished(e -> actualizarFila(var, val, datos, tabla));
                secuencia.getChildren().add(actualizador);

            } else if (accion.equals("repite")) {

            } else if (accion.startsWith("hasta_")) {
                String var = accion.replace("hasta_", "");
                int limite = c.getParametroX();
                int valor = variables.getOrDefault(var, 0);

                if (valor < limite) {
                    int nuevoValor = valor + 1;
                    variables.put(var, nuevoValor);

                    PauseTransition actualizador = new PauseTransition(Duration.millis(1));
                    actualizador.setOnFinished(e -> actualizarFila(var, nuevoValor, datos, tabla));
                    secuencia.getChildren().add(actualizador);

                    int j = i;
                    while (j >= 0 && !comandos.get(j).getAccion().equals("repite")) {
                        j--;
                    }
                    i = j;
                }
            }
            i++;
        }
        return secuencia;
    }

    private void actualizarFila(String nombre, int valor, ObservableList<VariableFila> datos, TableView<VariableFila> tabla) {
        for (VariableFila fila : datos) {
            if (fila.getNombre().equals(nombre)) {
                fila.setValor(valor);
                tabla.refresh();
                return;
            }
        }
        datos.add(new VariableFila(nombre, valor));
    }
}