package learn.epam.com.dao;

import learn.epam.com.dao.impl.TraineeDaoImpl;
import learn.epam.com.entity.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TraineeDaoImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_TYPE = "IllegalArgumentException was expected";

    private Map<Long, Trainee> storage;
    private TraineeDaoImpl traineeDao;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        traineeDao = new TraineeDaoImpl(storage);
    }

    @Test
    void shouldReturnTraineeWhenGetByIdCalled() {
        // Given
        Trainee trainee = new Trainee(1L, 101L, "Almaty", LocalDate.of(2000, 1, 1));
        storage.put(1L, trainee);

        // When
        Optional<Trainee> result = traineeDao.getById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void shouldReturnEmptyWhenTraineeDoesNotExists() {
        // Given: empty storage

        // When
        Optional<Trainee> result = traineeDao.getById(99L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTraineesListWhenGetAllCalled() {
        // Given
        Trainee trainee1 = new Trainee(1L, 101L, "Almaty", LocalDate.of(2000, 1, 15));
        Trainee trainee2 = new Trainee(2L, 102L, "Astana", LocalDate.of(2001, 2, 21));
        storage.put(1L, trainee1);
        storage.put(2L, trainee2);

        // When
        List<Trainee> result = traineeDao.getAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(trainee1));
        assertTrue(result.contains(trainee2));
    }

    @Test
    void shouldGenerateIdWhenNull() {
        // Given
        Trainee trainee = new Trainee(null, 101L, "Shymkent", LocalDate.of(1999, 3, 3));

        // When
        traineeDao.save(trainee);

        // Then
        assertNotNull(trainee.getId());
        assertTrue(storage.containsKey(trainee.getId()));
        assertEquals(trainee, storage.get(trainee.getId()));
    }

    @Test
    void shouldUseExistingIdWhenPresent() {
        // Given
        Trainee trainee = new Trainee(10L, 101L, "Kokshetau", LocalDate.of(1995, 5, 5));

        // When
        traineeDao.save(trainee);

        // Then
        assertEquals(trainee, storage.get(10L));
    }

    @Test
    void shouldReplaceExistingTraineeWhenUpdateCalled() {
        // Given
        Trainee trainee = new Trainee(1L, 101L, "Almaty", LocalDate.of(2000, 1, 1));
        storage.put(1L, trainee);

        Trainee updated = new Trainee(1L, 202L, "Astana", LocalDate.of(1998, 8, 8));

        // When
        traineeDao.update(updated);

        // Then
        assertEquals(updated, storage.get(1L));
    }

    @Test
    void shouldRemoveTraineeWhenDeleteCalled() {
        // Given
        Trainee trainee = new Trainee(1L, 101L, "Almaty", LocalDate.of(2000, 1, 1));
        storage.put(1L, trainee);

        // When
        traineeDao.delete(trainee);

        // Then
        assertFalse(storage.containsKey(1L));
    }

    @Test
    void shouldReturnUserIdWhenGetUserIdCalled() {
        // Given
        Trainee trainee = new Trainee(1L, 101L, "Almaty", LocalDate.of(2000, 1, 1));

        // When
        Long result = traineeDao.getUserId(trainee);

        // Then
        assertEquals(101L, result);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenTraineeIsNull() {
        // Given: null trainee

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeDao.save(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenTraineeIsNull() {
        // Given: null trainee

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeDao.update(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnGetUserIdWhenTraineeIsNull() {
        // Given: null trainee

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeDao.getUserId(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenTraineeIsNull() {
        // Given: null trainee

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeDao.delete(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
