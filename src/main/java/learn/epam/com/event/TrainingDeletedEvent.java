package learn.epam.com.event;

import learn.epam.com.entity.Training;

public class TrainingDeletedEvent {
    private final Training training;

    public TrainingDeletedEvent(Training training) {
        this.training = training;
    }

    public Training getTraining() {
        return training;
    }
}

