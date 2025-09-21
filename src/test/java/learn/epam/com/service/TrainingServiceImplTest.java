package learn.epam.com.service;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TrainingDao;
import learn.epam.com.entity.Training;
import learn.epam.com.service.impl.TrainingServiceImpl;
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
public class TrainingServiceImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String DATABASE_ERROR = "Database error";
    private static final String FAIL_SAVE_TRAINING = "Failed to save training";
    private static final String FAIL_UPDATE_TRAINING = "Failed to update training";
    private static final String FAIL_DELETE_TRAINING = "Failed to delete training";
    private static final String FAIL_GET_BY_ID_TRAINING = "Failed to get training by id";
    private static final String FAIL_GET_ALL_TRAINING = "Failed to get all trainings";

    @Mock
    private TrainingDao trainingDao;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Training training;

    @BeforeEach
    void setUp() {
        training = new Training(1L, 1L, 2L, "Workout", "Power Lifting", LocalDate.of(2025, 10, 1), 1.5);
    }

    @Test
    void shouldSaveTrainingWhenValid() throws Exception {
        // Given
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
    void shouldThrowServiceExceptionWhenSaveFails() throws Exception {
        // Given
        doThrow(new DaoException(DATABASE_ERROR)).when(trainingDao).save(training);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainingService.save(training));

        // Then
        verify(trainingDao).save(training);
        assertEquals(FAIL_SAVE_TRAINING, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenUpdateFails() throws Exception {
        // Given
        doThrow(new DaoException(DATABASE_ERROR)).when(trainingDao).update(training);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainingService.update(training));

        // Then
        verify(trainingDao).update(training);
        assertEquals(FAIL_UPDATE_TRAINING, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenDeleteFails() throws Exception {
        // Given
        doThrow(new DaoException(DATABASE_ERROR)).when(trainingDao).delete(training);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainingService.delete(training));

        // Then
        verify(trainingDao).delete(training);
        assertEquals(FAIL_DELETE_TRAINING, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFindAllTrainingsFails() throws Exception {
        // Given
        doThrow(new DaoException(DATABASE_ERROR)).when(trainingDao).getAll();

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainingService.findAllTrainings());

        // Then
        verify(trainingDao).getAll();
        assertEquals(FAIL_GET_ALL_TRAINING, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFindByIdDaoFails() throws Exception {
        // Given
        when(trainingDao.getById(1L)).thenThrow(new DaoException(DATABASE_ERROR));

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainingService.findById(1L));

        // Then
        verify(trainingDao).getById(1L);
        assertEquals(FAIL_GET_BY_ID_TRAINING, exception.getMessage());
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
