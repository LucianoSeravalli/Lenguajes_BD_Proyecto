package lenguajesBD_Grupo06.Controller;

import lenguajesBD_Grupo06.Service.ReservaService;
import lenguajesBD_Grupo06.web.dto.Requests;
import lenguajesBD_Grupo06.web.dto.Responses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
 
    private final ReservaService reservaService;
 
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }
 
    /**
     * Pasa por SP_RESERVAR_CLASE. Si falla, el manejador global traduce:
     * 20002 y 20004 y 20005 a 409, y 20009 a 404.
     */
    @PostMapping
    public ResponseEntity<Responses.Mensaje> reservar(
            @Valid @RequestBody Requests.CrearReserva req) {
 
        String mensaje = reservaService.reservar(req.cedula(), req.idClase());
        return ResponseEntity.status(HttpStatus.CREATED).body(new Responses.Mensaje(mensaje));
    }
 
    @GetMapping("/cliente/{idCliente}")
    public List<Responses.ReservaDto> confirmadas(@PathVariable Long idCliente) {
        return reservaService.confirmadasDeCliente(idCliente).stream()
                .map(Responses.ReservaDto::de)
                .toList();
    }
 
    @PatchMapping("/{idReserva}/cancelar")
    public Responses.Mensaje cancelar(@PathVariable Long idReserva) {
        reservaService.cancelar(idReserva);
        return new Responses.Mensaje("Reserva cancelada. El cupo quedo libre.");
    }
 
    @PatchMapping("/{idReserva}/asistencia")
    public Responses.Mensaje asistencia(@PathVariable Long idReserva) {
        reservaService.marcarAsistencia(idReserva);
        return new Responses.Mensaje("Asistencia registrada.");
    }
 
    @GetMapping("/clase/{idClase}/cupos")
    public Responses.Mensaje cupos(@PathVariable Long idClase) {
        return new Responses.Mensaje(String.valueOf(reservaService.cuposDisponibles(idClase)));
    }
}