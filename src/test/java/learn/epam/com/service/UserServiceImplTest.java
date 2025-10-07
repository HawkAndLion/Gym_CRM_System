package learn.epam.com.service;

import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.User;
import learn.epam.com.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String FAIL_SAVE_USER = "Failed to save user";
    private static final String FAIL_UPDATE_USER = "Failed to update user";
    private static final String FAIL_DELETE_USER = "Failed to delete user";
    private static final String FIRSTNAME_REQUIRED = "User.firstName is required";

    @Mock
    private UserDao userDao;

    @Mock
    private UserCredentialService userCredentialService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldSaveUserWhenValid() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        doNothing().when(userCredentialService).ensureUsernameExists(user);
        doNothing().when(userCredentialService).ensurePassword(user);
        doNothing().when(userDao).save(user);

        // When
        userService.save(user);

        // Then
        verify(userCredentialService).ensureUsernameExists(user);
        verify(userCredentialService).ensurePassword(user);
        verify(userDao).save(user);
        assertDoesNotThrow(() -> new ServiceException(FAIL_SAVE_USER));
    }

    @Test
    void shouldReturnUserByIdWhenExists() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        when(userDao.getById(1L)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findById(1L);

        // Then
        verify(userDao).getById(1L);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void shouldUpdateWhenUserIsValid() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        doNothing().when(userDao).update(user);

        // When
        userService.update(user);

        // Then
        verify(userDao).update(user);
        assertDoesNotThrow(() -> new ServiceException(FAIL_UPDATE_USER));
    }

    @Test
    void shouldRemoveUserWhenDeleteIsCalled() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        doNothing().when(userDao).delete(user);

        // When
        userService.delete(user);

        // Then
        verify(userDao).delete(user);
        assertDoesNotThrow(() -> new ServiceException(FAIL_DELETE_USER));
    }

    @Test
    void shouldReturnUserListWhenFindAllIsCalled() {
        // Given
        List<User> trainings = new ArrayList<>();
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        trainings.add(user);
        trainings.add(new User(1L, "Amanda", "Smith", "Amanda.Smith", "qwertyuiop", true));
        when(userDao.getAll()).thenReturn(trainings);

        // When
        List<User> result = userService.findAllUsers();

        // Then
        verify(userDao).getAll();
        assertEquals(2, result.size());
        assertEquals(user, result.get(0));
    }

    @Test
    void shouldThrowWhenUserInvalidOnSave() {
        // Given
        User user = new User(1L, null, "Brown", null, "qwertyuiop", true);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> userService.save(user));

        // Then
        verifyNoInteractions(userDao);
        assertEquals(FIRSTNAME_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldThrowWhenUserInvalidOnUpdate() {
        // Given
        User user = new User(1L, null, "Brown", null, "qwertyuiop", true);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> userService.update(user));

        // Then
        verifyNoInteractions(userDao);
        assertEquals(FIRSTNAME_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenUserIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.save(null));

        // Then
        verifyNoInteractions(userDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }


    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenUserIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.update(null));

        // Then
        verifyNoInteractions(userDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenUserIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.delete(null));

        //Then
        verifyNoInteractions(userDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
