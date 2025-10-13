package learn.epam.com.dao;

import learn.epam.com.entity.Trainee;

import java.util.Optional;
import java.util.Set;

public interface TraineeDao extends GenericDao<Trainee> {
    Long getUserId(Trainee trainee);

    Optional<Trainee> findTraineeByUsername(String username);

    Set<Long> getTrainerIdsForTrainee(Long traineeId);

    void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds);

    void assignTrainer(Long traineeId, Long trainerId);

    void unassignTrainer(Long traineeId, Long trainerId);
}
