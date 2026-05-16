module fes.aragon.javafxcompiladores {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires java.cup;

    exports fes.aragon.controller;
    opens fes.aragon.controller to javafx.fxml;
    exports fes.aragon;
    opens fes.aragon to javafx.fxml;
    exports fes.aragon.inicio;
    opens fes.aragon.inicio to javafx.fxml;
    exports fes.aragon.modelo;
    opens fes.aragon.modelo to javafx.fxml;
    exports fes.aragon.extra;
    opens fes.aragon.extra to javafx.fxml;
    exports fes.aragon.token;
    opens fes.aragon.token to javafx.fxml;
    exports fes.aragon.lexico;
    opens fes.aragon.lexico to javafx.fxml;
    exports fes.aragon.sintactico;
    opens fes.aragon.sintactico to javafx.fxml;
}