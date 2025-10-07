package learn.epam.com.dao;

import learn.epam.com.entity.Trainer;

import java.util.List;
import java.util.Set;

public interface TraineeTrainerDao {
    Set<Long> getTrainerIdsForTrainee(Long traineeId);

    void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds);

    void assignTrainer(Long traineeId, Long trainerId);

    void unassignTrainer(Long traineeId, Long trainerId);

    List<Trainer> getUnassignedTrainersForTrainee(String traineeUsername) throws DaoException;

    List<Trainer> getUnassignedTrainersForTrainee(Long traineeId);

    void updateTraineeTrainersList(String traineeUsername, Set<Long> trainerIds) throws DaoException;
}
