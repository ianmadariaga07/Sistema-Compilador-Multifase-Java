package fes.aragon.controller;

import fes.aragon.modelo.Fondo;
import fes.aragon.modelo.MotorEjecucion;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InicioController implements Initializable {

    @FXML private Button idAbrir;
    @FXML private Button idCrearArchivo;
    @FXML private Button idEliminar;
    @FXML private Button idGuardar;
    @FXML private Button idGuardarComo;
    @FXML private Button idLimpiar;
    @FXML private Button idQuitar;
    @FXML private Label lblEstado;
    @FXML private StackPane contenedorEditor;

    @FXML private Canvas hoja;
    @FXML private ImageView nave;
    @FXML private Label consola;
    @FXML private ToggleButton btnVelocidad;

    private CodeArea txtAreaContenido;
    private File archivoAbierto;
    private Image imgDerecha, imgIzquierda, imgArriba, imgAbajo;
    private SequentialTransition animacionActual;

    private static final String[] KEYWORDS_CMD = new String[] { "derecha", "izquierda", "arriba", "abajo", "mover" };
    private static final String[] KEYWORDS_LOOP = new String[] { "repite", "hasta" };
    private static final String[] KEYWORDS_BLOCK = new String[] { "inicio", "fin" };

    private static final String KEYWORD_CMD_PATTERN = "\\b(" + String.join("|", KEYWORDS_CMD) + ")\\b";
    private static final String KEYWORD_LOOP_PATTERN = "\\b(" + String.join("|", KEYWORDS_LOOP) + ")\\b";
    private static final String KEYWORD_BLOCK_PATTERN = "\\b(" + String.join("|", KEYWORDS_BLOCK) + ")\\b";
    private static final String NUMBER_PATTERN = "\\b\\d+\\b";
    private static final String VARIABLE_PATTERN = "\\b[a-zA-Z_][a-zA-Z0-9_]*\\b";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<CMD>" + KEYWORD_CMD_PATTERN + ")"
                    + "|(?<LOOP>" + KEYWORD_LOOP_PATTERN + ")"
                    + "|(?<BLOCK>" + KEYWORD_BLOCK_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<VARIABLE>" + VARIABLE_PATTERN + ")"
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarBotones(false);

        txtAreaContenido = new CodeArea();
        txtAreaContenido.setParagraphGraphicFactory(LineNumberFactory.get(txtAreaContenido));
        txtAreaContenido.textProperty().addListener((obs, oldText, newText) -> {
            txtAreaContenido.setStyleSpans(0, computeHighlighting(newText));
        });

        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(txtAreaContenido);
        contenedorEditor.getChildren().add(scrollPane);

        Fondo fondo = new Fondo();
        fondo.pintar(hoja.getGraphicsContext2D());

        imgDerecha = new Image(getClass().getResourceAsStream("/fes.aragon/tablerointerprete/derecha.png"));
        imgIzquierda = new Image(getClass().getResourceAsStream("/fes.aragon/tablerointerprete/izquierda.png"));
        imgArriba = new Image(getClass().getResourceAsStream("/fes.aragon/tablerointerprete/arriba.png"));
        imgAbajo = new Image(getClass().getResourceAsStream("/fes.aragon/tablerointerprete/abajo.png"));

        nave.setImage(imgDerecha);
        nave.setFitWidth(30);
        nave.setFitHeight(30);
        nave.setX(0);
        nave.setY(0);
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("CMD") != null ? "keyword-cmd" :
                            matcher.group("LOOP") != null ? "keyword-loop" :
                                    matcher.group("BLOCK") != null ? "keyword-block" :
                                            matcher.group("NUMBER") != null ? "number" :
                                                    matcher.group("VARIABLE") != null ? "variable" :
                                                            null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            if (styleClass != null) {
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            }
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void limpiarTablero() {
        hoja.getGraphicsContext2D().clearRect(0, 0, hoja.getWidth(), hoja.getHeight());
        Fondo fondo = new Fondo();
        fondo.pintar(hoja.getGraphicsContext2D());
    }

    @FXML
    public void cambiarVelocidad(ActionEvent event) {
        if (btnVelocidad.isSelected()) {
            btnVelocidad.setText("Velocidad: x2");
            if (animacionActual != null) animacionActual.setRate(2.0);
        } else {
            btnVelocidad.setText("Velocidad: x1");
            if (animacionActual != null) animacionActual.setRate(1.0);
        }
    }

    @FXML
    public void mostrarInstrucciones(ActionEvent event) {
        String info = "Sintaxis y Palabras Reservadas Permitidas:\n\n" +
                "- inicio X Y (Establece la coordenada inicial de la nave en la matriz 20x20)\n" +
                "- derecha, izquierda, arriba, abajo (Apunta la nave hacia esa dirección)\n" +
                "- mover N (Avanza N casillas hacia la dirección actual)\n" +
                "- ID = N (Declaración e inicialización de variables, ej: a = 1)\n" +
                "- repite (Abre un bloque iterativo)\n" +
                "- hasta ID = N (Condición de cierre del bloque iterativo)\n" +
                "- fin (Concluye el programa)\n\n" +
                "Ejemplo de uso:\n" +
                "inicio 5 5\n" +
                "a = 1\n" +
                "repite\n" +
                "  derecha\n" +
                "  mover 2\n" +
                "  abajo\n" +
                "  mover 2\n" +
                "hasta a = 4\n" +
                "fin";
        mostrarAlerta("Instrucciones del Intérprete", info);
    }

    @FXML
    public void ejecutarCodigo(ActionEvent event) {
        if (txtAreaContenido.getText() == null || txtAreaContenido.getText().trim().isEmpty()) {
            consola.setText("Consola: El área de texto está vacía.");
            consola.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff6b6b; -fx-font-size: 14;");
            return;
        }

        if (animacionActual != null) {
            animacionActual.stop();
        }
        limpiarTablero();

        try {
            File archivo = new File("salida.fes");
            FileWriter escritor = new FileWriter(archivo);
            escritor.write(txtAreaContenido.getText());
            escritor.close();

            fes.aragon.lexico.Lexico lexico = new fes.aragon.lexico.Lexico(new FileReader("salida.fes"));
            fes.aragon.sintactico.parser p = new fes.aragon.sintactico.parser(lexico);
            p.parse();

            consola.setText("Consola: Análisis completado sin errores sintácticos. Ejecutando animación...");
            consola.setStyle("-fx-font-weight: bold; -fx-text-fill: #51cf66; -fx-font-size: 14;");

            MotorEjecucion motor = new MotorEjecucion(imgDerecha, imgIzquierda, imgArriba, imgAbajo);
            animacionActual = motor.procesar(p.listaComandos, nave, hoja);

            if (btnVelocidad.isSelected()) {
                animacionActual.setRate(2.0);
            } else {
                animacionActual.setRate(1.0);
            }

            animacionActual.play();

        } catch (RuntimeException ex) {
            //atrapa los errores con coordenadas de CUP
            consola.setText("Consola: " + ex.getMessage());
            consola.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff6b6b; -fx-font-size: 14;");
        } catch (Exception ex) {
            consola.setText("Consola: Error estructural. Revisa las instrucciones en el editor.");
            consola.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff6b6b; -fx-font-size: 14;");
        }
    }

    @FXML
    public void detenerAnimacion(ActionEvent event) {
        if (animacionActual != null) {
            animacionActual.stop();
            consola.setText("Consola: Ejecución interrumpida por el usuario.");
            consola.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffa94d; -fx-font-size: 14;");
        }
    }

    @FXML
    public void reiniciarNave(ActionEvent event) {
        if (animacionActual != null) {
            animacionActual.stop();
        }
        limpiarTablero();
        nave.setTranslateX(0);
        nave.setTranslateY(0);
        nave.setImage(imgDerecha);
        consola.setText("Consola: Nave reiniciada manualmente a la posición (0, 0).");
        consola.setStyle("-fx-font-weight: bold; -fx-text-fill: #5bc0de; -fx-font-size: 14;");
    }

    @FXML
    void accionAbrirArchivo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Archivo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos FES", "*.fes", "*.txt")
        );
        Stage stage = (Stage) idAbrir.getScene().getWindow();
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            configurarBotones(true);
            idAbrir.setDisable(true);
            idCrearArchivo.setDisable(true);
            this.archivoAbierto = archivoSeleccionado;
            lblEstado.setText("Editando: " + archivoSeleccionado.getName());
            leerContenidoArchivo(archivoSeleccionado);
        }
    }

    @FXML
    void crearArchivo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Crear Archivo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos FES", "*.fes", "*.txt")
        );
        Stage stage = (Stage) idCrearArchivo.getScene().getWindow();
        File archivoParaGuardar = fileChooser.showSaveDialog(stage);

        if (archivoParaGuardar != null) {
            try {
                Files.writeString(archivoParaGuardar.toPath(), txtAreaContenido.getText());
                configurarBotones(true);
                idAbrir.setDisable(true);
                idCrearArchivo.setDisable(true);
                lblEstado.setText("Editando: " + archivoParaGuardar.getName());
                this.archivoAbierto = archivoParaGuardar;
                mostrarAlerta("Éxito", "Archivo creado como: " + archivoParaGuardar.getName());
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo crear: " + e.getMessage());
            }
        }
    }

    @FXML
    void eliminarArchivo(ActionEvent event) {
        if (archivoAbierto != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Eliminar Archivo");
            confirmacion.setHeaderText("¿Estás seguro de eliminar " + archivoAbierto.getName() + "?");
            confirmacion.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> resultado = confirmacion.showAndWait();

            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                try {
                    Files.delete(archivoAbierto.toPath());
                    mostrarAlerta("Eliminado", "El archivo ha sido eliminado del disco.");
                    configurarBotones(false);
                    idAbrir.setDisable(false);
                    idCrearArchivo.setDisable(false);
                    limpiarAreaTexto(event);
                    archivoAbierto = null;
                } catch (IOException e) {
                    mostrarAlerta("Error", "No se pudo eliminar el archivo: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    void guardarArchivoComo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Archivo Como...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos FES", "*.fes", "*.txt")
        );
        Stage stage = (Stage) idGuardarComo.getScene().getWindow();
        File archivoParaGuardar = fileChooser.showSaveDialog(stage);

        if (archivoParaGuardar != null) {
            try {
                Files.writeString(archivoParaGuardar.toPath(), txtAreaContenido.getText());
                configurarBotones(true);
                lblEstado.setText("Editando: " + archivoParaGuardar.getName());
                this.archivoAbierto = archivoParaGuardar;
                mostrarAlerta("Éxito", "Archivo guardado como: " + archivoParaGuardar.getName());
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo guardar: " + e.getMessage());
            }
        }
    }

    @FXML
    void guardarEdicionArchivo(ActionEvent event) {
        if (archivoAbierto != null) {
            try {
                Path ruta = archivoAbierto.toPath();
                String contenido = txtAreaContenido.getText();
                Files.writeString(ruta, contenido);
                lblEstado.setText("Cambios guardados correctamente en " + archivoAbierto.getName());
                mostrarAlerta("Éxito", "Archivo guardado correctamente.");
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo guardar el archivo: " + e.getMessage());
            }
        } else {
            mostrarAlerta("Aviso", "No hay ningún archivo abierto para guardar.");
        }
    }

    @FXML
    void limpiarAreaTexto(ActionEvent event) {
        txtAreaContenido.clear();
        consola.setText("Consola: Esperando ejecución...");
        consola.setStyle("-fx-font-weight: bold; -fx-text-fill: #D6D8E1; -fx-font-size: 14;");
    }

    @FXML
    void quitarArchivo(ActionEvent event) {
        configurarBotones(false);
        idAbrir.setDisable(false);
        idCrearArchivo.setDisable(false);
        txtAreaContenido.clear();
        archivoAbierto = null;
        lblEstado.setText("Estado: Nuevo archivo sin título.");
    }

    private void leerContenidoArchivo(File archivo) {
        try {
            Path ruta = archivo.toPath();
            String contenido = Files.readString(ruta);
            txtAreaContenido.replaceText(contenido);
        } catch (IOException e) {
            txtAreaContenido.replaceText("Error al leer el archivo: " + e.getMessage());
        }
    }

    private void configurarBotones(boolean hayArchivoAbierto) {
        idGuardar.setDisable(!hayArchivoAbierto);
        idGuardarComo.setDisable(!hayArchivoAbierto);
        idQuitar.setDisable(!hayArchivoAbierto);
        idLimpiar.setDisable(!hayArchivoAbierto);
        idEliminar.setDisable(!hayArchivoAbierto);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}