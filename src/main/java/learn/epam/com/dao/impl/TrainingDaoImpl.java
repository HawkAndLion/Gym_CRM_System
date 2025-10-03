package learn.epam.com.dao.impl;

import learn.epam.com.dao.TrainingDao;
import learn.epam.com.entity.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingDaoImpl.class);
    private static final String SUCCESS_SAVE = "Saved training id={}";
    private static final String UPDATE_TRAINING = "Updated training id={}";
    private static final String DELETE_TRAINING = "Deleted training id={}";
    private static final String NULL_EXCEPTION = "Argument is null ";

    private final Map<Long, Training> storage;
    private final AtomicLong idGenerator = new AtomicLong(0);

    public TrainingDaoImpl(@Qualifier("trainingStorage") Map<Long, Training> storage) {
        this.storage = storage;

        storage.keySet().forEach(k -> idGenerator.updateAndGet(v -> Math.max(v, k)));
    }

    @Override
    public Optional<Training> getById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Training> getAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void save(Training training) {
        if (training != null) {
            if (training.getId() == null) {
                if (idGenerator.get() == 0 && !storage.isEmpty()) {
                    long maxId = storage.keySet().stream().max(Long::compare).orElse(0L);
                    idGenerator.set(maxId);
                }

                training.setId(idGenerator.incrementAndGet());
            }

            storage.put(training.getId(), training);

            LOG.info(SUCCESS_SAVE, training.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void update(Training training) {
        if (training != null) {
            storage.put(training.getId(), training);

            LOG.info(UPDATE_TRAINING, training.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(Training training) {
        if (training != null) {
            storage.remove(training.getId());

            LOG.info(DELETE_TRAINING, training.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public List<Training> findTrainingsByTraineeId(Long traineeId) {

      return getAll().stream()
              .filter(training -> training.getTraineeId().equals(traineeId))
              .collect(Collectors.toList());
    }

    @Override
    public List<Training> findTrainingsByTrainerId(Long trainerId) {

        return getAll().stream()
                .filter(training -> training.getTrainerId().equals(trainerId))
                .collect(Collectors.toList());
    }
}
