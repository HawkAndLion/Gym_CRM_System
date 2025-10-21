package learn.epam.com.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request to update trainee's trainers list")
public class TraineeTrainersDto {

    @NotBlank
    @Schema(description = "Trainee username", example = "Jacob.Franklin")
    private String traineeUsername;

    @NotEmpty
    @Schema(description = "List of trainer usernames", example = "[\"Amanda.Smith\", \"Lindsey.Adams\"]")
    private List<String> trainerUsernames;

    public TraineeTrainersDto() {
    }

    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public List<String> getTrainerUsernames() {
        return trainerUsernames;
    }

    public void setTrainerUsernames(List<String> trainerUsernames) {
        this.trainerUsernames = trainerUsernames;
    }
}
