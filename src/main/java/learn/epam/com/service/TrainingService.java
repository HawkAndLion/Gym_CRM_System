package learn.epam.com.service;

import learn.epam.com.entity.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingService {
    void save(Training training) throws ServiceException;

    Optional<Training> findById(Long id) throws ServiceException;

    void update(Training training) throws ServiceException;

    void delete(Training training) throws ServiceException;

    List<Training> findAllTrainings();

    List<Training> findTrainingsForTraineeByCriteria(String traineeUsername, LocalDate from, LocalDate to, String trainerName, Long trainingTypeId) throws ServiceException;

    List<Training> findTrainingsForTrainerByCriteria(String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeName) throws ServiceException;
}
