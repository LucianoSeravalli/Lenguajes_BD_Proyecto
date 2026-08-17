package lenguajesBD_Grupo06.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sin esta clase, Spring Security bloquea TODAS las rutas y redirige a su
 * formulario "Sign In" generado automaticamente. Por eso no se podia llegar
 * ni a la pagina de registro.
 *
 * AVISO PARA LA ENTREGA: esta configuracion deja el sitio abierto. La
 * autenticacion real se resuelve en UsuarioService.autenticar() contra el
 * hash BCrypt de PASSWORD_HASH, pero todavia no hay sesion que proteja las
 * rutas. Para el avance final habria que exigir login en /clientes,
 * /membresias y /rutinas, y dejar publicas solo /login y /registro.
 */
@Configuration
public class SeguridadWebConfig {

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
            // El frontend llama al API con fetch y JSON, sin token CSRF.
            // Al cerrar el sitio con login real, hay que reactivar CSRF.
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(rutas -> rutas
                .requestMatchers(
                    "/", "/login", "/registro",
                    "/recepcion", "/clientes", "/clases", "/membresias", "/rutinas",
                    "/css/**", "/js/**", "/img/**", "/favicon.ico",
                    "/api/**"
                ).permitAll()
                .anyRequest().permitAll()
            )

            // Se apagan el formulario y el popup que trae Spring por defecto:
            // el login lo maneja nuestra propia pagina contra /api/auth/login.
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}