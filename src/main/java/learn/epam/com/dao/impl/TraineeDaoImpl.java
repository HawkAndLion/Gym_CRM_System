package learn.epam.com.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TraineeDao;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public class TraineeDaoImpl implements TraineeDao {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeDaoImpl.class);
    private static final String FIND_TRAINEE_BY_USERNAME =
            "SELECT t.* FROM trainees t JOIN users u ON/**/ t.user_id = u.id WHERE u.username = :username";
    private static final String FROM_TRAINEE = "from Trainee";
    private static final String SAVE_TRAINEE = "Saved trainee id={}";
    private static final String UPDATE_TRAINEE = "Updated trainee id={}";
    private static final String DELETE_TRAINEE = "Deleted trainee id={}";
    private static final String USERNAME = "username";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String GET_ASSIGNED_TRAINER_IDS = "Getting trainer Ids for Trainee";
    private static final String ASSIGN_TRAINER_IDS = "Setting trainer Ids for Trainee";
    private static final String TRAINEE_ID = "traineeId";
    private static final String GET_TRAINER_ID_LIST = "SELECT tr.id FROM Trainee t JOIN t.trainers tr WHERE t.id = :traineeId";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found for id=";

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
            return trainee.getUser().getId();
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Optional<Trainee> findTraineeByUsername(String username) throws DaoException {
        if (username != null) {
            try {
                List<Trainee> trainees = entityManager.createNativeQuery(FIND_TRAINEE_BY_USERNAME, Trainee.class)
                        .setParameter("username", username)
                        .getResultList();

                return trainees.stream().findFirst();
            } catch (Exception e) {
                throw new DaoException("Error while finding trainee by username: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Set<Long> getTrainerIdsForTrainee(Long traineeId) {
        if (traineeId != null) {
            LOG.info(GET_ASSIGNED_TRAINER_IDS);

            List<Long> trainerIds = entityManager.createQuery(
                            GET_TRAINER_ID_LIST,
                            Long.class)
                    .setParameter(TRAINEE_ID, traineeId)
                    .getResultList();

            return new HashSet<>(trainerIds);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds) {
        if (traineeId != null && trainerIds != null) {
            LOG.info(ASSIGN_TRAINER_IDS);

            Trainee trainee = entityManager.find(Trainee.class, traineeId);

            if (trainee == null) {
                throw new IllegalArgumentException(TRAINEE_NOT_FOUND + traineeId);
            }

            trainee.getTrainers().clear();

            for (Long trainerId : trainerIds) {
                Trainer trainer = entityManager.find(Trainer.class, trainerId);

                if (trainer != null) {
                    trainee.getTrainers().add(trainer);
                    trainer.getTrainees().add(trainee);
                }
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void assignTrainer(Long traineeId, Long trainerId) {
        if (traineeId != null && trainerId != null) {
            Trainee trainee = entityManager.find(Trainee.class, traineeId);
            Trainer trainer = entityManager.find(Trainer.class, trainerId);

            trainee.getTrainers().add(trainer);
            trainer.getTrainees().add(trainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void unassignTrainer(Long traineeId, Long trainerId) {
        if (traineeId != null && trainerId != null) {
            Trainee trainee = entityManager.find(Trainee.class, traineeId);
            Trainer trainer = entityManager.find(Trainer.class, trainerId);

            trainee.getTrainers().remove(trainer);
            trainer.getTrainees().remove(trainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }
}
