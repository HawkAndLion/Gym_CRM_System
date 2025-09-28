package learn.epam.com.dao.impl;

import learn.epam.com.dao.TrainingTypeDao;
import learn.epam.com.entity.TrainingType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {
    private final Map<Long, TrainingType> storage;
    private final AtomicLong idGenerator = new AtomicLong(0);

    public TrainingTypeDaoImpl(@Qualifier("trainingTypeStorage") Map<Long, TrainingType> storage) {
        this.storage = storage;
        
        storage.keySet().forEach(k -> idGenerator.updateAndGet(v -> Math.max(v, k)));
    }

    @Override
    public void save(TrainingType trainingType) {
        if (trainingType.getId() == null) {
            trainingType.setId(idGenerator.incrementAndGet());
        }
        storage.put(trainingType.getId(), trainingType);
    }

    @Override
    public Optional<TrainingType> getById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void update(TrainingType trainingType) {
        if (trainingType.getId() != null && storage.containsKey(trainingType.getId())) {
            storage.put(trainingType.getId(), trainingType);
        }
    }

    @Override
    public void delete(TrainingType trainingType) {
        if (trainingType.getId() != null) {
            storage.remove(trainingType.getId());
        }
    }

    @Override
    public List<TrainingType> getAll() {
        return new ArrayList<>(storage.values());
    }
}