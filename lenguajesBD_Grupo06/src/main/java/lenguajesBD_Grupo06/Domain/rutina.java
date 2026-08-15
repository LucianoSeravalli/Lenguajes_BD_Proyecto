
package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.*;
import lombok.*;
 
/**
 * TRG_RUTINA_VALIDAR_ENTRENADOR exige que el usuario asignado
 * tenga rol ENTRENADOR activo (ORA-20010).
 */
@Entity
@Table(name = "RUTINA")
@Getter @Setter @NoArgsConstructor
public class Rutina {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RUTINA")
    private Long idRutina;
 
    @Column(name = "NOMBRE", nullable = false, length = 60)
    private String nombre;
 
    /** principiante | intermedio | avanzado */
    @Column(name = "NIVEL", nullable = false, length = 15)
    private String nivel;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ENTRENADOR", nullable = false)
    private Usuario entrenador;
}
