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
    public List<Trainer> findAllTrainers() throws ServiceException {
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
        return users.stream()
                .filter(u -> username.equalsIgnoreCase(u.getUsername()) && password.equals(u.getPassword()))
                .findFirst()
                .flatMap(u -> {
                    return trainerDao.getAll().stream()
                            .filter(t -> t.getUserId().equals(u.getId()))
                            .findFirst();
                });
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
