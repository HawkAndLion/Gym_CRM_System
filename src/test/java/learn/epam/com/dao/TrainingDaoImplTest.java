package learn.epam.com.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import learn.epam.com.config.TestConfig;
import learn.epam.com.dao.impl.TrainingDaoImpl;
import learn.epam.com.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(TestConfig.class)
@Transactional
public class TrainingDaoImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_TYPE = "IllegalArgumentException was expected";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TrainingDaoImpl trainingDao;

    @Test
    void shouldReturnTrainingWhenGetByIdCalled() {
        // Given
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();
        User anotherUser = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(anotherUser);
        entityManager.flush();

        Trainee trainee = new Trainee(null, anotherUser, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();
        TrainingType type = new TrainingType(null, "Strength Training");
        entityManager.persist(type);
        entityManager.flush();
        LocalDate date = LocalDate.parse("2025-10-01");
        Training training = new Training(null, trainee.getId(), trainer.getId(), "Workout", type.getId(), date, 1.5);
        entityManager.persist(training);
        entityManager.flush();

        // When
        Optional<Training> result = trainingDao.getById(training.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    void shouldReturnEmptyWhenTrainingDoesNotExists() {
        // Given: training does not exist

        // When
        Optional<Training> result = trainingDao.getById(99L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrainingListWhenGetAllCalled() {
        // Given
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();
        User anotherUser = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(anotherUser);
        entityManager.flush();

        Trainee trainee = new Trainee(null, anotherUser, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();
        TrainingType type = new TrainingType(null, "Strength Training");
        entityManager.persist(type);
        entityManager.flush();
        LocalDate date = LocalDate.parse("2025-10-01");

        User user3 = new User(null, "Patrick", "Lee", "Patrick.Lee", "password", true);
        entityManager.persist(user3);
        entityManager.flush();

        Trainer trainer2 = new Trainer(null, user3, "Training Coach", true, new HashSet<>());
        entityManager.persist(trainer2);
        entityManager.flush();
        User user4 = new User(null, "Andy", "Right", "Andy.Right", "secret", true);
        entityManager.persist(user4);
        entityManager.flush();

        Trainee trainee2 = new Trainee(null, user4, "Wakanda", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee2);
        entityManager.flush();
        TrainingType type2 = new TrainingType(null, "Power Lifting");
        entityManager.persist(type2);
        entityManager.flush();
        LocalDate date2 = LocalDate.parse("2025-10-02");

        Training training1 = new Training(null, trainee.getId(), trainer.getId(), "Workout", type.getId(), date, 1.5);
        Training training2 = new Training(null, trainee2.getId(), trainer2.getId(), "Fitness", type2.getId(), date2, 2.0);
        entityManager.persist(training1);
        entityManager.persist(training2);
        entityManager.flush();

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
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();
        User anotherUser = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(anotherUser);
        entityManager.flush();

        Trainee trainee = new Trainee(null, anotherUser, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();
        TrainingType type = new TrainingType(null, "Strength Training");
        entityManager.persist(type);
        entityManager.flush();
        LocalDate date = LocalDate.parse("2025-10-01");
        Training training = new Training(null, trainee.getId(), trainer.getId(), "Workout", type.getId(), date, 1.5);
        entityManager.persist(training);
        entityManager.flush();

        // When
        Training actual = entityManager.find(Training.class, training.getId());

        // Then
        assertNotNull(training.getId());
        assertEquals("Workout", actual.getName());
    }

    @Test
    void shouldUseExistingIdWhenPresent() {
        // Given
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();
        User anotherUser = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(anotherUser);
        entityManager.flush();

        Trainee trainee = new Trainee(null, anotherUser, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();
        TrainingType type = new TrainingType(null, "Strength Training");
        entityManager.persist(type);
        entityManager.flush();
        LocalDate date = LocalDate.parse("2025-10-01");
        Training training = new Training(null, trainee.getId(), trainer.getId(), "Workout", type.getId(), date, 1.5);
        entityManager.persist(training);
        entityManager.flush();

        // When
        Training training1 = trainingDao.getById(training.getId()).get();

        // Then
        assertTrue(trainingDao.getById(training.getId()).isPresent());
        assertEquals("Workout", training1.getName());
    }

    @Test
    void shouldReplaceExistingTrainingWhenUpdateCalled() {
        // Given
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();
        User anotherUser = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(anotherUser);
        entityManager.flush();

        Trainee trainee = new Trainee(null, anotherUser, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();
        TrainingType type = new TrainingType(null, "Strength Training");
        entityManager.persist(type);
        entityManager.flush();
        LocalDate date = LocalDate.parse("2025-10-01");

        User user3 = new User(null, "Patrick", "Lee", "Patrick.Lee", "password", true);
        entityManager.persist(user3);
        entityManager.flush();

        Trainer trainer2 = new Trainer(null, user3, "Training Coach", true, new HashSet<>());
        entityManager.persist(trainer2);
        entityManager.flush();
        User user4 = new User(null, "Andy", "Right", "Andy.Right", "secret", true);
        entityManager.persist(user4);
        entityManager.flush();

        Trainee trainee2 = new Trainee(null, user4, "Wakanda", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee2);
        entityManager.flush();
        TrainingType type2 = new TrainingType(null, "Power Lifting");
        entityManager.persist(type2);
        entityManager.flush();
        LocalDate date2 = LocalDate.parse("2025-10-02");
        Training training = new Training(null, trainee.getId(), trainer.getId(), "Workout", type.getId(), date, 1.5);
        entityManager.persist(training);
        entityManager.flush();

        Training updated = new Training(training.getId(), trainee2.getId(), trainer2.getId(), "Fitness", type2.getId(), date2, 2.0);

        // When
        trainingDao.update(updated);

        // Then
        Training persisted = entityManager.find(Training.class, training.getId());
        assertEquals("Fitness", persisted.getName());
    }

    @Test
    void shouldRemoveTrainingWhenDeleteCalled() {
        // Given
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();
        User anotherUser = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(anotherUser);
        entityManager.flush();

        Trainee trainee = new Trainee(null, anotherUser, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();
        TrainingType type = new TrainingType(null, "Strength Training");
        entityManager.persist(type);
        entityManager.flush();
        LocalDate date = LocalDate.parse("2025-10-01");
        Training training = new Training(null, trainee.getId(), trainer.getId(), "Workout", type.getId(), date, 1.5);
        entityManager.persist(training);
        entityManager.flush();

        // When
        trainingDao.delete(training);

        // Then
        assertFalse(trainingDao.getById(training.getId()).isPresent());
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
