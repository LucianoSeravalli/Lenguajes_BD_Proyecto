package lenguajesBD_Grupo06.Controller;

import lenguajesBD_Grupo06.Service.ClienteService;
import lenguajesBD_Grupo06.web.dto.Requests;
import lenguajesBD_Grupo06.web.dto.Responses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
 
    private final ClienteService clienteService;
 
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }
 
    @GetMapping
    public List<Responses.ClienteDto> listarActivos() {
        return clienteService.listarActivos().stream()
                .map(Responses.ClienteDto::de)
                .toList();
    }
 
    @GetMapping("/{idCliente}")
    public Responses.ClienteDto porId(@PathVariable Long idCliente) {
        return Responses.ClienteDto.de(clienteService.buscarPorId(idCliente));
    }
 
    @GetMapping("/cedula/{cedula}")
    public Responses.ClienteDto porCedula(@PathVariable String cedula) {
        return Responses.ClienteDto.de(clienteService.buscarPorCedula(cedula));
    }
 
    @PutMapping("/{idCliente}")
    public Responses.ClienteDto actualizar(@PathVariable Long idCliente,
                                           @Valid @RequestBody Requests.ActualizarCliente req) {
        return Responses.ClienteDto.de(clienteService.actualizarDatos(idCliente,
                req.nombre(), req.apellido(), req.telefono(), req.fechaNacimiento()));
    }
 
    /** No hay DELETE: se inactiva para no romper el historial de pagos y accesos. */
    @PatchMapping("/{idCliente}/estado")
    public Responses.Mensaje cambiarEstado(@PathVariable Long idCliente,
                                           @RequestParam String estado) {
        clienteService.cambiarEstado(idCliente, estado);
        return new Responses.Mensaje("Cliente marcado como " + estado + ".");
    }
}