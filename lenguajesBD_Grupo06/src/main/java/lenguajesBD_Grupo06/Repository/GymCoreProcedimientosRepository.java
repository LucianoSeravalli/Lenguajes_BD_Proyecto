
package lenguajesBD_Grupo06.Repository;

import exception.GymCoreException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
 
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
 
/**
 * Acceso al cuerpo transaccional de la base. Todo lo que implique reglas de
 * negocio (reservar, cobrar, validar membresia) pasa por aqui y no por JPA.
 *
 * Importante: SP_REGISTRAR_PAGO y SP_RESERVAR_CLASE hacen COMMIT y ROLLBACK
 * internamente. Por eso estos metodos NO deben llamarse dentro de un
 * @Transactional de Spring: el COMMIT del PL/SQL cerraria tambien la
 * transaccion de Hibernate y dejaria el EntityManager en un estado invalido.
 * Llamalos desde un servicio sin transaccion, o marcado como
 * @Transactional(propagation = Propagation.NOT_SUPPORTED).
 */
@Repository
public class GymCoreProcedimientosRepository {
 
    private final JdbcTemplate jdbcTemplate;
 
    public GymCoreProcedimientosRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
 
    // ------------------------------------------------------------------
    // FUNCIONES
    // ------------------------------------------------------------------
 
    /**
     * FN_MEMBRESIA_ACTIVA. Devuelve true si el cliente tiene una membresia
     * vigente, con al menos un pago completado y sin pagos pendientes.
     * Esta es la regla que decide el control de acceso al gimnasio.
     */
    public boolean membresiaActiva(Long idCliente) {
        return "S".equals(llamarFuncionTexto("fn_membresia_activa", idCliente));
    }
 
    /** FN_USUARIO_TIENE_ROL. Comprueba un rol de aplicacion activo. */
    public boolean usuarioTieneRol(Long idUsuario, String nombreRol) {
        String salida = jdbcTemplate.execute(
                (java.sql.Connection con) -> {
                    CallableStatement cs = con.prepareCall("{? = call fn_usuario_tiene_rol(?,?)}");
                    cs.registerOutParameter(1, Types.VARCHAR);
                    cs.setLong(2, idUsuario);
                    cs.setString(3, nombreRol);
                    return cs;
                },
                (CallableStatement cs) -> {
                    try {
                        cs.execute();
                        return cs.getString(1);
                    } catch (SQLException e) {
                        throw traducir(e);
                    }
                });
        return "S".equals(salida);
    }
 
    /**
     * FN_CUPO_DISPONIBLE. Cupos restantes de la clase.
     * Lanza GymCoreException(20008) si la clase no existe.
     */
    public int cupoDisponible(Long idClase) {
        Integer cupo = jdbcTemplate.execute(
                (java.sql.Connection con) -> {
                    CallableStatement cs = con.prepareCall("{? = call fn_cupo_disponible(?)}");
                    cs.registerOutParameter(1, Types.NUMERIC);
                    cs.setLong(2, idClase);
                    return cs;
                },
                (CallableStatement cs) -> {
                    try {
                        cs.execute();
                        return cs.getInt(1);
                    } catch (SQLException e) {
                        throw traducir(e);
                    }
                });
        return cupo == null ? 0 : cupo;
    }
 
    // ------------------------------------------------------------------
    // PROCEDIMIENTOS
    // ------------------------------------------------------------------
 
    /**
     * SP_REGISTRAR_PAGO. Inserta el pago y confirma. El trigger
     * TRG_PAGO_SYNC_MEMBRESIA recalcula despues el estado de la membresia.
     *
     * @throws GymCoreException 20007 si la membresia no existe.
     */
    public void registrarPago(Long idMembresia, BigDecimal monto,
                              String metodoPago, String estado) {
        jdbcTemplate.execute(
                (java.sql.Connection con) -> {
                    CallableStatement cs = con.prepareCall("{call sp_registrar_pago(?,?,?,?)}");
                    cs.setLong(1, idMembresia);
                    cs.setBigDecimal(2, monto);
                    cs.setString(3, metodoPago);
                    cs.setString(4, estado);
                    return cs;
                },
                (CallableStatement cs) -> {
                    try {
                        cs.execute();
                        return null;
                    } catch (SQLException e) {
                        throw traducir(e);
                    }
                });
    }
 
    public void registrarPago(Long idMembresia, BigDecimal monto, String metodoPago) {
        registrarPago(idMembresia, monto, metodoPago, "completado");
    }
 
    /**
     * SP_RESERVAR_CLASE. El bloqueo FOR UPDATE dentro del procedimiento
     * serializa las reservas de la misma clase y evita sobre-reservas.
     *
     * @return el mensaje de confirmacion que devuelve el parametro OUT.
     * @throws GymCoreException 20002 sin membresia activa y pagada,
     *                          20004 cupo maximo alcanzado,
     *                          20005 ya tiene una reserva confirmada,
     *                          20009 cliente activo o clase no encontrados.
     */
    public String reservarClase(String cedula, Long idClase) {
        return jdbcTemplate.execute(
                (java.sql.Connection con) -> {
                    CallableStatement cs = con.prepareCall("{call sp_reservar_clase(?,?,?)}");
                    cs.setString(1, cedula);
                    cs.setLong(2, idClase);
                    cs.registerOutParameter(3, Types.VARCHAR);
                    return cs;
                },
                (CallableStatement cs) -> {
                    try {
                        cs.execute();
                        return cs.getString(3);
                    } catch (SQLException e) {
                        throw traducir(e);
                    }
                });
    }
 
    // ------------------------------------------------------------------
    // Traduccion de errores
    // ------------------------------------------------------------------
 
    /**
     * Convierte un ORA-200xx en GymCoreException con el mensaje limpio.
     * Oracle entrega el codigo 20002 en getErrorCode() y el texto en la
     * forma "ORA-20002: El cliente no tiene membresia activa y pagada.".
     */
    private RuntimeException traducir(SQLException e) {
        int codigo = e.getErrorCode();
        if (codigo >= 20001 && codigo <= 20999) {
            return new GymCoreException(codigo, limpiarMensaje(e.getMessage()), e);
        }
        return new GymCoreException(codigo,
                "Error inesperado en la base de datos: " + e.getMessage(), e);
    }
 
    private String limpiarMensaje(String mensaje) {
        if (mensaje == null) {
            return "Error de negocio sin detalle.";
        }
        // Quita el prefijo "ORA-20002: " y corta la traza ORA-06512 que Oracle anexa.
        String limpio = mensaje.replaceFirst("^ORA-\\d{5}:\\s*", "");
        int corte = limpio.indexOf("ORA-06512");
        if (corte > 0) {
            limpio = limpio.substring(0, corte);
        }
        return limpio.trim();
    }
 
    private String llamarFuncionTexto(String nombreFuncion, Long parametro) {
        return jdbcTemplate.execute(
                (java.sql.Connection con) -> {
                    CallableStatement cs = con.prepareCall("{? = call " + nombreFuncion + "(?)}");
                    cs.registerOutParameter(1, Types.VARCHAR);
                    cs.setLong(2, parametro);
                    return cs;
                },
                (CallableStatement cs) -> {
                    try {
                        cs.execute();
                        return cs.getString(1);
                    } catch (SQLException e) {
                        throw traducir(e);
                    }
                });
    }
}
 