package learn.epam.com.service;

import learn.epam.com.entity.Trainer;

import java.util.List;
import java.util.Set;

public interface TraineeTrainerService {
    Set<Long> getTrainerIdsForTrainee(Long traineeId);

    void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds);

    void assignTrainer(Long traineeId, Long trainerId);

    void unassignTrainer(Long traineeId, Long trainerId);

    List<Trainer> getUnassignedTrainersForTrainee(String username) throws ServiceException;

    void updateTraineeTrainersList(String traineeUsername, Set<Long> trainerIds);
}
