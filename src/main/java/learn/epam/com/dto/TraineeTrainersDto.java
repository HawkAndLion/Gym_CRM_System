package learn.epam.com.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Request to update trainee's trainers list")
public class TraineeTrainersDto {

    @NotEmpty
    @Schema(description = "List of trainer usernames", example = "[\"Amanda.Smith\", \"Lindsey.Adams\"]")
    private List<String> trainerUsernames;

}
