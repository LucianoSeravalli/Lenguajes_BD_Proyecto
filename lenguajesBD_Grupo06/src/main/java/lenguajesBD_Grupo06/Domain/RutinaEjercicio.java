
package lenguajesBD_Grupo06.Domain;


import jakarta.persistence.*;
import lombok.*;
 
/** Tabla puente N:M entre RUTINA y EJERCICIO con atributos propios. */
@Entity
@Table(name = "RUTINA_EJERCICIO")
@Getter @Setter @NoArgsConstructor
public class RutinaEjercicio {
 
    @EmbeddedId
    private RutinaEjercicioId id;
 
    @MapsId("idRutina")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_RUTINA")
    private Rutina rutina;
 
    @MapsId("idEjercicio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EJERCICIO")
    private Ejercicio ejercicio;
 
    @Column(name = "SERIES", nullable = false)
    private Integer series;
 
    @Column(name = "REPETICIONES", nullable = false)
    private Integer repeticiones;
 
    @Column(name = "DESCANSO_SEGUNDOS", nullable = false)
    private Integer descansoSegundos = 60;
}