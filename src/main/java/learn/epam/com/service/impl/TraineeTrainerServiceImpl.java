package learn.epam.com.service.impl;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TraineeTrainerDao;
import learn.epam.com.entity.Trainer;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TraineeTrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TraineeTrainerServiceImpl implements TraineeTrainerService {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeTrainerServiceImpl.class);
    private static final String FETCH_TRAINERS_MESSAGE = "Fetching trainers for traineeId={}";
    private static final String SET_TRAINERS_MESSAGE = "Setting trainers {} for traineeId={}";
    private static final String ASSIGN_TRAINER_MESSAGE = "Assigning trainerId={} to traineeId={}";
    private static final String UNASSIGN_TRAINER_MESSAGE = "Unassigning trainerId={} from traineeId={}";
    private static final String FETCH_UNASSIGNED_TRAINER = "Fetching unassigned trainers for traineeUsername={}";
    private static final String UPDATE_TRAINERS = "Updating trainers={} for trainee username={}";
    private static final String CHECK_TRAINEE_USERNAME = "Check if trainee username correct";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found: ";

    private final TraineeTrainerDao traineeTrainerDao;

    public TraineeTrainerServiceImpl(TraineeTrainerDao traineeTrainerDao) {
        this.traineeTrainerDao = traineeTrainerDao;
    }

    @Override
    public Set<Long> getTrainerIdsForTrainee(Long traineeId) {
        LOG.debug(FETCH_TRAINERS_MESSAGE, traineeId);

        return traineeTrainerDao.getTrainerIdsForTrainee(traineeId);
    }

    @Override
    public void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds) {
        LOG.info(SET_TRAINERS_MESSAGE, trainerIds, traineeId);

        traineeTrainerDao.setTrainerIdsForTrainee(traineeId, trainerIds);
    }

    @Override
    public void assignTrainer(Long traineeId, Long trainerId) {
        LOG.info(ASSIGN_TRAINER_MESSAGE, trainerId, traineeId);

        traineeTrainerDao.assignTrainer(traineeId, trainerId);
    }

    @Override
    public void unassignTrainer(Long traineeId, Long trainerId) {
        LOG.info(UNASSIGN_TRAINER_MESSAGE, trainerId, traineeId);

        traineeTrainerDao.unassignTrainer(traineeId, trainerId);
    }

    @Override
    public List<Trainer> getUnassignedTrainersForTrainee(String username) {
        LOG.debug(FETCH_UNASSIGNED_TRAINER, username);

        try {
            return traineeTrainerDao.getUnassignedTrainersForTrainee(username);
        } catch (DaoException exception) {
            throw new RuntimeException(TRAINEE_NOT_FOUND, exception);
        }
    }

    @Override
    public void updateTraineeTrainersList(String traineeUsername, Set<Long> trainerIds) throws ServiceException {
        LOG.info(UPDATE_TRAINERS, trainerIds, traineeUsername);

        try {
            traineeTrainerDao.updateTraineeTrainersList(traineeUsername, trainerIds);
        } catch (DaoException e) {
            throw new ServiceException(CHECK_TRAINEE_USERNAME, e);
        }
    }
}
