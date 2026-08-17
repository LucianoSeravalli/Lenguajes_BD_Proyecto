package lenguajesBD_Grupo06.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
 
import java.util.List;
import java.util.Map;
 
/**
 * Rutas de vistas. Se separan en dos grupos:
 *   publicas -> "/", "/login", "/registro"
 *   panel    -> "/panel" y las demas, protegidas por SesionInterceptor
 */
@Controller
public class VistaController {
 
    /** Landing publica. Los datos de sedes son de muestra, no vienen de la base. */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("sedes", List.of(
                Map.of("nombre", "San Jose Centro",  "horario", "24/7"),
                Map.of("nombre", "Escazu",           "horario", "5 a.m. - 11 p.m."),
                Map.of("nombre", "Heredia",          "horario", "5 a.m. - 10 p.m."),
                Map.of("nombre", "Alajuela",         "horario", "24/7"),
                Map.of("nombre", "Cartago",          "horario", "5 a.m. - 10 p.m."),
                Map.of("nombre", "Liberia",          "horario", "6 a.m. - 9 p.m."),
                Map.of("nombre", "Puntarenas",       "horario", "6 a.m. - 9 p.m."),
                Map.of("nombre", "Limon",            "horario", "6 a.m. - 9 p.m.")
        ));
        return "paginas/index";
    }
 
    @GetMapping("/login")
    public String login() {
        return "paginas/login";
    }
 
    @GetMapping("/registro")
    public String registro() {
        return "paginas/registro";
    }
 
    /** Cierra la sesion y devuelve al sitio publico. */
    @GetMapping("/logout")
    public String logout(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/?salida";
    }
 
    // ---------------- Panel interno ----------------
 
    @GetMapping("/panel")
    public String panel() {
        return "paginas/inicio";
    }
 
    @GetMapping("/recepcion")
    public String recepcion() {
        return "paginas/recepcion";
    }
 
    @GetMapping("/clientes")
    public String clientes() {
        return "paginas/clientes";
    }
 
    @GetMapping("/clases")
    public String clases() {
        return "paginas/clases";
    }
 
    @GetMapping("/membresias")
    public String membresias() {
        return "paginas/membresias";
    }
 
    @GetMapping("/rutinas")
    public String rutinas() {
        return "paginas/rutinas";
    }
}