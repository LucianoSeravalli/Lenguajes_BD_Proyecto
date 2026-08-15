package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
/**
 * Asignacion de roles de aplicacion.
 * TRG_USUARIO_ROL_PROTEGER impide retirar un rol si el usuario
 * conserva perfil de cliente o rutinas/clases asignadas (ORA-20012).
 */
@Entity
@Table(name = "USUARIO_ROL")
@Getter @Setter @NoArgsConstructor
public class Usuario_rol {
 
    @EmbeddedId
    private UsuarioRolId id;
 
    @MapsId("idUsuario")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIO")
    private Usuario usuario;
 
    @MapsId("idRol")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ROL")
    private AppRol rol;
 
    @Column(name = "FECHA_ASIGNACION", insertable = false, updatable = false)
    private LocalDateTime fechaAsignacion;
 
    public Usuario_rol (Usuario usuario, AppRol rol) {
        this.usuario = usuario;
        this.rol = rol;
        this.id = new UsuarioRolId(usuario.getIdUsuario(), rol.getIdRol());
    }
}
