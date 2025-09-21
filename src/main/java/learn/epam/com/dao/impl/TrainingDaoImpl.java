package learn.epam.com.dao.impl;

import learn.epam.com.dao.DaoException;
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
    public Optional<Training> getById(long id) throws DaoException {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Training> getAll() throws DaoException {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void save(Training training) throws DaoException {
        if (training != null) {
            if (training.getId() == null) {
                training.setId(idGenerator.incrementAndGet());
            }

            storage.put(training.getId(), training);

            LOG.info(SUCCESS_SAVE, training.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void update(Training training) throws DaoException {
        if (training != null) {
            storage.put(training.getId(), training);

            LOG.info(UPDATE_TRAINING, training.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(Training training) throws DaoException {
        if (training != null) {
            storage.remove(training.getId());

            LOG.info(DELETE_TRAINING, training.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }
}
