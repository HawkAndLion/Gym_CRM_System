package learn.epam.com.service;

import learn.epam.com.dto.TrainingDto;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TrainingService {
    void save(Training training) throws ServiceException;

    Optional<Training> findById(Long id) throws ServiceException;

    void update(Training training) throws ServiceException;

    Training update(Trainee trainee, Trainer trainer, TrainingDto training, Long trainingTypeId) throws ServiceException;

    void delete(Training training) throws ServiceException;

    List<Training> findAllTrainings();

    List<Training> findTrainingsForTraineeByCriteria(String traineeUsername, LocalDate from, LocalDate to, String trainerName, Long trainingTypeId) throws ServiceException;

    List<Training> findTrainingsForTrainerByCriteria(String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeName) throws ServiceException;

    List<TrainingDto> getTrainingDtoList(List<Training> trainings);

    void updateTrainingsByTrainee(String username, Set<Trainer> trainers) throws ServiceException;

    void deleteById(Long id) throws ServiceException;

    double getTotalDurationForTrainer(Long trainerId);
}
