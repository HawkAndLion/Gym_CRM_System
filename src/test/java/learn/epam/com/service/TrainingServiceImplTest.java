package learn.epam.com.service;

import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.repository.TrainingRepository;
import learn.epam.com.repository.UserRepository;
import learn.epam.com.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceImplTest {
    private static final String TRAINING_DATE_REQUIRED = "trainingDate required";
    private static final String DURATION_MUST_BE_POSITIVE = "duration must be positive";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String FAIL_SAVE_TRAINING = "Failed to save training";
    private static final String FAIL_UPDATE_TRAINING = "Failed to update training";
    private static final String FAIL_DELETE_TRAINING = "Failed to delete training";
    private static final String TRAINEE_ID_REQUIRED = "Training.traineeId is required";
    private static final String TRAINER_ID_REQUIRED = "Training.trainerId is required";

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Test
    void shouldSaveTrainingWhenValid() throws ServiceException {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", 2L, LocalDate.of(2025, 10, 1), 1.5);

        // When
        trainingService.save(training);

        // Then
        verify(trainingRepository).save(training);
        assertDoesNotThrow(() -> new ServiceException(FAIL_SAVE_TRAINING));
    }

    @Test
    void shouldReturnTrainingByIdWhenExists() throws ServiceException {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", 2L, LocalDate.of(2025, 10, 1), 1.5);

        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));

        // When
        Optional<Training> result = trainingService.findById(1L);

        // Then
        verify(trainingRepository).findById(1L);
        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    void shouldUpdateWhenTrainingIsValid() throws ServiceException {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", 2L, LocalDate.of(2025, 10, 1), 1.5);

        when(trainingRepository.save(training)).thenReturn(training);

        // When
        trainingService.update(training);

        // Then
        verify(trainingRepository).save(training);
        assertDoesNotThrow(() -> new ServiceException(FAIL_UPDATE_TRAINING));
    }

    @Test
    void shouldRemoveTrainingWhenDeleteIsCalled() throws ServiceException {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", 2L, LocalDate.of(2025, 10, 1), 1.5);

        doNothing().when(trainingRepository).delete(training);

        // When
        trainingService.delete(training);

        // Then
        verify(trainingRepository).delete(training);
        assertDoesNotThrow(() -> new ServiceException(FAIL_DELETE_TRAINING));
    }

    @Test
    void shouldReturnTrainingListWhenFindAllIsCalled() {
        // Given
        List<Training> trainings = new ArrayList<>();
        Training training = new Training(1L, 1L, 2L, "Workout", 2L, LocalDate.of(2025, 10, 1), 1.5);
        trainings.add(training);
        trainings.add(new Training(2L, 2L, 3L, "Fitness", 3L, LocalDate.of(2025, 10, 2), 2.0));

        when(trainingRepository.findAll()).thenReturn(trainings);

        // When
        List<Training> result = trainingService.findAllTrainings();

        // Then
        verify(trainingRepository).findAll();
        assertEquals(2, result.size());
        assertEquals(training, result.get(0));
    }

    @Test
    void shouldFindTrainingsForTraineeByCriteriaWhenRequested() throws ServiceException {
        // Given
        Trainee trainee = new Trainee();
        trainee.setId(1L);

        Training inRange = new Training(1L, 1L, 2L, "Workout", 2L,
                LocalDate.of(2025, 10, 10), 1.5);
        Training outOfRange = new Training(2L, 1L, 2L, "Yoga", 2L,
                LocalDate.of(2025, 9, 15), 2.0);

        when(traineeService.findTraineeByUsername("trainee1")).thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeId(1L)).thenReturn(List.of(inRange, outOfRange));

        // When
        List<Training> result = trainingService.findTrainingsForTraineeByCriteria(
                "trainee1", LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 30), null, null);

        // Then
        verify(traineeService).findTraineeByUsername("trainee1");
        verify(trainingRepository).findTrainingsByTraineeId(1L);
        assertEquals(1, result.size());
        assertEquals(inRange, result.get(0));
    }

    @Test
    void shouldReturnTrainingsForTrainerByCriteria() throws ServiceException {
        // Given
        String trainerUsername = "John.Doe";
        User user = new User();
        user.setId(10L);
        User user2 = new User();
        user.setId(20L);
        User user3 = new User();
        user.setId(21L);
        Trainer trainer = new Trainer(2L, user, "Fitness", true, new HashSet<>());
        Trainee trainee1 = new Trainee(1L, user2, "Addr1", LocalDate.of(1995, 1, 1), true, new HashSet<>());
        Trainee trainee2 = new Trainee(3L, user3, "Addr2", LocalDate.of(1998, 2, 2), true, new HashSet<>());

        Training training1 = new Training(100L, trainee1.getId(), trainer.getId(), "Cardio", 2L,
                LocalDate.of(2025, 10, 10), 1.5);
        Training training2 = new Training(101L, trainee2.getId(), trainer.getId(), "Strength", 3L,
                LocalDate.of(2025, 10, 15), 2.0);

        when(trainerService.findTrainerByUsername(trainerUsername)).thenReturn(Optional.of(trainer));
        when(trainingRepository.findTrainingsByTrainerId(trainer.getId())).thenReturn(List.of(training1, training2));

        // When
        List<Training> result = trainingService.findTrainingsForTrainerByCriteria(
                trainerUsername,
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 31),
                null
        );

        // Then
        verify(trainerService).findTrainerByUsername(trainerUsername);
        verify(trainingRepository).findTrainingsByTrainerId(trainer.getId());
        assertEquals(2, result.size());
        assertTrue(result.contains(training1));
        assertTrue(result.contains(training2));
    }


    @Test
    void shouldThrowWhenTrainerNotFoundByUsername() throws ServiceException {
        // Given
        String username = "missing.trainer";

        when(trainerService.findTrainerByUsername(username)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> trainingService.findTrainingsForTrainerByCriteria(
                        username, null, null, null));

        // Then
        verify(trainerService).findTrainerByUsername(username);
        assertEquals("Trainer not found for username: " + username, exception.getMessage());
    }


    @Test
    void shouldThrowWhenTraineeNotFound() {
        // Given
        Training training = new Training(null, null, 2L, "Workout", 2L,
                LocalDate.now(), 1.5);

        //When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainingService.save(training));

        //Then
        verifyNoInteractions(trainingRepository);
        assertEquals(TRAINEE_ID_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldThrowWhenTrainerNotFound() {
        // Given
        Training training = new Training(1L, 1L, null, "Workout", 2L,
                LocalDate.now(), 1.5);

        // When
        ServiceException ex = assertThrows(ServiceException.class, () -> trainingService.save(training));

        // Then
        verifyNoInteractions(trainingRepository);
        assertEquals(TRAINER_ID_REQUIRED, ex.getMessage());
    }

    @Test
    void shouldThrowWhenTrainingDateIsNull() {
        //Given
        Training training = new Training(1L, 1L, 2L, "Workout", 2L, null, 1.5);

        //When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainingService.save(training));

        //Then
        verifyNoInteractions(trainingRepository);
        assertEquals(TRAINING_DATE_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldThrowWhenDurationIsNotPositive() {
        //Given
        Training training = new Training(1L, 1L, 2L, "Workout", 2L,
                LocalDate.now(), 0.0);

        //When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainingService.save(training));

        //Then
        verifyNoInteractions(trainingRepository);
        assertEquals(DURATION_MUST_BE_POSITIVE, exception.getMessage());
    }

    @Test
    void shouldFindTrainingsForTraineeWhenCriteriaProvided() throws ServiceException {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", 2L,
                LocalDate.of(2025, 10, 1), 1.5);

        Trainee trainee = new Trainee();
        trainee.setId(1L);

        User trainerUser = new User();
        trainerUser.setId(10L);
        trainerUser.setUsername("trainer1");

        Trainer trainer = new Trainer();
        trainer.setId(2L);
        trainer.setUser(trainerUser);

        when(traineeService.findTraineeByUsername("trainee1")).thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeId(1L)).thenReturn(List.of(training));
        when(trainerService.findById(2L)).thenReturn(Optional.of(trainer));
        when(userRepository.findById(10L)).thenReturn(Optional.of(trainerUser));

        // When
        List<Training> result = trainingService.findTrainingsForTraineeByCriteria(
                "trainee1", LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 2), "trainer1", 2L);

        // Then
        verify(trainingRepository).findTrainingsByTraineeId(trainee.getId());
        assertEquals(1, result.size());
        assertEquals(training, result.get(0));
    }

    @Test
    void shouldFindTrainingsForTrainerWhenCriteriaProvided() throws ServiceException {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", 2L,
                LocalDate.of(2025, 10, 1), 1.5);

        Trainer trainer = new Trainer();
        trainer.setId(2L);

        User user = new User();
        user.setUsername("trainee1");

        when(trainerService.findTrainerByUsername("trainer1")).thenReturn(Optional.of(trainer));
        when(trainingRepository.findTrainingsByTrainerId(2L)).thenReturn(List.of(training));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        // When
        List<Training> result = trainingService.findTrainingsForTrainerByCriteria(
                "trainer1", LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 2), "trainee1");

        // Then
        verify(trainingRepository).findTrainingsByTrainerId(trainer.getId());
        assertEquals(1, result.size());
        assertEquals(training, result.get(0));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenTrainingIsNull() {
        // Given: null training

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainingService.save(null));

        // Then
        verifyNoInteractions(trainingRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }


    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenTrainingIsNull() {
        // Given: null training

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainingService.update(null));

        // Then
        verifyNoInteractions(trainingRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenTrainingIsNull() {
        // Given: null training

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainingService.delete(null));

        //Then
        verifyNoInteractions(trainingRepository);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
