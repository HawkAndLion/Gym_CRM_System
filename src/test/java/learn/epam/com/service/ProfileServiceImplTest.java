package learn.epam.com.service;

import learn.epam.com.dto.TraineeProfileDto;
import learn.epam.com.dto.TrainerProfileDto;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {
    private static final String MISSING_USER_FIELD = "User missing required fields: firstName/lastName";
    private static final String NULL_EXCEPTION = "Argument is null ";

    @Mock
    UserService userService;

    @Mock
    TraineeService traineeService;

    @Mock
    TrainerService trainerService;

    @Mock
    UserCredentialService userCredentialService;

    @InjectMocks
    ProfileServiceImpl profileService;

    @Test
    void shouldCreateTraineeProfileWhenValidUserAndTrainee() throws ServiceException {
        // Given
        User user = new User(null, "John", "Doe", "John.Doe", "password", true);
        Trainee trainee = new Trainee(null, null, "Some address", LocalDate.of(1990, 1, 1), true, new HashSet<>());

        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(123L);
            return null;
        }).when(userService).save(any(User.class));

        // When
        profileService.createTraineeProfile(user, trainee);

        // Then
        verify(userService).save(user);
        verify(traineeService).save(trainee);
        assertEquals(123L, trainee.getUser().getId());
    }

    @Test
    void shouldThrowServiceExceptionWhenMissingFields() {
        // Given
        User user = new User(null, "Doe", null, null, true); // missing firstName
        Trainee trainee = new Trainee(null, null, "addr", LocalDate.of(1990, 1, 1), true, new HashSet<>());

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> profileService.createTraineeProfile(user, trainee));

        // Then
        assertEquals(MISSING_USER_FIELD, exception.getMessage());
    }

    @Test
    void shouldCreateTrainerProfileWhenValidUserAndTrainer() throws ServiceException {
        // Given
        User user = new User("John", "Doe", null, null, true);
        Trainer trainer = new Trainer(null, null, "Specialization", true, new HashSet<>());

        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(123L);
            return null;
        }).when(userService).save(any(User.class));

        // When
        profileService.createTrainerProfile(user, trainer);

        // Then
        verify(userCredentialService).ensureUsernameExists(user);
        verify(userCredentialService).ensurePassword(user);
        verify(userService).save(user);
        verify(trainerService).save(trainer);
        assertEquals(123L, trainer.getUser().getId());
    }

    @Test
    void shouldThrowServiceExceptionWhenMissingFieldsWhileCreatingTrainerProfile() {
        // Given
        User user = new User(null, "Doe", null, null, true);
        Trainer trainer = new Trainer(null, null, "Specialization", true, new HashSet<>());

        // When
        ServiceException exception = assertThrows(ServiceException.class, () -> profileService.createTrainerProfile(user, trainer));

        // Then
        assertEquals(MISSING_USER_FIELD, exception.getMessage());
    }

    @Test
    void shouldReturnTraineeProfileWhenUsernameExists() throws ServiceException {
        // Given
        User user = new User(15L, "John", "Doe", "John.Doe", null, true);
        Trainee trainee = new Trainee(1L, user, "Some address", LocalDate.of(1990, 1, 1), true, new HashSet<>());
        TraineeProfileDto traineeProfile = new TraineeProfileDto("John.Doe", "John", "Doe", LocalDate.of(1990, 1, 1), "Some address", true, new ArrayList<>());

        when(userService.findById(15L)).thenReturn(Optional.of(user));
        when(traineeService.getTrainerIdsForTrainee(trainee.getId())).thenReturn(new HashSet<>());

        // When
        TraineeProfileDto result = profileService.getTraineeProfile(trainee);

        // Then
        verify(userService).findById(15L);
        verify(traineeService).getTrainerIdsForTrainee(trainee.getId());
        assertEquals(traineeProfile, result);
    }

    @Test
    void shouldThrowExceptionWhenTraineeProfileNotFound() {
        // Given
        Trainee trainee = new Trainee();
        trainee.setUser(new User(1L, "John", "Brown", "John.Brown", "secret", true));

        when(userService.findById(1L)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> profileService.getTraineeProfile(trainee));

        // Then
        assertEquals("User not found for trainee", exception.getMessage());
    }

    @Test
    void shouldReturnTrainerProfileWhenUsernameExists() throws ServiceException {
        // Given
        User user = new User(15L, "John", "Doe", "John.Doe", null, true);
        Trainer trainer = new Trainer(1L, user, "Specialization", true, new HashSet<>());
        TrainerProfileDto trainerProfile = new TrainerProfileDto("John.Doe", "John", "Doe", "Specialization");

        when(userService.findById(15L)).thenReturn(Optional.of(user));
        when(trainerService.getTraineeIdsForTrainer(trainer.getId())).thenReturn(new HashSet<>());

        // When
        TrainerProfileDto result = profileService.getTrainerProfile(trainer);

        // Then
        verify(userService).findById(15L);
        verify(trainerService).getTraineeIdsForTrainer(trainer.getId());
        assertEquals(trainerProfile.getUsername(), result.getUsername());
        assertEquals(trainerProfile.getFirstName(), result.getFirstName());
        assertEquals(trainerProfile.getLastName(), result.getLastName());
        assertEquals(trainerProfile.getSpecialization(), result.getSpecialization());
        assertTrue(result.getTrainees().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenTrainerProfileNotFound() {
        // Given
        User user = new User(1L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(userService.findById(1L)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> profileService.getTrainerProfile(trainer));

        // Then
        assertEquals("User not found for trainer", exception.getMessage());
    }

    @Test
    void shouldUpdateTraineeProfileSuccessfullyWhenValidInput() throws ServiceException {
        // Given
        User user = new User(15L, "John", "Doe", "John.Doe", null, true);
        Trainee trainee = new Trainee(1L, user, "Some address", LocalDate.of(1990, 1, 1), true, new HashSet<>());
        TraineeProfileDto traineeProfile = new TraineeProfileDto("John.Doe", "John", "Doe", LocalDate.of(1990, 1, 1), "Some address", true, new ArrayList<>());
        traineeProfile.setAddress("New address");

        when(userService.findById(15L)).thenReturn(Optional.of(user));
        when(userService.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(traineeService.findTraineeByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        doNothing().when(userService).update(user);
        doNothing().when(traineeService).update(trainee);

        // When
        TraineeProfileDto updated = profileService.updateTraineeProfile(user.getUsername(), traineeProfile);

        // Then
        verify(userService).findByUsername("John.Doe");
        verify(userService).update(user);
        verify(traineeService).update(trainee);
        assertEquals("New address", updated.getAddress());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNullTrainee() {
        //Given null trainee

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> profileService.updateTraineeProfile(null, null));

        // Then
        assertEquals(NULL_EXCEPTION, ex.getMessage());
    }

    @Test
    void shouldUpdateTrainerProfileSuccessfullyWhenValidInput() throws ServiceException {
        // Given
        User user = new User(15L, "John", "Doe", "John.Doe", null, true);
        Trainer trainer = new Trainer(1L, user, "Specialization", true, new HashSet<>());
        trainer.setSpecialization("Updated Spec");

        when(userService.findById(15L)).thenReturn(Optional.of(user));
        when(userService.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(trainerService.findTrainerByUsername("John.Doe")).thenReturn(Optional.of(trainer));
        doNothing().when(userService).update(user);
        doNothing().when(trainerService).update(trainer);

        // When
        TrainerProfileDto trainerProfile = new TrainerProfileDto("John.Doe", "John", "Doe", "Specialization");
        TrainerProfileDto updated = profileService.updateTrainerProfile(user.getUsername(), trainerProfile);

        // Then
        verify(trainerService).update(trainer);
        assertEquals(trainerProfile.getSpecialization(), updated.getSpecialization());
    }

    @Test
    void shouldChangePasswordSuccessfullyWhenValidInput() throws ServiceException {
        // Given
        String username = "John.Doe";
        String oldPassword = "oldPass";
        String newPassword = "newPass123";

        List<User> users = new ArrayList<>();
        User user = new User(1L, "John", "Doe", username, oldPassword, true);
        users.add(user);
        when(userService.findAllUsers()).thenReturn(users);
        doNothing().when(userService).update(user);

        // When
        profileService.changePassword(username, oldPassword, newPassword);

        // Then
        verify(userService).findAllUsers();
        verify(userService).update(user);
        assertEquals(newPassword, user.getPassword());
    }

    @Test
    void shouldDeleteTraineeProfileSuccessfullyWhenValidInput() throws ServiceException {
        // Given
        String username = "john.trainee";
        User user = new User(1L, "John", "Trainee", username, "pass", true);
        Trainee trainee = new Trainee(20L, user, "Addr", LocalDate.of(1990, 1, 1), true, new HashSet<>());
        doNothing().when(traineeService).deleteTraineeByUsername(username);

        // When
        profileService.deleteTraineeProfile(username);

        // Then
        verify(traineeService).deleteTraineeByUsername(username);
        assertDoesNotThrow(() -> profileService.deleteTraineeProfile(username));
    }

    @Test
    void shouldThrowServiceExceptionWhenUserValidationFails() {
        // Given: user missing fields
        User invalidUser = new User(null, null, "Doe", null, true);
        Trainee trainee = new Trainee(1L, null, "Some address", LocalDate.of(1990, 1, 1), true, new HashSet<>());

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> profileService.createTraineeProfile(invalidUser, trainee));

        // Then
        assertEquals(MISSING_USER_FIELD, exception.getMessage());
    }


    @Test
    void shouldThrowIllegalArgumentExceptionOnCreatingTraineeProfileWhenNull() {
        // Given: null user and trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> profileService.createTraineeProfile(null, null));

        // Then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnCreatingTrainerProfileWhenNull() {
        // Given: null user and trainee

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> profileService.createTrainerProfile(null, null));

        // Then
        assertEquals(NULL_EXCEPTION, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNullTrainer() {
        // Given null trainer

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> profileService.updateTrainerProfile("username", null));

        // Then
        assertEquals(NULL_EXCEPTION, ex.getMessage());
    }
}
