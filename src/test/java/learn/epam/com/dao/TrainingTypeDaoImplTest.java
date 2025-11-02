package learn.epam.com.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import learn.epam.com.entity.TrainingType;
import learn.epam.com.main.GymCrmSystemApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = GymCrmSystemApplication.class)
@ActiveProfiles("test")
@EntityScan(basePackages = "learn.epam.com.entity")
@Transactional
public class TrainingTypeDaoImplTest {
    private static final String TRAINING_TYPE_CANNOT_BE_MODIFIED = "Training types are constant and cannot be modified.";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Test
    void shouldReturnTrainingTypeWhenExists() {
        // Given
        TrainingType strength = new TrainingType(null, "Strength");
        TrainingType cardio = new TrainingType(null, "Cardio");
        entityManager.persist(strength);
        entityManager.persist(cardio);
        entityManager.flush();

        // When
        Optional<TrainingType> found = trainingTypeDao.getById(strength.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Strength", found.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenNotExists() {
        // Given: training type does not exist

        // When
        Optional<TrainingType> found = trainingTypeDao.getById(999L);

        // Then
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldReturnAllTrainingTypesWhenGetAllCalled() {
        //Given
        TrainingType strength = new TrainingType(null, "Strength");
        TrainingType cardio = new TrainingType(null, "Cardio");
        entityManager.persist(strength);
        entityManager.persist(cardio);
        entityManager.flush();

        //When
        List<TrainingType> result = trainingTypeDao.getAll();

        //Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> "Strength".equals(t.getName())));
        assertTrue(result.stream().anyMatch(t -> "Cardio".equals(t.getName())));
    }

    @Test
    void saveShouldThrowExceptionOnSave() {
        // Given
        TrainingType type = new TrainingType(null, "Yoga");

        // When
        RuntimeException exception = assertThrows(RuntimeException.class, () -> trainingTypeDao.save(type));

        // Then
        assertEquals(TRAINING_TYPE_CANNOT_BE_MODIFIED, exception.getMessage());
    }

    @Test
    void updateShouldThrowExceptionOnUpdate() {
        // Given
        TrainingType strength = new TrainingType(null, "Strength");
        TrainingType updated = new TrainingType(null, "Updated Strength");
        entityManager.persist(strength);
        entityManager.flush();

        //When
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> trainingTypeDao.update(updated));

        // Then
        assertEquals(TRAINING_TYPE_CANNOT_BE_MODIFIED, runtimeException.getMessage());
    }

    @Test
    void deleteShouldThrowExceptionOnDelete() {
        // Given
        TrainingType type = new TrainingType(null, "Strength");
        entityManager.persist(type);
        entityManager.flush();

        // When
        RuntimeException exception = assertThrows(RuntimeException.class, () -> trainingTypeDao.delete(type));

        // Then
        assertEquals(TRAINING_TYPE_CANNOT_BE_MODIFIED, exception.getMessage());
    }
}
