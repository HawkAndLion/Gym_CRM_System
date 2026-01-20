package learn.epam.com.service;

import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.repository.TraineeRepository;
import learn.epam.com.repository.TrainerRepository;
import learn.epam.com.repository.TrainingRepository;
import learn.epam.com.repository.UserRepository;
import learn.epam.com.service.impl.TraineeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceImplTest {
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "secret";
    private static final String ENCRYPTED_PASSWORD = "secret";
    private static final String ANOTHER_USERNAME = "username";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String FAIL_SAVE_TRAINEE = "Failed to save trainee";
    private static final String FAIL_UPDATE_TRAINEE = "Failed to update trainee";
    private static final String FAIL_DELETE_TRAINEE = "Failed to delete trainee";
    private static final String TRAINEE_NOT_FOUND_BY_ID = "Trainee not found for id %d";
    private static final String TRAINER_NOT_FOUND_BY_ID = "Trainer not found for id %d";

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private UserCredentialService userCredentialService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Test
    void shouldSaveTraineeWhenValid() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "password", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());

        doNothing().when(userCredentialService).ensureUsernameExists(trainee.getUser());
        doNothing().when(userCredentialService).ensurePassword(trainee.getUser());
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        // When
        traineeService.save(trainee);

        // Then
        verify(userCredentialService).ensureUsernameExists(trainee.getUser());
        verify(userCredentialService).ensurePassword(trainee.getUser());
        verify(traineeRepository).save(trainee);
        assertDoesNotThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, FAIL_SAVE_TRAINEE));
    }

    @Test
    void shouldReturnTraineeByIdWhenExists() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "password", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        // When
        Optional<Trainee> result = traineeService.findById(1L);

        // Then
        verify(traineeRepository).findById(1L);
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void shouldUpdateWhenTraineeIsValid() {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "password", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());

        when(traineeRepository.save(trainee)).thenReturn(trainee);

        // When
        traineeService.update(trainee);

        // Then
        verify(traineeRepository).save(trainee);
        assertDoesNotThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, FAIL_UPDATE_TRAINEE));
    }

    @Test
    void shouldRemoveTraineeWhenDeleteIsCalled() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "password", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());

        doNothing().when(traineeRepository).delete(trainee);

        // When
        traineeService.delete(trainee);

        // Then
        verify(traineeRepository).delete(trainee);
        assertDoesNotThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, FAIL_DELETE_TRAINEE));
    }

    @Test
    void shouldReturnTraineeListWhenFindAllIsCalled() {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "password", true);
        User user2 = new User(202L, "John", "Doe", "John.Doe", "password", true);
        List<Trainee> trainees = new ArrayList<>();
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());
        trainees.add(trainee);
        trainees.add(new Trainee(2L, user2, "Astana", LocalDate.of(1998, 8, 8), true, new HashSet<>()));

        when(traineeRepository.findAll()).thenReturn(trainees);

        // When
        List<Trainee> result = traineeService.findAllTrainee();

        // Then
        verify(traineeRepository).findAll();
        assertEquals(2, result.size());
        assertEquals(trainee, result.get(0));
    }

    @Test
    void shouldReturnTrueWhenCheckCredentialsAreValid() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "password", true);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(userCredentialService.loadUserOrThrow(10L)).thenReturn(user);
        when(passwordEncoder.matches(PASSWORD, ENCRYPTED_PASSWORD)).thenReturn(true);

        // When
        boolean result = traineeService.checkCredentials(1L, USERNAME, PASSWORD);

        // Then
        verify(traineeRepository).findById(1L);
        verify(userCredentialService).loadUserOrThrow(10L);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenCheckCredentialsAreInvalid() throws ServiceException {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "password", true);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(userCredentialService.loadUserOrThrow(10L)).thenReturn(user);

        // When
        boolean result = traineeService.checkCredentials(1L, ANOTHER_USERNAME, PASSWORD);

        // Then
        verify(traineeRepository).findById(1L);
        verify(userCredentialService).loadUserOrThrow(10L);
        assertFalse(result);
    }

    @Test
    void shouldReturnTraineeWhenFindTraineeByCredentials() {
        // Given
        User user = new User(10L, "John", "Doe", "John.Doe", "password", true);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1998, 4, 15), true, new HashSet<>());

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(traineeRepository.findAll()).thenReturn(List.of(trainee));

        // When
        Optional<Trainee> result = traineeService.findTraineeByCredentials(USERNAME, PASSWORD);

        // Then
        verify(userRepository).findAll();
        verify(traineeRepository).findAll();
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
        verify(traineeRepository).findTrainerIdsByTraineeId(traineeId);
        assertNotNull(result);
        assertEquals(expected, result);
        assertTrue(result.containsAll(Set.of(10L, 20L)));
    }

    @Test
    void shouldSetTrainerIdsForTraineeWhenMethodCalled() throws ServiceException {
        // Given
        Long traineeId = 1L;
        Set<Long> trainerIds = Set.of(5L, 6L);
        User user1 = new User(10L, "John", "Doe", "John.Doe", "password", true);
        User user2 = new User(11L, "Jane", "Doe", "Jane.Doe", "password", true);
        User user3 = new User(12L, "Jack", "Doe", "Jack.Doe", "password", true);
        Trainee trainee = new Trainee(traineeId, user1, "address", LocalDate.of(2000, 10, 12), true, new HashSet<>());
        Trainer trainer1 = new Trainer(5L, user2, "Specialization", true, new HashSet<>());
        Trainer trainer2 = new Trainer(6L, user3, "Yoga", true, new HashSet<>());

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAllById(trainerIds)).thenReturn(List.of(trainer1, trainer2));

        // When
        traineeService.setTrainerIdsForTrainee(traineeId, trainerIds);

        // Then
        verify(traineeRepository).findById(traineeId);
        verify(trainerRepository).findAllById(trainerIds);
        verify(trainerRepository, times(2)).save(any(Trainer.class));
        verify(traineeRepository).save(trainee);

        assertDoesNotThrow(() -> traineeService.setTrainerIdsForTrainee(traineeId, trainerIds));
    }

    @Test
    void shouldAssignTrainerWhenTraineeIdValid() throws ServiceException {
        // Given
        Long traineeId = 1L;
        Long trainerId = 10L;

        User user1 = new User(10L, "John", "Doe", "John.Doe", "password", true);
        User user2 = new User(11L, "Jane", "Doe", "Jane.Doe", "password", true);
        Trainee trainee = new Trainee(traineeId, user1, "address", LocalDate.of(2000, 10, 12), true, new HashSet<>());
        Trainer trainer = new Trainer(trainerId, user2, "Specialization", true, new HashSet<>());

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));

        // When
        traineeService.assignTrainer(traineeId, trainerId);

        // Then
        verify(traineeRepository).findById(traineeId);
        verify(trainerRepository).findById(trainerId);
        verify(trainerRepository).save(trainer);
        verify(traineeRepository).save(trainee);
        assertTrue(trainee.getTrainers().contains(trainer));
        assertTrue(trainer.getTrainees().contains(trainee));
        assertDoesNotThrow(() -> traineeService.assignTrainer(traineeId, trainerId));
    }

    @Test
    void shouldUnassignTrainerWhenMethodCalled() {
        // Given
        Long traineeId = 1L;
        Long trainerId = 10L;

        User user1 = new User(10L, "John", "Doe", "John.Doe", "password", true);
        User user2 = new User(11L, "Jane", "Doe", "Jane.Doe", "password", true);
        Trainee trainee = new Trainee(traineeId, user1, "address", LocalDate.of(2000, 10, 12), true, new HashSet<>());
        Trainer trainer = new Trainer(trainerId, user2, "Specialization", true, new HashSet<>());

        trainee.setTrainers(new HashSet<>(Set.of(trainer)));
        trainer.setTrainees(new HashSet<>(Set.of(trainee)));

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));

        // When
        traineeService.unassignTrainer(traineeId, trainerId);

        // Then
        verify(traineeRepository).findById(traineeId);
        verify(trainerRepository).findById(trainerId);
        verify(trainerRepository).save(trainer);
        verify(traineeRepository).save(trainee);
        assertFalse(trainee.getTrainers().contains(trainer));
        assertFalse(trainer.getTrainees().contains(trainee));
        assertDoesNotThrow(() -> traineeService.unassignTrainer(traineeId, trainerId));
    }

    @Test
    void shouldReturnTraineeWhenFindTraineeByUsername() throws ServiceException {
        // Given
        String username = "John.Brown";
        User user = new User(10L, "John", "Doe", "John.Doe", "password", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));

        // When
        Optional<Trainee> result = traineeService.findTraineeByUsername(username);

        // Then
        verify(traineeRepository).findByUsername(username);
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void shouldActivateTraineeSuccessfullyWhenValidInput() throws ServiceException {
        // Given
        String username = "John.Brown";
        User user = new User(10L, "John", "Brown", username, "pass", false);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1995, 5, 5), false, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // When
        traineeService.activateTrainee(username);

        // Then
        verify(userRepository).save(user);
        verify(traineeRepository).save(trainee);
        assertTrue(trainee.isActive());
        assertTrue(user.isActive());
    }

    @Test
    void shouldDeactivateTraineeSuccessfully() throws ServiceException {
        // Given
        String username = "John.Brown";
        User user = new User(10L, "John", "Brown", username, "pass", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // When
        traineeService.deactivateTrainee(username);

        // Then
        assertFalse(trainee.isActive());
        assertFalse(user.isActive());
        verify(userRepository).save(user);
        verify(traineeRepository).save(trainee);
    }

    @Test
    void shouldDeleteTraineeByUsernameSuccessfully() throws ServiceException {
        // Given
        String username = "john.trainee";
        User user = new User(10L, "John", "Trainee", username, "pass", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(trainingRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        traineeService.deleteTraineeByUsername(username);

        // Then
        verify(trainingRepository).findAll();
        verify(traineeRepository).delete(trainee);
        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeService.save(null));

        // Then
        verifyNoInteractions(traineeRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }


    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeService.update(null));

        // Then
        verifyNoInteractions(traineeRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeService.delete(null));

        //Then
        verifyNoInteractions(traineeRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnCheckCredentialsWhenNullArguments() {
        // Given: null arguments

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.checkCredentials(null, USERNAME, PASSWORD));

        // Then
        verifyNoInteractions(traineeRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFindTraineeByUsernameWithNull() {
        //Given null username

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.findTraineeByUsername(null));

        // Then
        verifyNoInteractions(traineeRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldSaveUserWhenIdIsNullDuringTraineeSave() throws ServiceException {
        // Given
        User user = new User(null, "John", "Doe", "john.doe", "pass", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        doNothing().when(userCredentialService).ensureUsernameExists(user);
        doNothing().when(userCredentialService).ensurePassword(user);
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        // When
        traineeService.save(trainee);

        // Then
        verify(userRepository).save(user);
        verify(traineeRepository).save(trainee);
    }

    @Test
    void shouldReturnTrueWhenCheckCredentialsValid() throws ServiceException {
        // Given
        Long traineeId = 1L;
        User user = new User(10L, "John", "Doe", "john.doe", "encodedPass", true);
        Trainee trainee = new Trainee(traineeId, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(userCredentialService.loadUserOrThrow(10L)).thenReturn(user);
        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);

        // When
        boolean result = traineeService.checkCredentials(traineeId, "john.doe", "rawPass");

        // Then
        assertTrue(result);
        verify(traineeRepository).findById(traineeId);
        verify(userCredentialService).loadUserOrThrow(10L);
        verify(passwordEncoder).matches("rawPass", "encodedPass");
    }


    @Test
    void shouldThrowServiceExceptionWhenTraineeNotFoundDuringCheckCredentials() {
        // Given
        Long traineeId = 1L;

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeService.checkCredentials(traineeId, "john.doe", "pass"));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Trainee not found"));
    }

    @Test
    void shouldReturnFalseWhenPasswordDoesNotMatch() throws ServiceException {
        // Given
        Long traineeId = 1L;
        User user = new User(10L, "John", "Doe", "john.doe", "encodedPass", true);
        Trainee trainee = new Trainee(traineeId, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(userCredentialService.loadUserOrThrow(10L)).thenReturn(user);
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        // When
        boolean result = traineeService.checkCredentials(traineeId, "john.doe", "wrongPass");

        // Then
        assertFalse(result);
    }


    @Test
    void shouldUpdateTraineeTrainersSuccessfullyWhenValidArgs() throws ServiceException {
        // Given
        String username = "john.doe";
        User user = new User(10L, "John", "Doe", username, "pass", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        Trainer trainer1 = new Trainer(1L, new User(11L, "Jane", "Doe", "jane.doe", "pass", true), "Yoga", true, new HashSet<>());
        Trainer trainer2 = new Trainer(2L, new User(12L, "Jack", "Doe", "jack.doe", "pass", true), "Pilates", true, new HashSet<>());
        Set<Trainer> trainers = new HashSet<>(Set.of(trainer1, trainer2));

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(trainee)).thenReturn(trainee);

        // When
        traineeService.update(username, trainers);

        // Then
        assertEquals(trainers, trainee.getTrainers());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUsernameCredentialsIsNull() {
        // Given

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.findTraineeByCredentials(null, "password"));


        // Then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPasswordIsNull() {
        // Given

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.findTraineeByCredentials("username", null));

        // Then

        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldReturnEmptyOptionalWhenNoMatchingUser() {
        // Given
        String username = "john.doe";
        String password = "secret";

        when(userRepository.findAll()).thenReturn(List.of(
                new User(10L, "Jane", "Doe", "jane.doe", "pass", true),
                new User(11L, "Jack", "Doe", "jack.doe", "pass", true)
        ));

        // When
        Optional<Trainee> result = traineeService.findTraineeByCredentials(username, password);

        // Then
        verify(userRepository).findAll();
        assertTrue(result.isEmpty());
    }


    @Test
    void shouldReturnTraineeWhenCredentialsMatch() {
        // Given
        String username = "john.doe";
        String password = "secret";

        User user = new User(10L, "John", "Doe", username, password, true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(traineeRepository.findAll()).thenReturn(List.of(trainee));

        // When
        Optional<Trainee> result = traineeService.findTraineeByCredentials(username, password);

        // Then
        verify(userRepository).findAll();
        verify(traineeRepository).findAll();
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }


    @Test
    void shouldThrowServiceExceptionWhenRepositoryThrows() {
        // Given
        String username = "john.doe";

        when(traineeRepository.findByUsername(username)).thenThrow(new RuntimeException("DB error"));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeService.findTraineeByUsername(username));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Trainee not found", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUsernameIsNull() {
        // Given

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.findTraineeByUsername(null));

        // Then
        verifyNoInteractions(traineeRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenActivateTraineeWithNullUsername() {
        // Given

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.activateTrainee(null));

        // Then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenTraineeNotFoundOnActivate() throws ServiceException {
        // Given
        String username = "john.doe";

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeService.activateTrainee(username));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Trainee not found", exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenUserNotFoundOnActivate() throws ServiceException {
        // Given
        String username = "john.doe";
        User user = new User(10L, "John", "Doe", username, "pass", false);
        Trainee trainee = new Trainee(1L, user, "Address", LocalDate.of(1995, 5, 5), false, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeService.activateTrainee(username));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenTraineeAlreadyActive() throws ServiceException {
        // Given
        String username = "john.doe";
        User user = new User(10L, "John", "Doe", username, "pass", true);
        Trainee trainee = new Trainee(1L, user, "Address", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeService.activateTrainee(username));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Trainee already active", exception.getMessage());
    }

    @Test
    void shouldActivateTraineeSuccessfullyWhenValidArgs() throws ServiceException {
        // Given
        String username = "john.doe";
        User user = new User(10L, "John", "Doe", username, "pass", false);
        Trainee trainee = new Trainee(1L, user, "Address", LocalDate.of(1995, 5, 5), false, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // When
        traineeService.activateTrainee(username);

        // Then
        verify(userRepository).save(user);
        verify(traineeRepository).save(trainee);
        assertTrue(user.isActive());
        assertTrue(trainee.isActive());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDeactivateTraineeWithNullUsername() {
        // Given

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.deactivateTrainee(null));

        // Then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenTraineeNotFoundOnDeactivate() throws ServiceException {
        // Given
        String username = "john.doe";

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeService.deactivateTrainee(username));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Trainee not found", exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenUserNotFoundOnDeactivate() throws ServiceException {
        // Given
        String username = "john.doe";
        User user = new User(10L, "John", "Doe", username, "pass", true);
        Trainee trainee = new Trainee(1L, user, "Address", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeService.deactivateTrainee(username));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenTraineeAlreadyInactive() throws ServiceException {
        // Given
        String username = "john.doe";
        User user = new User(10L, "John", "Doe", username, "pass", false);
        Trainee trainee = new Trainee(1L, user, "Address", LocalDate.of(1995, 5, 5), false, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeService.deactivateTrainee(username));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Trainee already inactive", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDeleteTraineeByUsernameWithNull() {
        //Given

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.deleteTraineeByUsername(null));

        // Then
        verifyNoInteractions(traineeRepository, trainingRepository, trainerRepository, userRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenTraineeNotFoundOnDelete() throws ServiceException {
        // Given
        String username = "john.trainee";

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeService.deleteTraineeByUsername(username));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Authentication failed", exception.getMessage());
    }

    @Test
    void shouldDeleteTraineeWithNoTrainingsAndNoTrainersWhenTraineeDeleted() throws ServiceException {
        // Given
        String username = "john.trainee";
        User user = new User(10L, "John", "Doe", username, "pass", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // When
        traineeService.deleteTraineeByUsername(username);

        // Then
        verify(trainingRepository).findAll();
        verify(traineeRepository).delete(trainee);
        verify(userRepository).delete(user);
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void shouldDeleteTraineeWithTrainingsWhenTraineeDeleted() throws ServiceException {
        // Given
        String username = "john.trainee";
        User user = new User(10L, "John", "Doe", username, "pass", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        Training training1 = new Training(1L, trainee.getId(), 2L, "Fitness", 5L, LocalDate.of(2025, 12, 25), 1.5);
        Training training2 = new Training(2L, 2L, 2L, "Fitness", 5L, LocalDate.of(2025, 12, 25), 1.5);

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findAll()).thenReturn(List.of(training1, training2));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        traineeService.deleteTraineeByUsername(username);

        // Then
        verify(trainingRepository).findAll();
        verify(trainingRepository).delete(training1);
        verify(trainingRepository, never()).delete(training2);
        verify(traineeRepository).delete(trainee);
        verifyNoInteractions(trainerRepository);
        verify(userRepository).delete(user);
    }

    @Test
    void shouldDeleteTraineeAndUnassignTrainersWhenTraineeDeleted() throws ServiceException {
        // Given
        String username = "john.trainee";

        User user = new User(10L, "John", "Doe", username, "pass", true);
        User trainerUser = new User(11L, "Jane", "Doe", "jane.trainer", "pass", true);

        Trainer trainer = new Trainer(5L, trainerUser, "Yoga", true, new HashSet<>());
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>(Set.of(trainer)));
        trainer.getTrainees().add(trainee);

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // When
        traineeService.deleteTraineeByUsername(username);

        // Then
        verify(trainerRepository).save(trainer);
        verify(traineeRepository).delete(trainee);
        verify(userRepository).delete(user);
        assertFalse(trainer.getTrainees().contains(trainee));
    }

    @Test
    void shouldDeleteTraineeWhenUserNotFound() throws ServiceException {
        // Given
        String username = "john.trainee";

        User user = new User(10L, "John", "Doe", username, "pass", true);
        Trainee trainee = new Trainee(1L, user, "Almaty", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        // When
        traineeService.deleteTraineeByUsername(username);

        // Then
        verify(traineeRepository).delete(trainee);
        verify(userRepository, never()).delete(user);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTraineeIdIsNull() {
        // Given : Null traineeId

        // When
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> traineeService.setTrainerIdsForTrainee(null, Set.of(1L, 2L)));

        // Then
        verifyNoInteractions(traineeRepository, trainerRepository);
        assertEquals(NULL_EXCEPTION, ex1.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTrainerIdsNull() {
        // Given : Null trainerIds

        // When
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> traineeService.setTrainerIdsForTrainee(1L, null));

        // Then
        verifyNoInteractions(traineeRepository, trainerRepository);
        assertEquals(NULL_EXCEPTION, ex2.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenTraineeNotFoundById() {
        // Given
        Long traineeId = 1L;
        Set<Long> trainerIds = Set.of(10L, 20L);

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeService.setTrainerIdsForTrainee(traineeId, trainerIds));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals(String.format(TRAINEE_NOT_FOUND_BY_ID, traineeId), exception.getMessage());
    }

    @Test
    void shouldAssignTrainerIdsToTraineeSuccessfullyWhenTraineeAndTrainerExist() throws ServiceException {
        // Given
        Long traineeId = 1L;
        Set<Long> trainerIds = Set.of(10L, 20L);

        Trainee trainee = new Trainee(traineeId, new User(1L, "John", "Doe", "john.doe", "pass", true), "", LocalDate.now(), true, new HashSet<>());
        Trainer trainer1 = new Trainer(10L, new User(2L, "Trainer", "One", "t1", "pass", true), "", true, new HashSet<>());
        Trainer trainer2 = new Trainer(20L, new User(3L, "Trainer", "Two", "t2", "pass", true), "", true, new HashSet<>());

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAllById(trainerIds)).thenReturn(List.of(trainer1, trainer2));

        // When
        traineeService.setTrainerIdsForTrainee(traineeId, trainerIds);

        // Then
        verify(trainerRepository).save(trainer1);
        verify(trainerRepository).save(trainer2);
        verify(traineeRepository).save(trainee);
        assertTrue(trainee.getTrainers().contains(trainer1));
        assertTrue(trainee.getTrainers().contains(trainer2));
        assertTrue(trainer1.getTrainees().contains(trainee));
        assertTrue(trainer2.getTrainees().contains(trainee));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTraineeIdNull() {
        // Given  : Null traineeId

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.unassignTrainer(null, 10L));

        // Then
        verifyNoInteractions(traineeRepository, trainerRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTrainerIdNull() {
        // Given : Null trainerId

        // When
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> traineeService.unassignTrainer(1L, null));

        // Then
        verifyNoInteractions(traineeRepository, trainerRepository);
        assertEquals(NULL_EXCEPTION, ex2.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTraineeNotFound() {
        // Given
        Long traineeId = 1L;
        Long trainerId = 10L;

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.empty());

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> traineeService.unassignTrainer(traineeId, trainerId));

        // Then
        verify(traineeRepository).findById(traineeId);
        verifyNoInteractions(trainerRepository);
        assertEquals(String.format(TRAINEE_NOT_FOUND_BY_ID, traineeId), ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTrainerNotFound() {
        // Given
        Long traineeId = 1L;
        Long trainerId = 10L;

        Trainee trainee = new Trainee("address", LocalDate.of(2000, 10, 15), true);
        trainee.setId(traineeId);
        trainee.setTrainers(new HashSet<>());

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(trainerId)).thenReturn(Optional.empty());

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> traineeService.unassignTrainer(traineeId, trainerId));

        // Then
        verify(traineeRepository).findById(traineeId);
        verify(trainerRepository).findById(trainerId);
        assertEquals(String.format(TRAINER_NOT_FOUND_BY_ID, trainerId), ex.getMessage());
    }

    @Test
    void shouldUnassignTrainerSuccessfullyWhenValidArgs() {
        // Given
        Long traineeId = 1L;
        Long trainerId = 10L;
        User traineeUser = new User(1L, "John", "Doe", "john.doe", "pass", true);
        Trainee trainee = new Trainee(traineeId, traineeUser, "", null, true, new HashSet<>());

        User trainerUser = new User(2L, "Jane", "Doe", "jane.trainer", "pass", true);
        Trainer trainer = new Trainer(trainerId, trainerUser, "", true, new HashSet<>());

        trainee.getTrainers().add(trainer);
        trainer.getTrainees().add(trainee);

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));

        // When
        traineeService.unassignTrainer(traineeId, trainerId);

        // Then
        verify(trainerRepository).save(trainer);
        verify(traineeRepository).save(trainee);
        assertFalse(trainee.getTrainers().contains(trainer));
        assertFalse(trainer.getTrainees().contains(trainee));
    }

    @Test
    void shouldPassValidationWhenTraineeIsValid() {
        // Given
        User traineeUser = new User(1L, "John", "Doe", "John.Doe", "password", true);
        Trainee trainee = new Trainee(2L, traineeUser, "address", LocalDate.of(2000, 8, 30), true, new HashSet<>());

        // When
        traineeService.save(trainee);

        // Then
        assertDoesNotThrow(() -> traineeService.save(trainee));
    }

    @Test
    void shouldThrowWhenUserIsNull() {
        // Given
        Trainee trainee = new Trainee(2L, null, "address", LocalDate.of(2000, 8, 30), true, new HashSet<>());

        // When
        ServiceException exception =
                assertThrows(ServiceException.class, () -> traineeService.save(trainee));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Trainee.userId is required", exception.getMessage());
    }

    @Test
    void shouldThrowWhenAddressIsBlank() {
        // Given
        User traineeUser = new User(1L, "John", "Doe", "John.Doe", "password", true);
        Trainee trainee = new Trainee(2L, traineeUser, "", LocalDate.of(2000, 8, 30), true, new HashSet<>());

        // When
        ServiceException exception =
                assertThrows(ServiceException.class, () -> traineeService.save(trainee));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Trainee.address is required", exception.getMessage());
    }

    @Test
    void shouldThrowWhenDateOfBirthIsNull() {
        // Given
        User traineeUser = new User(1L, "John", "Doe", "John.Doe", "password", true);
        Trainee trainee = new Trainee(2L, traineeUser, "address", null, true, new HashSet<>());

        // When
        ServiceException exception =
                assertThrows(ServiceException.class, () -> traineeService.save(trainee));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Trainee.dateOfBirth is required", exception.getMessage());
    }

    @Test
    void shouldThrowWhenDateOfBirthIsToday() {
        // Given
        User traineeUser = new User(1L, "John", "Doe", "John.Doe", "password", true);
        Trainee trainee = new Trainee(2L, traineeUser, "address", LocalDate.now(), true, new HashSet<>());

        // When
        ServiceException exception =
                assertThrows(ServiceException.class, () -> traineeService.save(trainee));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Trainee.dateOfBirth must be in the past", exception.getMessage());
    }

    @Test
    void shouldThrowWhenDateOfBirthIsInFuture() {
        // Given
        User traineeUser = new User(1L, "John", "Doe", "John.Doe", "password", true);
        Trainee trainee = new Trainee(2L, traineeUser, "address", LocalDate.now().plusDays(1), true, new HashSet<>());

        // When
        ServiceException exception =
                assertThrows(ServiceException.class, () -> traineeService.save(trainee));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Trainee.dateOfBirth must be in the past", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTraineeIsNull() {
        // Given

        // When
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> traineeService.save(null));

        // Then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

}
