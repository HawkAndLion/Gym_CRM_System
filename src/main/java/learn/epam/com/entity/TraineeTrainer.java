package learn.epam.com.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "trainee_trainers")
@IdClass(TraineeTrainerId.class)
public class TraineeTrainer {

    @Id
    @Column(name = "trainee_id", nullable = false)
    private Long traineeId;

    @Id
    @Column(name = "trainer_id", nullable = false)
    private Long trainerId;

    public TraineeTrainer() {
        super();
    }

    public TraineeTrainer(Long traineeId, Long trainerId) {
        this.traineeId = traineeId;
        this.trainerId = trainerId;
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
