package learn.epam.com.dao.impl;

import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.User;
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
public class UserDaoImpl implements UserDao {
    private static final Logger LOG = LoggerFactory.getLogger(UserDaoImpl.class);
    private static final String SUCCESS_SAVE = "Saved user id={}";
    private static final String UPDATE_USER = "Updated user id={}";
    private static final String DELETE_USER = "Deleted user id={}";
    private static final String NULL_EXCEPTION = "Argument is null ";

    private final Map<Long, User> storage;
    private final AtomicLong idGenerator = new AtomicLong(0);

    public UserDaoImpl(@Qualifier("userStorage") Map<Long, User> storage) {
        this.storage = storage;

        storage.keySet().forEach(k -> idGenerator.updateAndGet(v -> Math.max(v, k)));
    }

    @Override
    public Optional<User> getById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void save(User user) {
        if (user != null) {
            if (user.getId() == null) {
                if (idGenerator.get() == 0 && !storage.isEmpty()) {
                    long maxId = storage.keySet().stream().max(Long::compare).orElse(0L);
                    idGenerator.set(maxId);
                }

                user.setId(idGenerator.incrementAndGet());
            }

            storage.put(user.getId(), user);

            LOG.info(SUCCESS_SAVE, user.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void update(User user) {
        if (user != null) {
            storage.put(user.getId(), user);

            LOG.info(UPDATE_USER, user.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(User user) {
        if (user != null) {
            storage.remove(user.getId());

            LOG.info(DELETE_USER, user.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }
}
