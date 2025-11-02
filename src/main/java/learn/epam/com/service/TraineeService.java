package learn.epam.com.service;

import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TraineeService {
    void save(Trainee trainee) throws ServiceException;

    Optional<Trainee> findById(Long id) throws ServiceException;

    void update(Trainee trainee) throws ServiceException;

    void update(String username, Set<Trainer> trainers) throws ServiceException;

    void delete(Trainee trainee) throws ServiceException;

    List<Trainee> findAllTrainee();

    boolean checkCredentials(Long traineeId, String username, String password) throws ServiceException;

    Optional<Trainee> findTraineeByCredentials(String username, String password) throws ServiceException;

    Optional<Trainee> findTraineeByUsername(String username) throws ServiceException;

    void activateTrainee(String username) throws ServiceException;

    void deactivateTrainee(String username) throws ServiceException;

    void deleteTraineeByUsername(String username) throws ServiceException;

    Set<Long> getTrainerIdsForTrainee(Long traineeId);

    void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds);

    void assignTrainer(Long traineeId, Long trainerId);

    void unassignTrainer(Long traineeId, Long trainerId);
}
