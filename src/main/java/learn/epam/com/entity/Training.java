package learn.epam.com.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Training {
    private Long id;
    private Long traineeId;
    private Long trainerId;
    private String name;
    private Long trainingTypeId;
    private LocalDate trainingDate;
    private double duration;

    public Training() {
        super();
    }

    public Training(Long id, Long traineeId, Long trainerId, String name, Long trainingTypeId, LocalDate trainingDate, double duration) {
        this.id = id;
        this.traineeId = traineeId;
        this.trainerId = trainerId;
        this.name = name;
        this.trainingTypeId = trainingTypeId;
        this.trainingDate = trainingDate;
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTrainingTypeId() {
        return trainingTypeId;
    }

    public void setTrainingTypeId(Long trainingTypeId) {
        this.trainingTypeId = trainingTypeId;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Training training = (Training) object;
        return Double.compare(duration, training.duration) == 0 && Objects.equals(id, training.id) && Objects.equals(traineeId, training.traineeId) && Objects.equals(trainerId, training.trainerId) && Objects.equals(name, training.name) && Objects.equals(trainingTypeId, training.trainingTypeId) && Objects.equals(trainingDate, training.trainingDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, traineeId, trainerId, name, trainingTypeId, trainingDate, duration);
    }
}
