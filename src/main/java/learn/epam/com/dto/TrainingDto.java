package learn.epam.com.dto;

import java.time.LocalDate;
import java.util.Objects;

public class TrainingDto {
    private String name;
    private LocalDate date;
    private String trainingType;
    private double duration;
    private String traineeUsername;
    private String trainerUsername;
    private String trainerName;


    public TrainingDto() {
    }

    public TrainingDto(String name, LocalDate date, String trainingType, double duration, String trainerName) {
        this.name = name;
        this.date = date;
        this.trainingType = trainingType;
        this.duration = duration;
        this.trainerName = trainerName;
    }

    public TrainingDto(String name, String traineeUsername, String trainerUsername, String trainingType, LocalDate date, double duration) {
        this.name = name;
        this.traineeUsername = traineeUsername;
        this.trainerUsername = trainerUsername;
        this.trainingType = trainingType;
        this.date = date;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(String trainingType) {
        this.trainingType = trainingType;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TrainingDto that = (TrainingDto) object;
        return Double.compare(duration, that.duration) == 0 && Objects.equals(name, that.name) && Objects.equals(date, that.date) && Objects.equals(trainingType, that.trainingType) && Objects.equals(traineeUsername, that.traineeUsername) && Objects.equals(trainerUsername, that.trainerUsername) && Objects.equals(trainerName, that.trainerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, date, trainingType, duration, traineeUsername, trainerUsername, trainerName);
    }
}
