package lenguajesBD_Grupo06.Service;

import lenguajesBD_Grupo06.Domain.*;
import lenguajesBD_Grupo06.exception.GymCoreException;
import lenguajesBD_Grupo06.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDate;
import java.util.List;
 
/**
 * Rutinas, sus ejercicios y la asignacion a clientes.
 * TRG_RUTINA_VALIDAR_ENTRENADOR rechaza con ORA-20010 si el usuario
 * asignado no es un entrenador activo.
 */
@Service
public class RutinaService {
 
    private final RutinaRepository rutinaRepository;
    private final RutinaEjercicioRepository rutinaEjercicioRepository;
    private final RutinaClienteRepository rutinaClienteRepository;
    private final EjercicioRepository ejercicioRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
 
    public RutinaService(RutinaRepository rutinaRepository,
                         RutinaEjercicioRepository rutinaEjercicioRepository,
                         RutinaClienteRepository rutinaClienteRepository,
                         EjercicioRepository ejercicioRepository,
                         ClienteRepository clienteRepository,
                         UsuarioRepository usuarioRepository) {
        this.rutinaRepository = rutinaRepository;
        this.rutinaEjercicioRepository = rutinaEjercicioRepository;
        this.rutinaClienteRepository = rutinaClienteRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }
 
    @Transactional
    public Rutina crear(String nombre, String nivel, Long idEntrenador) {
        Usuario entrenador = usuarioRepository.findById(idEntrenador)
                .orElseThrow(() -> new GymCoreException(0, "El entrenador no existe."));
        if (!usuarioRepository.tieneRol(idEntrenador, "ENTRENADOR")) {
            throw new GymCoreException(20010,
                    "El usuario asignado a la rutina no es un entrenador activo.");
        }
 
        Rutina rutina = new Rutina();
        rutina.setNombre(nombre);
        rutina.setNivel(nivel);
        rutina.setEntrenador(entrenador);
        return rutinaRepository.save(rutina);
    }
 
    /** Agrega o actualiza un ejercicio dentro de la rutina. */
    @Transactional
    public RutinaEjercicio agregarEjercicio(Long idRutina, Long idEjercicio, int series,
                                            int repeticiones, int descansoSegundos) {
        Rutina rutina = buscarPorId(idRutina);
        Ejercicio ejercicio = ejercicioRepository.findById(idEjercicio)
                .orElseThrow(() -> new GymCoreException(0, "El ejercicio no existe."));
 
        RutinaEjercicio detalle = new RutinaEjercicio();
        detalle.setId(new RutinaEjercicioId(idRutina, idEjercicio));
        detalle.setRutina(rutina);
        detalle.setEjercicio(ejercicio);
        detalle.setSeries(series);
        detalle.setRepeticiones(repeticiones);
        detalle.setDescansoSegundos(descansoSegundos);
        return rutinaEjercicioRepository.save(detalle);
    }
 
    @Transactional
    public void quitarEjercicio(Long idRutina, Long idEjercicio) {
        rutinaEjercicioRepository.deleteById(new RutinaEjercicioId(idRutina, idEjercicio));
    }
 
    /** Asigna la rutina al cliente. La anterior se cierra poniendole fecha de fin. */
    @Transactional
    public RutinaCliente asignarACliente(Long idRutina, Long idCliente, LocalDate fechaFin) {
        Rutina rutina = buscarPorId(idRutina);
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new GymCoreException(0, "El cliente no existe."));
 
        RutinaCliente asignacion = new RutinaCliente();
        asignacion.setRutina(rutina);
        asignacion.setCliente(cliente);
        asignacion.setFechaAsignacion(LocalDate.now());
        asignacion.setFechaFin(fechaFin);
        return rutinaClienteRepository.save(asignacion);
    }
 
    @Transactional
    public void finalizarAsignacion(Long idRutinaCliente) {
        RutinaCliente asignacion = rutinaClienteRepository.findById(idRutinaCliente)
                .orElseThrow(() -> new GymCoreException(0, "La asignacion no existe."));
        asignacion.setFechaFin(LocalDate.now());
        rutinaClienteRepository.save(asignacion);
    }
 
    @Transactional(readOnly = true)
    public Rutina buscarPorId(Long idRutina) {
        return rutinaRepository.findById(idRutina)
                .orElseThrow(() -> new GymCoreException(0, "La rutina no existe."));
    }
 
    @Transactional(readOnly = true)
    public List<RutinaEjercicio> detalleDe(Long idRutina) {
        return rutinaEjercicioRepository.findDetalleDeRutina(idRutina);
    }
 
    @Transactional(readOnly = true)
    public List<RutinaCliente> rutinasVigentesDe(Long idCliente) {
        return rutinaClienteRepository.findVigentesHoy(idCliente);
    }
 
    @Transactional(readOnly = true)
    public List<Rutina> deEntrenador(Long idEntrenador) {
        return rutinaRepository.findByEntrenadorIdUsuario(idEntrenador);
    }
 
    @Transactional(readOnly = true)
    public List<Ejercicio> catalogoEjercicios() {
        return ejercicioRepository.findAll();
    }
}
 