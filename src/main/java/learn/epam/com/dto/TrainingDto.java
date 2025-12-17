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
        description = "Create training request"
)
public class TrainingDto {

    @NotBlank
    private String name;

    @NotBlank
    private LocalDate date;

    @NotBlank
    private String trainingType;

    @NotBlank
    private double duration;
    private String traineeUsername;
    private String trainerUsername;
    private String traineeName;

    public TrainingDto(String name, LocalDate date, String trainingType, double duration, String traineeName) {
        this.name = name;
        this.date = date;
        this.trainingType = trainingType;
        this.duration = duration;
        this.traineeName = traineeName;
    }
}
