package mx.unison;

public class ErrorCalculadora extends Exception {

    private static final long serialVersionUID = 1L;

    public static final int SIN_ARGUMENTOS = 1;
    public static final int FALTA_VARIABLE = 2;
    public static final int USO_INVALIDO = 3;
    public static final int DOCUMENTO_NO_ABIERTO = 4;
    public static final int DOCUMENTO_MAL_FORMADO = 5;
    public static final int VARIABLE_INEXISTENTE = 6;
    public static final int VARIABLE_NO_NUMERICA = 7;
    public static final int LONGITUDES_DISTINTAS = 8;

    private final int codigoSalida;

    public ErrorCalculadora(int codigoSalida, String mensaje) {
        super(mensaje);
        this.codigoSalida = codigoSalida;
    }

    public ErrorCalculadora(int codigoSalida, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigoSalida = codigoSalida;
    }

    public int getCodigoSalida() {
        return codigoSalida;
    }
}