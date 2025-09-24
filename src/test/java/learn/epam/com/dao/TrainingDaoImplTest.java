package learn.epam.com.dao;

import learn.epam.com.dao.impl.TrainingDaoImpl;
import learn.epam.com.entity.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TrainingDaoImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_TYPE = "IllegalArgumentException was expected";

    private Map<Long, Training> storage;
    private TrainingDaoImpl trainingDao;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainingDao = new TrainingDaoImpl(storage);
    }

    @Test
    void shouldReturnTrainingWhenGetByIdCalled() {
        // Given
        LocalDate date = LocalDate.parse("2025-10-01");
        Training training = new Training(1L, 1L, 2L, "Workout", "Power Lifting", date, 1.5);
        storage.put(1L, training);

        // When
        Optional<Training> result = trainingDao.getById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    void shouldReturnEmptyWhenTrainingDoesNotExists() {
        // Given: empty storage

        // When
        Optional<Training> result = trainingDao.getById(99L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrainingListWhenGetAllCalled() {
        // Given
        LocalDate date = LocalDate.parse("2025-10-01");
        LocalDate date2 = LocalDate.parse("2025-10-02");
        Training training1 = new Training(1L, 1L, 2L, "Workout", "Power Lifting", date, 1.5);
        Training training2 = new Training(2L, 2L, 3L, "Fitness", "Body", date2, 2.0);
        storage.put(1L, training1);
        storage.put(2L, training2);

        // When
        List<Training> result = trainingDao.getAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(training1));
        assertTrue(result.contains(training2));
    }

    @Test
    void shouldGenerateIdWhenNull() {
        // Given
        LocalDate date = LocalDate.parse("2025-10-01");
        Training training = new Training(null, 1L, 2L, "Workout", "Power Lifting", date, 1.5);

        // When
        trainingDao.save(training);

        // Then
        assertNotNull(training.getId());
        assertTrue(storage.containsKey(training.getId()));
        assertEquals(training, storage.get(training.getId()));
    }

    @Test
    void shouldUseExistingIdWhenPresent() {
        // Given
        LocalDate date = LocalDate.parse("2025-10-01");
        Training training = new Training(10L, 1L, 2L, "Workout", "Power Lifting", date, 1.5);

        // When
        trainingDao.save(training);

        // Then
        assertEquals(training, storage.get(10L));
    }

    @Test
    void shouldReplaceExistingTrainingWhenUpdateCalled() {
        // Given
        LocalDate date = LocalDate.parse("2025-10-01");
        Training training = new Training(1L, 1L, 2L, "Workout", "Power Lifting", date, 1.5);
        storage.put(1L, training);

        LocalDate date2 = LocalDate.parse("2025-10-02");
        Training updated = new Training(1L, 2L, 3L, "Fitness", "Body", date2, 2.0);

        // When
        trainingDao.update(updated);

        // Then
        assertEquals(updated, storage.get(1L));
    }

    @Test
    void shouldRemoveTrainingWhenDeleteCalled() {
        // Given
        LocalDate date = LocalDate.parse("2025-10-01");
        Training training = new Training(1L, 1L, 2L, "Workout", "Power Lifting", date, 1.5);
        storage.put(1L, training);

        // When
        trainingDao.delete(training);

        // Then
        assertFalse(storage.containsKey(1L));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenTrainingIsNull() {
        // Given: null training

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainingDao.save(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenTrainingIsNull() {
        // Given: null training

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainingDao.update(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }


    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenTrainingIsNull() {
        // Given: null training

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainingDao.delete(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
