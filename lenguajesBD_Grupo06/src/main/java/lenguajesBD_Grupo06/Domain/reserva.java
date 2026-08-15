
package lenguajesBD_Grupo06.Domain;


import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDate;
 
/**
 * Las altas deben hacerse por SP_RESERVAR_CLASE: valida membresia
 * (ORA-20002), cupo (ORA-20004) y duplicados (ORA-20005), y usa
 * SELECT ... FOR UPDATE para evitar sobre-reservas concurrentes.
 */
@Entity
@Table(name = "RESERVA")
@Getter @Setter @NoArgsConstructor
public class Reserva {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RESERVA")
    private Long idReserva;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CLASE", nullable = false)
    private Clase clase;
 
    @Column(name = "FECHA_RESERVA", nullable = false)
    private LocalDate fechaReserva;
 
    /** confirmada | cancelada | asistio */
    @Column(name = "ESTADO", nullable = false, length = 12)
    private String estado = "confirmada";
}