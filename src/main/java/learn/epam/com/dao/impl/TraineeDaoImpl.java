package learn.epam.com.dao.impl;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TraineeDao;
import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.Trainee;
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
public class TraineeDaoImpl implements TraineeDao {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeDaoImpl.class);
    private static final String SUCCESS_SAVE = "Saved trainee id={}";
    private static final String UPDATE_TRAINEE = "Updated trainee id={}";
    private static final String DELETE_TRAINEE = "Deleted trainee id={}";
    private static final String NO_SUCH_USERNAME = "Trainee not found with username: ";
    private static final String NULL_EXCEPTION = "Argument is null ";

    private final UserDao userDao;
    private final Map<Long, Trainee> storage;
    private final AtomicLong idGenerator = new AtomicLong(0);

    public TraineeDaoImpl(@Qualifier("traineeStorage") Map<Long, Trainee> storage, UserDao userDao) {
        this.storage = storage;
        this.userDao = userDao;

        storage.keySet().forEach(k -> idGenerator.updateAndGet(v -> Math.max(v, k)));
    }

    @Override
    public Optional<Trainee> getById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainee> getAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void save(Trainee trainee) {
        if (trainee != null) {
            if (trainee.getId() == null) {
                if (idGenerator.get() == 0 && !storage.isEmpty()) {
                    long maxId = storage.keySet().stream().max(Long::compare).orElse(0L);
                    idGenerator.set(maxId);
                }

                trainee.setId(idGenerator.incrementAndGet());
            }

            storage.put(trainee.getId(), trainee);

            LOG.info(SUCCESS_SAVE, trainee.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void update(Trainee trainee) {
        if (trainee != null) {
            storage.put(trainee.getId(), trainee);

            LOG.info(UPDATE_TRAINEE, trainee.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(Trainee trainee) {
        if (trainee != null) {
            storage.remove(trainee.getId());

            LOG.info(DELETE_TRAINEE, trainee.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Long getUserId(Trainee trainee) {
        if (trainee != null) {

            return trainee.getUserId();
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Optional<Trainee> findTraineeByUsername(String username) throws DaoException {
        if (username != null) {
            return Optional.ofNullable(userDao.getAll().stream()
                    .filter(user -> username.equalsIgnoreCase(user.getUsername()))
                    .findFirst()
                    .flatMap(user -> getAll().stream()
                            .filter(trainee -> trainee.getUserId().equals(user.getId()))
                            .findFirst())
                    .orElseThrow(() -> new DaoException(NO_SUCH_USERNAME + username)));


        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }
}
