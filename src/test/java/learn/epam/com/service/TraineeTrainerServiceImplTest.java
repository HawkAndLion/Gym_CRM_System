package learn.epam.com.service;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TraineeTrainerDao;
import learn.epam.com.entity.Trainer;
import learn.epam.com.service.impl.TraineeTrainerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeTrainerServiceImplTest {
    private static final String TRAINEE_NOT_FOUND = "Trainee not found: ";
    private static final String DB_ERROR = "DB error";
    private static final String CHECK_TRAINEE_USERNAME = "Check if trainee username correct";

    @Mock
    private TraineeTrainerDao traineeTrainerDao;

    @InjectMocks
    private TraineeTrainerServiceImpl traineeTrainerService;

    @Test
    void shouldReturnTrainerIdsForTraineeWhenTraineeValid() {
        // Given
        Long traineeId = 1L;
        Set<Long> expected = Set.of(10L, 20L);
        when(traineeTrainerDao.getTrainerIdsForTrainee(traineeId)).thenReturn(expected);

        // When
        Set<Long> result = traineeTrainerService.getTrainerIdsForTrainee(traineeId);

        // Then
        verify(traineeTrainerDao).getTrainerIdsForTrainee(traineeId);
        assertNotNull(result);
        assertEquals(expected, result);
        assertTrue(result.containsAll(Set.of(10L, 20L)));
    }

    @Test
    void shouldSetTrainerIdsForTraineeWhenMethodCalled() {
        // Given
        Long traineeId = 1L;
        Set<Long> trainerIds = Set.of(5L, 6L);

        // When
        traineeTrainerService.setTrainerIdsForTrainee(traineeId, trainerIds);

        // Then
        verify(traineeTrainerDao).setTrainerIdsForTrainee(traineeId, trainerIds);
        assertDoesNotThrow(() -> traineeTrainerService.setTrainerIdsForTrainee(traineeId, trainerIds));
    }

    @Test
    void shouldAssignTrainerWhenTraineeIdValid() {
        // Given
        Long traineeId = 1L;
        Long trainerId = 10L;

        // When
        traineeTrainerService.assignTrainer(traineeId, trainerId);

        // Then
        verify(traineeTrainerDao).assignTrainer(traineeId, trainerId);
        assertDoesNotThrow(() -> traineeTrainerService.assignTrainer(traineeId, trainerId));
    }

    @Test
    void shouldUnassignTrainerWhenMethodCalled() {
        // Given
        Long traineeId = 1L;
        Long trainerId = 10L;

        // When
        traineeTrainerService.unassignTrainer(traineeId, trainerId);

        // Then
        verify(traineeTrainerDao).unassignTrainer(traineeId, trainerId);
        assertDoesNotThrow(() -> traineeTrainerService.unassignTrainer(traineeId, trainerId));
    }

    @Test
    void shouldReturnUnassignedTrainersWhenMethodCalled() throws ServiceException, DaoException {
        // Given
        String username = "trainee1";
        List<Trainer> trainers = List.of(
                new Trainer(1L, 100L, "Cardio"),
                new Trainer(2L, 101L, "Yoga")
        );

        when(traineeTrainerDao.getUnassignedTrainersForTrainee(username)).thenReturn(trainers);

        // When
        List<Trainer> result = traineeTrainerService.getUnassignedTrainersForTrainee(username);

        // Then
        verify(traineeTrainerDao).getUnassignedTrainersForTrainee(username);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cardio", result.get(0).getSpecialization());
        assertEquals("Yoga", result.get(1).getSpecialization());
    }

    @Test
    void shouldThrowServiceExceptionWhenMethodFails() throws DaoException {
        // Given
        String username = "nonexistent";
        when(traineeTrainerDao.getUnassignedTrainersForTrainee(username))
                .thenThrow(new DaoException(DB_ERROR));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeTrainerService.getUnassignedTrainersForTrainee(username));

        // Then
        verify(traineeTrainerDao).getUnassignedTrainersForTrainee(username);
        assertEquals(TRAINEE_NOT_FOUND, exception.getMessage());
        assertInstanceOf(ServiceException.class, exception);
    }

    @Test
    void shouldUpdateTraineeTrainersList() throws DaoException, ServiceException {
        // Given
        String username = "trainee.user";
        Set<Long> trainerIds = Set.of(1L, 2L);

        // When
        traineeTrainerService.updateTraineeTrainersList(username, trainerIds);

        // Then
        verify(traineeTrainerDao).updateTraineeTrainersList(username, trainerIds);
        assertDoesNotThrow(() -> traineeTrainerService.updateTraineeTrainersList(username, trainerIds));
    }

    @Test
    void shouldThrowServiceExceptionWhenUpdateFails() throws DaoException {
        // Given
        String username = "wrong.user";
        Set<Long> trainerIds = Set.of(1L, 2L);

        doThrow(new DaoException(DB_ERROR)).when(traineeTrainerDao)
                .updateTraineeTrainersList(username, trainerIds);

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> traineeTrainerService.updateTraineeTrainersList(username, trainerIds));

        // Then
        verify(traineeTrainerDao).updateTraineeTrainersList(username, trainerIds);
        assertTrue(exception.getMessage().contains(CHECK_TRAINEE_USERNAME));
    }
}
