package learn.epam.com.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TraineeDao;
import learn.epam.com.dao.TraineeTrainerDao;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.TraineeTrainer;
import learn.epam.com.entity.TraineeTrainerId;
import learn.epam.com.entity.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class TraineeTrainerDaoImpl implements TraineeTrainerDao {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeDaoImpl.class);
    private static final String GET_ASSIGNED_TRAINER_IDS = "Getting trainer Ids for Trainee";
    private static final String ASSIGN_TRAINER_IDS = "Setting trainer Ids for Trainee";
    private static final String UPDATE_TRAINERS = "Updating trainers={} for trainee username={}";
    private static final String GET_UNASSIGNED_TRAINERS = "Getting Unassigned Trainer Ids for TraineeId=";
    private static final String FETCH_UNASSIGNED_TRAINERS = "Fetching unassigned trainers for trainee username={}";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found";
    private static final String TRAINER_ID = "traineeId";
    private static final String GET_TRAINER_ID_LIST = "SELECT tt FROM TraineeTrainer tt WHERE tt.traineeId = :traineeId";
    private static final String DELETE_EXISTING_TRAINER_ID = "DELETE FROM TraineeTrainer tt WHERE tt.traineeId = :traineeId";
    private static final String FROM_TRAINER = "FROM Trainer";
    private static final String NULL_EXCEPTION = "Argument is null ";


    @PersistenceContext
    private EntityManager entityManager;

    private final TraineeDao traineeDao;

    public TraineeTrainerDaoImpl(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Override
    public Set<Long> getTrainerIdsForTrainee(Long traineeId) {
        if (traineeId != null) {
            LOG.info(GET_ASSIGNED_TRAINER_IDS);

            List<TraineeTrainer> mappings = entityManager.createQuery(
                            GET_TRAINER_ID_LIST,
                            TraineeTrainer.class)
                    .setParameter(TRAINER_ID, traineeId)
                    .getResultList();

            return mappings.stream()
                    .map(TraineeTrainer::getTrainerId)
                    .collect(Collectors.toSet());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds) {
        if (traineeId != null) {
            LOG.info(ASSIGN_TRAINER_IDS);

            entityManager.createQuery(DELETE_EXISTING_TRAINER_ID)
                    .setParameter(TRAINER_ID, traineeId)
                    .executeUpdate();

            for (Long trainerId : trainerIds) {
                entityManager.persist(new TraineeTrainer(traineeId, trainerId));
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void assignTrainer(Long traineeId, Long trainerId) {
        if (traineeId != null && trainerId != null) {
            TraineeTrainerId id = new TraineeTrainerId(traineeId, trainerId);

            if (entityManager.find(TraineeTrainer.class, id) == null) {
                entityManager.persist(new TraineeTrainer(traineeId, trainerId));
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void unassignTrainer(Long traineeId, Long trainerId) {
        if (traineeId != null && trainerId != null) {
            TraineeTrainerId id = new TraineeTrainerId(traineeId, trainerId);

            TraineeTrainer traineeTrainer = entityManager.find(TraineeTrainer.class, id);

            if (traineeTrainer != null) {
                entityManager.remove(traineeTrainer);
            }
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

            Set<Long> assignedTrainerIds = getTrainerIdsForTrainee(traineeId);

            return trainers.stream()
                    .filter(trainer -> !assignedTrainerIds.contains(trainer.getId()))
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


            setTrainerIdsForTrainee(trainee.getId(), trainerIds);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }
}
