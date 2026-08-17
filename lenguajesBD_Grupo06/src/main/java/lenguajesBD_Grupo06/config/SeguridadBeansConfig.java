package lenguajesBD_Grupo06.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
 
/**
 * Necesita spring-boot-starter-security en el pom.
 * PASSWORD_HASH y TOKEN_CONFIRMACION_HASH nunca guardan texto plano.
 */
@Configuration
public class SeguridadBeansConfig {
 
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
 
