package service;

import dto.LoginResponseDTO;
import model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.UserRepository;
import auth.SecretsManagerJwtKeyProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.smallrye.mutiny.Uni;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @Mock
    SecretsManagerJwtKeyProvider keyProvider;

    @InjectMocks
    AuthService authService;

    @Test
    public void testLoginSuccess() throws Exception {
        String email = "test@example.com";
        String rawPassword = "plain";
        String encodedPassword = "encoded";

        User user = new User();
        user.setId("id-123");
        user.setEmail(email);
        user.setUsername("tester");
        user.setPassword(encodedPassword);

        when(userRepository.findByEmail(email)).thenReturn(Uni.createFrom().item(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(userRepository.save(any())).thenReturn(Uni.createFrom().item(user));

        // generate a temporary RSA keypair for signing
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        PrivateKey privateKey = kp.getPrivate();
        when(keyProvider.getPrivateKey()).thenReturn(privateKey);

        LoginResponseDTO dto = authService.login(email, rawPassword).await().indefinitely();

        assertNotNull(dto);
        assertNotNull(dto.getAccessToken());
        assertEquals("Bearer", dto.getTokenType());
        assertTrue(dto.getExpiresIn() > 0);

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
        verify(userRepository).save(any());
        verify(keyProvider).getPrivateKey();
    }

}
