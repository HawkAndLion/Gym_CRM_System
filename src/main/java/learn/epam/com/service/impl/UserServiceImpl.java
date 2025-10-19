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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String SUCCESS_SAVE_USER = "User was created successfully";
    private static final String SUCCESS_UPDATE_USER = "User was updated successfully";
    private static final String SUCCESS_DELETE_USER = "User was deleted successfully";
    private static final String FIRSTNAME_REQUIRED = "User.firstName is required";
    private static final String LASTNAME_REQUIRED = "User.lastName is required";
    private static final String ID_REQUIRED = "User.id is required for update";
    private static final String USERNAME_REQUIRED = "User.username is required for update";
    private static final String PASSWORD_REQUIRED = "User.password is required for update";
    private static final String NULL_EXCEPTION = "Argument is null ";

    private final UserCredentialService userCredentialService;
    private final UserDao userDao;

    @Autowired
    public UserServiceImpl(UserDao userDao, UserCredentialService userCredentialService) {
        this.userDao = userDao;
        this.userCredentialService = userCredentialService;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void save(User user) throws ServiceException {
        if (user != null) {
            validateUserForCreate(user);

            userCredentialService.ensureUsernameExists(user);
            userCredentialService.ensurePassword(user);

            userDao.save(user);

            LOG.info(SUCCESS_SAVE_USER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Optional<User> findById(Long id) throws ServiceException {
        return userDao.getById(id);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void update(User user) throws ServiceException {
        if (user != null) {
            validateUserForUpdate(user);

            userDao.update(user);

            LOG.info(SUCCESS_UPDATE_USER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void delete(User user) throws ServiceException {
        if (user != null) {
            userDao.delete(user);

            LOG.info(SUCCESS_DELETE_USER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public List<User> findAllUsers() {
        return userDao.getAll();
    }

    @Override
    @Transactional
    public Optional<User> findByUsername(String username){
        if (isBlank(username)) {
            return userDao.getByUsername(username);
        } else {
            throw new IllegalArgumentException(USERNAME_REQUIRED);
        }
    }

    private static void validateUserForCreate(User user) throws ServiceException {
        if (user != null) {
            if (isBlank(user.getFirstName())) throw new ServiceException(FIRSTNAME_REQUIRED);
            if (isBlank(user.getLastName())) throw new ServiceException(LASTNAME_REQUIRED);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private static void validateUserForUpdate(User user) throws ServiceException {
        if (user != null) {
            if (user.getId() == null) throw new ServiceException(ID_REQUIRED);
            if (isBlank(user.getFirstName())) throw new ServiceException(FIRSTNAME_REQUIRED);
            if (isBlank(user.getLastName())) throw new ServiceException(LASTNAME_REQUIRED);
            if (isBlank(user.getUsername())) throw new ServiceException(USERNAME_REQUIRED);
            if (isBlank(user.getPassword())) throw new ServiceException(PASSWORD_REQUIRED);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
