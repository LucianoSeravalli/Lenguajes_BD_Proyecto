package lenguajesBD_Grupo06.Domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tabla unica de personas. Un ENTRENADOR es un USUARIO con rol ENTRENADOR
 * (por eso RUTINA.ID_ENTRENADOR y CLASE.ID_ENTRENADOR apuntan aqui).
 * Un CLIENTE es un USUARIO con rol CLIENTE mas su fila en CLIENTE.
 */
@Entity
@Table(name = "USUARIO")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;

    @Column(name = "APELLIDO", nullable = false, length = 50)
    private String apellido;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    @Column(name = "CORREO", nullable = false, length = 150)
    private String correo;

    /** Hash BCrypt/Argon2id. Nunca se expone al frontend ni se guarda en claro. */
    @JsonIgnore
    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    @JsonIgnore
    @Column(name = "TOKEN_CONFIRMACION_HASH", length = 255)
    private String tokenConfirmacionHash;

    @JsonIgnore
    @Column(name = "TOKEN_EXPIRACION")
    private LocalDateTime tokenExpiracion;

    /** 'S' / 'N' */
    @Column(name = "CORREO_VERIFICADO", nullable = false, length = 1)
    private String correoVerificado = "N";

    /** activo | inactivo | bloqueado */
    @Column(name = "ESTADO", nullable = false, length = 10)
    private String estado = "activo";

    /** Solo aplica a entrenadores. */
    @Column(name = "ESPECIALIDAD", length = 50)
    private String especialidad;

    @Column(name = "FECHA_CONTRATACION")
    private LocalDate fechaContratacion;

    @Column(name = "FECHA_CREACION", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** La actualiza TRG_USUARIO_FECHA_ACTUALIZA. */
    @Column(name = "FECHA_ACTUALIZACION", insertable = false, updatable = false)
    private LocalDateTime fechaActualizacion;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}