package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.LocalDate;
 
/**
 * Las altas deben hacerse por SP_REGISTRAR_PAGO para que
 * TRG_PAGO_SYNC_MEMBRESIA recalcule el estado de la membresia.
 */
@Entity
@Table(name = "PAGO")
@Getter @Setter @NoArgsConstructor
public class Pago {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PAGO")
    private Long idPago;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_MEMBRESIA", nullable = false)
    private Membresia membresia;
 
    @Column(name = "MONTO", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;
 
    @Column(name = "FECHA_PAGO", nullable = false)
    private LocalDate fechaPago;
 
    /** efectivo | tarjeta | transferencia | sinpe */
    @Column(name = "METODO_PAGO", nullable = false, length = 20)
    private String metodoPago;
 
    /** completado | pendiente */
    @Column(name = "ESTADO", nullable = false, length = 15)
    private String estado = "completado";
}