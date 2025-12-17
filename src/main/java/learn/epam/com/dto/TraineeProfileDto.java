package learn.epam.com.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "trainers")
@Schema(
        description = "Trainee profile retrieve request",
        requiredProperties = {"username", "firstName", "lastName", "isActive"}
)
public class TraineeProfileDto {

    @NotBlank
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;

    @NotBlank
    private boolean isActive;
    private List<TrainerDto> trainers;
}
