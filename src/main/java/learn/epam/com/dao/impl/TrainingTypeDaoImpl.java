package learn.epam.com.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import learn.epam.com.dao.TrainingTypeDao;
import learn.epam.com.entity.TrainingType;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@EntityScan(basePackages = "learn.epam.com.entity")
public class TrainingTypeDaoImpl implements TrainingTypeDao {
    private static final String TRAINING_TYPE_CANNOT_BE_MODIFIED = "Training types are constant and cannot be modified.";
    private static final String FROM_TRAINING_TYPE = "from TrainingType";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(TrainingType trainingType) {
        throw new RuntimeException(TRAINING_TYPE_CANNOT_BE_MODIFIED);
    }

    @Override
    public Optional<TrainingType> getById(Long id) {
        return Optional.ofNullable(entityManager.find(TrainingType.class, id));
    }

    @Override
    public void update(TrainingType trainingType) {
        throw new RuntimeException(TRAINING_TYPE_CANNOT_BE_MODIFIED);
    }

    @Override
    public void delete(TrainingType trainingType) {
        throw new RuntimeException(TRAINING_TYPE_CANNOT_BE_MODIFIED);
    }

    @Override
    public List<TrainingType> getAll() {
        return entityManager.createQuery(FROM_TRAINING_TYPE, TrainingType.class).getResultList();
    }
}