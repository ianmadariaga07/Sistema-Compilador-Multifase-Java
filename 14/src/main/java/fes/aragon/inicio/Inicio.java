package fes.aragon.inicio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

public class Inicio extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Inicio.class.getResource("/fes.aragon/xml/inicio.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1120, 780);
        stage.setTitle("Interprete 20x20");
        stage.setScene(scene);
        //scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setMinWidth(1120);
        stage.setMinHeight(800);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}