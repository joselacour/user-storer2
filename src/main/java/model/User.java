package model;

import lombok.*;
import model.converter.InstantAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    private String id;
    
    private String username;

    private String email;
    @ToString.Exclude
    private String password;

    // No necesita un converter, DynamoDB mapea Set<String> directamente a SS
    private Set<String> roles;

    //En el fondo dynamodb requiere un Epoch time entonces usamos un converter que esta en moderl.converter
    private Instant lastLogin;
    private Instant created;
    private Instant modified;

    @DynamoDbPartitionKey
    //@EqualsAndHashCode.Include
    public String getId() {
        return id;
    }

    //@EqualsAndHashCode.Include
    //@DynamoDbSortKey
    @DynamoDbConvertedBy(InstantAttributeConverter.class)
    @DynamoDbSecondaryPartitionKey(indexNames = "created-index")
    public Instant getCreated() {
        return created;
    }

    @DynamoDbConvertedBy(InstantAttributeConverter.class)
    public Instant getLastLogin() {
        return lastLogin;
    }

    @DynamoDbConvertedBy(InstantAttributeConverter.class)
    public Instant getModified() {
        return modified;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "email-index")
    public String getEmail() {
        return email;
    }

}
