package lenguajesBD_Grupo06.Controller;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lenguajesBD_Grupo06.Domain.Usuario;
import lenguajesBD_Grupo06.Service.UsuarioService;
import lenguajesBD_Grupo06.web.dto.Requests;
import lenguajesBD_Grupo06.web.dto.Responses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/auth")
public class AuthController {
 
    /** Rol que habilita el panel administrativo. */
    private static final String ROL_PANEL = "ADMINISTRADOR";
 
    private final UsuarioService usuarioService;
 
    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
 
    @PostMapping("/registro/cliente")
    public ResponseEntity<Responses.Mensaje> registrarCliente(
            @Valid @RequestBody Requests.RegistroCliente req) {
 
        usuarioService.registrarCliente(req.nombre(), req.apellido(),
                req.telefono(), req.correo(), req.password(), req.cedula(),
                req.fechaNacimiento());
 
        return ResponseEntity.status(HttpStatus.CREATED).body(new Responses.Mensaje(
                "Cuenta creada correctamente."));
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
 
    /**
     * Valida credenciales y abre la sesion. Solo quien tenga el rol
     * ADMINISTRADOR queda habilitado para el panel; un cliente puede
     * autenticarse pero recibe 403.
     */
    @PostMapping("/login")
    public Responses.UsuarioDto login(@Valid @RequestBody Requests.Login req,
                                      HttpSession sesion) {
 
        Usuario usuario = usuarioService.autenticar(req.correo(), req.password());
 
        if (!usuarioService.tieneRol(usuario.getIdUsuario(), ROL_PANEL)) {
            throw new lenguajesBD_Grupo06.exception.GymCoreException(20013,
                    "Su cuenta no tiene permisos para el panel administrativo.");
        }
 
        sesion.setAttribute("usuarioId", usuario.getIdUsuario());
        sesion.setAttribute("usuarioNombre", usuario.getNombreCompleto());
        sesion.setAttribute("esAdministrador", Boolean.TRUE);
 
        return Responses.UsuarioDto.de(usuario);
    }
 
    @PostMapping("/logout")
    public Responses.Mensaje logout(HttpSession sesion) {
        sesion.invalidate();
        return new Responses.Mensaje("Sesion cerrada.");
    }
 
    @GetMapping("/entrenadores")
    public List<Responses.UsuarioDto> entrenadores() {
        return usuarioService.listarEntrenadores().stream()
                .map(Responses.UsuarioDto::de)
                .toList();
    }
}