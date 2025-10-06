package learn.epam.com.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import learn.epam.com.dao.TraineeDao;
import learn.epam.com.entity.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public class TraineeDaoImpl implements TraineeDao {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeDaoImpl.class);
    private static final String FIND_TRAINEE_BY_USERNAME =
            "select t from Trainee t join User u on t.userId = u.id where u.username = :username";
    private static final String FROM_TRAINEE = "from Trainee";
    private static final String SAVE_TRAINEE = "Saved trainee id={}";
    private static final String UPDATE_TRAINEE = "Updated trainee id={}";
    private static final String DELETE_TRAINEE = "Deleted trainee id={}";
    private static final String USERNAME = "username";
    private static final String NULL_EXCEPTION = "Argument is null ";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Trainee> getById(long id) {
        return Optional.ofNullable(entityManager.find(Trainee.class, id));
    }

    @Override
    public List<Trainee> getAll() {
        return entityManager.createQuery(FROM_TRAINEE, Trainee.class).getResultList();
    }

    @Override
    public void save(Trainee trainee) {
        if (trainee != null) {
            if (trainee.getId() == null) {
                entityManager.persist(trainee);
            } else {
                entityManager.merge(trainee);
            }

            LOG.info(SAVE_TRAINEE, trainee.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void update(Trainee trainee) {
        if (trainee != null) {
            entityManager.merge(trainee);

            LOG.info(UPDATE_TRAINEE, trainee.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(Trainee trainee) {
        if (trainee != null) {
            entityManager.remove(entityManager.contains(trainee) ? trainee : entityManager.merge(trainee));

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
    public Optional<Trainee> findTraineeByUsername(String username) {
        if (username != null) {
            return entityManager.createQuery(FIND_TRAINEE_BY_USERNAME, Trainee.class)
                    .setParameter(USERNAME, username)
                    .getResultList()
                    .stream()
                    .findFirst();
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }
}
