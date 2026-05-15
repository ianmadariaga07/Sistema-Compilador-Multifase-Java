package fes.aragon.modelo;

import fes.aragon.token.TokensBooleano;
import java.io.IOException;

public class SintacticoBooleano {
    private LexicoBooleano lexico;
    private TokensBooleano tokenActual;
    private boolean hayError;
    private StringBuilder mensajeError;

    public SintacticoBooleano(LexicoBooleano lexico) {
        this.lexico = lexico;
        this.hayError = false;
        this.mensajeError = new StringBuilder();
    }

    public String evaluar() {
        try {
            siguienteToken();
            S();

            if (!hayError && tokenActual == null) {
                return "Expresión Válida [ACEPTADA]";
            } else if (hayError) {
                return "Error: " + mensajeError.toString();
            } else {
                return "Error Sintáctico: Sobran elementos después del punto y coma.";
            }
        } catch (Exception e) {
            return "Error de ejecución: " + e.getMessage();
        }
    }

    private void siguienteToken() throws IOException {
        tokenActual = lexico.yylex();
    }

    private void error(String esperado) {
        hayError = true;
        String encontrado = (lexico.lexema != null) ? lexico.lexema : "Fin de cadena";
        mensajeError.append("Se esperaba '").append(esperado)
                .append("' pero se encontró '").append(encontrado)
                .append("' en la línea ").append(lexico.getYyline())
                .append(", columna ").append(lexico.getYycolumn()).append(".\n");
    }

    //regla 1 S -> E ;
    private void S() throws IOException {
        E();
        if (tokenActual == TokensBooleano.PUNTO_COMA) {
            siguienteToken(); // Consumimos el punto y coma
        } else {
            error(";");
        }
    }

    //regla 2 E -> T | T or E
    private void E() throws IOException {
        T();
        if (tokenActual == TokensBooleano.OR) {
            siguienteToken();
            E();
        }
    }

    //regla 3 T -> F | F and T
    private void T() throws IOException {
        F();
        if (tokenActual == TokensBooleano.AND) {
            siguienteToken();
            T();
        }
    }

    //regla 4 F -> not F | true | false | ( E )
    private void F() throws IOException {
        if (tokenActual == TokensBooleano.NOT) {
            siguienteToken();
            F();
        } else if (tokenActual == TokensBooleano.TRUE) {
            siguienteToken();
        } else if (tokenActual == TokensBooleano.FALSE) {
            siguienteToken();
        } else if (tokenActual == TokensBooleano.PARENTESIS_A) {
            siguienteToken();
            E();
            if (tokenActual == TokensBooleano.PARENTESIS_C) {
                siguienteToken();
            } else {
                error(")");
            }
        } else {
            error("true, false, not o '('");
        }
    }
}