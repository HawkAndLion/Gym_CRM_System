package learn.epam.com.service.impl;

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

import java.util.List;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeServiceImpl.class);
    private static final String SUCCESS_SAVE_TRAINEE = "Trainee was created successfully";
    private static final String SUCCESS_UPDATE_TRAINEE = "Trainee was updated successfully";
    private static final String SUCCESS_DELETE_TRAINEE = "Trainee was deleted successfully";
    private static final String FAIL_FIND_TRAINEE = "Trainee not found with id=";
    private static final String FAIL_LOAD_USER = "Failed to load user for trainee";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found";
    private static final String INVALID_PASSWORD = "Invalid current password";
    private static final String NEW_PASSWORD_REQUIRED = "New password required";
    private static final String AUTHENTICATION_FAIL = "Authentication failed";
    private static final String TRAINEE_ALREADY_ACTIVE = "Trainee already active";
    private static final String TRAINEE_ALREADY_INACTIVE = "Trainee already inactive";
    private static final String NULL_EXCEPTION = "Argument is null ";

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
    public void save(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            userCredentialService.ensureUsernameExists(trainee.getUserId());
            userCredentialService.ensurePassword(trainee.getUserId());

            traineeDao.save(trainee);

            LOG.info(SUCCESS_SAVE_TRAINEE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Optional<Trainee> findById(Long id) throws ServiceException {
        return traineeDao.getById(id);
    }

    @Override
    public void update(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            traineeDao.update(trainee);

            LOG.info(SUCCESS_UPDATE_TRAINEE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            traineeDao.delete(trainee);

            LOG.info(SUCCESS_DELETE_TRAINEE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public List<Trainee> findAllTrainee() {
        return traineeDao.getAll();
    }

    @Override
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
    public Optional<Trainee> findTraineeByCredentials(String username, String password) throws ServiceException {
        List<User> users = userDao.getAll();
        return users.stream()
                .filter(u -> username.equalsIgnoreCase(u.getUsername()) && password.equals(u.getPassword()))
                .findFirst()
                .flatMap(u ->
                        traineeDao.getAll().stream()
                                .filter(t -> t.getUserId().equals(u.getId()))
                                .findFirst());
    }

    @Override
    public Optional<Trainee> findTraineeByUsername(String username) {
        if (username != null) {


            return traineeDao.findTraineeByUsername(username);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void changePasswordForTrainee(String username, String oldPassword, String newPassword) throws ServiceException {
        User user = userDao.getAll().stream()
                .filter(user1 -> username.equalsIgnoreCase(user1.getUsername()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

        if (!user.getPassword().equals(oldPassword)) {
            throw new ServiceException(INVALID_PASSWORD);
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new ServiceException(NEW_PASSWORD_REQUIRED);
        }

        user.setPassword(newPassword);
        userDao.update(user);
    }

    @Override
    public void updateTraineeProfile(String username, String password, Trainee updated) throws ServiceException {
        if (username != null && password != null && updated != null) {
            Trainee trainee = findTraineeByCredentials(username, password).orElseThrow(() -> new ServiceException(AUTHENTICATION_FAIL));

            updated.setId(trainee.getId());
            updated.setUserId(trainee.getUserId());
            traineeDao.update(updated);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void activateTrainee(String username) throws ServiceException {
        Trainee trainee = findTraineeByUsername(username).orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND));
        if (trainee.isActive()) throw new ServiceException(TRAINEE_ALREADY_ACTIVE);
        trainee.setActive(true);
        traineeDao.update(trainee);
    }

    @Override
    public void deactivateTrainee(String username) throws ServiceException {
        Trainee trainee = findTraineeByUsername(username).orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND));
        if (!trainee.isActive()) throw new ServiceException(TRAINEE_ALREADY_INACTIVE);
        trainee.setActive(false);
        traineeDao.update(trainee);
    }

    @Override
    @Transactional
    public void deleteTraineeByUsername(String username) throws ServiceException {
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
    }

    private User loadUser(Long userId) throws ServiceException {
        try {
            return userCredentialService
                    .loadUserOrThrow(userId);
        } catch (ServiceException exception) {
            throw new ServiceException(FAIL_LOAD_USER, exception);
        }
    }
}
