package learn.epam.com.service;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TrainerDao;
import learn.epam.com.dao.TrainingDao;
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
    private TrainingDao trainingDao;

    @Mock
    private UserCredentialService userCredentialService;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Test
    void shouldSaveTrainerWhenValid() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());

        doNothing().when(userCredentialService).ensureUsernameExists(trainer.getUser());
        doNothing().when(userCredentialService).ensurePassword(trainer.getUser());
        doNothing().when(trainerDao).save(trainer);

        // When
        trainerService.save(trainer);

        // Then
        verify(userCredentialService).ensureUsernameExists(trainer.getUser());
        verify(userCredentialService).ensurePassword(trainer.getUser());
        verify(trainerDao).save(trainer);
        assertDoesNotThrow(() -> new ServiceException(FAIL_SAVE_TRAINER));
    }

    @Test
    void shouldReturnTrainerByIdWhenExists() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());

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
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(1L, user, "Coach", true, new HashSet<>());

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
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(1L, user, "Coach", true, new HashSet<>());

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
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        User user2 = new User(102L, "Patrick", "Bay", "Patrick.Bay", "pass", true);
        Trainer trainer = new Trainer(1L, user, "Coach", true, new HashSet<>());
        trainers.add(trainer);
        trainers.add(new Trainer(1L, user2, "Yoga Instructor", true, new HashSet<>()));

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
        User user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());

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
        User user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());

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
        User user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());

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
        User user = new User(100L, "John", "Doe", "John.Doe", "pass", true);
        User user2 = new User(101L, "Mickey", "Mouse", "Mickey.Mouse", "pass", true);
        List<Trainer> trainers = List.of(
                new Trainer(1L, user, "Cardio", true, new HashSet<>()),
                new Trainer(2L, user2, "Yoga", true, new HashSet<>())
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
    void shouldReturnTrainerWhenFindTrainerByUsername() throws ServiceException {
        // Given
        String username = "trainer.user";
        User user = new User(10L, "John", "Doe", username, "pass", true);
        Trainer trainer = new Trainer(1L, user, "Fitness", true, new HashSet<>());

        when(userDao.getAll()).thenReturn(List.of(user));
        when(trainerDao.getAll()).thenReturn(List.of(trainer));

        // When
        Optional<Trainer> result = trainerService.findTrainerByUsername(username);

        // Then
        verify(userDao).getAll();
        verify(trainerDao).getAll();
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldDeleteTrainerByUsernameSuccessfullyWhenValidInput() throws ServiceException {
        // Given
        String username = "John.Doe";
        User user = new User(10L, "John", "Doe", username, "pass", true);
        List<User> users = new ArrayList<>();
        users.add(user);
        Trainer trainer = new Trainer(1L, user, "Yoga", true, new HashSet<>());
        List<Trainer> trainers = new ArrayList<>();
        trainers.add(trainer);

        when(userDao.getAll()).thenReturn(users);
        when(trainerDao.getAll()).thenReturn(trainers);
        when(userDao.getById(10L)).thenReturn(Optional.of(user));
        when(trainingDao.getAll()).thenReturn(Collections.emptyList());

        // When
        trainerService.deleteTrainerByUsername(username);

        // Then
        verify(trainingDao).getAll();
        verify(trainerDao).delete(trainer);
        verify(userDao).delete(user);
        assertDoesNotThrow(() -> new ServiceException("Authentication failed"));
    }

    @Test
    void shouldActivateTrainerSuccessfully() throws ServiceException {
        // Given
        String username = "John.Doe";
        User user = new User(10L, "John", "Doe", username, "pass", false);
        Trainer trainer = new Trainer(1L, user, "Yoga", false, new HashSet<>());

        List<User> users = new ArrayList<>();
        users.add(user);
        List<Trainer> trainers = new ArrayList<>();
        trainers.add(trainer);

        when(userDao.getAll()).thenReturn(users);
        when(trainerDao.getAll()).thenReturn(trainers);
        when(userDao.getById(10L)).thenReturn(Optional.of(user));

        // When
        trainerService.activateTrainer(username);

        // Then
        assertTrue(trainer.isActive());
        assertTrue(user.isActive());
        verify(userDao).update(user);
        verify(trainerDao).update(trainer);
    }

    @Test
    void shouldThrowServiceExceptionWhenTrainerAlreadyActive() {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(1L, user, "Fitness", true, new HashSet<>());
        List<User> users = new ArrayList<>();
        users.add(user);
        List<Trainer> trainers = new ArrayList<>();
        trainers.add(trainer);

        when(userDao.getAll()).thenReturn(users);
        when(trainerDao.getAll()).thenReturn(trainers);
        when(userDao.getById(10L)).thenReturn(Optional.of(user));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> trainerService.activateTrainer(user.getUsername()));

        // Then
        assertEquals("Trainee already active", exception.getMessage());
    }

    @Test
    void shouldDeactivateTrainerSuccessfully() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(1L, user, "Yoga", true, new HashSet<>());

        List<User> users = new ArrayList<>();
        users.add(user);
        List<Trainer> trainers = new ArrayList<>();
        trainers.add(trainer);

        when(userDao.getAll()).thenReturn(users);
        when(trainerDao.getAll()).thenReturn(trainers);
        when(userDao.getById(10L)).thenReturn(Optional.of(user));

        // When
        trainerService.deactivateTrainer(user.getUsername());

        // Then
        assertFalse(trainer.isActive());
        assertFalse(user.isActive());
        verify(userDao).update(user);
        verify(trainerDao).update(trainer);
    }

    @Test
    void shouldThrowServiceExceptionWhenTrainerAlreadyInactive() {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(1L, user, "Yoga", false, new HashSet<>());
        List<User> users = new ArrayList<>();
        users.add(user);
        List<Trainer> trainers = new ArrayList<>();
        trainers.add(trainer);

        when(userDao.getAll()).thenReturn(users);
        when(trainerDao.getAll()).thenReturn(trainers);
        when(userDao.getById(10L)).thenReturn(Optional.of(user));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> trainerService.deactivateTrainer(user.getUsername()));

        // Then
        assertEquals("Trainee already inactive", exception.getMessage());
    }

    @Test
    void shouldReturnTraineeIdsForTrainerWhenInputValid() {
        // Given
        Long trainerId = 1L;
        Set<Long> traineeIds = Set.of(5L, 6L);
        when(trainerDao.getTraineeIdsForTrainer(trainerId)).thenReturn(traineeIds);

        // When
        Set<Long> result = trainerService.getTraineeIdsForTrainer(trainerId);

        // Then
        verify(trainerDao).getTraineeIdsForTrainer(trainerId);
        assertEquals(2, result.size());
        assertTrue(result.contains(5L));
    }

    @Test
    void shouldThrowServiceExceptionWhenUpdateFails() throws DaoException {
        // Given
        String username = "Wrong.User";
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
        // Given
        User user = new User(102L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(null, user, null, true, new HashSet<>());

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

    @Test
    void shouldThrowServiceExceptionWhenTrainerByUsernameNotFound() {
        // Given
        String username = "Unknown.User";
        when(userDao.getAll()).thenReturn(Collections.emptyList());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> trainerService.findTrainerByUsername(username));

        // Then
        assertEquals("Trainer not found with username: " + username, exception.getMessage());
    }
}
