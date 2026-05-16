package fes.aragon.sintactico;

import fes.aragon.lexico.LexicoASDP;
import fes.aragon.modelo.PasoASDP;
import fes.aragon.token.TokensASDP;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SintacticoASDP {
    private LexicoASDP lexico;

    private class ElementoEntrada {
        TokensASDP token;
        String lexema;
        public ElementoEntrada(TokensASDP token, String lexema) {
            this.token = token; this.lexema = lexema;
        }
    }

    private String[][] tablaM = new String[4][5];

    public SintacticoASDP(LexicoASDP lexico) {
        this.lexico = lexico;
        inicializarTabla();
    }

    private void inicializarTabla() {
        tablaM[0][0] = "S:=A B";
        tablaM[0][1] = "S:=A B";
        tablaM[1][0] = "A:=a";
        tablaM[1][1] = "A:=λ";
        tablaM[2][1] = "B:=b C d";
        tablaM[3][2] = "C:=c";
        tablaM[3][3] = "C:=λ";
    }

    public List<PasoASDP> analizar() throws IOException {
        List<PasoASDP> pasos = new ArrayList<>();
        List<ElementoEntrada> entrada = new ArrayList<>();
        TokensASDP t = lexico.yylex();

        while (t != null && t != TokensASDP.EOF) {
            entrada.add(new ElementoEntrada(t, lexico.lexema));
            t = lexico.yylex();
        }
        // Agregamos un token de fin de archivo en lugar de regalar el punto y coma
        entrada.add(new ElementoEntrada(TokensASDP.EOF, ""));

        Stack<String> pila = new Stack<>();
        pila.push(";");
        pila.push("S");

        int indiceEntrada = 0;

        while (!pila.isEmpty()) {
            String cimaPila = pila.peek();
            ElementoEntrada tokenActual = entrada.get(indiceEntrada);

            String strPila = formatoPila(pila);
            String strEntrada = formatoEntrada(entrada, indiceEntrada);

            if (esTerminal(cimaPila)) {
                if (cimaPila.equals(tokenActual.lexema)) {
                    pila.pop();
                    indiceEntrada++;
                    if (cimaPila.equals(";")) {
                        pasos.add(new PasoASDP(strPila, strEntrada, "Aceptar"));
                    } else {
                        pasos.add(new PasoASDP(strPila, strEntrada, "Consumir(" + cimaPila + ")"));
                    }
                } else {
                    pasos.add(new PasoASDP(strPila, strEntrada, "Error: Se esperaba '" + cimaPila + "'"));
                    break; // Cortamos la ejecución al primer error
                }
            } else {
                int fila = obtenerFila(cimaPila);
                int col = obtenerColumna(tokenActual.token);

                if (col == -1 || fila == -1 || tablaM[fila][col] == null) {
                    pasos.add(new PasoASDP(strPila, strEntrada, "Error: Casilla vacía (M["+cimaPila+","+ (tokenActual.lexema.isEmpty() ? "EOF" : tokenActual.lexema) +"])"));
                    break;
                }

                String regla = tablaM[fila][col];
                pasos.add(new PasoASDP(strPila, strEntrada, regla));

                pila.pop();
                aplicarReglaEnPila(pila, regla);
            }
        }
        return pasos;
    }

    private void aplicarReglaEnPila(Stack<String> pila, String regla) {
        switch (regla) {
            case "S:=A B": pila.push("B"); pila.push("A"); break;
            case "A:=a": pila.push("a"); break;
            case "A:=λ": break;
            case "B:=b C d": pila.push("d"); pila.push("C"); pila.push("b"); break;
            case "C:=c": pila.push("c"); break;
            case "C:=λ": break;
        }
    }

    private boolean esTerminal(String s) {
        return s.equals("a") || s.equals("b") || s.equals("c") || s.equals("d") || s.equals(";");
    }

    private int obtenerFila(String noTerminal) {
        switch (noTerminal) { case "S": return 0; case "A": return 1; case "B": return 2; case "C": return 3; default: return -1; }
    }

    private int obtenerColumna(TokensASDP t) {
        switch (t) { case A_MINUSCULA: return 0; case B_MINUSCULA: return 1; case C_MINUSCULA: return 2; case D_MINUSCULA: return 3; case PUNTOYCOMA: return 4; default: return -1; }
    }

    private String formatoPila(Stack<String> pila) {
        StringBuilder sb = new StringBuilder();
        for (String s : pila) sb.append(s).append(" ");
        return sb.toString().trim();
    }

    private String formatoEntrada(List<ElementoEntrada> entrada, int indexActual) {
        StringBuilder sb = new StringBuilder();
        for (int i = indexActual; i < entrada.size(); i++) sb.append(entrada.get(i).lexema).append(" ");
        return sb.toString().trim();
    }
}