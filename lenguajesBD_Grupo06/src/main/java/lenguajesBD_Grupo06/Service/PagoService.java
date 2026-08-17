package lenguajesBD_Grupo06.Service;

import lenguajesBD_Grupo06.Domain.Pago;
import lenguajesBD_Grupo06.Repository.GymCoreProcedimientosRepository;
import lenguajesBD_Grupo06.Repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
 
/**
 * El registro de pagos pasa siempre por SP_REGISTRAR_PAGO, nunca por
 * pagoRepository.save(): el procedimiento dispara TRG_PAGO_SYNC_MEMBRESIA,
 * que recalcula el estado de la membresia.
 */
@Service
public class PagoService {
 
    private final GymCoreProcedimientosRepository procedimientos;
    private final PagoRepository pagoRepository;
 
    public PagoService(GymCoreProcedimientosRepository procedimientos,
                       PagoRepository pagoRepository) {
        this.procedimientos = procedimientos;
        this.pagoRepository = pagoRepository;
    }
 
    /**
     * NOT_SUPPORTED porque el procedimiento hace su propio COMMIT: si corriera
     * dentro de una transaccion de Spring, ese COMMIT la cerraria y dejaria el
     * EntityManager en un estado invalido.
     *
     * Lanza GymCoreException(20007) si la membresia no existe.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void registrar(Long idMembresia, BigDecimal monto, String metodoPago) {
        procedimientos.registrarPago(idMembresia, monto, metodoPago, "completado");
    }
 
    /** Pago a cuotas: mientras quede uno pendiente, la membresia queda vencida. */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void registrarPendiente(Long idMembresia, BigDecimal monto, String metodoPago) {
        procedimientos.registrarPago(idMembresia, monto, metodoPago, "pendiente");
    }
 
    @Transactional(readOnly = true)
    public List<Pago> historialDeCliente(Long idCliente) {
        return pagoRepository.findHistorialPorCliente(idCliente);
    }
 
    @Transactional(readOnly = true)
    public List<Pago> deMembresia(Long idMembresia) {
        return pagoRepository.findByMembresiaIdMembresiaOrderByFechaPagoDesc(idMembresia);
    }
 
    @Transactional(readOnly = true)
    public boolean tienePendientes(Long idMembresia) {
        return pagoRepository.existsByMembresiaIdMembresiaAndEstado(idMembresia, "pendiente");
    }
 
    @Transactional(readOnly = true)
    public BigDecimal recaudadoEntre(LocalDate desde, LocalDate hasta) {
        return pagoRepository.totalRecaudado(desde, hasta);
    }
}