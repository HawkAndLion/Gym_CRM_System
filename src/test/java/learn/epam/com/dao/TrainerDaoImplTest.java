package learn.epam.com.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import learn.epam.com.config.TestConfig;
import learn.epam.com.dao.impl.TrainerDaoImpl;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = TestConfig.class)
@Transactional
public class TrainerDaoImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_TYPE = "IllegalArgumentException was expected";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TrainerDaoImpl trainerDao;

    @Test
    void shouldReturnTrainerWhenGetByIdCalled() {
        // Given
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainer trainer = new Trainer(null, user, "Coach", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();

        // When
        Optional<Trainer> result = trainerDao.getById(trainer.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("Coach", result.get().getSpecialization());
    }

    @Test
    void shouldSaveTrainerWhenValid() {
        // Given
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainer trainer = new Trainer(null, user, "Personal Trainer", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();

        // When
        trainerDao.save(trainer);

        // Then
        assertNotNull(trainer.getId());
        assertTrue(trainerDao.getById(trainer.getId()).isPresent());
    }


    @Test
    void shouldReturnEmptyWhenTrainerDoesNotExists() {
        // Given: trainer does not exist in database

        // When
        Optional<Trainer> result = trainerDao.getById(99L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrainersListWhenGetAllCalled() {
        // Given
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();
        User anotherUser = new User(null, "Nancy", "Brown", "Nancy.Brown", "password", true);
        entityManager.persist(anotherUser);
        entityManager.flush();
        Trainer trainer1 = new Trainer(null, user, "Strength and Conditioning Coach", true, new HashSet<>());
        Trainer trainer2 = new Trainer(null, anotherUser, "Yoga Instructor", true, new HashSet<>());
        entityManager.persist(trainer1);
        entityManager.persist(trainer2);
        entityManager.flush();

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
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainer trainer = new Trainer(null, user, "Fitness Trainer", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();

        // When
        Trainer actual = trainerDao.getById(trainer.getId()).orElseThrow();

        // Then
        assertNotNull(actual.getId());
        assertEquals("Fitness Trainer", actual.getSpecialization());
    }

    @Test
    void shouldReplaceExistingTrainerWhenUpdateCalled() {
        // Given
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainer trainer = new Trainer(null, user, "Running Coach", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();

        Trainer updated = new Trainer(trainer.getId(), user, "Yoga Instructor", true, new HashSet<>());

        // When
        trainerDao.update(updated);

        // Then
        Trainer persisted = entityManager.find(Trainer.class, trainer.getId());
        assertEquals("Yoga Instructor", persisted.getSpecialization());
    }

    @Test
    void shouldRemoveTrainerWhenDeleteCalled() {
        // Given
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainer trainer = new Trainer(null, user, "Fitness Instructor", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();

        // When
        trainerDao.delete(trainer);

        // Then
        assertFalse(trainerDao.getById(trainer.getId()).isPresent());
    }

    @Test
    void shouldReturnUserIdWhenGetUserIdCalled() {
        // Given
        User user = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainer trainer = new Trainer(null, user, "Strength and Conditioning Coach", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();

        // When
        Long result = trainerDao.getUserId(trainer);

        // Then
        assertEquals(user.getId(), result);
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
