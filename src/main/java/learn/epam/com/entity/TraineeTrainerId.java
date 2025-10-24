package learn.epam.com.entity;

import java.io.Serializable;
import java.util.Objects;

public class TraineeTrainerId implements Serializable { // my composite key
    private Long traineeId;
    private Long trainerId;

    public TraineeTrainerId() {}

    public TraineeTrainerId(Long traineeId, Long trainerId) {
        this.traineeId = traineeId;
        this.trainerId = trainerId;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TraineeTrainerId that = (TraineeTrainerId) object;
        return Objects.equals(traineeId, that.traineeId) && Objects.equals(trainerId, that.trainerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traineeId, trainerId);
    }
}
