package learn.epam.com.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TraineeDao;
import learn.epam.com.dao.TrainerDao;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger LOG = LoggerFactory.getLogger(TrainerDaoImpl.class);
    private static final String SUCCESS_SAVE = "Saved trainer id={}";
    private static final String UPDATE_TRAINER = "Updated trainer id={}";
    private static final String DELETE_TRAINER = "Deleted trainer id={}";
    private static final String FROM_TRAINER = "from Trainer";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String UPDATE_TRAINERS = "Updating trainers={} for trainee username={}";
    private static final String GET_UNASSIGNED_TRAINERS = "Getting Unassigned Trainer Ids for TraineeId=";
    private static final String FETCH_UNASSIGNED_TRAINERS = "Fetching unassigned trainers for trainee username={}";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found";
    private static final String GET_ASSIGNED_TRAINEE_IDS = "Getting trainee Ids for Trainer";
    private static final String TRAINER_ID = "trainerId";
    private static final String GET_TRAINEE_ID_LIST = "SELECT t.id FROM Trainer tr JOIN tr.trainees t WHERE tr.id = :trainerId";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TraineeDao traineeDao;

    @Override
    public Optional<Trainer> getById(long id) {
        return Optional.ofNullable(entityManager.find(Trainer.class, id));
    }

    @Override
    public List<Trainer> getAll() {
        return entityManager.createQuery(FROM_TRAINER, Trainer.class).getResultList();
    }

    @Override
    public void save(Trainer trainer) {
        if (trainer != null) {
            entityManager.persist(trainer);

            LOG.info(SUCCESS_SAVE, trainer.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void update(Trainer trainer) {
        if (trainer != null) {
            entityManager.merge(trainer);

            LOG.info(UPDATE_TRAINER, trainer.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(Trainer trainer) {
        if (trainer != null) {
            entityManager.remove(entityManager.contains(trainer) ? trainer : entityManager.merge(trainer));

            LOG.info(DELETE_TRAINER, trainer.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Long getUserId(Trainer trainer) {
        if (trainer != null) {

            return trainer.getUser().getId();
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public List<Trainer> getUnassignedTrainersForTrainee(String traineeUsername) throws DaoException {
        if (traineeUsername != null) {
            LOG.debug(FETCH_UNASSIGNED_TRAINERS, traineeUsername);

            Trainee trainee = traineeDao.findTraineeByUsername(traineeUsername)
                    .orElseThrow(() -> new DaoException(TRAINEE_NOT_FOUND));

            return getUnassignedTrainersForTrainee(trainee.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public List<Trainer> getUnassignedTrainersForTrainee(Long traineeId) {
        if (traineeId != null) {
            LOG.info(GET_UNASSIGNED_TRAINERS + traineeId);

            List<Trainer> trainers = entityManager.createQuery(FROM_TRAINER, Trainer.class).getResultList();

            Set<Long> assignedTrainerIds = traineeDao.getTrainerIdsForTrainee(traineeId);

            return trainers.stream()
                    .filter(trainer -> trainer.isActive() && !assignedTrainerIds.contains(trainer.getId()))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void updateTraineeTrainersList(String traineeUsername, Set<Long> trainerIds) throws DaoException {
        if (traineeUsername != null) {
            LOG.info(UPDATE_TRAINERS, trainerIds, traineeUsername);

            Trainee trainee = traineeDao.findTraineeByUsername(traineeUsername)
                    .orElseThrow(() -> new DaoException(TRAINEE_NOT_FOUND));

            traineeDao.setTrainerIdsForTrainee(trainee.getId(), trainerIds);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Set<Long> getTraineeIdsForTrainer(Long traineeId) {
        if (traineeId != null) {
            LOG.info(GET_ASSIGNED_TRAINEE_IDS);

            List<Long> trainerIds = entityManager.createQuery(
                            GET_TRAINEE_ID_LIST,
                            Long.class)
                    .setParameter(TRAINER_ID, traineeId)
                    .getResultList();

            return new HashSet<>(trainerIds);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }
}
