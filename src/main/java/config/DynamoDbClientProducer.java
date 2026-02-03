package config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.net.URI;

@ApplicationScoped
public class DynamoDbClientProducer {

    @Produces
    @ApplicationScoped
    public DynamoDbAsyncClient dynamoDbAsyncClient(
            // Leemos de application.properties
            @ConfigProperty(name = "aws.region", defaultValue = "us-east-1") String region,
            @ConfigProperty(name = "aws.endpoint", defaultValue = "") String endpoint) {

        // Usamos netty por que es el recomendado para aplicaciones reactivas
        var builder = DynamoDbAsyncClient.builder()
                .httpClientBuilder(NettyNioAsyncHttpClient.builder())
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build());

        // Si se ha configurado un hardcode de endpoint lo usamos
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    // El cliente asyncrono mejorado de DynamoDB, que es el que usamos, el anterior se creo por que es necesario por este
    @Produces
    @ApplicationScoped
    public DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient(DynamoDbAsyncClient asyncClient) {
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(asyncClient)
                .build();
    }
}