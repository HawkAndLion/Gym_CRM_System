package learn.epam.com.service;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TrainerDao;
import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceImplTest {
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "secret";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String DATABASE_ERROR = "Database error";
    private static final String FAIL_SAVE_TRAINER = "Failed to save trainer";
    private static final String FAIL_UPDATE_TRAINER = "Failed to update trainer";
    private static final String FAIL_DELETE_TRAINER = "Failed to delete trainer";
    private static final String FAIL_GET_BY_ID_TRAINER = "Failed to get trainer by id ";
    private static final String FAIL_GET_ALL_TRAINER = "Failed to get all trainers";
    private static final String FAIL_FIND_BY_CREDENTIALS = "Failed to search trainer by credentials";

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UserDao userDao;

    @Mock
    private UserCredentialService userCredentialService;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer trainer;
    private User user;

    @BeforeEach
    void setUp() {
        trainer = new Trainer(null, 10L, "Coach");
        user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
    }

    @Test
    void shouldSaveTrainerWhenValid() throws Exception {
        // Given
        doNothing().when(userCredentialService).ensureUsername(trainer.getUserId());
        doNothing().when(userCredentialService).ensurePassword(trainer.getUserId());
        doNothing().when(trainerDao).save(trainer);

        // When
        trainerService.save(trainer);

        // Then
        verify(userCredentialService).ensureUsername(trainer.getUserId());
        verify(userCredentialService).ensurePassword(trainer.getUserId());
        verify(trainerDao).save(trainer);
        assertDoesNotThrow(() -> new ServiceException(FAIL_SAVE_TRAINER));
    }

    @Test
    void shouldReturnTrainerByIdWhenExists() throws Exception {
        // Given
        when(trainerDao.getById(1L)).thenReturn(Optional.of(trainer));

        // When
        Optional<Trainer> result = trainerService.findById(1L);

        // Then
        verify(trainerDao).getById(1L);
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldUpdateWhenTrainerIsValid() throws Exception {
        // Given
        doNothing().when(trainerDao).update(trainer);

        // When
        trainerService.update(trainer);

        // Then
        verify(trainerDao).update(trainer);
        assertDoesNotThrow(() -> new ServiceException(FAIL_UPDATE_TRAINER));
    }

    @Test
    void shouldRemoveTrainerWhenDeleteIsCalled() throws Exception {
        // Given
        doNothing().when(trainerDao).delete(trainer);

        // When
        trainerService.delete(trainer);

        // Then
        verify(trainerDao).delete(trainer);
        assertDoesNotThrow(() -> new ServiceException(FAIL_DELETE_TRAINER));
    }

    @Test
    void shouldReturnTrainerListWhenFindAllIsCalled() throws Exception {
        // Given
        List<Trainer> trainees = new ArrayList<>();
        trainees.add(trainer);
        trainees.add(new Trainer(null, 102L, "Yoga Instructor"));
        when(trainerDao.getAll()).thenReturn(trainees);

        // When
        List<Trainer> result = trainerService.findAllTrainers();

        // Then
        verify(trainerDao).getAll();
        assertEquals(2, result.size());
        assertEquals(trainer, result.get(0));
    }

    @Test
    void shouldReturnTrueWhenCheckCredentialsAreValid() throws Exception {
        // Given
        when(trainerDao.getById(1L)).thenReturn(Optional.of(trainer));
        when(userCredentialService.loadUserOrThrow(10L)).thenReturn(user);

        // When
        boolean result = trainerService.checkCredentials(1L, USERNAME, PASSWORD);

        // Then
        verify(trainerDao).getById(1L);
        verify(userCredentialService).loadUserOrThrow(10L);
        assertTrue(result);
    }

    @Test
    void shouldReturnTrainerWhenFindTrainerByCredentials() throws Exception {
        // Given
        when(userDao.getAll()).thenReturn(List.of(user));
        when(trainerDao.getAll()).thenReturn(List.of(trainer));

        // When
        Optional<Trainer> result = trainerService.findTrainerByCredentials(USERNAME, PASSWORD);

        // Then
        verify(userDao).getAll();
        verify(trainerDao).getAll();
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldThrowServiceExceptionWhenSaveFails() throws Exception {
        // Given
        doNothing().when(userCredentialService).ensureUsername(trainer.getUserId());
        doNothing().when(userCredentialService).ensurePassword(trainer.getUserId());
        doThrow(new DaoException(DATABASE_ERROR)).when(trainerDao).save(trainer);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainerService.save(trainer));

        // Then
        verify(trainerDao).save(trainer);
        assertEquals(FAIL_SAVE_TRAINER, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenUpdateFails() throws Exception {
        // Given
        doThrow(new DaoException(DATABASE_ERROR)).when(trainerDao).update(trainer);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainerService.update(trainer));

        // Then
        verify(trainerDao).update(trainer);
        assertEquals(FAIL_UPDATE_TRAINER, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenDeleteFails() throws Exception {
        // Given
        doThrow(new DaoException(DATABASE_ERROR)).when(trainerDao).delete(trainer);

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainerService.delete(trainer));

        // Then
        verify(trainerDao).delete(trainer);
        assertEquals(FAIL_DELETE_TRAINER, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFindAllTrainerFails() throws Exception {
        // Given
        doThrow(new DaoException(DATABASE_ERROR)).when(trainerDao).getAll();

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainerService.findAllTrainers());

        // Then
        verify(trainerDao).getAll();
        assertEquals(FAIL_GET_ALL_TRAINER, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFindTraineeByCredentialsDaoFails() throws Exception {
        // Given
        when(userDao.getAll()).thenThrow(new DaoException(DATABASE_ERROR));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> trainerService.findTrainerByCredentials(USERNAME, PASSWORD));

        // Then
        verify(userDao).getAll();
        assertEquals(FAIL_FIND_BY_CREDENTIALS, exception.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenFindByIdDaoFails() throws Exception {
        // Given
        when(trainerDao.getById(1L)).thenThrow(new DaoException(DATABASE_ERROR));

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> trainerService.findById(1L));

        // Then
        verify(trainerDao).getById(1L);
        assertEquals(FAIL_GET_BY_ID_TRAINER, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnSaveWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerService.save(null));

        // Then
        verifyNoInteractions(trainerDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }


    @Test
    void shouldThrowIllegalArgumentExceptionOnUpdateWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerService.update(null));

        // Then
        verifyNoInteractions(trainerDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnDeleteWhenTraineeIsNull() {
        // Given: null trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> trainerService.delete(null));

        //Then
        verifyNoInteractions(trainerDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnCheckCredentialsWhenNullArgs() {
        // Given: null arguments

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainerService.checkCredentials(null, USERNAME, PASSWORD));

        // Then
        verifyNoInteractions(trainerDao);
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }
}
