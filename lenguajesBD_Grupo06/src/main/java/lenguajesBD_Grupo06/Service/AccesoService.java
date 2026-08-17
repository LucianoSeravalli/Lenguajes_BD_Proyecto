package lenguajesBD_Grupo06.Service;

import lenguajesBD_Grupo06.Domain.Acceso;
import lenguajesBD_Grupo06.Domain.Cliente;
import lenguajesBD_Grupo06.exception.GymCoreException;
import lenguajesBD_Grupo06.Repository.AccesoRepository;
import lenguajesBD_Grupo06.Repository.ClienteRepository;
import lenguajesBD_Grupo06.Repository.GymCoreProcedimientosRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
 
/**
 * Control de acceso al gimnasio: el proceso transaccional central del sistema.
 * La decision la toma FN_MEMBRESIA_ACTIVA, que exige membresia vigente, al
 * menos un pago completado y ningun pago pendiente.
 *
 * El intento se registra siempre, permitido o denegado, porque la bitacora
 * de accesos denegados es lo que permite auditar despues.
 */
@Service
public class AccesoService {
 
    private final AccesoRepository accesoRepository;
    private final ClienteRepository clienteRepository;
    private final GymCoreProcedimientosRepository procedimientos;
 
    public AccesoService(AccesoRepository accesoRepository,
                         ClienteRepository clienteRepository,
                         GymCoreProcedimientosRepository procedimientos) {
        this.accesoRepository = accesoRepository;
        this.clienteRepository = clienteRepository;
        this.procedimientos = procedimientos;
    }
 
    /**
     * Las funciones PL/SQL solo leen y no hacen COMMIT, asi que este metodo
     * si puede ser transaccional: la consulta y el INSERT del registro de
     * acceso viajan juntos.
     *
     * @return el acceso registrado, con resultado 'permitido' o 'denegado'.
     */
    @Transactional
    public Acceso registrarEntrada(String cedula) {
        Cliente cliente = clienteRepository.findByCedula(cedula)
                .orElseThrow(() -> new GymCoreException(20009,
                        "No existe un cliente con la cedula " + cedula + "."));
 
        boolean permitido = "activo".equals(cliente.getEstado())
                && procedimientos.membresiaActiva(cliente.getIdCliente());
 
        return guardar(cliente, "entrada", permitido ? "permitido" : "denegado");
    }
 
    /** La salida no valida membresia: si entro, tiene que poder salir. */
    @Transactional
    public Acceso registrarSalida(String cedula) {
        Cliente cliente = clienteRepository.findByCedula(cedula)
                .orElseThrow(() -> new GymCoreException(20009,
                        "No existe un cliente con la cedula " + cedula + "."));
        return guardar(cliente, "salida", "permitido");
    }
 
    /** Consulta sin registrar nada, para la pantalla de recepcion. */
    @Transactional(readOnly = true)
    public boolean puedeIngresar(String cedula) {
        return clienteRepository.findByCedulaAndEstado(cedula, "activo")
                .map(c -> procedimientos.membresiaActiva(c.getIdCliente()))
                .orElse(false);
    }
 
    @Transactional(readOnly = true)
    public List<Acceso> historialDe(Long idCliente) {
        return accesoRepository.findByClienteIdClienteOrderByFechaHoraDesc(idCliente);
    }
 
    @Transactional(readOnly = true)
    public List<Acceso> delDia(LocalDate fecha) {
        return accesoRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
    }
 
    @Transactional(readOnly = true)
    public long denegadosEntre(LocalDate desde, LocalDate hasta) {
        return accesoRepository.contarPorResultado("denegado",
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }
 
    private Acceso guardar(Cliente cliente, String tipo, String resultado) {
        Acceso acceso = new Acceso();
        acceso.setCliente(cliente);
        acceso.setFechaHora(LocalDateTime.now());
        acceso.setTipo(tipo);
        acceso.setResultado(resultado);
        return accesoRepository.save(acceso);
    }
}
 