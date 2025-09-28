package learn.epam.com.service;

import learn.epam.com.entity.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeService {
    void save(TrainingType trainingType) throws ServiceException;

    Optional<TrainingType> findById(Long id) throws ServiceException;

    void update(TrainingType trainingType) throws ServiceException;

    void delete(TrainingType trainingType) throws ServiceException;

    List<TrainingType> findAllTrainingTypes() throws ServiceException;
}
