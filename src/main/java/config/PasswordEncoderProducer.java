package config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ApplicationScoped
public class PasswordEncoderProducer {

    @Produces
    @ApplicationScoped
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Segun owasp para bcrypt > 10 rondas es lo recomendado 
    }
}
