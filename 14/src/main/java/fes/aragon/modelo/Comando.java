package fes.aragon.modelo;

public class Comando {
    private String accion;
    private int parametroX;
    private int parametroY;

    public Comando(String accion) {
        this.accion = accion;
    }

    public Comando(String accion, int parametroX) {
        this.accion = accion;
        this.parametroX = parametroX;
    }

    public Comando(String accion, int parametroX, int parametroY) {
        this.accion = accion;
        this.parametroX = parametroX;
        this.parametroY = parametroY;
    }

    public String getAccion() { return accion; }
    public int getParametroX() { return parametroX; }
    public int getParametroY() { return parametroY; }
}