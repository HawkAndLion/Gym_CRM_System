package learn.epam.com.service.impl;

import learn.epam.com.dao.TraineeDao;
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
    private static final String NULL_EXCEPTION = "Argument is null ";

    private final TraineeDao traineeDao;
    private final UserCredentialService userCredentialService;
    private final UserDao userDao;

    @Autowired
    public TraineeServiceImpl(TraineeDao traineeDao, UserCredentialService userCredentialService, UserDao userDao) {
        this.traineeDao = traineeDao;
        this.userCredentialService = userCredentialService;
        this.userDao = userDao;
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
    public List<Trainee> findAllTrainee() throws ServiceException {
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
                .flatMap(u -> {
                    return traineeDao.getAll().stream()
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
