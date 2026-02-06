package auth;

import java.security.PrivateKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@ApplicationScoped
public class SecretsManagerJwtKeyProvider {

    @Inject
    SecretsManagerClient client;

    @ConfigProperty(name = "jwt.private-key.secret-name", defaultValue = "jwt-private-key")
    String privateKeySecretName;

    @ConfigProperty(name = "jwt.public-key.secret-name", defaultValue = "jwt-public-key")
    String publicKeySecretName;

    private volatile PrivateKey cachedPrivateKey;
    private volatile String cachedPublicKeyPem;

    public PrivateKey getPrivateKey() {
        if (cachedPrivateKey != null) {
            return cachedPrivateKey;
        }
        String pem = client.getSecretValue(GetSecretValueRequest.builder()
                .secretId(privateKeySecretName)
                .build())
            .secretString();

        cachedPrivateKey = PemUtils.readPrivateKeyFromPem(pem);
        return cachedPrivateKey;
    }
    

    public String getPublicKeyPem() {
        if (cachedPublicKeyPem != null) {
            return cachedPublicKeyPem;
        }
        cachedPublicKeyPem = client.getSecretValue(GetSecretValueRequest.builder()
                .secretId(publicKeySecretName)
                .build())
            .secretString();
        return cachedPublicKeyPem;
    }
}