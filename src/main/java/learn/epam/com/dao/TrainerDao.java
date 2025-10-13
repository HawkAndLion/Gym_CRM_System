package learn.epam.com.dao;

import learn.epam.com.entity.Trainer;

import java.util.List;
import java.util.Set;

public interface TrainerDao extends GenericDao<Trainer> {
    Long getUserId(Trainer trainer);

    List<Trainer> getUnassignedTrainersForTrainee(String traineeUsername) throws DaoException;

    List<Trainer> getUnassignedTrainersForTrainee(Long traineeId);

    void updateTraineeTrainersList(String traineeUsername, Set<Long> trainerIds) throws DaoException;
}
