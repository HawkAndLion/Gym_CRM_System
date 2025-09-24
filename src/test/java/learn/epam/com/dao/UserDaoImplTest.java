package learn.epam.com.dao;

import learn.epam.com.dao.impl.UserDaoImpl;
import learn.epam.com.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_TYPE = "IllegalArgumentException was expected";

    private Map<Long, User> storage;
    private UserDaoImpl userDao;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        userDao = new UserDaoImpl(storage);
    }

    @Test
    void shouldReturnUserWhenGetByIdCalled() {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        storage.put(1L, user);

        // When
        Optional<User> result = userDao.getById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExists() {
        // Given: empty storage

        // When
        Optional<User> result = userDao.getById(99L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnUserListWhenGetAllCalled() {
        // Given
        User user1 = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        User user2 = new User(1L, "Amanda", "Smith", "Amanda.Smith", "qwertyuiop", true);
        storage.put(1L, user1);
        storage.put(2L, user2);

        // When
        List<User> result = userDao.getAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
    }

    @Test
    void shouldGenerateIdWhenNull() {
        // Given
        User user = new User(null, "John", "Brown", "John.Brown", "qwertyuiop", true);

        // When
        userDao.save(user);

        // Then
        assertNotNull(user.getId());
        assertTrue(storage.containsKey(user.getId()));
        assertEquals(user, storage.get(user.getId()));
    }

    @Test
    void shouldUseExistingIdWhenPresent() {
        // Given
        User user = new User(10L, "John", "Brown", "John.Brown", "qwertyuiop", true);

        // When
        userDao.save(user);

        // Then
        assertEquals(user, storage.get(10L));
    }

    @Test
    void shouldReplaceExistingUserWhenUpdateCalled() {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        storage.put(1L, user);

        User updated = new User(1L, "Amanda", "Smith", "Amanda.Smith", "qwertyuiop", true);

        // When
        userDao.update(updated);

        // Then
        assertEquals(updated, storage.get(1L));
    }

    @Test
    void shouldRemoveUserWhenDeleteCalled() {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        storage.put(1L, user);

        // When
        userDao.delete(user);

        // Then
        assertFalse(storage.containsKey(1L));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenUserIsNull() {
        // Given: null user

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userDao.save(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenUserIsNull() {
        // Given: null user

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userDao.update(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenUserIsNull() {
        // Given: null user

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userDao.delete(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
