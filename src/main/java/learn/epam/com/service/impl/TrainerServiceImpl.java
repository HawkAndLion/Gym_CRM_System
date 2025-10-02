package learn.epam.com.service.impl;

import learn.epam.com.dao.TrainerDao;
import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainerService;
import learn.epam.com.service.UserCredentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerServiceImpl implements TrainerService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainerServiceImpl.class);
    private static final String SUCCESS_SAVE_TRAINER = "Trainer was created successfully";
    private static final String SUCCESS_UPDATE_TRAINER = "Trainer was updated successfully";
    private static final String SUCCESS_DELETE_TRAINER = "Trainer was deleted successfully";
    private static final String FAIL_FIND_TRAINER = "Trainer not found with id=";
    private static final String FAIL_LOAD_USER = "Failed to load user for trainer";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String INVALID_PASSWORD = "Invalid current password";
    private static final String NEW_PASSWORD_REQUIRED = "New password required";
    private static final String AUTHENTICATION_FAIL = "Authentication failed";
    private static final String TRAINER_NOT_FOUND = "Trainee not found";
    private static final String TRAINER_ALREADY_ACTIVE = "Trainee already active";
    private static final String TRAINER_ALREADY_INACTIVE = "Trainee already inactive";
    private static final String NO_SUCH_USERNAME = "Trainer not found with username: ";
    private static final String CHECK_USERNAME_AND_PASSWORD = "Please check username and password.";
    private static final String NULL_EXCEPTION = "Argument is null ";

    private final TrainerDao trainerDao;
    private final UserCredentialService userCredentialService;
    private final UserDao userDao;

    @Autowired
    public TrainerServiceImpl(TrainerDao trainerDao, UserCredentialService userCredentialService, UserDao userDao) {
        this.trainerDao = trainerDao;
        this.userCredentialService = userCredentialService;
        this.userDao = userDao;
    }


    @Override
    public void save(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            userCredentialService.ensureUsernameExists(trainer.getUserId());
            userCredentialService.ensurePassword(trainer.getUserId());

            trainerDao.save(trainer);

            LOG.info(SUCCESS_SAVE_TRAINER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Optional<Trainer> findById(Long id) throws ServiceException {
        return trainerDao.getById(id);
    }

    @Override
    public void update(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            trainerDao.update(trainer);

            LOG.info(SUCCESS_UPDATE_TRAINER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            trainerDao.delete(trainer);

            LOG.info(SUCCESS_DELETE_TRAINER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public List<Trainer> findAllTrainers() {
        return trainerDao.getAll();
    }

    @Override
    public boolean checkCredentials(Long trainerId, String username, String password) throws ServiceException {
        if (trainerId != null && username != null && password != null) {
            Trainer trainer = findById(trainerId)
                    .orElseThrow(() -> new ServiceException(FAIL_FIND_TRAINER + trainerId));

            Long userId = trainer.getUserId();
            User user = loadUser(userId);

            return username.equalsIgnoreCase(user.getUsername()) && password.equals(user.getPassword());

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Optional<Trainer> findTrainerByCredentials(String username, String password) throws ServiceException {
        List<User> users = userDao.getAll();
        return Optional.ofNullable(users.stream()
                .filter(u -> username.equalsIgnoreCase(u.getUsername()) && password.equals(u.getPassword()))
                .findFirst()
                .flatMap(u ->
                        trainerDao.getAll().stream()
                                .filter(t -> t.getUserId().equals(u.getId()))
                                .findFirst())
                .orElseThrow(() -> new ServiceException(CHECK_USERNAME_AND_PASSWORD)));
    }

    @Override
    public Optional<Trainer> findTrainerByUsername(String username) throws ServiceException {
        if (username != null) {
            return Optional.ofNullable(userDao.getAll().stream()
                    .filter(user -> user.getUsername().equalsIgnoreCase(username))
                    .findFirst()
                    .flatMap(user -> trainerDao.getAll().stream()
                            .filter(trainer -> trainer.getUserId().equals(user.getId()))
                            .findFirst())
                    .orElseThrow(() -> new ServiceException(NO_SUCH_USERNAME + username)));
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void changePasswordForTrainer(String username, String oldPassword, String newPassword) throws ServiceException {
        User user = userDao.getAll().stream()
                .filter(user2 -> username.equalsIgnoreCase(user2.getUsername()))
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
    public void updateTrainerProfile(String username, String password, Trainer updated) throws ServiceException {
        if (username != null && password != null && updated != null) {
            Trainer trainer = findTrainerByCredentials(username, password).orElseThrow(() -> new ServiceException(AUTHENTICATION_FAIL));

            updated.setId(trainer.getId());
            updated.setUserId(trainer.getUserId());
            trainerDao.update(updated);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void activateTrainer(String username) throws ServiceException {
        Trainer trainer = findTrainerByUsername(username).orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND));
        if (trainer.isActive()) throw new ServiceException(TRAINER_ALREADY_ACTIVE);
        trainer.setActive(true);
        trainerDao.update(trainer);
    }

    @Override
    public void deactivateTrainer(String username) throws ServiceException {
        Trainer trainer = findTrainerByUsername(username).orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND));
        if (!trainer.isActive()) throw new ServiceException(TRAINER_ALREADY_INACTIVE);
        trainer.setActive(false);
        trainerDao.update(trainer);
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
