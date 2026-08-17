
package lenguajesBD_Grupo06.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
 
/**
 * Bloquea las rutas del panel a quien no inicio sesion con rol ADMINISTRADOR.
 * AuthController guarda "usuarioId", "usuarioNombre" y "esAdministrador"
 * en la sesion cuando el login es correcto.
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {
 
    @Override
    public boolean preHandle(HttpServletRequest peticion,
                             HttpServletResponse respuesta,
                             Object manejador) throws Exception {
 
        HttpSession sesion = peticion.getSession(false);
        boolean autorizado = sesion != null
                && Boolean.TRUE.equals(sesion.getAttribute("esAdministrador"));
 
        if (autorizado) {
            return true;
        }
 
        // Se devuelve a la pagina de login con el motivo en la URL.
        respuesta.sendRedirect(peticion.getContextPath() + "/login?requiereSesion");
        return false;
    }
}
 