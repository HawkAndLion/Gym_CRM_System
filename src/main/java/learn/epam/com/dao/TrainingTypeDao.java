package learn.epam.com.dao;

import learn.epam.com.entity.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {
    void save(TrainingType trainingType);

    Optional<TrainingType> getById(Long id);

    void update(TrainingType trainingType);

    void delete(TrainingType trainingType);

    List<TrainingType> getAll();
}
