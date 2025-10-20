package learn.epam.com.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {
    private static final Logger LOG = LoggerFactory.getLogger(UserDaoImpl.class);
    private static final String SUCCESS_SAVE = "Saved user id={}";
    private static final String UPDATE_USER = "Updated user id={}";
    private static final String DELETE_USER = "Deleted user id={}";
    private static final String FROM_USER = "from User";
    private static final String FIND_BY_USERNAME = "SELECT u FROM User u WHERE u.username = :username";
    private static final String NULL_EXCEPTION = "Argument is null ";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<User> getById(long id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    public List<User> getAll() {
        return entityManager.createQuery(FROM_USER, User.class).getResultList();
    }

    @Override
    public void save(User user) {
        if (user != null) {
            entityManager.persist(user);

            LOG.info(SUCCESS_SAVE, user.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void update(User user) {
        if (user != null) {
            entityManager.merge(user);

            LOG.info(UPDATE_USER, user.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(User user) {
        if (user != null) {
            entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));

            LOG.info(DELETE_USER, user.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Optional<User> getByUsername(String username) {
        if (username != null && !username.isBlank()) {
            List<User> users = entityManager.createQuery(
                            FIND_BY_USERNAME, User.class)
                    .setParameter("username", username)
                    .getResultList();

            return users.stream().findFirst();

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }
}
