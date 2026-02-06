package service;

import model.User;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import repository.UserRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepository;

    @Test
    void testCreateUserWithoutIdSuccess() {
        // Creamos un usuario de prueba que vendria del controller
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("testpass");

        // Agregamos interceptores para evitar que se ejecute la logica real del repositorio
        when(userRepository.existsByEmail("test@test.com"))
            .thenReturn(Uni.createFrom().item(false));

        // Otro interceptor para la logica de guardado
        when(userRepository.save(any(User.class)))
            .thenAnswer(inv -> Uni.createFrom().item((User) inv.getArgument(0)));

        // Aqui se triggerea el metodo de create
        User result = userService.create(user).await().indefinitely();

        // Verificamos que se se intento guardar en realidad
        assertNotNull(result.getId());
        assertNotNull(result.getCreated());
        assertNotNull(result.getModified());

        // Verificamos que la password fue hasheada de forma correcta
        assertTrue(
            new BCryptPasswordEncoder()
                .matches("testpass", result.getPassword())
        );
    }

    @Test
    void testCreateUserWithIdSuccess() {
        // Creamos un usuario de prueba que vendria del controller
        User user = new User();
        user.setId("4fc6a4d1-e29a-4008-9c2f-8922eb3ad981");
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("testpass");

        // Agregamos interceptores para evitar que se ejecute la logica real del repositorio
        when(userRepository.existsByEmail("test@test.com"))
            .thenReturn(Uni.createFrom().item(false));

        // Otro interceptor para la logica de guardado
        when(userRepository.save(any(User.class)))
            .thenAnswer(inv -> Uni.createFrom().item((User) inv.getArgument(0)));

        // Aqui se triggerea el metodo de create
        User result = userService.create(user).await().indefinitely();

        // Verificamos que se se intento guardar en realidad
        assertNotNull(result.getId());
        assertNotNull(result.getCreated());
        assertNotNull(result.getModified());

        // Verificamos que la password fue hasheada de forma correcta
        assertTrue(
            new BCryptPasswordEncoder()
                .matches("testpass", result.getPassword())
        );
    }

    @Test
    void testCreateUserWhitDuplicateEmailFails(){
        // Creamos un usuario de prueba que vendria del controller
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("testpass");

        // Agregamos interceptores para evitar que se ejecute la logica real del repositorio
        when(userRepository.existsByEmail("test@test.com"))
            .thenReturn(Uni.createFrom().item(true)); // Simulamos que ya existe el email ya que interceptamos

        // Otro interceptor para la logica de guardado
        when(userRepository.save(any(User.class)))
            .thenAnswer(inv -> Uni.createFrom().item((User) inv.getArgument(0)));

        // Aqui se triggerea el metodo de create que deberia de generar una excepcion
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.create(user).await().indefinitely()
        );

        String expectedMessage = "Error creating user: User with email " + user.getEmail() + " already exists";
        assertEquals(expectedMessage, exception.getMessage());

    }



}