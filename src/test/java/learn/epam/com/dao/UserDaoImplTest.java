package learn.epam.com.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import learn.epam.com.dao.impl.UserDaoImpl;
import learn.epam.com.entity.User;
import learn.epam.com.main.GymCrmSystemApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = GymCrmSystemApplication.class)
@ActiveProfiles("test")
@EntityScan(basePackages = "learn.epam.com.entity")
@Transactional
public class UserDaoImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_TYPE = "IllegalArgumentException was expected";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserDaoImpl userDao;

    @Test
    void shouldReturnUserWhenGetByIdCalled() {
        // Given
        User user = new User(null, "John", "Brown", "john.brown", "qwerty", true);
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> result = userDao.getById(user.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExists() {
        // Given: user does not exist

        // When
        Optional<User> result = userDao.getById(99L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnUserListWhenGetAllCalled() {
        // Given
        User user1 = new User(null, "John", "Brown", "john.brown", "pass1", true);
        User user2 = new User(null, "Amanda", "Smith", "amanda.smith", "pass2", true);
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // When
        List<User> result = userDao.getAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getUsername().equals("john.brown")));
        assertTrue(result.stream().anyMatch(u -> u.getUsername().equals("amanda.smith")));
    }

    @Test
    void shouldGenerateIdWhenNull() {
        // Given
        User user = new User(null, "John", "Brown", "John.Brown", "qwertyuiop", true);

        // When
        userDao.save(user);

        // Then
        assertNotNull(user.getId());
        User found = entityManager.find(User.class, user.getId());
        assertEquals(user.getUsername(), found.getUsername());
    }

    @Test
    void shouldUseExistingIdWhenPresent() {
        // Given
        User user = new User(null, "John", "Brown", "John.Brown", "qwertyuiop", true);
        entityManager.persist(user);
        entityManager.flush();

        // When
        User foundUser = entityManager.find(User.class, user.getId());

        // Then
        assertEquals(user, foundUser);
    }

    @Test
    void shouldReplaceExistingUserWhenUpdateCalled() {
        // Given
        User user = new User(null, "John", "Brown", "john.brown", "qwerty", true);
        entityManager.persist(user);
        entityManager.flush();

        User updated = new User(user.getId(), "Updated", "Brown", "john.brown", "qwerty", true);

        // When
        userDao.update(updated);

        // Then
        assertEquals(user, updated);
        assertEquals("Updated", updated.getFirstName());
    }

    @Test
    void shouldRemoveUserWhenDeleteCalled() {
        // Given
        User user = new User(null, "John", "Brown", "john.brown", "qwerty", true);
        entityManager.persist(user);
        entityManager.flush();

        // When
        userDao.delete(user);

        // Then
        User deleted = entityManager.find(User.class, user.getId());
        assertNull(deleted);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenUserIsNull() {
        // Given: null user

        //when
        InvalidDataAccessApiUsageException ex = assertThrows(
                InvalidDataAccessApiUsageException.class,
                () -> userDao.save(null)
        );

        //then
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenUserIsNull() {
        // Given: null user

        //when
        InvalidDataAccessApiUsageException ex = assertThrows(
                InvalidDataAccessApiUsageException.class,
                () -> userDao.update(null)
        );

        //then
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenUserIsNull() {
        // Given: null user

        //when
        InvalidDataAccessApiUsageException ex = assertThrows(
                InvalidDataAccessApiUsageException.class,
                () -> userDao.delete(null)
        );

        //then
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }
}
