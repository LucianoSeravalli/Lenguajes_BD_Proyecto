package exception;






/**
 * Envuelve los errores de negocio que lanzan los procedimientos PL/SQL
 * con RAISE_APPLICATION_ERROR, para que la capa web no tenga que leer
 * mensajes ORA- crudos.
 */
public class GymCoreException extends RuntimeException {
 
    private final int codigoOracle;
 
    public GymCoreException(int codigoOracle, String mensaje) {
        super(mensaje);
        this.codigoOracle = codigoOracle;
    }
 
    public GymCoreException(int codigoOracle, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigoOracle = codigoOracle;
    }
 
    /** 20002, 20004, 20005, 20007, 20008, 20009, 20010, 20011 o 20012. */
    public int getCodigoOracle() {
        return codigoOracle;
    }
 
    /** true cuando el cliente puede corregir el problema (400/409); false si es error del sistema. */
    public boolean esErrorDeNegocio() {
        return codigoOracle >= 20001 && codigoOracle <= 20999;
    }
}