package fes.aragon.modelo;

public class PasoASDP {
    private String pila;
    private String entrada;
    private String accion;

    public PasoASDP(String pila, String entrada, String accion) {
        this.pila = pila;
        this.entrada = entrada;
        this.accion = accion;
    }

    // Getters necesarios para que JavaFX pueda leer los datos
    public String getPila() { return pila; }
    public String getEntrada() { return entrada; }
    public String getAccion() { return accion; }
}