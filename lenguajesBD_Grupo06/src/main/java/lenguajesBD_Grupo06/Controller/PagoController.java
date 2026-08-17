package lenguajesBD_Grupo06.Controller;

import lenguajesBD_Grupo06.Service.PagoService;
import lenguajesBD_Grupo06.web.dto.Requests;
import lenguajesBD_Grupo06.web.dto.Responses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
 
@RestController
@RequestMapping("/api/pagos")
public class PagoController {
 
    private final PagoService pagoService;
 
    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }
 
    /**
     * Pasa por SP_REGISTRAR_PAGO. El trigger TRG_PAGO_SYNC_MEMBRESIA
     * actualiza despues el estado de la membresia.
     */
    @PostMapping
    public ResponseEntity<Responses.Mensaje> registrar(
            @Valid @RequestBody Requests.RegistrarPago req) {
 
        if ("pendiente".equals(req.estado())) {
            pagoService.registrarPendiente(req.idMembresia(), req.monto(), req.metodoPago());
        } else {
            pagoService.registrar(req.idMembresia(), req.monto(), req.metodoPago());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Responses.Mensaje("Pago registrado."));
    }
 
    @GetMapping("/membresia/{idMembresia}")
    public List<Responses.PagoDto> deMembresia(@PathVariable Long idMembresia) {
        return pagoService.deMembresia(idMembresia).stream()
                .map(Responses.PagoDto::de)
                .toList();
    }
 
    @GetMapping("/cliente/{idCliente}")
    public List<Responses.PagoDto> historial(@PathVariable Long idCliente) {
        return pagoService.historialDeCliente(idCliente).stream()
                .map(Responses.PagoDto::de)
                .toList();
    }
 
    @GetMapping("/recaudado")
    public BigDecimal recaudado(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate hasta) {
        return pagoService.recaudadoEntre(desde, hasta);
    }
}