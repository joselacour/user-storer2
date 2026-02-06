package service;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Set;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import auth.SecretsManagerJwtKeyProvider;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import dto.LoginResponseDTO;
import model.User;
import repository.UserRepository;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    BCryptPasswordEncoder passwordEncoder;

    @Inject
    SecretsManagerJwtKeyProvider keyProvider;

    public Uni<LoginResponseDTO> login(String email, String password) {
        return userRepository.findByEmail(email)
            .onItem().transformToUni(user -> {
                if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
                    return Uni.createFrom().failure(new WebApplicationException("Invalid credentials", 401));
                }

                Instant now = Instant.now();
                user.setLastLogin(now);
                user.setModified(now);

                return userRepository.save(user)
                    .replaceWith(() -> buildToken(user, now));
            });
    }

    private LoginResponseDTO buildToken(User user, Instant now) {
        long expiresIn = 900; // esta en segundos
        PrivateKey privateKey = keyProvider.getPrivateKey();

        String token = Jwt.claims()
            .issuer("user-storer")
            .subject(user.getId())
            .upn(user.getEmail())
            .groups(user.getRoles() == null ? Set.of() : user.getRoles())
            .claim("username", user.getUsername())
            .issuedAt(now.getEpochSecond())
            .expiresAt(now.plusSeconds(expiresIn).getEpochSecond())
            .sign(privateKey);

        return new LoginResponseDTO(token, "Bearer", expiresIn);
    }
}