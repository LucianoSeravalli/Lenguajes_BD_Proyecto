package lenguajesBD_Grupo06.Controller;

import lenguajesBD_Grupo06.Domain.Acceso;
import lenguajesBD_Grupo06.Service.AccesoService;
import lenguajesBD_Grupo06.web.dto.Requests;
import lenguajesBD_Grupo06.web.dto.Responses;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
 
import java.time.LocalDate;
import java.util.List;
 
/**
 * Torniquete y pantalla de recepcion. Es el proceso transaccional central:
 * la decision viene de FN_MEMBRESIA_ACTIVA y cada intento queda en bitacora.
 */
@RestController
@RequestMapping("/api/accesos")
public class AccesoController {
 
    private final AccesoService accesoService;
 
    public AccesoController(AccesoService accesoService) {
        this.accesoService = accesoService;
    }
 
    /**
     * Responde 200 tanto si permite como si deniega: el intento denegado no
     * es un error de la peticion, es un resultado valido que se registra.
     */
    @PostMapping("/entrada")
    public Responses.ResultadoAcceso entrada(@Valid @RequestBody Requests.MarcarAcceso req) {
        Acceso acceso = accesoService.registrarEntrada(req.cedula());
        boolean permitido = "permitido".equals(acceso.getResultado());
        return new Responses.ResultadoAcceso(permitido,
                permitido ? "Acceso permitido. Bienvenido."
                          : "Acceso denegado: membresia vencida, sin pago o cliente inactivo.",
                Responses.AccesoDto.de(acceso));
    }
 
    @PostMapping("/salida")
    public Responses.ResultadoAcceso salida(@Valid @RequestBody Requests.MarcarAcceso req) {
        Acceso acceso = accesoService.registrarSalida(req.cedula());
        return new Responses.ResultadoAcceso(true, "Salida registrada.",
                Responses.AccesoDto.de(acceso));
    }
 
    /** Consulta previa sin dejar registro, para la pantalla de recepcion. */
    @GetMapping("/verificar/{cedula}")
    public Responses.ResultadoAcceso verificar(@PathVariable String cedula) {
        boolean puede = accesoService.puedeIngresar(cedula);
        return new Responses.ResultadoAcceso(puede,
                puede ? "Membresia vigente y al dia." : "Sin membresia activa y pagada.", null);
    }
 
    @GetMapping("/cliente/{idCliente}")
    public List<Responses.AccesoDto> historial(@PathVariable Long idCliente) {
        return accesoService.historialDe(idCliente).stream()
                .map(Responses.AccesoDto::de)
                .toList();
    }
 
    @GetMapping("/dia")
    public List<Responses.AccesoDto> delDia(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return accesoService.delDia(fecha != null ? fecha : LocalDate.now()).stream()
                .map(Responses.AccesoDto::de)
                .toList();
    }
 
    @GetMapping("/denegados")
    public long denegados(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return accesoService.denegadosEntre(desde, hasta);
    }
}