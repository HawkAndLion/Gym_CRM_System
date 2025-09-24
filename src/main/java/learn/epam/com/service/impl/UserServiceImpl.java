package learn.epam.com.service.impl;

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

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
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

            userDao.save(user);

            LOG.info(SUCCESS_SAVE_USER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws ServiceException {
        return userDao.getById(id);
    }

    @Override
    public void update(User user) throws ServiceException {
        if (user != null) {
            userDao.update(user);

            LOG.info(SUCCESS_UPDATE_USER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(User user) throws ServiceException {
        if (user != null) {
            userDao.delete(user);

            LOG.info(SUCCESS_DELETE_USER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public List<User> findAllUsers() throws ServiceException {
        return userDao.getAll();
    }
}
