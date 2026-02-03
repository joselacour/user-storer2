package service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.smallrye.mutiny.Uni;
import model.User;
import repository.UserRepository;


@ApplicationScoped
public class UserService {

	@Inject
	UserRepository userRepository;

    @Inject
    BCryptPasswordEncoder passwordEncoder;

    // Forma un poco mas tradicional trabajando con el Boolean dentro del uni diferente a create2
    public Uni<User> create(User user) {
        if (user == null) {
            return Uni.createFrom()
                    .failure(new IllegalArgumentException("User cannot be null"));
        }
        
        // Si viene con ID, verificar que no exista
        if (user.getId() != null && !user.getId().isBlank()) {
            return userRepository.existsById(user.getId())
                .onItem().transformToUni(exists -> {
                    if (exists) {
                        return Uni.createFrom()
                                .failure(new IllegalArgumentException(
                                        "User with id " + user.getId() + " already exists"));
                    }
                    // TAG: FALTA agregar la validacion para el UUID valido
                    user.setCreated(Instant.now());
                    user.setModified(Instant.now());
                    
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    
                    return userRepository.save(user);
                });
        }
        
        // Si no viene con ID, generar uno nuevo
        user.setId(UUID.randomUUID().toString());
        user.setCreated(Instant.now());
        user.setModified(Instant.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    // Forma fluida no es valida en esta version de Mutiny pero usa .ifTrue que es mas limpio
    /*public Uni<User> create2(User user) {
            return userRepository.existsById(user.getId())
                    .onItem().ifTrue().failWith(() -> new IllegalArgumentException(
                        "User with id " + user.getId() + " already exists"))
                    .onItem().ifFalse().transformToUni(ignored -> userRepository.save(user));
    }*/

    public Uni<List<User>> findAll() {
        return userRepository.findAll();
    }

	public Uni<User> findById(String id) {
		return userRepository.findById(id);
    }

	public Uni<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public Uni<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

    // Esto es asi por que el delete del repository espera el user entero
	public Uni<Void> delete(String id) {
        
        return userRepository.findById(id).onItem().transformToUni(user -> {
            if (user == null) {
                return Uni.createFrom()
                                .failure(new IllegalArgumentException(
                                        "User with id " + id + " does not exist"));
            }
            return userRepository.delete(user);
        });
    }

}
