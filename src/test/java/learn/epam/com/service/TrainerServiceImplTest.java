package learn.epam.com.service;

import learn.epam.com.dao.TrainerDao;
import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.service.impl.TrainerServiceImpl;
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
    private static final String FAIL_SAVE_TRAINER = "Failed to save trainer";
    private static final String FAIL_UPDATE_TRAINER = "Failed to update trainer";
    private static final String FAIL_DELETE_TRAINER = "Failed to delete trainer";

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UserDao userDao;

    @Mock
    private UserCredentialService userCredentialService;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Test
    void shouldSaveTrainerWhenValid() throws Exception {
        // Given
        Trainer trainer = new Trainer(null, 10L, "Coach");
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
        Trainer trainer = new Trainer(null, 10L, "Coach");
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
        Trainer trainer = new Trainer(null, 10L, "Coach");
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
        Trainer trainer = new Trainer(null, 10L, "Coach");
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
        Trainer trainer = new Trainer(null, 10L, "Coach");
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
        Trainer trainer = new Trainer(null, 10L, "Coach");
        User user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
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
        Trainer trainer = new Trainer(null, 10L, "Coach");
        User user = new User();
        user.setId(10L);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
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
