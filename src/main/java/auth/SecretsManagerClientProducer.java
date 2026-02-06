package auth;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@ApplicationScoped
public class SecretsManagerClientProducer {

    @Produces
    @ApplicationScoped
    public SecretsManagerClient secretsManagerClient(
            @ConfigProperty(name = "aws.region", defaultValue = "us-east-1") String region,
            @ConfigProperty(name = "aws.secretsmanager.endpoint", defaultValue = "") String endpoint,
            @ConfigProperty(name = "aws.access-key-id", defaultValue = "") String accessKeyId,
            @ConfigProperty(name = "aws.secret-access-key", defaultValue = "") String secretAccessKey) {

        var builder = SecretsManagerClient.builder().region(Region.of(region));

        if (!accessKeyId.isBlank() && !secretAccessKey.isBlank()) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
        }

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }
}