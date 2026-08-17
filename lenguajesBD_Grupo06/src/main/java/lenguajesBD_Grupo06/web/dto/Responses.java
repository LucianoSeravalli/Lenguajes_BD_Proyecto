
package lenguajesBD_Grupo06.web.dto;

import lenguajesBD_Grupo06.Domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Vistas de salida. Ninguna expone PASSWORD_HASH ni los tokens de
 * confirmacion, aunque la entidad los tenga marcados con @JsonIgnore.
 */
public final class Responses {

    private Responses() {}

    public record UsuarioDto(Long idUsuario, String nombre, String apellido,
                             String telefono, String correo, String estado,
                             String correoVerificado, String especialidad) {

        public static UsuarioDto de(Usuario u) {
            return new UsuarioDto(u.getIdUsuario(), u.getNombre(), u.getApellido(),
                    u.getTelefono(), u.getCorreo(), u.getEstado(),
                    u.getCorreoVerificado(), u.getEspecialidad());
        }
    }

    public record ClienteDto(Long idCliente, Long idUsuario, String nombre, String apellido,
                             String cedula, String telefono, String correo,
                             LocalDate fechaNacimiento, LocalDate fechaRegistro,
                             String estado) {

        public static ClienteDto de(Cliente c) {
            Usuario u = c.getUsuario();
            return new ClienteDto(c.getIdCliente(), u.getIdUsuario(), u.getNombre(),
                    u.getApellido(), c.getCedula(), u.getTelefono(), u.getCorreo(),
                    c.getFechaNacimiento(), c.getFechaRegistro(), c.getEstado());
        }
    }

    public record MembresiaDto(Long idMembresia, Long idCliente, String tipo,
                               LocalDate fechaInicio, LocalDate fechaFin,
                               String estado, BigDecimal precio) {

        public static MembresiaDto de(Membresia m) {
            return new MembresiaDto(m.getIdMembresia(), m.getCliente().getIdCliente(),
                    m.getTipoMembresia().getNombre(), m.getFechaInicio(), m.getFechaFin(),
                    m.getEstado(), m.getTipoMembresia().getPrecio());
        }
    }

    public record TipoMembresiaDto(Long idTipoMembresia, String nombre,
                                   Integer duracionMeses, BigDecimal precio,
                                   String beneficios) {

        public static TipoMembresiaDto de(TipoMembresia t) {
            return new TipoMembresiaDto(t.getIdTipoMembresia(), t.getNombre(),
                    t.getDuracionMeses(), t.getPrecio(), t.getBeneficios());
        }
    }

    public record PagoDto(Long idPago, Long idMembresia, BigDecimal monto,
                          LocalDate fechaPago, String metodoPago, String estado) {

        public static PagoDto de(Pago p) {
            return new PagoDto(p.getIdPago(), p.getMembresia().getIdMembresia(),
                    p.getMonto(), p.getFechaPago(), p.getMetodoPago(), p.getEstado());
        }
    }

    public record ClaseDto(Long idClase, String nombre, String descripcion,
                           String entrenador, Long idEntrenador, Integer cupoMaximo,
                           Integer cuposDisponibles, String diaSemana,
                           String horaInicio, String horaFin) {

        public static ClaseDto de(Clase c, Integer cuposDisponibles) {
            return new ClaseDto(c.getIdClase(), c.getNombre(), c.getDescripcion(),
                    c.getEntrenador().getNombreCompleto(), c.getEntrenador().getIdUsuario(),
                    c.getCupoMaximo(), cuposDisponibles, c.getDiaSemana(),
                    c.getHoraInicio(), c.getHoraFin());
        }
    }

    public record ReservaDto(Long idReserva, Long idCliente, Long idClase,
                             String clase, LocalDate fechaReserva, String estado) {

        public static ReservaDto de(Reserva r) {
            return new ReservaDto(r.getIdReserva(), r.getCliente().getIdCliente(),
                    r.getClase().getIdClase(), r.getClase().getNombre(),
                    r.getFechaReserva(), r.getEstado());
        }
    }

    public record AccesoDto(Long idAcceso, Long idCliente, String cliente,
                            LocalDateTime fechaHora, String tipo, String resultado) {

        public static AccesoDto de(Acceso a) {
            return new AccesoDto(a.getIdAcceso(), a.getCliente().getIdCliente(),
                    a.getCliente().getUsuario().getNombreCompleto(),
                    a.getFechaHora(), a.getTipo(), a.getResultado());
        }
    }

    public record RutinaDto(Long idRutina, String nombre, String nivel,
                            String entrenador, Long idEntrenador) {

        public static RutinaDto de(Rutina r) {
            return new RutinaDto(r.getIdRutina(), r.getNombre(), r.getNivel(),
                    r.getEntrenador().getNombreCompleto(), r.getEntrenador().getIdUsuario());
        }
    }

    public record RutinaEjercicioDto(Long idEjercicio, String ejercicio,
                                     String grupoMuscular, Integer series,
                                     Integer repeticiones, Integer descansoSegundos) {

        public static RutinaEjercicioDto de(RutinaEjercicio re) {
            return new RutinaEjercicioDto(re.getEjercicio().getIdEjercicio(),
                    re.getEjercicio().getNombre(), re.getEjercicio().getGrupoMuscular(),
                    re.getSeries(), re.getRepeticiones(), re.getDescansoSegundos());
        }
    }

    public record RutinaAsignadaDto(Long idRutinaCliente, Long idRutina, String rutina,
                                    String nivel, LocalDate fechaAsignacion,
                                    LocalDate fechaFin) {

        public static RutinaAsignadaDto de(RutinaCliente rc) {
            return new RutinaAsignadaDto(rc.getIdRutinaCliente(),
                    rc.getRutina().getIdRutina(), rc.getRutina().getNombre(),
                    rc.getRutina().getNivel(), rc.getFechaAsignacion(), rc.getFechaFin());
        }
    }

    /** Respuesta del control de acceso en recepcion. */
    public record ResultadoAcceso(boolean permitido, String mensaje, AccesoDto acceso) {}

    public record Mensaje(String mensaje) {}

    public record ErrorDto(int estado, String error, String mensaje, Integer codigoOracle) {}
}