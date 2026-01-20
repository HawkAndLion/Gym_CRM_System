package learn.epam.com.service;

import learn.epam.com.api.model.TrainingRequest;
import learn.epam.com.api.model.TrainingResponse;
import learn.epam.com.entity.*;
import learn.epam.com.event.TrainingDeletedEvent;
import learn.epam.com.repository.TrainingRepository;
import learn.epam.com.repository.TrainingTypeRepository;
import learn.epam.com.repository.UserRepository;
import learn.epam.com.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.*;

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
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Test
    void shouldSaveTrainingWhenValid() throws ServiceException {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", 2L, LocalDate.of(2025, 10, 1), 1.5);
        when(traineeService.findById(1L)).thenReturn(Optional.of(new Trainee(1L, new User(10L, "John", "Doe", "trainee", "pass", true), "Addr", LocalDate.of(1990, 1, 1), true)));
        when(trainerService.findById(2L)).thenReturn(Optional.of(new Trainer(2L, new User(20L, "Mike", "Trainer", "trainer", "pass", true), "Fitness", true, new HashSet<>())));

        // When
        trainingService.save(training);

        // Then
        verify(trainingRepository).save(training);
        assertDoesNotThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, FAIL_SAVE_TRAINING));
    }

    @Test
    void shouldCreateTrainingWhenRequested() throws ServiceException {
        // Given
        Trainee trainee = new Trainee("address", LocalDate.of(2000, 12, 25), true);
        trainee.setId(1L);
        Trainer trainer = new Trainer("Trainer", true);
        trainer.setId(2L);

        TrainingRequest request = new TrainingRequest();
        request.setName("Workout");
        request.setDate(LocalDate.of(2025, 10, 1));
        request.setDuration(1.5);

        // When
        Training result = trainingService.update(trainee, trainer, request, 3L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getTraineeId());
        assertEquals(2L, result.getTrainerId());
        assertEquals("Workout", result.getName());
        assertEquals(3L, result.getTrainingTypeId());
        assertEquals(LocalDate.of(2025, 10, 1), result.getTrainingDate());
        assertEquals(1.5, result.getDuration());
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
        assertDoesNotThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, FAIL_UPDATE_TRAINING));
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
        assertDoesNotThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, FAIL_DELETE_TRAINING));
    }

    @Test
    void shouldDeleteTrainingWhenFindById() throws ServiceException {
        Training training = new Training();
        training.setId(1L);

        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));

        trainingService.deleteById(1L);

        verify(trainingRepository).delete(training);
        verify(eventPublisher).publishEvent(any(TrainingDeletedEvent.class));
    }

    @Test
    void shouldThrowWhenDeleteByIdTrainingNotFound() {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(
                ServiceException.class,
                () -> trainingService.deleteById(99L)
        );

        assertEquals("Training not found: 99", ex.getMessage());
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
        User user = new User(100L, "John", "Doe", "John.Doe", "pass", true);
        User user2 = new User(101L, "Mickey", "Mouse", "Mickey.Mouse", "pass", true);
        User user3 = new User(102L, "Sponge", "Bob", "Sponge.Bob", "pass", true);
        List<Trainer> trainers = List.of(
                new Trainer(1L, user, "Cardio", true, new HashSet<>()),
                new Trainer(2L, user2, "Yoga", true, new HashSet<>())
        );
        Trainee trainee = new Trainee(1L, user3, "Vietnam", LocalDate.of(2000, 12, 12), true);

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
        User user = new User(10L, "John", "Doe", "John.Doe", "pass", true);
        User user2 = new User(20L, "Mickey", "Mouse", "Mickey.Mouse", "pass", true);
        User user3 = new User(21L, "Sponge", "Bob", "Sponge.Bob", "pass", true);

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
    void shouldThrowWhenTraineeNotFoundOnSave() {
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

        User user = new User(100L, "John", "Doe", "John.Doe", "pass", true);
        User trainerUser = new User(10L, "Mickey", "Mouse", "trainer1", "pass", true);
        User user3 = new User(102L, "Sponge", "Bob", "Sponge.Bob", "pass", true);

        Trainee trainee = new Trainee(1L, user, "Vietnam", LocalDate.of(2000, 12, 12), true);
        Trainer trainer = new Trainer(2L, trainerUser, "Cardio", true, new HashSet<>());

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
        User user = new User(10L, "John", "Doe", "trainer1", "pass", true);

        Trainer trainer = new Trainer(2L, user, "Fitness", true, new HashSet<>());

        Training training = new Training(1L, 2L, 1L, "Workout", 2L,
                LocalDate.of(2025, 10, 1), 1.5);

        when(trainerService.findTrainerByUsername("trainer1")).thenReturn(Optional.of(trainer));
        when(trainingRepository.findTrainingsByTrainerId(2L)).thenReturn(List.of(training));

        // When
        List<Training> result = trainingService.findTrainingsForTrainerByCriteria(
                "trainer1", LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 2), null);

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

    @Test
    void shouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
        Trainee trainee = new Trainee("address", LocalDate.of(2000, 12, 25), true);
        Trainer trainer = new Trainer("Cardio", true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.update(trainee, trainer, null, 1L)
        );

        assertEquals("Argument is null ", ex.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTraineeUsernameIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.findTrainingsForTraineeByCriteria(
                        null, null, null, null, null)
        );

        assertEquals("Argument is null ", ex.getMessage());
    }

    @Test
    void shouldIgnoreTrainerFilterWhenTrainerNameIsBlank() throws ServiceException {
        // Given
        Trainee trainee = new Trainee("address", LocalDate.of(2000, 12, 25), true);
        trainee.setId(1L);

        Training training = new Training(1L, 1L, 2L, "Fitness", 5L, LocalDate.of(2025, 10, 15), 1.5);
        training.setTrainingDate(LocalDate.now());

        when(traineeService.findTraineeByUsername("trainee"))
                .thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeId(1L))
                .thenReturn(List.of(training));

        List<Training> result =
                trainingService.findTrainingsForTraineeByCriteria(
                        "trainee", null, null, " ", null);

        assertEquals(1, result.size());
    }

    @Test
    void shouldExcludeTrainingWhenDateIsAfterToDate() throws ServiceException {
        // Given
        User user = new User(1L, "Dan", "Brown", "Dan.Brown", "password", true);
        Trainee trainee = new Trainee(1L, user, "Addr", LocalDate.now(), true);

        Training afterToDate = new Training(
                1L, 1L, 2L, "Workout", 1L,
                LocalDate.of(2025, 11, 5), 1.0
        );

        when(traineeService.findTraineeByUsername("trainee"))
                .thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeId(1L))
                .thenReturn(List.of(afterToDate));

        // When
        List<Training> result = trainingService.findTrainingsForTraineeByCriteria("trainee", null, LocalDate.of(2025, 11, 1), null, null
        );

        // Then
        assertTrue(result.isEmpty());
    }


    @Test
    void shouldWrapServiceExceptionIntoRuntimeExceptionWhenTrainerLookupFails() throws ServiceException {
        // Given
        User user = new User(1L, "Dan", "Brown", "Dan.Brown", "password", true);
        Trainee trainee = new Trainee(1L, user, "Addr", LocalDate.now(), true);

        Training training = new Training(
                1L, 1L, 99L, "Workout", 1L,
                LocalDate.of(2025, 10, 10), 1.0
        );

        when(traineeService.findTraineeByUsername("trainee"))
                .thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeId(1L))
                .thenReturn(List.of(training));

        when(trainerService.findById(99L))
                .thenThrow(new ServiceException(HttpStatus.NOT_FOUND, "Trainer error"));

        // When
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                trainingService.findTrainingsForTraineeByCriteria(
                        "trainee",
                        null,
                        null,
                        "trainerName",
                        null
                )
        );

        // Then
        assertTrue(ex.getCause() instanceof ServiceException);
    }

    @Test
    void shouldReturnTrainingWhenTrainingTypeMatches() throws ServiceException {
        // Given
        User user = new User(1L, "Dan", "Brown", "Dan.Brown", "password", true);
        Trainee trainee = new Trainee(1L, user, "Addr", LocalDate.now(), true);

        Training matchingType = new Training(
                1L, 1L, 2L, "Workout", 10L,
                LocalDate.of(2025, 10, 10), 1.0
        );

        when(traineeService.findTraineeByUsername("trainee"))
                .thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeId(1L))
                .thenReturn(List.of(matchingType));

        // When
        List<Training> result = trainingService.findTrainingsForTraineeByCriteria(
                "trainee",
                null,
                null,
                null,
                10L
        );

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void shouldExcludeTrainingWhenTrainingTypeDoesNotMatch() throws ServiceException {
        // Given
        User user = new User(1L, "Dan", "Brown", "Dan.Brown", "password", true);
        Trainee trainee = new Trainee(1L, user, "Addr", LocalDate.now(), true);

        Training nonMatchingType = new Training(
                1L, 1L, 2L, "Workout", 20L,
                LocalDate.of(2025, 10, 10), 1.0
        );

        when(traineeService.findTraineeByUsername("trainee"))
                .thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeId(1L))
                .thenReturn(List.of(nonMatchingType));

        // When
        List<Training> result = trainingService.findTrainingsForTraineeByCriteria(
                "trainee",
                null,
                null,
                null,
                10L
        );

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldProcessWhenTrainerUsernameIsProvided() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(10L, user, "Fitness", true, new HashSet<>());

        Training training = new Training(
                1L, 1L, trainer.getId(), "Workout", 1L,
                LocalDate.of(2025, 10, 10), 1.0
        );

        when(trainerService.findTrainerByUsername("John.Doe"))
                .thenReturn(Optional.of(trainer));
        when(trainingRepository.findTrainingsByTrainerId(trainer.getId()))
                .thenReturn(List.of(training));

        // When
        List<Training> result = trainingService.findTrainingsForTrainerByCriteria(
                "John.Doe", null, null, null
        );

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void shouldExcludeTrainingWhenBeforeFromDate() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(10L, user, "Fitness", true, new HashSet<>());

        Training beforeFromDate = new Training(
                1L, 1L, trainer.getId(), "Workout", 1L,
                LocalDate.of(2025, 9, 1), 1.0
        );

        when(trainerService.findTrainerByUsername("John.Doe"))
                .thenReturn(Optional.of(trainer));
        when(trainingRepository.findTrainingsByTrainerId(trainer.getId()))
                .thenReturn(List.of(beforeFromDate));

        // When
        List<Training> result = trainingService.findTrainingsForTrainerByCriteria(
                "John.Doe",
                LocalDate.of(2025, 10, 1),
                null,
                null
        );

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldExcludeTrainingWhenAfterToDate() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(10L, user, "Fitness", true, new HashSet<>());

        Training afterToDate = new Training(
                1L, 1L, trainer.getId(), "Workout", 1L,
                LocalDate.of(2025, 11, 5), 1.0
        );

        when(trainerService.findTrainerByUsername("John.Doe"))
                .thenReturn(Optional.of(trainer));
        when(trainingRepository.findTrainingsByTrainerId(trainer.getId()))
                .thenReturn(List.of(afterToDate));

        // When
        List<Training> result = trainingService.findTrainingsForTrainerByCriteria(
                "John.Doe",
                null,
                LocalDate.of(2025, 11, 1),
                null
        );

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAllTrainingsWhenTraineeNameIsBlank() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(10L, user, "Fitness", true, new HashSet<>());

        Training training = new Training(
                1L, 1L, trainer.getId(), "Workout", 1L,
                LocalDate.of(2025, 10, 10), 1.0
        );

        when(trainerService.findTrainerByUsername("John.Doe"))
                .thenReturn(Optional.of(trainer));
        when(trainingRepository.findTrainingsByTrainerId(trainer.getId()))
                .thenReturn(List.of(training));

        // When
        List<Training> result = trainingService.findTrainingsForTrainerByCriteria(
                "John.Doe", null, null, "   "
        );

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void shouldIncludeTrainingWhenTraineeNameMatches() throws ServiceException {
        // Given
        User trainerUser = new User(1L, "John", "Doe", "John.Doe", "pass", true);
        User traineeUser = new User(2L, "Jane", "Smith", "Jane.Smith", "pass", true);

        Trainer trainer = new Trainer(10L, trainerUser, "Fitness", true, new HashSet<>());

        Training training = new Training(
                1L, 1L, trainer.getId(), "Workout", 1L,
                LocalDate.of(2025, 10, 10), 1.0
        );

        when(trainerService.findTrainerByUsername("John.Doe"))
                .thenReturn(Optional.of(trainer));
        when(trainingRepository.findTrainingsByTrainerId(trainer.getId()))
                .thenReturn(List.of(training));
        when(userRepository.findById(training.getTrainerId()))
                .thenReturn(Optional.of(traineeUser));

        // When
        List<Training> result = trainingService.findTrainingsForTrainerByCriteria(
                "John.Doe", null, null, "Jane.Smith"
        );

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void shouldExcludeTrainingWhenTraineeUserNotFound() throws ServiceException {
        // Given
        User trainerUser = new User(1L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(10L, trainerUser, "Fitness", true, new HashSet<>());

        Training training = new Training(
                1L, 1L, trainer.getId(), "Workout", 1L,
                LocalDate.of(2025, 10, 10), 1.0
        );

        when(trainerService.findTrainerByUsername("John.Doe"))
                .thenReturn(Optional.of(trainer));
        when(trainingRepository.findTrainingsByTrainerId(trainer.getId()))
                .thenReturn(List.of(training));
        when(userRepository.findById(training.getTrainerId()))
                .thenReturn(Optional.empty());

        // When
        List<Training> result = trainingService.findTrainingsForTrainerByCriteria(
                "John.Doe", null, null, "trainee1"
        );

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTrainerUsernameIsNull() {
        //Given

        // When
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.findTrainingsForTrainerByCriteria(
                        null, null, null, null
                )
        );

        // Then
        assertEquals("Argument is null ", ex.getMessage());
    }

    @Test
    void shouldMapTrainingToTrainingResponseWhenValidArgs() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Doe", "trainee1", "pass", true);
        Trainee trainee = new Trainee(10L, user, "Addr", LocalDate.of(1990, 1, 1), true);
        Training training = new Training(1L, trainee.getId(), 20L, "Workout", 5L,
                LocalDate.of(2025, 10, 10), 1.5);
        TrainingType trainingType = new TrainingType(5L, "Fitness");

        when(traineeService.findById(trainee.getId())).thenReturn(Optional.of(trainee));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(trainingTypeRepository.findById(5L)).thenReturn(Optional.of(trainingType));

        // When
        List<TrainingResponse> responses = trainingService.getTrainingResponseList(List.of(training));

        // Then
        assertEquals(1, responses.size());
        TrainingResponse response = responses.get(0);
        assertEquals("Workout", response.getName());
        assertEquals("John Doe", response.getTraineeName());
        assertEquals("Fitness", response.getTrainingType());
        assertEquals(training.getDuration(), response.getDuration());
        assertEquals(training.getTrainingDate(), response.getDate());
    }

    @Test
    void shouldReturnUnknownTraineeWhenTraineeNotFound() throws ServiceException {
        // Given
        Training training = new Training(1L, 99L, 20L, "Workout", 5L,
                LocalDate.of(2025, 10, 10), 1.5);
        TrainingType trainingType = new TrainingType(5L, "Fitness");

        when(traineeService.findById(99L)).thenReturn(Optional.empty());
        when(trainingTypeRepository.findById(5L)).thenReturn(Optional.of(trainingType));

        // When
        List<TrainingResponse> responses = trainingService.getTrainingResponseList(List.of(training));

        // Then
        assertEquals(1, responses.size());
        assertEquals("Unknown Trainee", responses.get(0).getTraineeName());
    }

    @Test
    void shouldReturnUnknownTrainingTypeWhenTypeNotFound() throws ServiceException {
        // Given
        User user = new User(1L, "John", "Doe", "trainee1", "pass", true);
        Trainee trainee = new Trainee(10L, user, "Addr", LocalDate.of(1990, 1, 1), true);
        Training training = new Training(1L, trainee.getId(), 20L, "Workout", 99L,
                LocalDate.of(2025, 10, 10), 1.5);

        when(traineeService.findById(trainee.getId())).thenReturn(Optional.of(trainee));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(trainingTypeRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        List<TrainingResponse> responses = trainingService.getTrainingResponseList(List.of(training));

        // Then
        assertEquals(1, responses.size());
        assertEquals("Unknown Training Type", responses.get(0).getTrainingType());
    }

    @Test
    void shouldFilterOutTrainingWhenServiceExceptionThrown() throws ServiceException {
        // Given
        Training training = new Training(1L, 10L, 20L, "Workout", 5L,
                LocalDate.of(2025, 10, 10), 1.5);

        when(traineeService.findById(10L)).thenThrow(new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "oops"));

        // When
        List<TrainingResponse> responses = trainingService.getTrainingResponseList(List.of(training));

        // Then
        assertTrue(responses.isEmpty());
    }

    @Test
    void shouldThrowWhenTraineeNotFoundOnUpdateTrainingsByTrainee() throws ServiceException {
        // Given
        String username = "missing.trainee";
        when(traineeService.findTraineeByUsername(username)).thenReturn(Optional.empty());

        // When
        ServiceException ex = assertThrows(ServiceException.class,
                () -> trainingService.updateTrainingsByTrainee(username, new HashSet<>()));

        // Then
        assertEquals("Trainee not found for username: " + username, ex.getMessage());
    }

    @Test
    void shouldThrowWhenTrainerHasNoTrainings() throws ServiceException {
        // Given
        String username = "trainee1";
        Trainee trainee = new Trainee(1L, new User(10L, "John", "Doe", "trainee1", "pass", true), "Addr", LocalDate.of(1990, 1, 1), true);
        Trainer trainer = new Trainer(2L, new User(20L, "Mike", "Trainer", "trainer1", "pass", true), "Fitness", true, new HashSet<>());

        when(traineeService.findTraineeByUsername(username)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeId(trainee.getId())).thenReturn(List.of());
        when(trainingRepository.findTrainingsByTrainerId(trainer.getId())).thenReturn(List.of());

        // When
        ServiceException ex = assertThrows(ServiceException.class,
                () -> trainingService.updateTrainingsByTrainee(username, Set.of(trainer)));

        // Then
        assertEquals("Assign trainer to a training", ex.getMessage());
    }

    @Test
    void shouldNotSaveDuplicateTrainingIfAlreadyAssigned() throws ServiceException {
        // Given
        String username = "trainee1";
        Trainee trainee = new Trainee(1L, new User(10L, "John", "Doe", "trainee1", "pass", true), "Addr", LocalDate.of(1990, 1, 1), true);
        Trainer trainer = new Trainer(2L, new User(20L, "Mike", "Trainer", "trainer1", "pass", true), "Fitness", true, new HashSet<>());

        Training existingTraining = new Training(100L, trainee.getId(), trainer.getId(), "Workout", 5L, LocalDate.of(2025, 10, 10), 1.5);

        when(traineeService.findTraineeByUsername(username)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeId(trainee.getId())).thenReturn(List.of(existingTraining));
        when(trainingRepository.findTrainingsByTrainerId(trainer.getId())).thenReturn(List.of(existingTraining));

        // When
        trainingService.updateTrainingsByTrainee(username, Set.of(trainer));

        // Then
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void shouldSaveNewTrainingIfNotAlreadyAssigned() throws ServiceException {
        // Given
        String username = "trainee1";
        Trainee trainee = new Trainee(1L, new User(10L, "John", "Doe", "trainee1", "pass", true), "Addr", LocalDate.of(1990, 1, 1), true);
        Trainer trainer = new Trainer(2L, new User(20L, "Mike", "Trainer", "trainer1", "pass", true), "Fitness", true, new HashSet<>());

        Training traineeTraining = new Training(100L, trainee.getId(), trainer.getId(), "Workout", 5L, LocalDate.of(2025, 10, 10), 1.5);
        Training trainerTraining = new Training(101L, 999L, trainer.getId(), "Yoga", 6L, LocalDate.of(2025, 10, 15), 2.0);

        when(traineeService.findTraineeByUsername(username)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeId(trainee.getId())).thenReturn(List.of(traineeTraining));
        when(trainingRepository.findTrainingsByTrainerId(trainer.getId())).thenReturn(List.of(trainerTraining));

        // When
        trainingService.updateTrainingsByTrainee(username, Set.of(trainer));

        // Then
        verify(trainingRepository, times(1)).save(any());
    }

    @Test
    void shouldHandleMultipleTrainersCorrectly() throws ServiceException {
        // Given
        String username = "trainee1";
        Trainee trainee = new Trainee(1L, new User(10L, "John", "Doe", "trainee1", "pass", true), "Addr", LocalDate.of(1990, 1, 1), true);

        Trainer trainer1 = new Trainer(2L, new User(20L, "Mike", "Trainer", "trainer1", "pass", true), "Fitness", true, new HashSet<>());
        Trainer trainer2 = new Trainer(3L, new User(30L, "Anna", "Coach", "trainer2", "pass", true), "Yoga", true, new HashSet<>());

        Training trainer1Training = new Training(101L, 999L, trainer1.getId(), "Yoga", 6L, LocalDate.of(2025, 10, 15), 2.0);
        Training trainer2Training = new Training(102L, 999L, trainer2.getId(), "Pilates", 7L, LocalDate.of(2025, 10, 20), 1.0);

        when(traineeService.findTraineeByUsername(username)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findTrainingsByTraineeId(trainee.getId())).thenReturn(List.of());
        when(trainingRepository.findTrainingsByTrainerId(trainer1.getId())).thenReturn(List.of(trainer1Training));
        when(trainingRepository.findTrainingsByTrainerId(trainer2.getId())).thenReturn(List.of(trainer2Training));

        // When
        trainingService.updateTrainingsByTrainee(username, Set.of(trainer1, trainer2));

        // Then
        verify(trainingRepository, times(2)).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTrainerIdIsNull() {
        // Given
        Long trainerId = null;

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> trainingService.getTotalDurationForTrainer(trainerId));

        // Then
        assertEquals("Training.trainerId is required", ex.getMessage());
    }

    @Test
    void shouldReturnSumOfDurationsForTrainerWhenGetTotalDuration() {
        // Given
        Long trainerId = 2L;

        Training t1 = new Training(1L, 1L, trainerId, "Workout", 2L, LocalDate.of(2025, 10, 1), 1.5);
        Training t2 = new Training(2L, 3L, trainerId, "Yoga", 3L, LocalDate.of(2025, 10, 2), 2.0);
        Training t3 = new Training(3L, 4L, trainerId, "Pilates", 4L, LocalDate.of(2025, 10, 3), 1.0);

        when(trainingRepository.findTrainingsByTrainerId(trainerId)).thenReturn(List.of(t1, t2, t3));

        // When
        double totalDuration = trainingService.getTotalDurationForTrainer(trainerId);

        // Then
        verify(trainingRepository).findTrainingsByTrainerId(trainerId);
        assertEquals(4.5, totalDuration);
    }

    @Test
    void shouldReturnZeroWhenTrainerHasNoTrainings() {
        // Given
        Long trainerId = 5L;
        when(trainingRepository.findTrainingsByTrainerId(trainerId)).thenReturn(List.of());

        // When
        double totalDuration = trainingService.getTotalDurationForTrainer(trainerId);

        // Then
        verify(trainingRepository).findTrainingsByTrainerId(trainerId);
        assertEquals(0.0, totalDuration);
    }

    @Test
    void shouldThrowServiceExceptionWhenTraineeNotFound() {
        // Given
        String missingUsername = "missing.trainee";
        when(traineeService.findTraineeByUsername(missingUsername)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(ServiceException.class, () ->
                trainingService.findTrainingsForTraineeByCriteria(
                        missingUsername,
                        null,
                        null,
                        null,
                        null
                )
        );

        // Then
        verify(traineeService).findTraineeByUsername(missingUsername);
        verifyNoInteractions(trainingRepository); // repo shouldn't be called
        assertEquals("Trainee not found for username: " + missingUsername, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTrainingIsNull() {
        // Given

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                trainingService.save(null)
        );

        // Then
        verifyNoInteractions(trainingRepository);
        assertEquals("Argument is null ", exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenTrainingNameIsBlank() {
        // Given
        Training training = new Training();
        training.setTraineeId(1L);
        training.setTrainerId(2L);
        training.setName(""); // blank name
        training.setTrainingTypeId(1L);
        training.setTrainingDate(LocalDate.now());
        training.setDuration(1.0);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () ->
                trainingService.save(training)
        );

        // Then
        verifyNoInteractions(trainingRepository);
        assertEquals("Training.name is required", exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenTrainingTypeIdIsNull() {
        // Given
        Training training = new Training();
        training.setTraineeId(1L);
        training.setTrainerId(2L);
        training.setName("Workout");
        training.setTrainingTypeId(null);
        training.setTrainingDate(LocalDate.now());
        training.setDuration(1.0);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () ->
                trainingService.save(training)
        );

        // Then
        verifyNoInteractions(trainingRepository);
        assertEquals("Training.trainingTypeId is required", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTrainingIsNullOnUpdate() {
        // Given

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                trainingService.update((Training) null)
        );

        // Then
        verifyNoInteractions(trainingRepository);
        assertEquals("Argument is null ", exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenTrainingIdIsNullOnUpdate() {
        // Given
        Training training = new Training();
        training.setTraineeId(1L);
        training.setTrainerId(2L);
        training.setName("Workout");
        training.setTrainingTypeId(1L);
        training.setTrainingDate(LocalDate.now());
        training.setDuration(1.0);
        training.setId(null);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () ->
                trainingService.update(training)
        );

        // Then
        verifyNoInteractions(trainingRepository);
        assertEquals("Training.id is required for update", exception.getMessage());
    }

    @Test
    void shouldUpdateTrainingWhenAllFieldsAreValid() throws ServiceException {
        // Given
        Training training = new Training();
        training.setId(1L);
        training.setTraineeId(1L);
        training.setTrainerId(2L);
        training.setName("Workout");
        training.setTrainingTypeId(1L);
        training.setTrainingDate(LocalDate.now());
        training.setDuration(1.0);

        when(trainingRepository.save(training)).thenReturn(training);

        // When
        trainingService.update(training);

        // Then
        verify(trainingRepository).save(training);
    }
}
