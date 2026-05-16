package fes.aragon.sintactico;

import fes.aragon.lexico.LexicoBooleanoAPrima;
import fes.aragon.token.TokensBooleanoAPrima;
import java.io.IOException;

public class SintacticoBooleanoAPrima {
    private LexicoBooleanoAPrima lexico;
    private TokensBooleanoAPrima token;
    private boolean hayError = false;
    private StringBuilder errorLog = new StringBuilder();

    public SintacticoBooleanoAPrima(LexicoBooleanoAPrima lexico) {
        this.lexico = lexico;
    }

    private void get_token() throws IOException {
        token = lexico.yylex();
        if (token == null) token = TokensBooleanoAPrima.EOF;
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
        System.out.println("EJECUTANDO LA NUEVA VERSIÓN ESTRICTA...");
        do {
            E();
            if (token != TokensBooleanoAPrima.PUNTOYCOMA) {
                error("Falta el punto y coma ';' al final de la expresión");
                if (token == TokensBooleanoAPrima.EOF) {
                    return;
                }
            }else{
                get_token();
            }
        } while (token != TokensBooleanoAPrima.EOF && !hayError);
    }

    private void E() throws IOException {
        T();
        E_prima();
    }

    //E' ::= or T E' | lambda
    private void E_prima() throws IOException {
        if (token == TokensBooleanoAPrima.OR) {
            get_token();
            T();
            E_prima();
        }
        //caso lambda no se hace nada
    }

    //T ::= F T'
    private void T() throws IOException {
        F();
        T_prima();
    }

    //T' ::= and F T' | lambda
    private void T_prima() throws IOException {
        if (token == TokensBooleanoAPrima.AND) {
            get_token();
            F();
            T_prima();
        }
    }

    //F ::= not E | true | false | ( E )
    private void F() throws IOException {
        if (token == TokensBooleanoAPrima.NOT) {
            get_token();
            E();
        } else if (token == TokensBooleanoAPrima.TRUE || token == TokensBooleanoAPrima.FALSE) {
            get_token();
        } else if (token == TokensBooleanoAPrima.AB_PAR) {
            get_token();
            E();
            if (token == TokensBooleanoAPrima.CE_PAR) {
                get_token();
            } else {
                error("Se esperaba paréntesis de cierre ')'");
            }
        } else {
            error("Se esperaba 'not', 'true', 'false' o '('");
        }
    }
}