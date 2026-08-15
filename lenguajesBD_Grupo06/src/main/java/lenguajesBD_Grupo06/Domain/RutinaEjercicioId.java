
package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
 
import java.io.Serializable;
 
/** Llave primaria compuesta de RUTINA_EJERCICIO. */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class RutinaEjercicioId implements Serializable {
 
    @Column(name = "ID_RUTINA")
    private Long idRutina;
 
    @Column(name = "ID_EJERCICIO")
    private Long idEjercicio;
}
 