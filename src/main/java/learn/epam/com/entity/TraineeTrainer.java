package learn.epam.com.entity;

import java.util.Objects;

public class TraineeTrainer {
    private Long traineeId;
    private Long trainerId;

    public TraineeTrainer(){
        super();
    }

    public Long getTraineeId() {
        return traineeId;
    }

    public void setTraineeId(Long traineeId) {
        this.traineeId = traineeId;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TraineeTrainer that = (TraineeTrainer) object;
        return Objects.equals(traineeId, that.traineeId) && Objects.equals(trainerId, that.trainerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traineeId, trainerId);
    }
}
