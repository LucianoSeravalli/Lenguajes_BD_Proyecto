
package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDate;
 
/**
 * El ESTADO lo mantienen los triggers TRG_MEMBRESIA_FECHA_ESTADO
 * y TRG_PAGO_SYNC_MEMBRESIA, no la capa Java.
 */
@Entity
@Table(name = "MEMBRESIA")
@Getter @Setter @NoArgsConstructor
public class Membresia {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MEMBRESIA")
    private Long idMembresia;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TIPO_MEMBRESIA", nullable = false)
    private TipoMembresia tipoMembresia;
 
    @Column(name = "FECHA_INICIO", nullable = false)
    private LocalDate fechaInicio;
 
    @Column(name = "FECHA_FIN", nullable = false)
    private LocalDate fechaFin;
 
    /** activa | vencida | cancelada */
    @Column(name = "ESTADO", nullable = false, length = 10)
    private String estado = "activa";
}