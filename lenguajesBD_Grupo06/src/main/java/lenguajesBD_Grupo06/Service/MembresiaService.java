package lenguajesBD_Grupo06.Service;

import lenguajesBD_Grupo06.Domain.Cliente;
import lenguajesBD_Grupo06.Domain.Membresia;
import lenguajesBD_Grupo06.Domain.TipoMembresia;
import lenguajesBD_Grupo06.exception.GymCoreException;
import lenguajesBD_Grupo06.Repository.ClienteRepository;
import lenguajesBD_Grupo06.Repository.MembresiaRepository;
import lenguajesBD_Grupo06.Repository.TipoMembresiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
 
/**
 * El campo ESTADO no se asigna a mano salvo en la cancelacion:
 * TRG_MEMBRESIA_FECHA_ESTADO y TRG_PAGO_SYNC_MEMBRESIA lo mantienen.
 */
@Service
public class MembresiaService {
 
    private final MembresiaRepository membresiaRepository;
    private final TipoMembresiaRepository tipoMembresiaRepository;
    private final ClienteRepository clienteRepository;
 
    public MembresiaService(MembresiaRepository membresiaRepository,
                            TipoMembresiaRepository tipoMembresiaRepository,
                            ClienteRepository clienteRepository) {
        this.membresiaRepository = membresiaRepository;
        this.tipoMembresiaRepository = tipoMembresiaRepository;
        this.clienteRepository = clienteRepository;
    }
 
    /** La fecha de fin sale de DURACION_MESES del tipo elegido. */
    @Transactional
    public Membresia contratar(Long idCliente, Long idTipoMembresia, LocalDate fechaInicio) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new GymCoreException(0, "El cliente no existe."));
 
        if (!"activo".equals(cliente.getEstado())) {
            throw new GymCoreException(0, "No se puede contratar para un cliente inactivo.");
        }
 
        TipoMembresia tipo = tipoMembresiaRepository.findById(idTipoMembresia)
                .orElseThrow(() -> new GymCoreException(0, "El tipo de membresia no existe."));
 
        LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.now();
 
        Membresia membresia = new Membresia();
        membresia.setCliente(cliente);
        membresia.setTipoMembresia(tipo);
        membresia.setFechaInicio(inicio);
        membresia.setFechaFin(inicio.plusMonths(tipo.getDuracionMeses()));
        membresia.setEstado("activa");
        return membresiaRepository.save(membresia);
    }
 
    /** Renovacion: arranca al dia siguiente del vencimiento si aun esta vigente. */
    @Transactional
    public Membresia renovar(Long idMembresia) {
        Membresia anterior = buscarPorId(idMembresia);
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = anterior.getFechaFin().isAfter(hoy)
                ? anterior.getFechaFin().plusDays(1)
                : hoy;
        return contratar(anterior.getCliente().getIdCliente(),
                anterior.getTipoMembresia().getIdTipoMembresia(), inicio);
    }
 
    @Transactional
    public void cancelar(Long idMembresia) {
        Membresia membresia = buscarPorId(idMembresia);
        membresia.setEstado("cancelada");
        membresiaRepository.save(membresia);
    }
 
    @Transactional(readOnly = true)
    public Membresia buscarPorId(Long idMembresia) {
        return membresiaRepository.findById(idMembresia)
                .orElseThrow(() -> new GymCoreException(20007, "La membresia no existe."));
    }
 
    @Transactional(readOnly = true)
    public List<Membresia> historialDe(Long idCliente) {
        return membresiaRepository.findByClienteIdClienteOrderByFechaFinDesc(idCliente);
    }
 
    @Transactional(readOnly = true)
    public Optional<Membresia> vigenteDe(Long idCliente) {
        return membresiaRepository.findVigenteActual(idCliente);
    }
 
    /** Para avisar de renovaciones proximas. */
    @Transactional(readOnly = true)
    public List<Membresia> porVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        return membresiaRepository.findByEstadoAndFechaFinBetween("activa", hoy, hoy.plusDays(dias));
    }
 
    @Transactional(readOnly = true)
    public List<TipoMembresia> listarTipos() {
        return tipoMembresiaRepository.findAllByOrderByDuracionMesesAsc();
    }
}