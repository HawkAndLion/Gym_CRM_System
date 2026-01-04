package learn.epam.com.service;

import learn.epam.com.api.model.TraineeTrainersRequest;
import learn.epam.com.api.model.TrainerProfileResponse;
import learn.epam.com.entity.Trainer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TrainerService {
    void save(Trainer trainer) throws ServiceException;

    Optional<Trainer> findById(Long id) throws ServiceException;

    void update(Trainer trainer) throws ServiceException;

    void delete(Trainer trainer) throws ServiceException;

    List<Trainer> findAllTrainers();

    boolean checkCredentials(Long trainerId, String username, String password) throws ServiceException;

    Optional<Trainer> findTrainerByCredentials(String username, String password);

    Optional<Trainer> findTrainerByUsername(String username) throws ServiceException;

    void activateTrainer(String username) throws ServiceException;

    void deactivateTrainer(String username) throws ServiceException;

    List<Trainer> getUnassignedTrainersForTrainee(String username) throws ServiceException;

    void updateTraineeTrainersList(String traineeUsername, Set<Long> trainerIds) throws ServiceException;

    void deleteTrainerByUsername(String username) throws ServiceException;

    Set<Long> getTraineeIdsForTrainer(Long trainerId);

    Set<Trainer> getTrainersByUsername(TraineeTrainersRequest request) throws ServiceException;

    List<TrainerProfileResponse> getTrainerProfileResponse(Set<Trainer> trainers) throws ServiceException;

    Set<TrainerProfileResponse> getTrainerProfileDtoList(String username) throws ServiceException;

    void assignTrainerToTrainee(String trainerUsername, String traineeUsername) throws ServiceException;
}
