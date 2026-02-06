package dto;

import java.time.Instant;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UserOutput", description = "DTO for output user information")
public class UserResponseDTO {
    
    private String id;

    private String username;

    private String email;

    private Instant lastLogin;
    private Instant created;
    private Instant modified;

}
