package learn.epam.com.service;

import learn.epam.com.entity.User;
import learn.epam.com.repository.UserRepository;
import learn.epam.com.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private static final String ENCODED_PASSWORD = "encodedPassword";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialService userCredentialService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldSaveUserWhenValid() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        doNothing().when(userCredentialService).ensureUsernameExists(user);
        doNothing().when(userCredentialService).ensurePassword(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(user)).thenReturn(user);

        // When
        userService.save(user);

        // Then
        verify(userCredentialService).ensureUsernameExists(user);
        verify(userCredentialService).ensurePassword(user);
        verify(userRepository).save(user);
        assertDoesNotThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, FAIL_SAVE_USER));
    }

    @Test
    void shouldReturnUserByIdWhenExists() {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findById(1L);

        // Then
        verify(userRepository).findById(1L);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void shouldUpdateWhenUserIsValid() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        userService.update(user);

        // Then
        verify(userRepository).save(user);
        assertDoesNotThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, FAIL_UPDATE_USER));
    }

    @Test
    void shouldRemoveUserWhenDeleteIsCalled() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        doNothing().when(userRepository).delete(user);

        // When
        userService.delete(user);

        // Then
        verify(userRepository).delete(user);
        assertDoesNotThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, FAIL_DELETE_USER));
    }

    @Test
    void shouldReturnUserListWhenFindAllIsCalled() {
        // Given
        List<User> trainings = new ArrayList<>();
        User user = new User(1L, "John", "Brown", "John.Brown", "qwertyuiop", true);
        trainings.add(user);
        trainings.add(new User(1L, "Amanda", "Smith", "Amanda.Smith", "qwertyuiop", true));
        when(userRepository.findAll()).thenReturn(trainings);

        // When
        List<User> result = userService.findAllUsers();

        // Then
        verify(userRepository).findAll();
        assertEquals(2, result.size());
        assertEquals(user, result.get(0));
    }

    @Test
    void shouldReturnUserWhenUsernameExists() {
        // Given
        String username = "John.Brown";
        User user = new User(1L, "John", "Brown", username, "pass", true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByUsername(username);

        // Then
        verify(userRepository).findByUsername(username);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void shouldThrowWhenUsernameIsBlank() {
        // Given
        String username = " ";

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.findByUsername(username));

        // Then
        verifyNoInteractions(userRepository);
        assertEquals("User.username is required for update", exception.getMessage());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundById() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findById(99L);

        // Then
        verify(userRepository).findById(99L);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowWhenUserInvalidOnSave() {
        // Given
        User user = new User(1L, null, "Brown", null, "qwertyuiop", true);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> userService.save(user));

        // Then
        verifyNoInteractions(userRepository);
        assertEquals(FIRSTNAME_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldThrowWhenUserInvalidOnUpdate() {
        // Given
        User user = new User(1L, null, "Brown", null, "qwertyuiop", true);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> userService.update(user));

        // Then
        verifyNoInteractions(userRepository);
        assertEquals(FIRSTNAME_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenUserIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.save(null));

        // Then
        verifyNoInteractions(userRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }


    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenUserIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.update(null));

        // Then
        verifyNoInteractions(userRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenUserIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.delete(null));

        //Then
        verifyNoInteractions(userRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
