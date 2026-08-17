package lenguajesBD_Grupo06.Service;


import lenguajesBD_Grupo06.Domain.Reserva;
import lenguajesBD_Grupo06.exception.GymCoreException;
import lenguajesBD_Grupo06.Repository.GymCoreProcedimientosRepository;
import lenguajesBD_Grupo06.Repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * El alta de reservas pasa siempre por SP_RESERVAR_CLASE. Ahi estan el
 * SELECT FOR UPDATE que serializa las reservas de la misma clase y las
 * validaciones de membresia, cupo y duplicados. Insertar con JPA se saltaria
 * todo eso y permitiria sobre-reservas bajo concurrencia.
 */
@Service
public class ReservaService {

    private final GymCoreProcedimientosRepository procedimientos;
    private final ReservaRepository reservaRepository;

    public ReservaService(GymCoreProcedimientosRepository procedimientos,
                          ReservaRepository reservaRepository) {
        this.procedimientos = procedimientos;
        this.reservaRepository = reservaRepository;
    }

    /**
     * NOT_SUPPORTED por el COMMIT interno del procedimiento.
     *
     * @return mensaje de confirmacion del parametro OUT.
     * @throws GymCoreException 20002 sin membresia activa y pagada,
     *                          20004 cupo lleno, 20005 reserva duplicada,
     *                          20009 cliente activo o clase no encontrados.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String reservar(String cedula, Long idClase) {
        return procedimientos.reservarClase(cedula, idClase);
    }

    @Transactional(readOnly = true)
    public int cuposDisponibles(Long idClase) {
        return procedimientos.cupoDisponible(idClase);
    }

    /**
     * Cancelar si se puede hacer por JPA: libera el cupo al salir del indice
     * unico UQ_RESERVA_CONFIRMADA y no toca reglas de concurrencia.
     */
    @Transactional
    public void cancelar(Long idReserva) {
        Reserva reserva = buscarPorId(idReserva);
        if (!"confirmada".equals(reserva.getEstado())) {
            throw new GymCoreException(0, "Solo se pueden cancelar reservas confirmadas.");
        }
        reserva.setEstado("cancelada");
        reservaRepository.save(reserva);
    }

    /** Marca la asistencia cuando el cliente se presenta a la clase. */
    @Transactional
    public void marcarAsistencia(Long idReserva) {
        Reserva reserva = buscarPorId(idReserva);
        if (!"confirmada".equals(reserva.getEstado())) {
            throw new GymCoreException(0, "La reserva no esta confirmada.");
        }
        reserva.setEstado("asistio");
        reservaRepository.save(reserva);
    }

    @Transactional(readOnly = true)
    public Reserva buscarPorId(Long idReserva) {
        return reservaRepository.findById(idReserva)
                .orElseThrow(() -> new GymCoreException(0, "La reserva no existe."));
    }

    @Transactional(readOnly = true)
    public List<Reserva> confirmadasDeCliente(Long idCliente) {
        return reservaRepository.findPorClienteYEstado(idCliente, "confirmada");
    }

    /** Lista de asistentes para el entrenador. */
    @Transactional(readOnly = true)
    public List<Reserva> listaDeClase(Long idClase) {
        return reservaRepository.findConfirmadasDeClase(idClase);
    }
}