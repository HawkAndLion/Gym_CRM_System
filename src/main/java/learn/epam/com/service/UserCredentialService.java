package learn.epam.com.service;

import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserCredentialService {
    private static final Logger LOG = LoggerFactory.getLogger(UserCredentialService.class);
    private static final String CREATE_USERNAME_MESSAGE = "Created username={}";
    private static final String CREATE_PASSWORD_MESSAGE = "Password generated successfully for userId={}";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String NULL_EXCEPTION = "Argument cannot be null";
    private static final String DOT = ".";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final Random random = new Random();
    private final UserDao userDao;

    public UserCredentialService(UserDao userDao) {
        this.userDao = userDao;
    }

    private void generateUsername(User user) throws ServiceException {
        if (user != null) {
            String base = user.getFirstName() + DOT + user.getLastName();
            String candidate = base;

            List<User> users = userDao.getAll().stream()
                    .filter(u -> user.getId() == null || !user.getId().equals(u.getId()))
                    .collect(Collectors.toList());

            int suffix = 2;
            while (usernameExists(candidate, users)) {
                candidate = base + suffix;
                suffix++;
            }

            user.setUsername(candidate);

            LOG.info(CREATE_USERNAME_MESSAGE, candidate);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private boolean usernameExists(String username, List<User> users) {
        return users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    private void generatePassword(User user) {
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            password.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        user.setPassword(password.toString());

        LOG.info(CREATE_PASSWORD_MESSAGE, user.getId());
    }

    public void ensureUsernameExists(User user) throws ServiceException {
        if (user != null) {
            List<User> others = userDao.getAll().stream()
                    .filter(u -> user.getId() == null || !user.getId().equals(u.getId()))
                    .toList();

            boolean providedIsDuplicate = false;
            if (user.getUsername() != null && !user.getUsername().isBlank()) {
                String provided = user.getUsername();
                providedIsDuplicate = others.stream()
                        .anyMatch(u -> provided.equalsIgnoreCase(u.getUsername()));
            }

            if (user.getUsername() == null || user.getUsername().isBlank() || providedIsDuplicate) {
                generateUsername(user);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    public void ensureUsernameExists(long userId) throws ServiceException {
        User user = loadUserOrThrow(userId);

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            generateUsername(user);

            userDao.update(user);
        }
    }

    public void ensurePassword(User user) {
        if (user != null) {
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                generatePassword(user);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    public void ensurePassword(long userId) throws ServiceException {
        User user = loadUserOrThrow(userId);

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            generatePassword(user);
            userDao.update(user);
        }
    }

    public User loadUserOrThrow(long userId) throws ServiceException {
        Optional<User> opt = userDao.getById(userId);

        return opt.orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));
    }
}
