package learn.epam.com.service.impl;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.User;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.UserCredentialService;
import learn.epam.com.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String FAIL_SAVE_USER = "Failed to save user";
    private static final String FAIL_UPDATE_USER = "Failed to update user";
    private static final String FAIL_DELETE_USER = "Failed to delete user";
    private static final String FAIL_GET_ALL_USERS = "Failed to get all user";
    private static final String FAIL_GET_BY_ID_USER = "Failed to get user by id";
    private static final String SUCCESS_SAVE_USER = "User was created successfully";
    private static final String SUCCESS_UPDATE_USER = "User was updated successfully";
    private static final String SUCCESS_DELETE_USER = "User was deleted successfully";
    private static final String NULL_EXCEPTION = "Argument is null ";

    private final UserCredentialService userCredentialService;
    private final UserDao userDao;

    @Autowired
    public UserServiceImpl(UserDao userDao, UserCredentialService userCredentialService) {
        this.userDao = userDao;
        this.userCredentialService = userCredentialService;
    }

    @Override
    public void save(User user) throws ServiceException {
        if (user != null) {
            userCredentialService.ensureUsername(user);
            userCredentialService.ensurePassword(user);

            try {
                userDao.save(user);

                LOG.info(SUCCESS_SAVE_USER);
            } catch (DaoException exception) {
                throw new ServiceException(FAIL_SAVE_USER, exception);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws ServiceException {
        try {
            return userDao.getById(id);

        } catch (DaoException exception) {
            throw new ServiceException(FAIL_GET_BY_ID_USER, exception);
        }
    }

    @Override
    public void update(User user) throws ServiceException {
        if (user != null) {
            try {
                userDao.update(user);

                LOG.info(SUCCESS_UPDATE_USER);
            } catch (DaoException exception) {
                throw new ServiceException(FAIL_UPDATE_USER, exception);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(User user) throws ServiceException {
        if (user != null) {
            try {
                userDao.delete(user);

                LOG.info(SUCCESS_DELETE_USER);
            } catch (DaoException exception) {
                throw new ServiceException(FAIL_DELETE_USER, exception);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public List<User> findAllUsers() throws ServiceException {
        try {
            return userDao.getAll();

        } catch (DaoException exception) {
            throw new ServiceException(FAIL_GET_ALL_USERS, exception);
        }
    }
}
