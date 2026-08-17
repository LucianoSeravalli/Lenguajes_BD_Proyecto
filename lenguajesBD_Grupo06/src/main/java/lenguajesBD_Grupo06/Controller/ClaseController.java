package lenguajesBD_Grupo06.Controller;

import lenguajesBD_Grupo06.Domain.Clase;
import lenguajesBD_Grupo06.Service.ClaseService;
import lenguajesBD_Grupo06.Service.ReservaService;
import lenguajesBD_Grupo06.web.dto.Requests;
import lenguajesBD_Grupo06.web.dto.Responses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/clases")
public class ClaseController {
 
    private final ClaseService claseService;
    private final ReservaService reservaService;
 
    public ClaseController(ClaseService claseService, ReservaService reservaService) {
        this.claseService = claseService;
        this.reservaService = reservaService;
    }
 
    @GetMapping
    public List<Responses.ClaseDto> listar() {
        return claseService.listar().stream().map(this::conCupos).toList();
    }
 
    @GetMapping("/{idClase}")
    public Responses.ClaseDto porId(@PathVariable Long idClase) {
        return conCupos(claseService.buscarPorId(idClase));
    }
 
    /** Horario semanal agrupado por dia, listo para pintar la grilla. */
    @GetMapping("/horario")
    public Map<String, List<Responses.ClaseDto>> horario() {
        Map<String, List<Responses.ClaseDto>> salida = new LinkedHashMap<>();
        claseService.horarioSemanal().forEach((dia, clases) ->
                salida.put(dia, clases.stream().map(this::conCupos).toList()));
        return salida;
    }
 
    @GetMapping("/entrenador/{idEntrenador}")
    public List<Responses.ClaseDto> deEntrenador(@PathVariable Long idEntrenador) {
        return claseService.deEntrenador(idEntrenador).stream().map(this::conCupos).toList();
    }
 
    @PostMapping
    public ResponseEntity<Responses.ClaseDto> crear(
            @Valid @RequestBody Requests.GuardarClase req) {
 
        Clase clase = claseService.crear(req.nombre(), req.descripcion(), req.idEntrenador(),
                req.cupoMaximo(), req.diaSemana(), req.horaInicio(), req.horaFin());
        return ResponseEntity.status(HttpStatus.CREATED).body(conCupos(clase));
    }
 
    @PutMapping("/{idClase}")
    public Responses.ClaseDto actualizar(@PathVariable Long idClase,
                                         @Valid @RequestBody Requests.GuardarClase req) {
        return conCupos(claseService.actualizar(idClase, req.nombre(), req.descripcion(),
                req.idEntrenador(), req.cupoMaximo(), req.diaSemana(),
                req.horaInicio(), req.horaFin()));
    }
 
    /** Lista de asistentes confirmados, para el entrenador. */
    @GetMapping("/{idClase}/reservas")
    public List<Responses.ReservaDto> asistentes(@PathVariable Long idClase) {
        return reservaService.listaDeClase(idClase).stream()
                .map(Responses.ReservaDto::de)
                .toList();
    }
 
    private Responses.ClaseDto conCupos(Clase clase) {
        return Responses.ClaseDto.de(clase, reservaService.cuposDisponibles(clase.getIdClase()));
    }
}
 