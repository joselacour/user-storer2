package auth;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;

@ApplicationScoped
@Alternative // Indica que esta es una implementacion alternativa para ser usada en vez de la por defecto
@Priority(1) // Prioridad alta para que sea seleccionada sobre la implementacion por defecto
public class SecretsManagerJwtAuthContextInfoProvider {

    @Inject
    SecretsManagerJwtKeyProvider keyProvider;

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "user-storer")
    String issuer;

    @Produces
    @ApplicationScoped
    public JWTAuthContextInfo getContextInfo() {
        JWTAuthContextInfo info = new JWTAuthContextInfo();
        info.setIssuedBy(issuer);
        info.setPublicKeyContent(keyProvider.getPublicKeyPem());
        return info;
    }

}