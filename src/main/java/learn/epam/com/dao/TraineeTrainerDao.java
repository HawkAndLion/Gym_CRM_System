package learn.epam.com.dao;

import java.util.Set;

public interface TraineeTrainerDao {
    Set<Long> getTrainerIdsForTrainee(Long traineeId);
    void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds);
    void assignTrainer(Long traineeId, Long trainerId);
    void unassignTrainer(Long traineeId, Long trainerId);
//    Set<Long> getTraineeIdsForTrainer(Long trainerId);
}
