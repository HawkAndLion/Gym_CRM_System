package learn.epam.com.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import learn.epam.com.config.TestConfig;
import learn.epam.com.dao.impl.TraineeDaoImpl;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = TestConfig.class)
@Transactional
public class TraineeDaoImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_TYPE = "IllegalArgumentException was expected";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TraineeDaoImpl traineeDao;

    @Test
    void shouldReturnTraineeWhenGetByIdCalled() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        // When
        Optional<Trainee> result = traineeDao.getById(trainee.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void shouldReturnEmptyWhenTraineeDoesNotExists() {
        // Given: empty

        // When
        Optional<Trainee> result = traineeDao.getById(99L);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTraineesListWhenGetAllCalled() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();

        User anotherUser = new User(null, "Ashley", "Right", "Ashley.Right", "secret", true);
        entityManager.persist(anotherUser);
        entityManager.flush();

        Trainee trainee1 = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 15));
        Trainee trainee2 = new Trainee(null, anotherUser.getId(), "Astana", LocalDate.of(2001, 2, 21));
        entityManager.persist(trainee1);
        entityManager.persist(trainee2);
        entityManager.flush();

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
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user.getId(), "Shymkent", LocalDate.of(1999, 3, 3));
        entityManager.persist(trainee);
        entityManager.flush();

        // When
        traineeDao.save(trainee);

        // Then
        assertNotNull(trainee.getId());
        assertEquals(trainee, traineeDao.getById(trainee.getId()).orElseThrow());
    }

    @Test
    void shouldUseExistingIdWhenPresent() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();

        Long userId = user.getId();
        Trainee trainee = new Trainee(null, userId, "Kokshetau", LocalDate.of(1995, 5, 5));
        entityManager.persist(trainee);
        entityManager.flush();

        Long id = trainee.getId();
        Trainee updatedTrainee = new Trainee(id, userId, "Some city", LocalDate.of(1995, 5, 5));

        // When
        traineeDao.save(updatedTrainee);

        // Then
        Trainee actual = traineeDao.getById(id).orElseThrow();
        assertEquals(updatedTrainee.getUserId(), actual.getUserId());
        assertEquals("Some city", actual.getAddress());
    }

    @Test
    void shouldReplaceExistingTraineeWhenUpdateCalled() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        User anotherUser = new User(null, "Melissa", "Right", "Melissa.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainee updated = new Trainee(trainee.getId(), anotherUser.getId(), "Astana", LocalDate.of(1998, 8, 8));

        // When
        traineeDao.update(updated);

        // Then
        Trainee actual = traineeDao.getById(trainee.getId()).orElseThrow();

        assertEquals(updated.getId(), actual.getId());
        assertEquals(updated.getUserId(), actual.getUserId());
        assertEquals(updated.getAddress(), actual.getAddress());
        assertEquals(updated.getDateOfBirth(), actual.getDateOfBirth());

    }

    @Test
    void shouldRemoveTraineeWhenDeleteCalled() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        // When
        traineeDao.delete(trainee);

        // Then
        assertFalse(traineeDao.getById(trainee.getId()).isPresent());
    }

    @Test
    void shouldReturnUserIdWhenGetUserIdCalled() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Long userId = user.getId();

        Trainee trainee = new Trainee(null, userId, "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        // When
        Long result = traineeDao.getUserId(trainee);

        // Then
        assertEquals(userId, result);
    }

    @Test
    void shouldReturnTraineeWhenFindByUsernameCalled() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        // When
        Optional<Trainee> result = traineeDao.findTraineeByUsername("Dan.Right");

        // Then
        assertTrue(result.isPresent());
        assertEquals(trainee.getId(), result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenFindByUsernameNotExists() {
        // Given: empty DB or username that doesn’t exist

        // When
        Optional<Trainee> result = traineeDao.findTraineeByUsername("Non.Existent");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnFindByUsernameWhenNull() {
        // Given: null username

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeDao.findTraineeByUsername(null),
                ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        // Then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
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
