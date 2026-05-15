package fes.aragon.modelo;

import fes.aragon.token.TokensOperaciones;
import java.io.IOException;

public class SintacticoOperaciones {
    private LexicoOperaciones lexico;
    private TokensOperaciones token;
    private boolean hayError = false;
    private StringBuilder errorLog = new StringBuilder();

    public SintacticoOperaciones(LexicoOperaciones lexico) {
        this.lexico = lexico;
    }

    private void get_token() throws IOException {
        token = lexico.yylex();
        if (token == null) token = TokensOperaciones.EOF;
    }

    public String analizar() throws IOException {
        get_token();
        secuencia();
        return hayError ? errorLog.toString() : "ANALISIS EXITOSO";
    }

    private void error(String mensaje) {
        hayError = true;
        errorLog.append("Error en L:").append(lexico.getYyline())
                .append(" C:").append(lexico.getYycolumn())
                .append(" -> ").append(mensaje).append("\n");
    }

    private void secuencia() throws IOException {
        do {
            expresion();
            while (token != TokensOperaciones.PUNTOYCOMA) {
                error("Se esperaba ';' después de la instrucción");
                if (token == TokensOperaciones.EOF) {
                    return;
                }
                get_token();
            }

            if (token == TokensOperaciones.PUNTOYCOMA) {
                get_token();
            }

        } while (token != TokensOperaciones.EOF);
    }

    private void expresion() throws IOException {
        exprSimple();
        if (token == TokensOperaciones.IGUAL || token == TokensOperaciones.DIST || token == TokensOperaciones.ME ||
                token == TokensOperaciones.MEI || token == TokensOperaciones.MA || token == TokensOperaciones.MAI) {
            get_token();
            exprSimple();
        }
    }

    private void exprSimple() throws IOException {
        if (token == TokensOperaciones.MAS || token == TokensOperaciones.MENOS) {
            get_token();
        }
        termino();
        while (token == TokensOperaciones.MAS || token == TokensOperaciones.MENOS || token == TokensOperaciones.OR) {
            get_token();
            termino();
        }
    }

    private void termino() throws IOException {
        factor();
        while (token == TokensOperaciones.POR || token == TokensOperaciones.DIV ||
                token == TokensOperaciones.MOD || token == TokensOperaciones.AND || token == TokensOperaciones.DIV_ENT) {
            get_token();
            factor();
        }
    }

    private void factor() throws IOException {
        switch (token) {
            case ID:
            case NUM:
                get_token();
                break;
            case NOT:
                get_token();
                factor();
                break;
            case AB_PAR:
                get_token();
                expresion();
                if (token == TokensOperaciones.CE_PAR) {
                    get_token();
                } else {
                    error("Falta paréntesis de cierre ')'");
                }
                break;
            default:
                error("Expresión no válida (se esperaba ID, NUM, NOT o '(' )");
                get_token();
                break;
        }
    }
}