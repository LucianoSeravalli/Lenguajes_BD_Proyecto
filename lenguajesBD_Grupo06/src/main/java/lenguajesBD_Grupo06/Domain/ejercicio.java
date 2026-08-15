
package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "EJERCICIO")
@Getter @Setter @NoArgsConstructor
public class Ejercicio {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EJERCICIO")
    private Long idEjercicio;
 
    @Column(name = "NOMBRE", nullable = false, length = 80)
    private String nombre;
 
    @Column(name = "DESCRIPCION", length = 300)
    private String descripcion;
 
    @Column(name = "GRUPO_MUSCULAR", length = 40)
    private String grupoMuscular;
}
 