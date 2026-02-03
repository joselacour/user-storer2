package repository;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import model.User;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;

@ApplicationScoped
public class UserRepository {

    private final DynamoDbAsyncTable<User> userTable;

    @Inject
    public UserRepository(DynamoDbEnhancedAsyncClient enhancedClient) {
        this.userTable = enhancedClient.table("User", TableSchema.fromBean(User.class));
    }

    public Uni<User> save(User user) {
        return Uni.createFrom()
                .completionStage(() -> userTable.putItem(user)
                        .thenApply(ignored -> user));
    }

    // TAG: INVESTIGAR
    // Scan para obtener todos los usuarios esto es costoso en tablas grandes, volvemos a trabajar com completablefuture para manejar la asincronia con awssdk
    public Uni<List<User>> findAll() {
        ScanEnhancedRequest request = ScanEnhancedRequest.builder().build();
        
        return Uni.createFrom().completionStage(() -> {
            CompletableFuture<List<User>> future = new CompletableFuture<>();
            List<User> result = new ArrayList<>();
            
            userTable.scan(request).subscribe(page -> {
                result.addAll(page.items());
            }).whenComplete((v, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                } else {
                    future.complete(result);
                }
            });
            
            return future;
        });
    }

    // Esto es asi por que es get por PK Partition Key sin SortKey, si tenia sortkey deberiamos de buildear la llave con la sortkey o esperar ya la llave como param
    public Uni<User> findById(String userId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .build();
        return Uni.createFrom()
                .completionStage(() -> userTable.getItem(r -> r.key(key)));
    }

    // TAG: INVESTIGAR
    // Esto es asi utilizando un GSI (Global Secondary Index) se predefine
    public Uni<User> findByEmail(String email) {
        return Uni.createFrom().completionStage(() -> {
            CompletableFuture<User> future = new CompletableFuture<>();

            var index = userTable.index("email-index");
            Key key = Key.builder().partitionValue(email).build();

            index.query(r -> r
                    .queryConditional(
                            QueryConditional.keyEqualTo(key))
                    .limit(1)
            ).subscribe(page -> {
                future.complete(page.items().stream().findFirst().orElse(null));
            }).exceptionally(throwable -> {
                future.completeExceptionally(throwable);
                return null;
            });

            return future;
        });
    }

    // TAG: INVESTIGAR
    // Esto es asi utilizando un Scan con filtro ya que username no es ni PK ni SK ni index (es mas costoso ya que recorre toda la tabla primero)
    public Uni<User> findByUsername(String username) {
        return Uni.createFrom().completionStage(() -> {
            CompletableFuture<User> future = new CompletableFuture<>();
            userTable.scan(r -> r
                    .filterExpression(
                            Expression.builder()
                                    .expression("username = :value")
                                    .putExpressionValue(":value",
                                            AttributeValue.builder().s(username).build())
                                    .build()
                    )
            ).subscribe(page -> {
                future.complete(page.items().stream().findFirst().orElse(null));
            });
            return future;
        });
    }

    public Uni<Void> delete(User user) {
        return Uni.createFrom()
                .completionStage(() -> userTable.deleteItem(user)
                        .thenApply(ignored -> null));
    }

    public Uni<Boolean> existsById(String userId) {
        return findById(userId)
                .map(user -> user != null);
    }

}
