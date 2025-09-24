package learn.epam.com.dao;

import learn.epam.com.dao.impl.TrainerDaoImpl;
import learn.epam.com.entity.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TrainerDaoImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_TYPE = "IllegalArgumentException was expected";

    private Map<Long, Trainer> storage;
    private TrainerDaoImpl trainerDao;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainerDao = new TrainerDaoImpl(storage);
    }

    @Test
    void shouldReturnTrainerWhenGetByIdCalled() {
        // Given
        Trainer trainer = new Trainer(null, 101L, "Coach");
        storage.put(1L, trainer);

        // When
        Optional<Trainer> result = trainerDao.getById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldReturnEmptyWhenTrainerDoesNotExists() {
        // Given: empty storage

        // When
        Optional<Trainer> result = trainerDao.getById(99L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrainersListWhenGetAllCalled() {
        // Given
        Trainer trainer1 = new Trainer(null, 101L, "Strength and Conditioning Coach");
        Trainer trainer2 = new Trainer(null, 102L, "Yoga Instructor");
        storage.put(1L, trainer1);
        storage.put(2L, trainer2);

        // When
        List<Trainer> result = trainerDao.getAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(trainer1));
        assertTrue(result.contains(trainer2));
    }

    @Test
    void shouldGenerateIdWhenNull() {
        // Given
        Trainer trainer = new Trainer(null, 101L, "Fitness Trainer");

        // When
        trainerDao.save(trainer);

        // Then
        assertNotNull(trainer.getId());
        assertTrue(storage.containsKey(trainer.getId()));
        assertEquals(trainer, storage.get(trainer.getId()));
    }

    @Test
    void shouldUseExistingIdWhenPresent() {
        // Given
        Trainer trainer = new Trainer(1L, 101L, "Nutrition Coach");

        // When
        trainerDao.save(trainer);

        // Then
        assertEquals(trainer, storage.get(1L));
    }

    @Test
    void shouldReplaceExistingTrainerWhenUpdateCalled() {
        // Given
        Trainer trainer = new Trainer(1L, 101L, "Running Coach");
        storage.put(1L, trainer);

        Trainer updated = new Trainer(1L, 202L, "Yoga Instructor");

        // When
        trainerDao.update(updated);

        // Then
        assertEquals(updated, storage.get(1L));
    }

    @Test
    void shouldRemoveTrainerWhenDeleteCalled() {
        // Given
        Trainer trainer = new Trainer(1L, 101L, "Fitness Instructor");
        storage.put(1L, trainer);

        // When
        trainerDao.delete(trainer);

        // Then
        assertFalse(storage.containsKey(1L));
    }

    @Test
    void shouldReturnUserIdWhenGetUserIdCalled() {
        // Given
        Trainer trainee = new Trainer(1L, 101L, "Strength and Conditioning Coach");

        // When
        Long result = trainerDao.getUserId(trainee);

        // Then
        assertEquals(101L, result);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenTrainerIsNull() {
        // Given: null trainer

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerDao.save(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenTrainerIsNull() {
        // Given: null trainer

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerDao.update(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnGetUserIdWhenTrainerIsNull() {
        // Given: null trainer

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerDao.getUserId(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenTrainerIsNull() {
        // Given: null trainer

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerDao.delete(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
