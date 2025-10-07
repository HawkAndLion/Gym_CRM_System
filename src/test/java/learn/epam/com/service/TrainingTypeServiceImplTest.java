package learn.epam.com.service;

import learn.epam.com.dao.TrainingTypeDao;
import learn.epam.com.entity.TrainingType;
import learn.epam.com.service.impl.TrainingTypeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TrainingTypeServiceImplTest {
    private static final String INVALID_TRAINING_TYPE = "Invalid training type";
    private static final String NULL_EXCEPTION = "Argument is null ";

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingTypeServiceImpl trainingTypeService;

    @Test
    void shouldSaveTrainingTypeWhenValid() throws ServiceException {
        // Given
        TrainingType trainingType = new TrainingType(1L, "Cardio");

        // When
        trainingTypeService.save(trainingType);

        // Then
        verify(trainingTypeDao).save(trainingType);
        assertDoesNotThrow(() -> new ServiceException(INVALID_TRAINING_TYPE));
    }

    @Test
    void shouldReturnTrainingTypeWhenFindById() throws ServiceException {
        // Given
        TrainingType trainingType = new TrainingType(1L, "Running");
        when(trainingTypeDao.getById(1L)).thenReturn(Optional.of(trainingType));

        // When
        Optional<TrainingType> result = trainingTypeService.findById(1L);

        // Then
        verify(trainingTypeDao).getById(1L);
        assertTrue(result.isPresent());
        assertEquals("Running", result.get().getName());
    }

    @Test
    void shouldUpdateTrainingTypeWhenValid() throws ServiceException {
        // Given
        TrainingType trainingType = new TrainingType(1L, "Cardio");

        // When
        trainingTypeService.update(trainingType);

        // Then
        verify(trainingTypeDao).update(trainingType);
        assertDoesNotThrow(() -> new ServiceException(INVALID_TRAINING_TYPE));
    }

    @Test
    void shouldDeleteTrainingTypeWhenValid() throws ServiceException {
        // Given
        TrainingType trainingType = new TrainingType(1L, "Cardio");

        // When
        trainingTypeService.delete(trainingType);

        // Then
        verify(trainingTypeDao).delete(trainingType);
    }

    @Test
    void shouldReturnAllTrainingTypes() {
        // Given
        List<TrainingType> list = List.of(
                new TrainingType(1L, "Running"),
                new TrainingType(2L, "Boxing")
        );

        when(trainingTypeDao.getAll()).thenReturn(list);

        // When
        List<TrainingType> result = trainingTypeService.findAllTrainingTypes();

        // Then
        verify(trainingTypeDao).getAll();
        assertEquals(2, result.size());
    }


    @Test
    void shouldThrowExceptionWhenSavingNullTrainingType() {
        // Given: null training type

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.save(null));

        // Then
        verifyNoInteractions(trainingTypeDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNullTrainingType() {
        // Given: null training type

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.update(null));

        // Then
        verifyNoInteractions(trainingTypeDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDeleteNullTrainingType() {
        // Given: null training type

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.delete(null));

        // Then
        verifyNoInteractions(trainingTypeDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
