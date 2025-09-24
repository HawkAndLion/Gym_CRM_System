package learn.epam.com.service;

import learn.epam.com.dao.TrainingDao;
import learn.epam.com.entity.Training;
import learn.epam.com.service.impl.TrainingServiceImpl;
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
public class TrainingServiceImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String FAIL_SAVE_TRAINING = "Failed to save training";
    private static final String FAIL_UPDATE_TRAINING = "Failed to update training";
    private static final String FAIL_DELETE_TRAINING = "Failed to delete training";

    @Mock
    private TrainingDao trainingDao;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Test
    void shouldSaveTrainingWhenValid() throws Exception {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", "Power Lifting", LocalDate.of(2025, 10, 1), 1.5);
        doNothing().when(trainingDao).save(training);

        // When
        trainingService.save(training);

        // Then
        verify(trainingDao).save(training);
        assertDoesNotThrow(() -> new ServiceException(FAIL_SAVE_TRAINING));
    }

    @Test
    void shouldReturnTrainingByIdWhenExists() throws Exception {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", "Power Lifting", LocalDate.of(2025, 10, 1), 1.5);
        when(trainingDao.getById(1L)).thenReturn(Optional.of(training));

        // When
        Optional<Training> result = trainingService.findById(1L);

        // Then
        verify(trainingDao).getById(1L);
        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    void shouldUpdateWhenTrainingIsValid() throws Exception {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", "Power Lifting", LocalDate.of(2025, 10, 1), 1.5);
        doNothing().when(trainingDao).update(training);

        // When
        trainingService.update(training);

        // Then
        verify(trainingDao).update(training);
        assertDoesNotThrow(() -> new ServiceException(FAIL_UPDATE_TRAINING));
    }

    @Test
    void shouldRemoveTrainingWhenDeleteIsCalled() throws Exception {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", "Power Lifting", LocalDate.of(2025, 10, 1), 1.5);
        doNothing().when(trainingDao).delete(training);

        // When
        trainingService.delete(training);

        // Then
        verify(trainingDao).delete(training);
        assertDoesNotThrow(() -> new ServiceException(FAIL_DELETE_TRAINING));
    }

    @Test
    void shouldReturnTrainingListWhenFindAllIsCalled() throws Exception {
        // Given
        List<Training> trainings = new ArrayList<>();
        Training training = new Training(1L, 1L, 2L, "Workout", "Power Lifting", LocalDate.of(2025, 10, 1), 1.5);
        trainings.add(training);
        trainings.add(new Training(2L, 2L, 3L, "Fitness", "Body", LocalDate.of(2025, 10, 2), 2.0));
        when(trainingDao.getAll()).thenReturn(trainings);

        // When
        List<Training> result = trainingService.findAllTrainings();

        // Then
        verify(trainingDao).getAll();
        assertEquals(2, result.size());
        assertEquals(training, result.get(0));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenTrainingIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainingService.save(null));

        // Then
        verifyNoInteractions(trainingDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }


    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenTrainingIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainingService.update(null));

        // Then
        verifyNoInteractions(trainingDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenTrainingIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainingService.delete(null));

        //Then
        verifyNoInteractions(trainingDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
