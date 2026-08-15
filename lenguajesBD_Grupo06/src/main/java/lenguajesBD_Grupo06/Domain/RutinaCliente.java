package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDate;
 
/** Asignacion de una rutina a un cliente durante un periodo. */
@Entity
@Table(name = "RUTINA_CLIENTE")
@Getter @Setter @NoArgsConstructor
public class RutinaCliente {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RUTINA_CLIENTE")
    private Long idRutinaCliente;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_RUTINA", nullable = false)
    private Rutina rutina;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;
 
    @Column(name = "FECHA_ASIGNACION", nullable = false)
    private LocalDate fechaAsignacion;
 
    @Column(name = "FECHA_FIN")
    private LocalDate fechaFin;
}
 