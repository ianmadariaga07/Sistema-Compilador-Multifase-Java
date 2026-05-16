package fes.aragon.sintactico;

import fes.aragon.lexico.LexicoBooleanoCup;
import fes.aragon.token.TokensBooleanoCup;
import java.io.IOException;

public class SintacticoBooleanoCup {
    private LexicoBooleanoCup lexico;
    private TokensBooleanoCup token;
    private boolean hayError = false;
    private StringBuilder errorLog = new StringBuilder();

    public SintacticoBooleanoCup(LexicoBooleanoCup lexico) {
        this.lexico = lexico;
    }

    private void get_token() throws IOException {
        token = lexico.yylex();
        if (token == null) token = TokensBooleanoCup.EOF;
    }

    public String analizar() throws IOException {
        get_token();
        secuencia();
        return hayError ? errorLog.toString() : "Análisis Sintáctico Booleano A' Exitoso";
    }

    private void error(String mensaje) {
        hayError = true;
        errorLog.append("Error en L:").append(lexico.getYyline())
                .append(" C:").append(lexico.getYycolumn())
                .append(" -> ").append(mensaje).append("\n");
    }

    private void secuencia() throws IOException {
        do {
            E();
            if (token == TokensBooleanoCup.PUNTOYCOMA) {
                get_token();
            } else if (token != TokensBooleanoCup.EOF) {
                error("Se esperaba ';' al final de la expresión");
                // Freno para evitar bucles si falta el ;
                if(token == TokensBooleanoCup.EOF) return;
                get_token();
            }
        } while (token != TokensBooleanoCup.EOF && !hayError);
    }

    // E ::= T E'
    private void E() throws IOException {
        T();
        E_prima();
    }

    // E' ::= or T E' | lambda
    private void E_prima() throws IOException {
        if (token == TokensBooleanoCup.OR) {
            get_token();
            T();
            E_prima();
        }
        // Caso lambda: no se hace nada
    }

    // T ::= F T'
    private void T() throws IOException {
        F();
        T_prima();
    }

    // T' ::= and F T' | lambda
    private void T_prima() throws IOException {
        if (token == TokensBooleanoCup.AND) {
            get_token();
            F();
            T_prima();
        }
    }

    // F ::= not E | true | false | ( E )
    private void F() throws IOException {
        if (token == TokensBooleanoCup.NOT) {
            get_token();
            E();
        } else if (token == TokensBooleanoCup.TRUE || token == TokensBooleanoCup.FALSE) {
            get_token();
        } else if (token == TokensBooleanoCup.AB_PAR) {
            get_token();
            E();
            if (token == TokensBooleanoCup.CE_PAR) {
                get_token();
            } else {
                error("Se esperaba paréntesis de cierre ')'");
            }
        } else {
            error("Se esperaba 'not', 'true', 'false' o '('");
        }
    }
}