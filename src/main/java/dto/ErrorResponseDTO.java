package dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(name = "ErrorOutput", description = "DTO for error output")
public class ErrorResponseDTO {

    private Integer errorCode;
    private String errorMessage;
    private String details;
    private Instant timestamp;
    private UserResponseDTO user;


}