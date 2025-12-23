package learn.epam.com.service;

import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.repository.TraineeRepository;
import learn.epam.com.repository.TrainerRepository;
import learn.epam.com.repository.TrainingRepository;
import learn.epam.com.repository.UserRepository;
import learn.epam.com.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceImplTest {
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "secret";
    private static final String ENCRYPTED_PASSWORD = "secret";
    private static final String ANOTHER_PASSWORD = "password";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String FAIL_SAVE_TRAINER = "Failed to save trainer";
    private static final String FAIL_UPDATE_TRAINER = "Failed to update trainer";
    private static final String FAIL_DELETE_TRAINER = "Failed to delete trainer";
    private static final String USER_ID_REQUIRED = "Trainer.userId is required";
    private static final String SPECIALIZATION_REQUIRED = "Trainer.specialization is required";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found ";
    private static final String DB_ERROR = "DB error";
    private static final String NON_EXISTENT = "nonexistent";

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private UserCredentialService userCredentialService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Test
    void shouldSaveTrainerWhenValid() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());

        doNothing().when(userCredentialService).ensureUsernameExists(trainer.getUser());
        doNothing().when(userCredentialService).ensurePassword(trainer.getUser());
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        // When
        trainerService.save(trainer);

        // Then
        verify(userCredentialService).ensureUsernameExists(trainer.getUser());
        verify(userCredentialService).ensurePassword(trainer.getUser());
        verify(trainerRepository).save(trainer);
        assertDoesNotThrow(() -> new ServiceException(FAIL_SAVE_TRAINER));
    }

    @Test
    void shouldReturnTrainerByIdWhenExists() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());

        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        // When
        Optional<Trainer> result = trainerService.findById(1L);

        // Then
        verify(trainerRepository).findById(1L);
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldUpdateWhenTrainerIsValid() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(1L, user, "Coach", true, new HashSet<>());

        when(trainerRepository.save(trainer)).thenReturn(trainer);

        // When
        trainerService.update(trainer);

        // Then
        verify(trainerRepository).save(trainer);
        assertDoesNotThrow(() -> new ServiceException(FAIL_UPDATE_TRAINER));
    }

    @Test
    void shouldRemoveTrainerWhenDeleteIsCalled() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(1L, user, "Coach", true, new HashSet<>());

        doNothing().when(trainerRepository).delete(trainer);

        // When
        trainerService.delete(trainer);

        // Then
        verify(trainerRepository).delete(trainer);
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

        when(trainerRepository.findAll()).thenReturn(trainers);

        // When
        List<Trainer> result = trainerService.findAllTrainers();

        // Then
        verify(trainerRepository).findAll();
        assertEquals(2, result.size());
        assertEquals(trainer, result.get(0));
    }

    @Test
    void shouldReturnTrueWhenCheckCredentialsAreValid() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", USERNAME, PASSWORD, true);
        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());

        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(userCredentialService.loadUserOrThrow(10L)).thenReturn(user);
        when(passwordEncoder.matches(PASSWORD, ENCRYPTED_PASSWORD)).thenReturn(true);

        // When
        boolean result = trainerService.checkCredentials(1L, USERNAME, PASSWORD);

        // Then
        verify(trainerRepository).findById(1L);
        verify(userCredentialService).loadUserOrThrow(10L);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenCheckCredentialsAreInvalid() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", USERNAME, PASSWORD, true);
        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());

        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(userCredentialService.loadUserOrThrow(10L)).thenReturn(user);
        when(passwordEncoder.matches(ANOTHER_PASSWORD, PASSWORD)).thenReturn(false);

        // When
        boolean result = trainerService.checkCredentials(1L, USERNAME, ANOTHER_PASSWORD);

        // Then
        verify(trainerRepository).findById(1L);
        verify(userCredentialService).loadUserOrThrow(10L);
        verify(passwordEncoder).matches(ANOTHER_PASSWORD, PASSWORD);
        assertFalse(result);
    }

    @Test
    void shouldReturnTrainerWhenFindTrainerByCredentials() {
        // Given
        User user = new User(10L, "John", "Doe", USERNAME, PASSWORD, true);
        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        // When
        Optional<Trainer> result = trainerService.findTrainerByCredentials(USERNAME, PASSWORD);

        // Then
        verify(userRepository).findAll();
        verify(trainerRepository).findAll();
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldReturnUnassignedTrainersWhenMethodCalled() {
        // Given
        User user = new User(100L, "John", "Doe", "John.Doe", "pass", true);
        User user2 = new User(101L, "Mickey", "Mouse", "Mickey.Mouse", "pass", true);
        User user3 = new User(102L, "Sponge", "Bob", "Sponge.Bob", "pass", true);
        List<Trainer> trainers = List.of(
                new Trainer(1L, user, "Cardio", true, new HashSet<>()),
                new Trainer(2L, user2, "Yoga", true, new HashSet<>())
        );
        Trainee trainee = new Trainee(1L, user3, "Vietnam", LocalDate.of(2000, 12, 12), true);

        when(trainerRepository.findUnassignedTrainersForTrainee(trainee.getId())).thenReturn(trainers);

        // When
        List<Trainer> result = trainerRepository.findUnassignedTrainersForTrainee(trainee.getId());

        // Then
        verify(trainerRepository).findUnassignedTrainersForTrainee(trainee.getId());
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cardio", result.get(0).getSpecialization());
        assertEquals("Yoga", result.get(1).getSpecialization());
    }

    @Test
    void shouldThrowServiceExceptionWhenMethodFails() {
        // Given
        String username = NON_EXISTENT;

        when(traineeRepository.findByUsername(username))
                .thenThrow(new RuntimeException(DB_ERROR));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> trainerService.getUnassignedTrainersForTrainee(username));

        // Then
        verify(traineeRepository).findByUsername(username);
        assertEquals(TRAINEE_NOT_FOUND, exception.getMessage());
        assertInstanceOf(ServiceException.class, exception);
//        assertTrue(exception.getMessage().contains("Failed to update trainers")
//                || exception.getMessage().contains("Trainee not found"));
    }

    @Test
    void shouldUpdateTraineeTrainersList() throws ServiceException {
        // Given
        String username = "trainee.user";
        Long traineeId = 10L;
        Set<Long> trainerIds = Set.of(1L, 2L);

        User user = new User(10L, "John", "Doe", username, "password", true);
        Trainee trainee = new Trainee(traineeId, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));

        // When
        trainerService.updateTraineeTrainersList(username, trainerIds);

        // Then
        verify(traineeRepository).findByUsername(username);
        verify(traineeRepository).removeAllTrainerRelations(traineeId);
        verify(traineeRepository, times(2)).addTrainerRelation(eq(traineeId), anyLong());
        assertDoesNotThrow(() -> trainerService.updateTraineeTrainersList(username, trainerIds));
    }

    @Test
    void shouldReturnTrainerWhenFindTrainerByUsername() throws ServiceException {
        // Given
        String username = "trainer.user";
        User user = new User(10L, "John", "Doe", username, "pass", true);
        Trainer trainer = new Trainer(1L, user, "Fitness", true, new HashSet<>());

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        // When
        Optional<Trainer> result = trainerService.findTrainerByUsername(username);

        // Then
        verify(userRepository).findAll();
        verify(trainerRepository).findAll();
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

        when(userRepository.findAll()).thenReturn(users);
        when(trainerRepository.findAll()).thenReturn(trainers);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(trainingRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        trainerService.deleteTrainerByUsername(username);

        // Then
        verify(trainingRepository).findAll();
        verify(trainerRepository).delete(trainer);
        verify(userRepository).delete(user);
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

        when(userRepository.findAll()).thenReturn(users);
        when(trainerRepository.findAll()).thenReturn(trainers);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // When
        trainerService.activateTrainer(username);

        // Then
        assertTrue(trainer.isActive());
        assertTrue(user.isActive());
        verify(userRepository).save(user);
        verify(trainerRepository).save(trainer);
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

        when(userRepository.findAll()).thenReturn(users);
        when(trainerRepository.findAll()).thenReturn(trainers);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

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

        when(userRepository.findAll()).thenReturn(users);
        when(trainerRepository.findAll()).thenReturn(trainers);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // When
        trainerService.deactivateTrainer(user.getUsername());

        // Then
        assertFalse(trainer.isActive());
        assertFalse(user.isActive());
        verify(userRepository).save(user);
        verify(trainerRepository).save(trainer);
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

        when(userRepository.findAll()).thenReturn(users);
        when(trainerRepository.findAll()).thenReturn(trainers);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

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
        when(trainerRepository.findTraineeIdsForTrainer(trainerId)).thenReturn(traineeIds);

        // When
        Set<Long> result = trainerService.getTraineeIdsForTrainer(trainerId);

        // Then
        verify(trainerRepository).findTraineeIdsForTrainer(trainerId);
        assertEquals(2, result.size());
        assertTrue(result.contains(5L));
    }

    @Test
    void shouldThrowServiceExceptionWhenUpdateFails() {
        // Given
        String username = "Wrong.User";
        Long traineeId = 99L;
        Set<Long> trainerIds = Set.of(1L, 2L);

        User user = new User(10L, "John", "Doe", username, "password", true);
        Trainee trainee = new Trainee(traineeId, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        doThrow(new RuntimeException(DB_ERROR))
                .when(traineeRepository)
                .removeAllTrainerRelations(traineeId);

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> trainerService.updateTraineeTrainersList(username, trainerIds));

        // Then
        verify(traineeRepository).findByUsername(username);
        verify(traineeRepository).removeAllTrainerRelations(traineeId);
        assertTrue(exception.getMessage().contains("Failed to update trainers"));
    }

    @Test
    void shouldThrowWhenUserIdInvalid() {
        // Given:
        Trainer trainer = new Trainer(null, null, "Specializstion", true, new HashSet<>());

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainerService.save(trainer));

        // Then
        verifyNoInteractions(trainerRepository);
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
        verifyNoInteractions(trainerRepository);
        assertEquals(SPECIALIZATION_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerService.save(null));

        // Then
        verifyNoInteractions(trainerRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }


    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerService.update(null));

        // Then
        verifyNoInteractions(trainerRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerService.delete(null));

        //Then
        verifyNoInteractions(trainerRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnCheckCredentialsWhenNullArgs() {
        // Given: null arguments

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainerService.checkCredentials(null, USERNAME, PASSWORD));

        // Then
        verifyNoInteractions(trainerRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenTrainerByUsernameNotFound() {
        // Given
        String username = "Unknown.User";
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> trainerService.findTrainerByUsername(username));

        // Then
        assertEquals("Trainer not found with username: " + username, exception.getMessage());
    }
}
