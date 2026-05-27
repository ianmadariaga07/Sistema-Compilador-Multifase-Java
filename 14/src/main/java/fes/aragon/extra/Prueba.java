package fes.aragon.extra;

import fes.aragon.lexico.Lexico;
import fes.aragon.sintactico.parser;
import java.io.FileReader;

public class Prueba {
    public static void main(String[] args) {
        try {
            Lexico lexico = new Lexico(new FileReader("salida.fes"));
            parser p = new parser(lexico);
            p.parse();
            System.out.println("Análisis completado sin errores sintácticos.");
        } catch (Exception e) {
            System.out.println("Error durante la ejecución del análisis.");
            e.printStackTrace();
        }
    }
}