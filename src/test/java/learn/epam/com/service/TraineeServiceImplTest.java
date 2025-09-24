package learn.epam.com.service;

import learn.epam.com.dao.TraineeDao;
import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.User;
import learn.epam.com.service.impl.TraineeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceImplTest {
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "secret";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String FAIL_SAVE_TRAINEE = "Failed to save trainee";
    private static final String FAIL_UPDATE_TRAINEE = "Failed to update trainee";
    private static final String FAIL_DELETE_TRAINEE = "Failed to delete trainee";

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private UserDao userDao;

    @Mock
    private UserCredentialService userCredentialService;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee trainee;
    private User user;

    @BeforeEach
    void setUp() {
        trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1998, 4, 15));
        user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
    }

    @Test
    void shouldSaveTraineeWhenValid() throws Exception {
        // Given
        doNothing().when(userCredentialService).ensureUsername(trainee.getUserId());
        doNothing().when(userCredentialService).ensurePassword(trainee.getUserId());
        doNothing().when(traineeDao).save(trainee);

        // When
        traineeService.save(trainee);

        // Then
        verify(userCredentialService).ensureUsername(trainee.getUserId());
        verify(userCredentialService).ensurePassword(trainee.getUserId());
        verify(traineeDao).save(trainee);
        assertDoesNotThrow(() -> new ServiceException(FAIL_SAVE_TRAINEE));
    }

    @Test
    void shouldReturnTraineeByIdWhenExists() throws Exception {
        // Given
        when(traineeDao.getById(1L)).thenReturn(Optional.of(trainee));

        // When
        Optional<Trainee> result = traineeService.findById(1L);

        // Then
        verify(traineeDao).getById(1L);
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void shouldUpdateWhenTraineeIsValid() throws Exception {
        // Given
        doNothing().when(traineeDao).update(trainee);

        // When
        traineeService.update(trainee);

        // Then
        verify(traineeDao).update(trainee);
        assertDoesNotThrow(() -> new ServiceException(FAIL_UPDATE_TRAINEE));
    }

    @Test
    void shouldRemoveTraineeWhenDeleteIsCalled() throws Exception {
        // Given
        doNothing().when(traineeDao).delete(trainee);

        // When
        traineeService.delete(trainee);

        // Then
        verify(traineeDao).delete(trainee);
        assertDoesNotThrow(() -> new ServiceException(FAIL_DELETE_TRAINEE));
    }

    @Test
    void shouldReturnTraineeListWhenFindAllIsCalled() throws Exception {
        // Given
        List<Trainee> trainees = new ArrayList<>();
        trainees.add(trainee);
        trainees.add(new Trainee(2L, 202L, "Astana", LocalDate.of(1998, 8, 8)));
        when(traineeDao.getAll()).thenReturn(trainees);

        // When
        List<Trainee> result = traineeService.findAllTrainee();

        // Then
        verify(traineeDao).getAll();
        assertEquals(2, result.size());
        assertEquals(trainee, result.get(0));
    }

    @Test
    void shouldReturnTrueWhenCheckCredentialsAreValid() throws Exception {
        // Given
        when(traineeDao.getById(1L)).thenReturn(Optional.of(trainee));
        when(userCredentialService.loadUserOrThrow(10L)).thenReturn(user);

        // When
        boolean result = traineeService.checkCredentials(1L, USERNAME, PASSWORD);

        // Then
        verify(traineeDao).getById(1L);
        verify(userCredentialService).loadUserOrThrow(10L);
        assertTrue(result);
    }

    @Test
    void shouldReturnTraineeWhenFindTraineeByCredentials() throws Exception {
        // Given
        when(userDao.getAll()).thenReturn(List.of(user));
        when(traineeDao.getAll()).thenReturn(List.of(trainee));

        // When
        Optional<Trainee> result = traineeService.findTraineeByCredentials(USERNAME, PASSWORD);

        // Then
        verify(userDao).getAll();
        verify(traineeDao).getAll();
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeService.save(null));

        // Then
        verifyNoInteractions(traineeDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }


    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeService.update(null));

        // Then
        verifyNoInteractions(traineeDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeService.delete(null));

        //Then
        verifyNoInteractions(traineeDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnCheckCredentialsWhenNullArgs() {
        // Given: null arguments

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.checkCredentials(null, USERNAME, PASSWORD));

        // Then
        verifyNoInteractions(traineeDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
