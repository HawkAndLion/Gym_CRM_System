package learn.epam.com.service;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TrainerDao;
import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceImplTest {
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "secret";
    private static final String ANOTHER_PASSWORD = "password";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String FAIL_SAVE_TRAINER = "Failed to save trainer";
    private static final String FAIL_UPDATE_TRAINER = "Failed to update trainer";
    private static final String FAIL_DELETE_TRAINER = "Failed to delete trainer";
    private static final String USER_ID_REQUIRED = "Trainer.userId is required";
    private static final String SPECIALIZATION_REQUIRED = "Trainer.specialization is required";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found: ";
    private static final String DB_ERROR = "DB error";
    private static final String CHECK_TRAINEE_USERNAME = "Check if trainee username correct";

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UserDao userDao;

    @Mock
    private UserCredentialService userCredentialService;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Test
    void shouldSaveTrainerWhenValid() throws ServiceException {
        // Given
        Trainer trainer = new Trainer(null, 10L, "Coach", true, new HashSet<>());
        doNothing().when(userCredentialService).ensureUsernameExists(trainer.getUserId());
        doNothing().when(userCredentialService).ensurePassword(trainer.getUserId());
        doNothing().when(trainerDao).save(trainer);

        // When
        trainerService.save(trainer);

        // Then
        verify(userCredentialService).ensureUsernameExists(trainer.getUserId());
        verify(userCredentialService).ensurePassword(trainer.getUserId());
        verify(trainerDao).save(trainer);
        assertDoesNotThrow(() -> new ServiceException(FAIL_SAVE_TRAINER));
    }

    @Test
    void shouldReturnTrainerByIdWhenExists() throws ServiceException {
        // Given
        Trainer trainer = new Trainer(null, 10L, "Coach", true, new HashSet<>());
        when(trainerDao.getById(1L)).thenReturn(Optional.of(trainer));

        // When
        Optional<Trainer> result = trainerService.findById(1L);

        // Then
        verify(trainerDao).getById(1L);
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldUpdateWhenTrainerIsValid() throws ServiceException {
        // Given
        Trainer trainer = new Trainer(1L, 10L, "Coach", true, new HashSet<>());
        doNothing().when(trainerDao).update(trainer);

        // When
        trainerService.update(trainer);

        // Then
        verify(trainerDao).update(trainer);
        assertDoesNotThrow(() -> new ServiceException(FAIL_UPDATE_TRAINER));
    }

    @Test
    void shouldRemoveTrainerWhenDeleteIsCalled() throws ServiceException {
        // Given
        Trainer trainer = new Trainer(1L, 10L, "Coach", true, new HashSet<>());
        doNothing().when(trainerDao).delete(trainer);

        // When
        trainerService.delete(trainer);

        // Then
        verify(trainerDao).delete(trainer);
        assertDoesNotThrow(() -> new ServiceException(FAIL_DELETE_TRAINER));
    }

    @Test
    void shouldReturnTrainerListWhenFindAllIsCalled() {
        // Given
        List<Trainer> trainers = new ArrayList<>();
        Trainer trainer = new Trainer(1L, 10L, "Coach", true, new HashSet<>());
        trainers.add(trainer);
        trainers.add(new Trainer(1L, 102L, "Yoga Instructor", true, new HashSet<>()));
        when(trainerDao.getAll()).thenReturn(trainers);

        // When
        List<Trainer> result = trainerService.findAllTrainers();

        // Then
        verify(trainerDao).getAll();
        assertEquals(2, result.size());
        assertEquals(trainer, result.get(0));
    }

    @Test
    void shouldReturnTrueWhenCheckCredentialsAreValid() throws ServiceException {
        // Given
        Trainer trainer = new Trainer(null, 10L, "Coach", true, new HashSet<>());
        User user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        when(trainerDao.getById(1L)).thenReturn(Optional.of(trainer));
        when(userCredentialService.loadUserOrThrow(10L)).thenReturn(user);

        // When
        boolean result = trainerService.checkCredentials(1L, USERNAME, PASSWORD);

        // Then
        verify(trainerDao).getById(1L);
        verify(userCredentialService).loadUserOrThrow(10L);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenCheckCredentialsAreInvalid() throws ServiceException {
        // Given
        Trainer trainer = new Trainer(null, 10L, "Coach", true, new HashSet<>());
        User user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        when(trainerDao.getById(1L)).thenReturn(Optional.of(trainer));
        when(userCredentialService.loadUserOrThrow(10L)).thenReturn(user);

        // When
        boolean result = trainerService.checkCredentials(1L, USERNAME, ANOTHER_PASSWORD);

        // Then
        verify(trainerDao).getById(1L);
        verify(userCredentialService).loadUserOrThrow(10L);
        assertFalse(result);
    }

    @Test
    void shouldReturnTrainerWhenFindTrainerByCredentials() {
        // Given
        Trainer trainer = new Trainer(null, 10L, "Coach", true, new HashSet<>());
        User user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        when(userDao.getAll()).thenReturn(List.of(user));
        when(trainerDao.getAll()).thenReturn(List.of(trainer));

        // When
        Optional<Trainer> result = trainerService.findTrainerByCredentials(USERNAME, PASSWORD);

        // Then
        verify(userDao).getAll();
        verify(trainerDao).getAll();
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldReturnUnassignedTrainersWhenMethodCalled() throws DaoException {
        // Given
        String username = "trainee1";
        List<Trainer> trainers = List.of(
                new Trainer(1L, 100L, "Cardio", true, new HashSet<>()),
                new Trainer(2L, 101L, "Yoga", true, new HashSet<>())
        );

        when(trainerDao.getUnassignedTrainersForTrainee(username)).thenReturn(trainers);

        // When
        List<Trainer> result = trainerDao.getUnassignedTrainersForTrainee(username);

        // Then
        verify(trainerDao).getUnassignedTrainersForTrainee(username);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cardio", result.get(0).getSpecialization());
        assertEquals("Yoga", result.get(1).getSpecialization());
    }

    @Test
    void shouldThrowServiceExceptionWhenMethodFails() throws DaoException {
        // Given
        String username = "nonexistent";
        when(trainerDao.getUnassignedTrainersForTrainee(username))
                .thenThrow(new DaoException(DB_ERROR));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> trainerService.getUnassignedTrainersForTrainee(username));

        // Then
        verify(trainerDao).getUnassignedTrainersForTrainee(username);
        assertEquals(TRAINEE_NOT_FOUND, exception.getMessage());
        assertInstanceOf(ServiceException.class, exception);
    }

    @Test
    void shouldUpdateTraineeTrainersList() throws DaoException {
        // Given
        String username = "trainee.user";
        Set<Long> trainerIds = Set.of(1L, 2L);

        // When
        trainerDao.updateTraineeTrainersList(username, trainerIds);

        // Then
        verify(trainerDao).updateTraineeTrainersList(username, trainerIds);
        assertDoesNotThrow(() -> trainerDao.updateTraineeTrainersList(username, trainerIds));
    }

    @Test
    void shouldThrowServiceExceptionWhenUpdateFails() throws DaoException {
        // Given
        String username = "wrong.user";
        Set<Long> trainerIds = Set.of(1L, 2L);

        doThrow(new DaoException(DB_ERROR)).when(trainerDao)
                .updateTraineeTrainersList(username, trainerIds);

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> trainerService.updateTraineeTrainersList(username, trainerIds));

        // Then
        verify(trainerDao).updateTraineeTrainersList(username, trainerIds);
        assertTrue(exception.getMessage().contains(CHECK_TRAINEE_USERNAME));
    }

    @Test
    void shouldThrowWhenUserIdInvalid() {
        // Given:
        Trainer trainer = new Trainer(null, null, "Specializstion", true, new HashSet<>());

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainerService.save(trainer));

        // Then
        verifyNoInteractions(trainerDao);
        assertEquals(USER_ID_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldThrowWhenTrainerInvalid() {
        // Given:
        Trainer trainer = new Trainer(null, 102L, null, true, new HashSet<>());

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainerService.save(trainer));

        // Then
        verifyNoInteractions(trainerDao);
        assertEquals(SPECIALIZATION_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerService.save(null));

        // Then
        verifyNoInteractions(trainerDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }


    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerService.update(null));

        // Then
        verifyNoInteractions(trainerDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerService.delete(null));

        //Then
        verifyNoInteractions(trainerDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnCheckCredentialsWhenNullArgs() {
        // Given: null arguments

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainerService.checkCredentials(null, USERNAME, PASSWORD));

        // Then
        verifyNoInteractions(trainerDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
