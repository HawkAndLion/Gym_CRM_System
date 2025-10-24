package learn.epam.com.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import learn.epam.com.config.TestConfig;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = TestConfig.class)
@Transactional
public class TraineeDaoImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_TYPE = "IllegalArgumentException was expected";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TraineeDao traineeDao;

    @Test
    void shouldReturnTraineeWhenGetByIdCalled() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainee trainee = new Trainee(null, user, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
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

        Trainee trainee1 = new Trainee(null, user, "Almaty", LocalDate.of(2000, 1, 15), true, new HashSet<>());
        Trainee trainee2 = new Trainee(null, anotherUser, "Astana", LocalDate.of(2001, 2, 21), true, new HashSet<>());
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

        Trainee trainee = new Trainee(null, user, "Shymkent", LocalDate.of(1999, 3, 3), true, new HashSet<>());
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

        Trainee trainee = new Trainee(null, user, "Kokshetau", LocalDate.of(1995, 5, 5), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();

        Long id = trainee.getId();

        Trainee updatedTrainee = new Trainee(id, user, "Some city", LocalDate.of(1995, 5, 5), true, new HashSet<>());

        // When
        traineeDao.save(updatedTrainee);

        // Then
        Trainee actual = traineeDao.getById(id).orElseThrow();
        assertEquals(updatedTrainee.getUser().getId(), actual.getUser().getId());
        assertEquals("Some city", actual.getAddress());
    }

    @Test
    void shouldReplaceExistingTraineeWhenUpdateCalled() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainee trainee = new Trainee(null, user, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();

        User anotherUser = new User(null, "Melissa", "Right", "Melissa.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainee updated = new Trainee(trainee.getId(), anotherUser, "Astana", LocalDate.of(1998, 8, 8), true, new HashSet<>());

        // When
        traineeDao.update(updated);

        // Then
        Trainee actual = traineeDao.getById(trainee.getId()).orElseThrow();

        assertEquals(updated.getId(), actual.getId());
        assertEquals(updated.getUser().getId(), actual.getUser().getId());
        assertEquals(updated.getAddress(), actual.getAddress());
        assertEquals(updated.getDateOfBirth(), actual.getDateOfBirth());

    }

    @Test
    void shouldRemoveTraineeWhenDeleteCalled() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainee trainee = new Trainee(null, user, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
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

        Trainee trainee = new Trainee(null, user, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();

        // When
        Long result = traineeDao.getUserId(trainee);

        // Then
        assertEquals(userId, result);
    }

    @Test
    void shouldReturnTraineeWhenFindByUsernameCalled() throws DaoException {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();

        Trainee trainee = new Trainee(null, user, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();

        // When
        Optional<Trainee> result = traineeDao.findTraineeByUsername("Dan.Right");

        // Then
        assertTrue(result.isPresent());
        assertEquals(trainee.getId(), result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenFindByUsernameNotExists() throws DaoException {
        // Given: empty DB or username that doesn’t exist

        // When
        Optional<Trainee> result = traineeDao.findTraineeByUsername("Non.Existent");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAssignedTrainerIdsWhenRequested() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();

        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainerA = new Trainer(null, user2, "Running Coach", true, new HashSet<>());

        User user3 = new User(null, "Carol", "Carter", "Carol.Carter", "password", true);
        entityManager.persist(user3);
        entityManager.flush();
        Trainer trainerB = new Trainer(null, user3, "Yoga Coach", true, new HashSet<>());
        entityManager.persist(trainerA);
        entityManager.persist(trainerB);
        entityManager.flush();

        traineeDao.setTrainerIdsForTrainee(trainee.getId(), Set.of(trainerA.getId(), trainerB.getId()));
        entityManager.flush();
        entityManager.clear();

        // When
        Set<Long> result = traineeDao.getTrainerIdsForTrainee(trainee.getId());

        // Then
        assertEquals(Set.of(trainerA.getId(), trainerB.getId()), result);
    }

    @Test
    void shouldReplaceTrainerIdsWhenNewIdsProvided() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();

        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainerA = new Trainer(null, user2, "Running Coach", true, new HashSet<>());

        User user3 = new User(null, "Carol", "Carter", "Carol.Carter", "password", true);
        entityManager.persist(user3);
        entityManager.flush();
        Trainer trainerB = new Trainer(null, user3, "Yoga Coach", true, new HashSet<>());
        entityManager.persist(trainerA);
        entityManager.persist(trainerB);
        entityManager.flush();

        // When
        traineeDao.setTrainerIdsForTrainee(trainee.getId(), Set.of(trainerA.getId(), trainerB.getId()));
        entityManager.flush();
        entityManager.clear();

        // Then
        Set<Long> result = traineeDao.getTrainerIdsForTrainee(trainee.getId());
        assertEquals(Set.of(trainerA.getId(), trainerB.getId()), result);
    }

    @Test
    void shouldAssignTrainerWhenNotExists() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();

        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainerA = new Trainer(null, user2, "Running Coach", true, new HashSet<>());

        User user3 = new User(null, "Carol", "Carter", "Carol.Carter", "password", true);
        entityManager.persist(user3);
        entityManager.flush();
        Trainer trainerB = new Trainer(null, user3, "Yoga Coach", true, new HashSet<>());
        entityManager.persist(trainerA);
        entityManager.persist(trainerB);
        entityManager.flush();

        // When: assign same and new trainer
        traineeDao.assignTrainer(trainee.getId(), trainerA.getId());
        traineeDao.assignTrainer(trainee.getId(), trainerB.getId());
        entityManager.flush();

        // Then
        Set<Long> result = traineeDao.getTrainerIdsForTrainee(trainee.getId());
        assertEquals(Set.of(trainerA.getId(), trainerB.getId()), result);
    }

    @Test
    void shouldUnassignTrainerWhenExists() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();

        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainerA = new Trainer(null, user2, "Running Coach", true, new HashSet<>());
        entityManager.persist(trainerA);
        entityManager.flush();

        traineeDao.setTrainerIdsForTrainee(trainee.getId(), Set.of(trainerA.getId()));
        entityManager.flush();

        // When
        traineeDao.unassignTrainer(trainee.getId(), trainerA.getId());
        entityManager.flush();

        // Then
        Set<Long> result = traineeDao.getTrainerIdsForTrainee(trainee.getId());
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

    @Test
    void shouldThrowIllegalArgumentExceptionOnFetchingTrainerIdsWhenTraineeIdIsNull() {
        // Given: null

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeDao.getTrainerIdsForTrainee(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSettingTrainerIdsWhenTraineeIsNull() {
        // Given: null
        Set<Long> trainerIds = new HashSet<>();

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeDao.setTrainerIdsForTrainee(null, trainerIds), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnAssigningTrainerWhenTraineeIsNull() {
        // Given
        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainer = new Trainer(null, user2, "Running Coach", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeDao.assignTrainer(null, trainer.getId()), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnAssigningTrainerWhenTrainerIsNull() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user, "Almaty", LocalDate.of(2000, 1, 1), true, new HashSet<>());
        entityManager.persist(trainee);
        entityManager.flush();

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeDao.assignTrainer(trainee.getId(), null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnUnassigningTrainerWhenTraineeIsNull() {
        // Given
        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainer = new Trainer(null, user2, "Running Coach", true, new HashSet<>());
        entityManager.persist(trainer);
        entityManager.flush();

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeDao.unassignTrainer(null, trainer.getId()), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
