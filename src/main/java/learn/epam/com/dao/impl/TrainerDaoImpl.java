package learn.epam.com.dao.impl;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TrainerDao;
import learn.epam.com.entity.Trainer;
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
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger LOG = LoggerFactory.getLogger(TrainerDaoImpl.class);
    private static final String SUCCESS_SAVE = "Saved trainer id={}";
    private static final String UPDATE_TRAINER = "Updated trainer id={}";
    private static final String DELETE_TRAINER = "Deleted trainer id={}";
    private static final String NULL_EXCEPTION = "Argument is null ";

    private final Map<Long, Trainer> storage;
    private final AtomicLong idGenerator = new AtomicLong(0);

    public TrainerDaoImpl(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
        this.storage = storage;

        storage.keySet().forEach(k -> idGenerator.updateAndGet(v -> Math.max(v, k)));
    }

    @Override
    public Optional<Trainer> getById(long id) throws DaoException {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainer> getAll() throws DaoException {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void save(Trainer trainer) throws DaoException {
        if (trainer != null) {
            if (trainer.getId() == null) {
                trainer.setId(idGenerator.incrementAndGet());
            }

            storage.put(trainer.getId(), trainer);

            LOG.info(SUCCESS_SAVE, trainer.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void update(Trainer trainer) throws DaoException {
        if (trainer != null) {
            storage.put(trainer.getId(), trainer);

            LOG.info(UPDATE_TRAINER, trainer.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(Trainer trainer) throws DaoException {
        if (trainer != null) {
            storage.remove(trainer.getId());

            LOG.info(DELETE_TRAINER, trainer.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Long getUserId(Trainer trainer) throws DaoException {
        if (trainer != null) {

            return trainer.getUserId();
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }
}
