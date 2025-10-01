package learn.epam.com.service;

import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.User;
import learn.epam.com.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {
    private static final String MISSING_USER_FIELD = "User missing required fields: firstName/lastName";
    private static final String NULL_EXCEPTION = "Argument is null ";

    @Mock
    UserService userService;

    @Mock
    TraineeService traineeService;

    @Mock
    UserCredentialService userCredentialService;

    @InjectMocks
    ProfileServiceImpl profileService;

    @Test
    void shouldCreateTraineeProfileWhenValidUserAndTrainee() throws ServiceException {
        // Given
        User user = new User("John", "Doe", null, null, true);
        Trainee trainee = new Trainee(null, null, "Some address", LocalDate.of(1990, 1, 1));

        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(123L);
            return null;
        }).when(userService).save(any(User.class));

        // When
        profileService.createTraineeProfile(user, trainee);

        // Then
        verify(userCredentialService).ensureUsernameExists(user);
        verify(userCredentialService).ensurePassword(user);
        verify(userService).save(user);
        verify(traineeService).save(trainee);
        assertEquals(123L, trainee.getUserId());
    }

    @Test
    void shouldThrowServiceExceptionWhenMissingFields() {
        // Given
        User user = new User(null, "Doe", null, null, true); // missing firstName
        Trainee trainee = new Trainee(null, null, "addr", LocalDate.of(1990, 1, 1));

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> profileService.createTraineeProfile(user, trainee));

        // Then
        assertEquals(MISSING_USER_FIELD, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> profileService.createTraineeProfile(null, null));

        // Then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
