package learn.epam.com.service;

import learn.epam.com.entity.TrainingType;
import learn.epam.com.repository.TrainingTypeRepository;
import learn.epam.com.service.impl.TrainingTypeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TrainingTypeServiceImplTest {
    private static final String TRAINING_TYPE_NOT_FOUND = "Training type not found: ";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String BLANK = "  ";

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeServiceImpl trainingTypeService;

    @Test
    void shouldReturnTrainingTypeWhenFindById() throws ServiceException {
        // Given
        TrainingType trainingType = new TrainingType(1L, "Running");
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(trainingType));

        // When
        Optional<TrainingType> result = trainingTypeService.findById(1L);

        // Then
        verify(trainingTypeRepository).findById(1L);
        assertTrue(result.isPresent());
        assertEquals("Running", result.get().getName());
    }

    @Test
    void shouldReturnAllTrainingTypesWhenFindAll() {
        // Given
        List<TrainingType> list = List.of(
                new TrainingType(1L, "Running"),
                new TrainingType(2L, "Boxing")
        );

        when(trainingTypeRepository.findAll()).thenReturn(list);

        // When
        List<TrainingType> result = trainingTypeService.findAllTrainingTypes();

        // Then
        verify(trainingTypeRepository).findAll();
        assertEquals(2, result.size());
    }

    @Test
    void shouldGetTrainingTypeIdWhenTrainingTypeExists() throws ServiceException {
        // Given
        List<TrainingType> list = List.of(
                new TrainingType(1L, "Running")
        );
        when(trainingTypeRepository.findAll()).thenReturn(list);

        // When
        Long result = trainingTypeService.getTrainingTypeId("Running");

        // Then
        verify(trainingTypeRepository).findAll();
        assertEquals(list.get(0).getId(), result);
    }

    @Test
    void shouldGetTrainingTypesWhenMethodCalled() {
        // Given
        List<TrainingType> list = List.of(
                new TrainingType(1L, "Yoga"),
                new TrainingType(2L, "Running")
        );
        when(trainingTypeRepository.findAll()).thenReturn(list);

        // When
        List<Map<String, Object>> result = trainingTypeService.getTrainingTypes();

        // Then
        verify(trainingTypeRepository).findAll();
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).get("id"));
        assertEquals("Yoga", result.get(0).get("name"));
        assertEquals(2L, result.get(1).get("id"));
        assertEquals("Running", result.get(1).get("name"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFindByIdCalledWithNull() {
        // Given

        // When
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trainingTypeService.findById(null)
        );

        // Then
        verifyNoInteractions(trainingTypeRepository);
        assertEquals(NULL_EXCEPTION, ex.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGetTrainingTypeIdCalledWithNull() throws ServiceException {
        // Given

        // When
        Long result = trainingTypeService.getTrainingTypeId(null);

        // Then
        verifyNoInteractions(trainingTypeRepository);
        assertNull(result);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenGetTrainingTypeIdCalledWithBlank() throws ServiceException {
        // Given

        // When
        Long result = trainingTypeService.getTrainingTypeId(BLANK);

        // Then
        assertNull(result);
        verifyNoInteractions(trainingTypeRepository);
    }

    @Test
    void shouldThrowServiceExceptionWhenTrainingTypeNotFound() {
        // Given
        when(trainingTypeRepository.findAll()).thenReturn(List.of(
                new TrainingType(1L, "Running"),
                new TrainingType(2L, "Boxing")
        ));

        // When
        ServiceException ex = assertThrows(
                ServiceException.class,
                () -> trainingTypeService.getTrainingTypeId("Yoga")
        );

        // Then
        verify(trainingTypeRepository).findAll();
        assertEquals(TRAINING_TYPE_NOT_FOUND + "Yoga", ex.getMessage());
    }

    @Test
    void shouldReturnEmptyOptionalWhenTrainingTypeNotFoundById() throws ServiceException {
        // Given
        when(trainingTypeRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<TrainingType> result = trainingTypeService.findById(999L);

        // Then
        assertTrue(result.isEmpty());
        verify(trainingTypeRepository).findById(999L);
    }
}
