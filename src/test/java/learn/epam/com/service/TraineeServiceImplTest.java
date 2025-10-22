package learn.epam.com.service;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TraineeDao;
import learn.epam.com.dao.TrainingDao;
import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.service.impl.TraineeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceImplTest {
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "secret";
    private static final String ANOTHER_USERNAME = "username";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String FAIL_SAVE_TRAINEE = "Failed to save trainee";
    private static final String FAIL_UPDATE_TRAINEE = "Failed to update trainee";
    private static final String FAIL_DELETE_TRAINEE = "Failed to delete trainee";

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private UserDao userDao;

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private UserCredentialService userCredentialService;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Test
    void shouldSaveTraineeWhenValid() throws ServiceException {
        // Given
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());
        doNothing().when(userCredentialService).ensureUsernameExists(trainee.getUserId());
        doNothing().when(userCredentialService).ensurePassword(trainee.getUserId());
        doNothing().when(traineeDao).save(trainee);

        // When
        traineeService.save(trainee);

        // Then
        verify(userCredentialService).ensureUsernameExists(trainee.getUserId());
        verify(userCredentialService).ensurePassword(trainee.getUserId());
        verify(traineeDao).save(trainee);
        assertDoesNotThrow(() -> new ServiceException(FAIL_SAVE_TRAINEE));
    }

    @Test
    void shouldReturnTraineeByIdWhenExists() throws ServiceException {
        // Given
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());
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
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());
        doNothing().when(traineeDao).update(trainee);

        // When
        traineeService.update(trainee);

        // Then
        verify(traineeDao).update(trainee);
        assertDoesNotThrow(() -> new ServiceException(FAIL_UPDATE_TRAINEE));
    }

    @Test
    void shouldRemoveTraineeWhenDeleteIsCalled() throws ServiceException {
        // Given
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());
        doNothing().when(traineeDao).delete(trainee);

        // When
        traineeService.delete(trainee);

        // Then
        verify(traineeDao).delete(trainee);
        assertDoesNotThrow(() -> new ServiceException(FAIL_DELETE_TRAINEE));
    }

    @Test
    void shouldReturnTraineeListWhenFindAllIsCalled() {
        // Given
        List<Trainee> trainees = new ArrayList<>();
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());
        trainees.add(trainee);
        trainees.add(new Trainee(2L, 202L, "Astana", LocalDate.of(1998, 8, 8), true, new HashSet<>()));
        when(traineeDao.getAll()).thenReturn(trainees);

        // When
        List<Trainee> result = traineeService.findAllTrainee();

        // Then
        verify(traineeDao).getAll();
        assertEquals(2, result.size());
        assertEquals(trainee, result.get(0));
    }

    @Test
    void shouldReturnTrueWhenCheckCredentialsAreValid() throws ServiceException {
        // Given
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());
        User user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
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
    void shouldReturnFalseWhenCheckCredentialsAreInvalid() throws ServiceException {
        // Given
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());
        User user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        when(traineeDao.getById(1L)).thenReturn(Optional.of(trainee));
        when(userCredentialService.loadUserOrThrow(10L)).thenReturn(user);

        // When
        boolean result = traineeService.checkCredentials(1L, ANOTHER_USERNAME, PASSWORD);

        // Then
        verify(traineeDao).getById(1L);
        verify(userCredentialService).loadUserOrThrow(10L);
        assertFalse(result);
    }

    @Test
    void shouldReturnTraineeWhenFindTraineeByCredentials() throws ServiceException {
        // Given
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());
        User user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
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
    void shouldReturnTrainerIdsForTraineeWhenTraineeValid() {
        // Given
        Long traineeId = 1L;
        Set<Long> expected = Set.of(10L, 20L);
        when(traineeService.getTrainerIdsForTrainee(traineeId)).thenReturn(expected);

        // When
        Set<Long> result = traineeService.getTrainerIdsForTrainee(traineeId);

        // Then
        verify(traineeDao).getTrainerIdsForTrainee(traineeId);
        assertNotNull(result);
        assertEquals(expected, result);
        assertTrue(result.containsAll(Set.of(10L, 20L)));
    }

    @Test
    void shouldSetTrainerIdsForTraineeWhenMethodCalled() {
        // Given
        Long traineeId = 1L;
        Set<Long> trainerIds = Set.of(5L, 6L);

        // When
        traineeService.setTrainerIdsForTrainee(traineeId, trainerIds);

        // Then
        verify(traineeDao).setTrainerIdsForTrainee(traineeId, trainerIds);
        assertDoesNotThrow(() -> traineeService.setTrainerIdsForTrainee(traineeId, trainerIds));
    }

    @Test
    void shouldAssignTrainerWhenTraineeIdValid() {
        // Given
        Long traineeId = 1L;
        Long trainerId = 10L;

        // When
        traineeService.assignTrainer(traineeId, trainerId);

        // Then
        verify(traineeDao).assignTrainer(traineeId, trainerId);
        assertDoesNotThrow(() -> traineeService.assignTrainer(traineeId, trainerId));
    }

    @Test
    void shouldUnassignTrainerWhenMethodCalled() {
        // Given
        Long traineeId = 1L;
        Long trainerId = 10L;

        // When
        traineeService.unassignTrainer(traineeId, trainerId);

        // Then
        verify(traineeDao).unassignTrainer(traineeId, trainerId);
        assertDoesNotThrow(() -> traineeService.unassignTrainer(traineeId, trainerId));
    }

    @Test
    void shouldReturnTraineeWhenFindTraineeByUsername() throws ServiceException, DaoException {
        // Given
        String username = "John.Brown";
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());
        when(traineeDao.findTraineeByUsername(username)).thenReturn(Optional.of(trainee));

        // When
        Optional<Trainee> result = traineeService.findTraineeByUsername(username);

        // Then
        verify(traineeDao).findTraineeByUsername(username);
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void shouldActivateTraineeSuccessfullyWhenValidInput() throws ServiceException, DaoException {
        // Given
        String username = "John.Brown";
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1995, 5, 5), false, new HashSet<>());
        User user = new User(10L, "John", "Brown", username, "pass", false);

        when(traineeDao.findTraineeByUsername(username)).thenReturn(Optional.of(trainee));
        when(userDao.getById(10L)).thenReturn(Optional.of(user));

        // When
        traineeService.activateTrainee(username);

        // Then
        verify(userDao).update(user);
        verify(traineeDao).update(trainee);
        assertTrue(trainee.isActive());
        assertTrue(user.isActive());
    }

    @Test
    void shouldDeactivateTraineeSuccessfully() throws ServiceException, DaoException {
        // Given
        String username = "John.Brown";
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());
        User user = new User(10L, "John", "Brown", username, "pass", true);

        when(traineeDao.findTraineeByUsername(username)).thenReturn(Optional.of(trainee));
        when(userDao.getById(10L)).thenReturn(Optional.of(user));

        // When
        traineeService.deactivateTrainee(username);

        // Then
        assertFalse(trainee.isActive());
        assertFalse(user.isActive());
        verify(userDao).update(user);
        verify(traineeDao).update(trainee);
    }

    @Test
    void shouldDeleteTraineeByUsernameSuccessfully() throws ServiceException, DaoException {
        // Given
        String username = "john.trainee";
        Trainee trainee = new Trainee(1L, 10L, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());
        User user = new User(10L, "John", "Trainee", username, "pass", true);
        when(traineeDao.findTraineeByUsername(username)).thenReturn(Optional.of(trainee));
        when(userDao.getById(10L)).thenReturn(Optional.of(user));
        when(trainingDao.getAll()).thenReturn(Collections.emptyList());

        // When
        traineeService.deleteTraineeByUsername(username);

        // Then
        verify(trainingDao).getAll();
        verify(traineeDao).delete(trainee);
        verify(userDao).delete(user);
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
    void shouldThrowIllegalArgumentExceptionOnCheckCredentialsWhenNullArguments() {
        // Given: null arguments

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.checkCredentials(null, USERNAME, PASSWORD));

        // Then
        verifyNoInteractions(traineeDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFindTraineeByUsernameWithNull() {
        //Given null username

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.findTraineeByUsername(null));

        // Then
        verifyNoInteractions(traineeDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
