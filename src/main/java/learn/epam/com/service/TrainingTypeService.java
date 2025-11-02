package learn.epam.com.service;

import learn.epam.com.entity.TrainingType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TrainingTypeService {
    Optional<TrainingType> findById(Long id) throws ServiceException;

    List<TrainingType> findAllTrainingTypes();

    Long getTrainingTypeId(String trainingType) throws ServiceException;

    List<Map<String, Object>> getTrainingTypes();
}
