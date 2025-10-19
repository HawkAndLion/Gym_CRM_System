package learn.epam.com.dto;

import java.time.LocalDate;
import java.util.Objects;

public class TrainingDto {
    String name;
    LocalDate date;
    String trainingType;
    double duration;
    String trainerName;  // NB! Check trainerName or traineeName depending on context

    public TrainingDto(){}

    public TrainingDto(String name, LocalDate date, String trainingType, double duration, String trainerName) {
        this.name = name;
        this.date = date;
        this.trainingType = trainingType;
        this.duration = duration;
        this.trainerName = trainerName;
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
        return Double.compare(duration, that.duration) == 0 && Objects.equals(name, that.name) && Objects.equals(date, that.date) && Objects.equals(trainingType, that.trainingType) && Objects.equals(trainerName, that.trainerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, date, trainingType, duration, trainerName);
    }
}
