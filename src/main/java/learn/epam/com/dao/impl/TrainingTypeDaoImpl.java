package learn.epam.com.dao.impl;

import learn.epam.com.dao.TrainingTypeDao;
import learn.epam.com.entity.TrainingType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {
    private static final String TRAINING_TYPE_CANNOT_BE_MODIFIED = "Training types are constant and cannot be modified.";

    private final Map<Long, TrainingType> storage;
    private final AtomicLong idGenerator = new AtomicLong(0);

    public TrainingTypeDaoImpl(@Qualifier("trainingTypeStorage") Map<Long, TrainingType> storage) {
        this.storage = storage;

        storage.keySet().forEach(k -> idGenerator.updateAndGet(v -> Math.max(v, k)));
    }

    @Override
    public void save(TrainingType trainingType) {
        throw new RuntimeException(TRAINING_TYPE_CANNOT_BE_MODIFIED);
    }

    @Override
    public Optional<TrainingType> getById(Long id) {
        return Optional.ofNullable(storage.get(id));
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
        return new ArrayList<>(storage.values());
    }
}