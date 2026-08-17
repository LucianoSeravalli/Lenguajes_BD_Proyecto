package lenguajesBD_Grupo06.web.dto;

import jakarta.validation.constraints.*;
 
import java.math.BigDecimal;
import java.time.LocalDate;
 
/**
 * Cuerpos de peticion. Se usan records para que sean inmutables y para no
 * exponer las entidades JPA directamente al frontend.
 */
public final class Requests {
 
    private Requests() {}
 
    public record RegistroCliente(
            @NotBlank @Size(max = 50) String nombre,
            @NotBlank @Size(max = 50) String apellido,
            @Size(max = 20) String telefono,
            @NotBlank @Email @Size(max = 150) String correo,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 20) String cedula,
            @Past LocalDate fechaNacimiento
    ) {}
 
    public record RegistroEntrenador(
            @NotBlank @Size(max = 50) String nombre,
            @NotBlank @Size(max = 50) String apellido,
            @Size(max = 20) String telefono,
            @NotBlank @Email @Size(max = 150) String correo,
            @NotBlank @Size(min = 8, max = 72) String password,
            @Size(max = 50) String especialidad,
            LocalDate fechaContratacion
    ) {}
 
    public record Login(
            @NotBlank @Email String correo,
            @NotBlank String password
    ) {}
 
    public record ActualizarCliente(
            @NotBlank @Size(max = 50) String nombre,
            @NotBlank @Size(max = 50) String apellido,
            @Size(max = 20) String telefono,
            @Past LocalDate fechaNacimiento
    ) {}
 
    public record ContratarMembresia(
            @NotNull Long idCliente,
            @NotNull Long idTipoMembresia,
            LocalDate fechaInicio
    ) {}
 
    public record RegistrarPago(
            @NotNull Long idMembresia,
            @NotNull @DecimalMin(value = "0.01") BigDecimal monto,
            @NotBlank @Pattern(regexp = "efectivo|tarjeta|transferencia|sinpe")
            String metodoPago,
            @Pattern(regexp = "completado|pendiente") String estado
    ) {}
 
    public record GuardarClase(
            @NotBlank @Size(max = 60) String nombre,
            @Size(max = 300) String descripcion,
            @NotNull Long idEntrenador,
            @NotNull @Min(1) Integer cupoMaximo,
            @NotBlank @Pattern(regexp = "lunes|martes|miercoles|jueves|viernes|sabado|domingo")
            String diaSemana,
            @NotBlank @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$") String horaInicio,
            @NotBlank @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$") String horaFin
    ) {}
 
    public record CrearReserva(
            @NotBlank @Size(max = 20) String cedula,
            @NotNull Long idClase
    ) {}
 
    public record MarcarAcceso(
            @NotBlank @Size(max = 20) String cedula
    ) {}
 
    public record CrearRutina(
            @NotBlank @Size(max = 60) String nombre,
            @NotBlank @Pattern(regexp = "principiante|intermedio|avanzado") String nivel,
            @NotNull Long idEntrenador
    ) {}
 
    public record AgregarEjercicio(
            @NotNull Long idEjercicio,
            @NotNull @Min(1) Integer series,
            @NotNull @Min(1) Integer repeticiones,
            @Min(0) Integer descansoSegundos
    ) {}
 
    public record AsignarRutina(
            @NotNull Long idCliente,
            LocalDate fechaFin
    ) {}
}