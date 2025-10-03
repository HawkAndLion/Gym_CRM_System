package learn.epam.com.dao.impl;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TraineeDao;
import learn.epam.com.dao.TraineeTrainerDao;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TraineeTrainerDaoImpl implements TraineeTrainerDao {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeDaoImpl.class);
    private static final String GET_ASSIGNED_TRAINER_IDS = "Getting trainer Ids for Trainee";
    private static final String ASSIGN_TRAINER_IDS = "Setting trainer Ids for Trainee";
    private static final String UPDATE_TRAINERS = "Updating trainers={} for trainee username={}";
    private static final String GET_UNASSIGNED_TRAINERS = "Getting Unassigned Trainer Ids for TraineeId=";
    private static final String FETCH_UNASSIGNED_TRAINERS = "Fetching unassigned trainers for trainee username={}";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found: ";

    private final Map<Long, Set<Long>> storage;
    private final Map<Long, Trainer> trainerStorage;
    private final TraineeDao traineeDao;

    public TraineeTrainerDaoImpl(@Qualifier("traineeTrainerStorage") Map<Long, Set<Long>> storage, @Qualifier("trainerStorage") Map<Long, Trainer> trainerStorage, TraineeDao traineeDao) {
        this.storage = storage;
        this.trainerStorage = trainerStorage;
        this.traineeDao = traineeDao;
    }

    @Override
    public Set<Long> getTrainerIdsForTrainee(Long traineeId) {
        LOG.info(GET_ASSIGNED_TRAINER_IDS);

        return storage.getOrDefault(traineeId, Collections.emptySet());
    }

    @Override
    public void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds) {
        LOG.info(ASSIGN_TRAINER_IDS);

        storage.put(traineeId, new HashSet<>(trainerIds));
    }

    @Override
    public void assignTrainer(Long traineeId, Long trainerId) {
        storage.computeIfAbsent(traineeId, k -> new HashSet<>()).add(trainerId);
    }

    @Override
    public void unassignTrainer(Long traineeId, Long trainerId) {
        storage.computeIfPresent(traineeId, (k, trainers) -> {
            trainers.remove(trainerId);
            return trainers;
        });
    }

    @Override
    public List<Trainer> getUnassignedTrainersForTrainee(String traineeUsername) throws DaoException {
        LOG.debug(FETCH_UNASSIGNED_TRAINERS, traineeUsername);

        Trainee trainee = traineeDao.findTraineeByUsername(traineeUsername)
                .orElseThrow(() -> new DaoException(TRAINEE_NOT_FOUND + traineeUsername));

        return getUnassignedTrainersForTrainee(trainee.getId());
    }

    @Override
    public List<Trainer> getUnassignedTrainersForTrainee(Long traineeId) {
        LOG.info(GET_UNASSIGNED_TRAINERS + traineeId);
        Set<Long> assignedTrainerIds = storage.getOrDefault(traineeId, Collections.emptySet());

        return trainerStorage.values().stream()
                .filter(trainer -> !assignedTrainerIds.contains(trainer.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void updateTraineeTrainersList(String traineeUsername, Set<Long> trainerIds) throws DaoException {
        LOG.info(UPDATE_TRAINERS, trainerIds, traineeUsername);

        Trainee trainee;

        trainee = traineeDao.findTraineeByUsername(traineeUsername)
                .orElseThrow(() -> new DaoException(TRAINEE_NOT_FOUND + traineeUsername));


        setTrainerIdsForTrainee(trainee.getId(), trainerIds);
    }
}
