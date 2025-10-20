package learn.epam.com.service.impl;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TraineeDao;
import learn.epam.com.dao.TrainingDao;
import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.User;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TraineeService;
import learn.epam.com.service.UserCredentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TraineeServiceImpl implements TraineeService {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeServiceImpl.class);
    private static final String SUCCESS_SAVE_TRAINEE = "Trainee was created successfully";
    private static final String SUCCESS_UPDATE_TRAINEE = "Trainee was updated successfully";
    private static final String SUCCESS_DELETE_TRAINEE = "Trainee was deleted successfully";
    private static final String FAIL_FIND_TRAINEE = "Trainee not found with id=";
    private static final String FAIL_LOAD_USER = "Failed to load user for trainee";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found";
    private static final String AUTHENTICATION_FAIL = "Authentication failed";
    private static final String TRAINEE_ALREADY_ACTIVE = "Trainee already active";
    private static final String TRAINEE_ALREADY_INACTIVE = "Trainee already inactive";
    private static final String USER_ID_REQUIRED = "Trainee.userId is required";
    private static final String ADDRESS_REQUIRED = "Trainee.address is required";
    private static final String DATE_OF_BIRTH_REQUIRED = "Trainee.dateOfBirth is required";
    private static final String DATE_OF_BIRTH_IN_PAST_REQUIRED = "Trainee.dateOfBirth must be in the past";
    private static final String ID_REQUIRED = "Trainee.id is required for update";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String FETCH_TRAINERS_MESSAGE = "Fetching trainers for traineeId={}";
    private static final String SET_TRAINERS_MESSAGE = "Setting trainers {} for traineeId={}";
    private static final String ASSIGN_TRAINER_MESSAGE = "Assigning trainerId={} to traineeId={}";
    private static final String UNASSIGN_TRAINER_MESSAGE = "Unassigning trainerId={} from traineeId={}";

    private final TraineeDao traineeDao;
    private final UserCredentialService userCredentialService;
    private final UserDao userDao;
    private final TrainingDao trainingDao;

    @Autowired
    public TraineeServiceImpl(TraineeDao traineeDao, UserCredentialService userCredentialService, UserDao userDao, TrainingDao trainingDao) {
        this.traineeDao = traineeDao;
        this.userCredentialService = userCredentialService;
        this.userDao = userDao;
        this.trainingDao = trainingDao;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void save(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            validateTraineeForCreate(trainee);

            userCredentialService.ensureUsernameExists(trainee.getUserId());
            userCredentialService.ensurePassword(trainee.getUserId());

            traineeDao.save(trainee);

            LOG.info(SUCCESS_SAVE_TRAINEE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Optional<Trainee> findById(Long id) throws ServiceException {
        return traineeDao.getById(id);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void update(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            validateTraineeForUpdate(trainee);

            traineeDao.update(trainee);

            LOG.info(SUCCESS_UPDATE_TRAINEE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void delete(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            traineeDao.delete(trainee);

            LOG.info(SUCCESS_DELETE_TRAINEE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public List<Trainee> findAllTrainee() {
        return traineeDao.getAll();
    }

    @Override
    @Transactional
    public boolean checkCredentials(Long traineeId, String username, String password) throws ServiceException {
        if (traineeId != null && username != null && password != null) {
            Trainee trainee = findById(traineeId)
                    .orElseThrow(() -> new ServiceException(FAIL_FIND_TRAINEE + traineeId));

            Long userId = trainee.getUserId();
            User user = loadUser(userId);

            return username.equalsIgnoreCase(user.getUsername()) && password.equals(user.getPassword());

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public Optional<Trainee> findTraineeByCredentials(String username, String password) {
        if (username != null & password != null) {
            List<User> users = userDao.getAll();

            return users.stream()
                    .filter(u -> username.equalsIgnoreCase(u.getUsername()) && password.equals(u.getPassword()))
                    .findFirst()
                    .flatMap(u ->
                            traineeDao.getAll().stream()
                                    .filter(t -> t.getUserId().equals(u.getId()))
                                    .findFirst());

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public Optional<Trainee> findTraineeByUsername(String username) throws ServiceException {
        if (username != null) {
            try {
                return traineeDao.findTraineeByUsername(username);
            } catch (DaoException exception) {
                throw new ServiceException(TRAINEE_NOT_FOUND);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void activateTrainee(String username) throws ServiceException {
        if (username != null) {
            Trainee trainee = findTraineeByUsername(username).orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND));

            if (trainee.isActive()) throw new ServiceException(TRAINEE_ALREADY_ACTIVE);

            trainee.setActive(true);
            traineeDao.update(trainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deactivateTrainee(String username) throws ServiceException {
        if (username != null) {
            Trainee trainee = findTraineeByUsername(username).orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND));

            if (!trainee.isActive()) throw new ServiceException(TRAINEE_ALREADY_INACTIVE);

            trainee.setActive(false);
            traineeDao.update(trainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteTraineeByUsername(String username) throws ServiceException {
        if (username != null) {
            Trainee trainee = findTraineeByUsername(username)
                    .orElseThrow(() -> new ServiceException(AUTHENTICATION_FAIL));

            Long traineeId = trainee.getId();

            trainingDao.getAll().stream()
                    .filter(training -> training.getTraineeId().equals(traineeId))
                    .forEach(training -> {
                        trainingDao.delete(training);
                    });

            traineeDao.delete(trainee);

            userDao.getById(trainee.getUserId()).ifPresent(userDao::delete);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Set<Long> getTrainerIdsForTrainee(Long traineeId) {
        LOG.debug(FETCH_TRAINERS_MESSAGE, traineeId);

        return traineeDao.getTrainerIdsForTrainee(traineeId);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds) {
        LOG.info(SET_TRAINERS_MESSAGE, trainerIds, traineeId);

        traineeDao.setTrainerIdsForTrainee(traineeId, trainerIds);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void assignTrainer(Long traineeId, Long trainerId) {
        LOG.info(ASSIGN_TRAINER_MESSAGE, trainerId, traineeId);

        traineeDao.assignTrainer(traineeId, trainerId);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void unassignTrainer(Long traineeId, Long trainerId) {
        LOG.info(UNASSIGN_TRAINER_MESSAGE, trainerId, traineeId);

        traineeDao.unassignTrainer(traineeId, trainerId);
    }

    private User loadUser(Long userId) throws ServiceException {
        try {
            return userCredentialService
                    .loadUserOrThrow(userId);
        } catch (ServiceException exception) {
            throw new ServiceException(FAIL_LOAD_USER, exception);
        }
    }

    private static void validateTraineeForCreate(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            if (trainee.getUserId() == null) throw new ServiceException(USER_ID_REQUIRED);

            if (isBlank(trainee.getAddress())) throw new ServiceException(ADDRESS_REQUIRED);

            if (trainee.getDateOfBirth() == null) throw new ServiceException(DATE_OF_BIRTH_REQUIRED);

            if (!trainee.getDateOfBirth().isBefore(LocalDate.now()))
                throw new ServiceException(DATE_OF_BIRTH_IN_PAST_REQUIRED);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private static void validateTraineeForUpdate(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            if (trainee.getId() == null) throw new ServiceException(ID_REQUIRED);

            if (trainee.getUserId() == null) throw new ServiceException(USER_ID_REQUIRED);

            validateTraineeForCreate(trainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
