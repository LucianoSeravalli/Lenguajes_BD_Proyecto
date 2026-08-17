package lenguajesBD_Grupo06.Controller;

import lenguajesBD_Grupo06.Service.RutinaService;
import lenguajesBD_Grupo06.web.dto.Requests;
import lenguajesBD_Grupo06.web.dto.Responses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/rutinas")
public class RutinaController {
 
    private final RutinaService rutinaService;
 
    public RutinaController(RutinaService rutinaService) {
        this.rutinaService = rutinaService;
    }
 
    @PostMapping
    public ResponseEntity<Responses.RutinaDto> crear(
            @Valid @RequestBody Requests.CrearRutina req) {
 
        return ResponseEntity.status(HttpStatus.CREATED).body(Responses.RutinaDto.de(
                rutinaService.crear(req.nombre(), req.nivel(), req.idEntrenador())));
    }
 
    @GetMapping("/{idRutina}")
    public Responses.RutinaDto porId(@PathVariable Long idRutina) {
        return Responses.RutinaDto.de(rutinaService.buscarPorId(idRutina));
    }
 
    @GetMapping("/entrenador/{idEntrenador}")
    public List<Responses.RutinaDto> deEntrenador(@PathVariable Long idEntrenador) {
        return rutinaService.deEntrenador(idEntrenador).stream()
                .map(Responses.RutinaDto::de)
                .toList();
    }
 
    @GetMapping("/{idRutina}/ejercicios")
    public List<Responses.RutinaEjercicioDto> detalle(@PathVariable Long idRutina) {
        return rutinaService.detalleDe(idRutina).stream()
                .map(Responses.RutinaEjercicioDto::de)
                .toList();
    }
 
    @PostMapping("/{idRutina}/ejercicios")
    public ResponseEntity<Responses.RutinaEjercicioDto> agregarEjercicio(
            @PathVariable Long idRutina,
            @Valid @RequestBody Requests.AgregarEjercicio req) {
 
        int descanso = req.descansoSegundos() != null ? req.descansoSegundos() : 60;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Responses.RutinaEjercicioDto.de(rutinaService.agregarEjercicio(
                        idRutina, req.idEjercicio(), req.series(),
                        req.repeticiones(), descanso)));
    }
 
    @DeleteMapping("/{idRutina}/ejercicios/{idEjercicio}")
    public ResponseEntity<Void> quitarEjercicio(@PathVariable Long idRutina,
                                                @PathVariable Long idEjercicio) {
        rutinaService.quitarEjercicio(idRutina, idEjercicio);
        return ResponseEntity.noContent().build();
    }
 
    @PostMapping("/{idRutina}/asignar")
    public ResponseEntity<Responses.RutinaAsignadaDto> asignar(
            @PathVariable Long idRutina,
            @Valid @RequestBody Requests.AsignarRutina req) {
 
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Responses.RutinaAsignadaDto.de(rutinaService.asignarACliente(
                        idRutina, req.idCliente(), req.fechaFin())));
    }
 
    @PatchMapping("/asignaciones/{idRutinaCliente}/finalizar")
    public Responses.Mensaje finalizar(@PathVariable Long idRutinaCliente) {
        rutinaService.finalizarAsignacion(idRutinaCliente);
        return new Responses.Mensaje("Asignacion finalizada.");
    }
 
    @GetMapping("/cliente/{idCliente}")
    public List<Responses.RutinaAsignadaDto> vigentesDeCliente(@PathVariable Long idCliente) {
        return rutinaService.rutinasVigentesDe(idCliente).stream()
                .map(Responses.RutinaAsignadaDto::de)
                .toList();
    }
 
    @GetMapping("/ejercicios")
    public List<Responses.RutinaEjercicioDto> catalogo() {
        return rutinaService.catalogoEjercicios().stream()
                .map(e -> new Responses.RutinaEjercicioDto(e.getIdEjercicio(), e.getNombre(),
                        e.getGrupoMuscular(), null, null, null))
                .toList();
    }
}
 