package learn.epam.com.event;

import learn.epam.com.entity.Training;

public class TrainingCreatedEvent {

    private final Training training;

    public TrainingCreatedEvent(Training training) {
        this.training = training;
    }

    public Training getTraining() {
        return training;
    }
}
