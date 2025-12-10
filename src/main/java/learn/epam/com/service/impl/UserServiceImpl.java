package learn.epam.com.service.impl;

import learn.epam.com.dto.UserDetailsDto;
import learn.epam.com.dto.UserDto;
import learn.epam.com.entity.User;
import learn.epam.com.repository.UserRepository;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.UserCredentialService;
import learn.epam.com.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private static final String INVALID_CREDENTIALS = "Invalid credentials";
    private static final String USER_NOT_FOUND = "User was not found. Check if username and password are correct";
    private static final String INVALID_USERNAME = "Invalid username";
    private static final String ENCODE_SIGN = "$2";

    private final UserCredentialService userCredentialService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserCredentialService userCredentialService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userCredentialService = userCredentialService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void save(User user) throws ServiceException {
        if (user != null) {
            validateUserForCreate(user);

            userCredentialService.ensureUsernameExists(user);
            userCredentialService.ensurePassword(user);

            if (!isBcryptHash(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            userRepository.save(user);

            LOG.info(SUCCESS_SAVE_USER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void update(User user) throws ServiceException {
        if (user != null) {
            validateUserForUpdate(user);

            String password = user.getPassword();

            User existing = userRepository.findById(user.getId()).orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
            existing.setFirstName(user.getFirstName());
            existing.setLastName(user.getLastName());
            existing.setActive(user.isActive());

            if (password != null && !password.isBlank()) {
                if (!isBcryptHash(password)) {
                    existing.setPassword(passwordEncoder.encode(password));
                } else {
                    existing.setPassword(password);
                }
            }

            userRepository.save(user);

            LOG.info(SUCCESS_UPDATE_USER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void delete(User user) throws ServiceException {
        if (user != null) {
            userRepository.delete(user);

            LOG.info(SUCCESS_DELETE_USER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public Optional<User> findByUsername(String username) {
        if (!isBlank(username)) {
            return userRepository.findByUsername(username);
        } else {
            throw new IllegalArgumentException(USERNAME_REQUIRED);
        }
    }

    @Override
    @Transactional
    public UserDto getUserDto(UserDetailsDto request) throws ServiceException {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username != null && password != null && !username.isBlank() && !password.isBlank()) {

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new ServiceException(INVALID_CREDENTIALS);
            }

            return new UserDto(user.getFirstName(), user.getLastName(), user.getUsername(), user.isActive());
        } else {
            throw new ServiceException(INVALID_CREDENTIALS);
        }
    }

    @Override
    @Transactional
    public UserDetailsDto getUserDetailsDto(String username) throws ServiceException {
        if (username != null) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

            return new UserDetailsDto(user.getUsername(), user.getPassword());
        } else {
            throw new ServiceException(INVALID_USERNAME);
        }
    }

    @Override
    @Transactional
    public UserDetailsDto getUserDetailsDtoByCredentials(String firstName, String lastname) throws ServiceException {
        if (firstName != null && lastname != null) {
            User extractedUser = userRepository.findAll().stream()
                    .filter(u -> u.getFirstName().equalsIgnoreCase(firstName) && u.getLastName().equalsIgnoreCase(lastname))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

            return new UserDetailsDto(extractedUser.getUsername(), extractedUser.getPassword());
        } else {
            throw new ServiceException(INVALID_USERNAME);
        }
    }

    private boolean isBcryptHash(String password) {
        return password != null && password.startsWith(ENCODE_SIGN);
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
