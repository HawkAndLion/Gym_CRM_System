package learn.epam.com.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Trainee registration request",
        requiredProperties = {"firstName", "lastName"}
)
public class TraineeDto {

    @Schema(description = "First name", example = "John")
    @NotBlank
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    @NotBlank
    private String lastName;

    @Schema(description = "Date of birth (optional)", example = "1998-05-23")
    private LocalDate dateOfBirth;

    @Schema(description = "Address (optional)", example = "123 Main St, New York")
    private String address;

    @NotBlank
    private String password;
}
