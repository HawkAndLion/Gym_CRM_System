package learn.epam.com.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import learn.epam.com.dao.TrainingDao;
import learn.epam.com.entity.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@EntityScan(basePackages = "learn.epam.com.entity")
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingDaoImpl.class);
    private static final String SUCCESS_SAVE = "Saved training id={}";
    private static final String UPDATE_TRAINING = "Updated training id={}";
    private static final String DELETE_TRAINING = "Deleted training id={}";
    private static final String FROM_TRAINING = "from Training";
    private static final String TRAINEE_ID = "traineeId";
    private static final String TRAINER_ID = "trainerId";
    private static final String FIND_TRAINING_BY_TRAINEE_ID = "FROM Training t WHERE t.traineeId = :traineeId";
    private static final String FIND_TRAINING_BY_TRAINER_ID = "FROM Training t WHERE t.trainerId = :trainerId";
    private static final String NULL_EXCEPTION = "Argument is null ";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Training> getById(long id) {
        return Optional.ofNullable(entityManager.find(Training.class, id));
    }

    @Override
    public List<Training> getAll() {
        return entityManager.createQuery(FROM_TRAINING, Training.class).getResultList();
    }

    @Override
    public void save(Training training) {
        if (training != null) {
            entityManager.persist(training);

            LOG.info(SUCCESS_SAVE, training.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void update(Training training) {
        if (training != null) {
            entityManager.merge(training);

            LOG.info(UPDATE_TRAINING, training.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(Training training) {
        if (training != null) {
            entityManager.remove(entityManager.contains(training) ? training : entityManager.merge(training));

            LOG.info(DELETE_TRAINING, training.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public List<Training> findTrainingsByTraineeId(Long traineeId) {

        return entityManager.createQuery(
                        FIND_TRAINING_BY_TRAINEE_ID, Training.class)
                .setParameter(TRAINEE_ID, traineeId)
                .getResultList();
    }

    @Override
    public List<Training> findTrainingsByTrainerId(Long trainerId) {

        return entityManager.createQuery(
                        FIND_TRAINING_BY_TRAINER_ID, Training.class)
                .setParameter(TRAINER_ID, trainerId)
                .getResultList();
    }
}
