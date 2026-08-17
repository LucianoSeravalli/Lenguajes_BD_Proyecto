
package lenguajesBD_Grupo06.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
/**
 * Registra el interceptor solo sobre las rutas del panel.
 * "/", "/login", "/registro", "/api/**" y los estaticos quedan publicos.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
 
    private final SessionInterceptor sesionInterceptor;
 
    public WebMvcConfig(SessionInterceptor sesionInterceptor) {
        this.sesionInterceptor = sesionInterceptor;
    }
 
    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(sesionInterceptor)
                .addPathPatterns("/panel", "/recepcion", "/clientes",
                                 "/clases", "/membresias", "/rutinas");
    }
}
 