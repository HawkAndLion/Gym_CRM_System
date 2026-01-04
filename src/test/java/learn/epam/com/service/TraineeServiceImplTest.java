package learn.epam.com.service;

import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
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
}
