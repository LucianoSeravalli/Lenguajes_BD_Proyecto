package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
 
import java.io.Serializable;
 
/** Llave primaria compuesta de USUARIO_ROL. */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class UsuarioRolId implements Serializable {
 
    @Column(name = "ID_USUARIO")
    private Long idUsuario;
 
    @Column(name = "ID_ROL")
    private Long idRol;
}
 