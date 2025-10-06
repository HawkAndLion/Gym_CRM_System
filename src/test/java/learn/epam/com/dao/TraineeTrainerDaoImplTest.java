package learn.epam.com.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import learn.epam.com.config.TestConfig;
import learn.epam.com.dao.impl.TraineeTrainerDaoImpl;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = TestConfig.class)
@Transactional
public class TraineeTrainerDaoImplTest {
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_TYPE = "IllegalArgumentException was expected";
    private static final String UNKNOWN_USERNAME = "uknown.username";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TraineeTrainerDaoImpl traineeTrainerDao;


    @Test
    void shouldReturnAssignedTrainerIdsWhenRequested() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainerA = new Trainer(null, user2.getId(), "Running Coach");

        User user3 = new User(null, "Carol", "Carter", "Carol.Carter", "password", true);
        entityManager.persist(user3);
        entityManager.flush();
        Trainer trainerB = new Trainer(null, user3.getId(), "Yoga Coach");
        entityManager.persist(trainerA);
        entityManager.persist(trainerB);
        entityManager.flush();

        traineeTrainerDao.setTrainerIdsForTrainee(trainee.getId(), Set.of(trainerA.getId(), trainerB.getId()));
        entityManager.flush();

        // When
        Set<Long> result = traineeTrainerDao.getTrainerIdsForTrainee(trainee.getId());

        // Then
        assertEquals(Set.of(trainerA.getId(), trainerB.getId()), result);
    }

    @Test
    void shouldReplaceTrainerIdsWhenNewIdsProvided() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainerA = new Trainer(null, user2.getId(), "Running Coach");

        User user3 = new User(null, "Carol", "Carter", "Carol.Carter", "password", true);
        entityManager.persist(user3);
        entityManager.flush();
        Trainer trainerB = new Trainer(null, user3.getId(), "Yoga Coach");
        entityManager.persist(trainerA);
        entityManager.persist(trainerB);
        entityManager.flush();

        // When
        traineeTrainerDao.setTrainerIdsForTrainee(trainee.getId(), Set.of(trainerA.getId(), trainerB.getId()));
        entityManager.flush();

        // Then
        Set<Long> result = traineeTrainerDao.getTrainerIdsForTrainee(trainee.getId());
        assertEquals(Set.of(trainerA.getId(), trainerB.getId()), result);
    }

    @Test
    void shouldAssignTrainerWhenNotExists() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainerA = new Trainer(null, user2.getId(), "Running Coach");

        User user3 = new User(null, "Carol", "Carter", "Carol.Carter", "password", true);
        entityManager.persist(user3);
        entityManager.flush();
        Trainer trainerB = new Trainer(null, user3.getId(), "Yoga Coach");
        entityManager.persist(trainerA);
        entityManager.persist(trainerB);
        entityManager.flush();

        // When: assign same and new trainer
        traineeTrainerDao.assignTrainer(trainee.getId(), trainerA.getId());
        traineeTrainerDao.assignTrainer(trainee.getId(), trainerB.getId());
        entityManager.flush();

        // Then
        Set<Long> result = traineeTrainerDao.getTrainerIdsForTrainee(trainee.getId());
        assertEquals(Set.of(trainerA.getId(), trainerB.getId()), result);
    }

    @Test
    void shouldUnassignTrainerWhenExists() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainerA = new Trainer(null, user2.getId(), "Running Coach");
        entityManager.persist(trainerA);
        entityManager.flush();

        traineeTrainerDao.setTrainerIdsForTrainee(trainee.getId(), Set.of(trainerA.getId()));
        entityManager.flush();

        // When
        traineeTrainerDao.unassignTrainer(trainee.getId(), trainerA.getId());
        entityManager.flush();

        // Then
        Set<Long> result = traineeTrainerDao.getTrainerIdsForTrainee(trainee.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnUnassignedTrainerWhenNotAssignedToTrainees() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainerA = new Trainer(null, user2.getId(), "Running Coach");

        User user3 = new User(null, "Carol", "Carter", "Carol.Carter", "password", true);
        entityManager.persist(user3);
        entityManager.flush();
        Trainer trainerB = new Trainer(null, user3.getId(), "Yoga Coach");
        entityManager.persist(trainerA);
        entityManager.persist(trainerB);
        entityManager.flush();

        // When
        List<Trainer> unassigned = traineeTrainerDao.getUnassignedTrainersForTrainee(trainee.getId());

        // Then
        Set<Long> unassignedIds = unassigned.stream().map(Trainer::getId).collect(Collectors.toSet());
        assertTrue(unassignedIds.containsAll(Set.of(trainerA.getId(), trainerB.getId())));
    }

    @Test
    void shouldReturnUnassignedTrainersWhenCalledByUsername() throws Exception {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainerA = new Trainer(null, user2.getId(), "Running Coach");
        entityManager.persist(trainerA);
        entityManager.flush();

        User user3 = new User(null, "Carol", "Carter", "Carol.Carter", "password", true);
        entityManager.persist(user3);
        entityManager.flush();
        Trainer trainerB = new Trainer(null, user3.getId(), "Yoga Coach");
        entityManager.persist(trainerB);
        entityManager.flush();

        traineeTrainerDao.setTrainerIdsForTrainee(trainee.getId(), Set.of(trainerA.getId()));
        entityManager.flush();

        // When
        List<Trainer> unassigned = traineeTrainerDao.getUnassignedTrainersForTrainee(user.getUsername());

        // Then
        assertFalse(unassigned.isEmpty());
        assertTrue(unassigned.stream().noneMatch(t -> t.getId().equals(trainerA.getId())));
        assertTrue(unassigned.stream().anyMatch(t -> t.getId().equals(trainerB.getId())));
    }

    @Test
    void shouldUpdateTraineeTrainerListWhenNewListProvided() throws Exception {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainerA = new Trainer(null, user2.getId(), "Running Coach");
        entityManager.persist(trainerA);
        entityManager.flush();

        traineeTrainerDao.setTrainerIdsForTrainee(trainee.getId(), Set.of(trainerA.getId()));
        entityManager.flush();

        // When
        User user3 = new User(null, "Carol", "Carter", "Carol.Carter", "password", true);
        entityManager.persist(user3);
        entityManager.flush();
        Trainer trainerB = new Trainer(null, user3.getId(), "Yoga Coach");
        entityManager.persist(trainerB);
        entityManager.flush();
        traineeTrainerDao.updateTraineeTrainersList(user.getUsername(), Set.of(trainerB.getId()));
        entityManager.flush();

        // Then
        Set<Long> result = traineeTrainerDao.getTrainerIdsForTrainee(trainee.getId());
        assertEquals(Set.of(trainerB.getId()), result);
    }

    @Test
    void shouldThrowWhenTraineeNotFound() {
        // Given: not existed trainee

        // When
        DaoException exception = assertThrows(
                DaoException.class,
                () -> traineeTrainerDao.getUnassignedTrainersForTrainee(UNKNOWN_USERNAME)
        );

        // Then
        assertEquals(TRAINEE_NOT_FOUND, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnFEtchingTrainerListWhenTraineeIsNull() {
        // Given: null

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeTrainerDao.getUnassignedTrainersForTrainee((String) null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnFetchingTrainerIdsWhenTraineeIdIsNull() {
        // Given: null

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeTrainerDao.getTrainerIdsForTrainee(null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSettingTrainerIdsWhenTraineeIsNull() {
        // Given: null
        Set<Long> trainerIds = new HashSet<>();

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeTrainerDao.setTrainerIdsForTrainee(null, trainerIds), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnAssigningTrainerWhenTraineeIsNull() {
        // Given
        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainer = new Trainer(null, user2.getId(), "Running Coach");
        entityManager.persist(trainer);
        entityManager.flush();

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeTrainerDao.assignTrainer(null, trainer.getId()), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnAssigningTrainerWhenTrainerIsNull() {
        // Given
        User user = new User(null, "Dan", "Right", "Dan.Right", "secret", true);
        entityManager.persist(user);
        entityManager.flush();
        Trainee trainee = new Trainee(null, user.getId(), "Almaty", LocalDate.of(2000, 1, 1));
        entityManager.persist(trainee);
        entityManager.flush();

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeTrainerDao.assignTrainer(trainee.getId(), null), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnUnassigningTrainerWhenTraineeIsNull() {
        // Given
        User user2 = new User(null, "Jackie", "Chan", "Jackie.Chan", "password", true);
        entityManager.persist(user2);
        entityManager.flush();
        Trainer trainer = new Trainer(null, user2.getId(), "Running Coach");
        entityManager.persist(trainer);
        entityManager.flush();

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> traineeTrainerDao.unassignTrainer(null, trainer.getId()), ILLEGAL_ARGUMENT_EXCEPTION_TYPE);

        //then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

}
