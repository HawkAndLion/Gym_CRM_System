package learn.epam.com.event;

import learn.epam.com.entity.Training;
import lombok.Getter;

@Getter
public class TrainingCreatedEvent {

    @Getter
    private final Training training;
    private final String traineeUsername;
    private final String trainerUsername;

    public TrainingCreatedEvent(Training training, String traineeUsername, String trainerUsername) {
        this.training = training;
        this.traineeUsername = traineeUsername;
        this.trainerUsername = trainerUsername;
    }

}
