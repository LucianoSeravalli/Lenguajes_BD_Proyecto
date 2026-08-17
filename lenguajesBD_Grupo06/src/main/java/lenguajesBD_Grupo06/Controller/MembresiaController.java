package lenguajesBD_Grupo06.Controller;

import lenguajesBD_Grupo06.Service.MembresiaService;
import lenguajesBD_Grupo06.web.dto.Requests;
import lenguajesBD_Grupo06.web.dto.Responses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/membresias")
public class MembresiaController {
 
    private final MembresiaService membresiaService;
 
    public MembresiaController(MembresiaService membresiaService) {
        this.membresiaService = membresiaService;
    }
 
    @GetMapping("/tipos")
    public List<Responses.TipoMembresiaDto> tipos() {
        return membresiaService.listarTipos().stream()
                .map(Responses.TipoMembresiaDto::de)
                .toList();
    }
 
    @PostMapping
    public ResponseEntity<Responses.MembresiaDto> contratar(
            @Valid @RequestBody Requests.ContratarMembresia req) {
 
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Responses.MembresiaDto.de(membresiaService.contratar(
                        req.idCliente(), req.idTipoMembresia(), req.fechaInicio())));
    }
 
    @PostMapping("/{idMembresia}/renovar")
    public Responses.MembresiaDto renovar(@PathVariable Long idMembresia) {
        return Responses.MembresiaDto.de(membresiaService.renovar(idMembresia));
    }
 
    @PatchMapping("/{idMembresia}/cancelar")
    public Responses.Mensaje cancelar(@PathVariable Long idMembresia) {
        membresiaService.cancelar(idMembresia);
        return new Responses.Mensaje("Membresia cancelada.");
    }
 
    @GetMapping("/cliente/{idCliente}")
    public List<Responses.MembresiaDto> historial(@PathVariable Long idCliente) {
        return membresiaService.historialDe(idCliente).stream()
                .map(Responses.MembresiaDto::de)
                .toList();
    }
 
    @GetMapping("/cliente/{idCliente}/vigente")
    public ResponseEntity<Responses.MembresiaDto> vigente(@PathVariable Long idCliente) {
        return membresiaService.vigenteDe(idCliente)
                .map(Responses.MembresiaDto::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
 
    /** Reporte de renovaciones proximas para el administrador. */
    @GetMapping("/por-vencer")
    public List<Responses.MembresiaDto> porVencer(@RequestParam(defaultValue = "7") int dias) {
        return membresiaService.porVencer(dias).stream()
                .map(Responses.MembresiaDto::de)
                .toList();
    }
}