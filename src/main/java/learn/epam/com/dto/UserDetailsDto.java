package learn.epam.com.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Registration response",
        requiredProperties = {"username", "password"}
)
public class UserDetailsDto {

    @Schema(description = "Generated username", example = "john.doe")
    @NotBlank
    private String username;

    @Schema(description = "Generated password", example = "A7f8dX#9kL")
    @NotBlank
    private String password;

}
