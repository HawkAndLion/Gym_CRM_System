package learn.epam.com.service;

import learn.epam.com.api.model.TraineeProfileResponse;
import learn.epam.com.api.model.TrainerProfileResponse;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {
    private static final String MISSING_USER_FIELD = "User missing required fields: firstName/lastName";
    private static final String NULL_EXCEPTION = "Argument is null ";

    @Mock
    private UserService userService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private UserCredentialService userCredentialService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileServiceImpl profileService;

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
        Trainee trainee = new Trainee(null, null, "address", LocalDate.of(1990, 1, 1), true, new HashSet<>());

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

        when(userService.findById(15L)).thenReturn(Optional.of(user));
        when(traineeService.getTrainerIdsForTrainee(trainee.getId())).thenReturn(new HashSet<>());

        // When
        TraineeProfileResponse response = profileService.getTraineeProfile(trainee);

        // Then
        verify(userService).findById(15L);
        verify(traineeService).getTrainerIdsForTrainee(trainee.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getFirstName(), response.getFirstName());
        assertEquals(user.getLastName(), response.getLastName());
        assertEquals(trainee.getAddress(), response.getAddress());
        assertEquals(trainee.getDateOfBirth(), response.getDateOfBirth());
        assertTrue(response.getTrainers().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenTraineeProfileNotFound() {
        // Given
        User user = new User(1L, "John", "Brown", "John.Brown", "secret", true);
        Trainee trainee = new Trainee(1L, user, "address", LocalDate.of(2000, 10, 12), true);

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

        when(userService.findById(15L)).thenReturn(Optional.of(user));
        when(trainerService.getTraineeIdsForTrainer(trainer.getId())).thenReturn(new HashSet<>());

        // When
        TrainerProfileResponse response = profileService.getTrainerProfile(trainer);

        // Then
        verify(userService).findById(15L);
        verify(trainerService).getTraineeIdsForTrainer(trainer.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getFirstName(), response.getFirstName());
        assertEquals(user.getLastName(), response.getLastName());
        assertEquals(trainer.getSpecialization(), response.getSpecialization());
        assertTrue(response.getTrainees().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenTrainerProfileNotFound() {
        // Given
        User user = new User(1L, "John", "Doe", "John.Doe", "pass", true);
        Trainer trainer = new Trainer(1L, user, "Yoga", true);


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
        String username = "John.Doe";
        User user = new User(15L, "John", "Doe", username, null, true);
        Trainee trainee = new Trainee(1L, user, "Some address", LocalDate.of(1990, 1, 1), true, new HashSet<>());
        TraineeProfileResponse profileResponse = new TraineeProfileResponse()
                .username(username)
                .firstName("John")
                .lastName("Doe")
                .address("New address")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .active(true)
                .trainers(Collections.emptyList());

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(traineeService.findTraineeByUsername(username)).thenReturn(Optional.of(trainee));
        when(userService.findById(15L)).thenReturn(Optional.of(user));
        doNothing().when(userService).update(user);
        doNothing().when(traineeService).update(trainee);

        // When
        TraineeProfileResponse updated = profileService.updateTraineeProfile(username, profileResponse);

        // Then
        verify(userService).findByUsername(username);
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
        String username = "John.Doe";
        User user = new User(15L, "John", "Doe", username, null, true);
        Trainer trainer = new Trainer(1L, user, "Specialization", true, new HashSet<>());

        TrainerProfileResponse profileResponse = new TrainerProfileResponse()
                .username(username)
                .firstName("John")
                .lastName("Doe")
                .specialization("Updated Specialization")
                .active(true)
                .trainees(Collections.emptyList());

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(trainerService.findTrainerByUsername(username)).thenReturn(Optional.of(trainer));
        when(userService.findById(15L)).thenReturn(Optional.of(user));
        doNothing().when(userService).update(user);
        doNothing().when(trainerService).update(trainer);

        // When
        TrainerProfileResponse updated = profileService.updateTrainerProfile(username, profileResponse);

        // Then
        verify(userService).update(user);
        verify(trainerService).update(trainer);
        assertEquals("Updated Specialization", updated.getSpecialization());
    }

    @Test
    @WithMockUser(username = "John.Doe", roles = {"ROLE_USER"})
    void shouldChangePasswordSuccessfullyWhenValidInput() throws ServiceException {
        // Given
        Authentication auth =
                new UsernamePasswordAuthenticationToken("John.Doe", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String oldPassword = "oldPass";
        String newPassword = "newPass123";
        String encodedOldPassword = "oldPass";
        String encodedNewPassword = "newPass123";

        User user = new User(1L, "John", "Doe", username, oldPassword, true);

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        doNothing().when(userService).update(user);

        // When
        profileService.changePassword(oldPassword, newPassword);

        // Then
        verify(userService).findByUsername(username);
        verify(passwordEncoder).matches(oldPassword, encodedOldPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(userService).update(user);
        assertEquals(newPassword, user.getPassword());
    }

    @Test
    void shouldDeleteTraineeProfileSuccessfullyWhenValidInput() throws ServiceException {
        // Given
        String username = "john.trainee";
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
