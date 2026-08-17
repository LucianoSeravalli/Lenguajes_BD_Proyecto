package lenguajesBD_Grupo06.Controller;

import lenguajesBD_Grupo06.Domain.Usuario;
import lenguajesBD_Grupo06.Service.UsuarioService;
import lenguajesBD_Grupo06.web.dto.Requests;
import lenguajesBD_Grupo06.web.dto.Responses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/auth")
public class AuthController {
 
    private final UsuarioService usuarioService;
 
    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
 
    /**
     * Devuelve 201 sin el token: el enlace de confirmacion viaja por correo.
     * Exponerlo en la respuesta permitiria activar cuentas ajenas.
     */
    @PostMapping("/registro/cliente")
    public ResponseEntity<Responses.Mensaje> registrarCliente(
            @Valid @RequestBody Requests.RegistroCliente req) {
 
        String token = usuarioService.registrarCliente(req.nombre(), req.apellido(),
                req.telefono(), req.correo(), req.password(), req.cedula(),
                req.fechaNacimiento());
 
        // TODO: enviar el token por correo con el enlace /api/auth/confirmar?token=...
        return ResponseEntity.status(HttpStatus.CREATED).body(new Responses.Mensaje(
                "Cuenta creada. Revise su correo para confirmarla."));
    }
 
    @PostMapping("/registro/entrenador")
    public ResponseEntity<Responses.UsuarioDto> registrarEntrenador(
            @Valid @RequestBody Requests.RegistroEntrenador req) {
 
        Usuario entrenador = usuarioService.registrarEntrenador(req.nombre(), req.apellido(),
                req.telefono(), req.correo(), req.password(), req.especialidad(),
                req.fechaContratacion());
 
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Responses.UsuarioDto.de(entrenador));
    }
 
    @GetMapping("/confirmar")
    public Responses.Mensaje confirmar(@RequestParam String token) {
        usuarioService.confirmarCorreo(token);
        return new Responses.Mensaje("Correo confirmado. Ya puede iniciar sesion.");
    }
 
    @PostMapping("/login")
    public Responses.UsuarioDto login(@Valid @RequestBody Requests.Login req) {
        return Responses.UsuarioDto.de(usuarioService.autenticar(req.correo(), req.password()));
    }
 
    @GetMapping("/entrenadores")
    public List<Responses.UsuarioDto> entrenadores() {
        return usuarioService.listarEntrenadores().stream()
                .map(Responses.UsuarioDto::de)
                .toList();
    }
}
 