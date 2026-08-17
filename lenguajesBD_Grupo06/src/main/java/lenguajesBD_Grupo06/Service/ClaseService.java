package lenguajesBD_Grupo06.Service;

import lenguajesBD_Grupo06.Domain.Clase;
import lenguajesBD_Grupo06.Domain.Usuario;
import exception.GymCoreException;
import lenguajesBD_Grupo06.Repository.ClaseRepository;
import lenguajesBD_Grupo06.Repository.GymCoreProcedimientosRepository;
import lenguajesBD_Grupo06.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
/**
 * TRG_CLASE_VALIDAR_ENTRENADOR ya rechaza entrenadores invalidos con
 * ORA-20010, pero validar antes da un mensaje mas util al usuario.
 */
@Service
public class ClaseService {
 
    private final ClaseRepository claseRepository;
    private final UsuarioRepository usuarioRepository;
    private final GymCoreProcedimientosRepository procedimientos;
 
    public ClaseService(ClaseRepository claseRepository,
                        UsuarioRepository usuarioRepository,
                        GymCoreProcedimientosRepository procedimientos) {
        this.claseRepository = claseRepository;
        this.usuarioRepository = usuarioRepository;
        this.procedimientos = procedimientos;
    }
 
    @Transactional
    public Clase crear(String nombre, String descripcion, Long idEntrenador,
                       Integer cupoMaximo, String diaSemana,
                       String horaInicio, String horaFin) {
 
        Usuario entrenador = validarEntrenador(idEntrenador);
        validarHorario(idEntrenador, diaSemana, horaInicio, horaFin, null);
 
        Clase clase = new Clase();
        clase.setNombre(nombre);
        clase.setDescripcion(descripcion);
        clase.setEntrenador(entrenador);
        clase.setCupoMaximo(cupoMaximo);
        clase.setDiaSemana(diaSemana);
        clase.setHoraInicio(horaInicio);
        clase.setHoraFin(horaFin);
        return claseRepository.save(clase);
    }
 
    @Transactional
    public Clase actualizar(Long idClase, String nombre, String descripcion,
                            Long idEntrenador, Integer cupoMaximo, String diaSemana,
                            String horaInicio, String horaFin) {
 
        Clase clase = buscarPorId(idClase);
        Usuario entrenador = validarEntrenador(idEntrenador);
        validarHorario(idEntrenador, diaSemana, horaInicio, horaFin, idClase);
 
        // Reducir el cupo por debajo de las reservas ya confirmadas dejaria
        // gente adentro sin lugar, asi que se bloquea.
        int ocupados = clase.getCupoMaximo() - procedimientos.cupoDisponible(idClase);
        if (cupoMaximo < ocupados) {
            throw new GymCoreException(0,
                    "La clase ya tiene " + ocupados + " reservas confirmadas.");
        }
 
        clase.setNombre(nombre);
        clase.setDescripcion(descripcion);
        clase.setEntrenador(entrenador);
        clase.setCupoMaximo(cupoMaximo);
        clase.setDiaSemana(diaSemana);
        clase.setHoraInicio(horaInicio);
        clase.setHoraFin(horaFin);
        return claseRepository.save(clase);
    }
 
    @Transactional(readOnly = true)
    public Clase buscarPorId(Long idClase) {
        return claseRepository.findById(idClase)
                .orElseThrow(() -> new GymCoreException(20008, "La clase no existe."));
    }
 
    @Transactional(readOnly = true)
    public List<Clase> listar() {
        return claseRepository.findAll();
    }
 
    @Transactional(readOnly = true)
    public List<Clase> deEntrenador(Long idEntrenador) {
        return claseRepository.findByEntrenadorIdUsuario(idEntrenador);
    }
 
    /** Horario semanal listo para pintar en el frontend. */
    @Transactional(readOnly = true)
    public Map<String, List<Clase>> horarioSemanal() {
        String[] dias = {"lunes","martes","miercoles","jueves","viernes","sabado","domingo"};
        Map<String, List<Clase>> horario = new LinkedHashMap<>();
        for (String dia : dias) {
            horario.put(dia, claseRepository.findByDiaSemanaOrderByHoraInicio(dia));
        }
        return horario;
    }
 
    private Usuario validarEntrenador(Long idEntrenador) {
        Usuario entrenador = usuarioRepository.findById(idEntrenador)
                .orElseThrow(() -> new GymCoreException(0, "El entrenador no existe."));
        if (!usuarioRepository.tieneRol(idEntrenador, "ENTRENADOR")) {
            throw new GymCoreException(20010,
                    "El usuario asignado no es un entrenador activo.");
        }
        return entrenador;
    }
 
    private void validarHorario(Long idEntrenador, String diaSemana,
                                String horaInicio, String horaFin, Long idClaseActual) {
        if (horaFin.compareTo(horaInicio) <= 0) {
            throw new GymCoreException(0, "La hora de fin debe ser mayor que la de inicio.");
        }
        if (claseRepository.existeChoqueHorario(idEntrenador, diaSemana,
                horaInicio, horaFin, idClaseActual)) {
            throw new GymCoreException(0,
                    "El entrenador ya tiene otra clase en ese horario.");
        }
    }
}