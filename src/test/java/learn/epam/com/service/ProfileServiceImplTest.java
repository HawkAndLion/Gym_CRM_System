package learn.epam.com.service;

import learn.epam.com.api.model.TraineeCreateRequest;
import learn.epam.com.api.model.TraineeProfileResponse;
import learn.epam.com.api.model.TrainerCreateRequest;
import learn.epam.com.api.model.TrainerProfileResponse;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {
    private static final String MISSING_USER_FIELD = "User missing required fields: firstName/lastName";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String USER_NOT_FOUND = "User not found. Check if firstname and lastname exist.";
    private static final String MISSING_TRAINER_FIELD = "Trainer missing required fields: specialization";

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        Mockito.reset(userService, passwordEncoder, trainerService, traineeService);
    }

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

    @Test
    void shouldPassCheckObNullWhenUserAndTraineeAreNotNull() throws ServiceException {
        // Given
        User user = new User(null, "John", "Doe", null, "pass", true);
        Trainee trainee = new Trainee(null, null, "Address", LocalDate.of(1995, 1, 1), true, new HashSet<>());

        doNothing().when(userService).save(user);
        doNothing().when(traineeService).save(trainee);

        // When
        profileService.createTraineeProfile(user, trainee);

        // Then
        verify(userService).save(user);
        verify(traineeService).save(trainee);
        assertEquals(user, trainee.getUser());
    }

    @Test
    void shouldThrowServiceExceptionWhenUserFirstNameIsNull() {
        // Given
        User user = new User(null, null, "Doe", null, "pass", true);
        Trainee trainee = new Trainee(null, null, "Address", LocalDate.of(1995, 1, 1), true, new HashSet<>());

        // When
        ServiceException ex = assertThrows(ServiceException.class,
                () -> profileService.createTraineeProfile(user, trainee));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(MISSING_USER_FIELD, ex.getMessage());
    }

    @Test
    void shouldAddTrainerToResponseWhenTrainerIdMatchesAndUserExists() throws ServiceException {
        // Given
        User traineeUser = new User(1L, "John", "Doe", "john.doe", null, true);
        Trainee trainee = new Trainee(10L, traineeUser, "Address", LocalDate.of(1995, 1, 1), true, new HashSet<>());

        when(userService.findById(1L)).thenReturn(Optional.of(traineeUser));
        when(traineeService.getTrainerIdsForTrainee(10L)).thenReturn(Set.of(100L));

        User trainerUser = new User(2L, "Jane", "Smith", "jane.smith", null, true);
        Trainer trainer = new Trainer(100L, trainerUser, "Yoga", true, new HashSet<>());

        when(trainerService.findAllTrainers()).thenReturn(List.of(trainer));
        when(userService.findById(2L)).thenReturn(Optional.of(trainerUser));

        // When
        TraineeProfileResponse response = profileService.getTraineeProfile(trainee);

        // Then
        assertEquals(1, response.getTrainers().size());

        TrainerProfileResponse trainerResponse = response.getTrainers().get(0);
        assertEquals("jane.smith", trainerResponse.getUsername());
        assertEquals("Jane", trainerResponse.getFirstName());
        assertEquals("Smith", trainerResponse.getLastName());
        assertEquals("Yoga", trainerResponse.getSpecialization());
        assertTrue(trainerResponse.getActive());
    }

    @Test
    void shouldCreateTraineeProfileFromPrimitiveArguments() throws ServiceException {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        LocalDate date = LocalDate.of(1995, 5, 5);
        String address = "Almaty";
        String password = "password";

        doNothing().when(userService).save(any(User.class));
        doNothing().when(traineeService).save(any(Trainee.class));

        // When
        profileService.createTraineeProfile(firstName, lastName, date, address, password);

        // Then
        verify(userService).save(any(User.class));
        verify(traineeService).save(any(Trainee.class));
        assertDoesNotThrow(() -> profileService);
    }

    @Test
    void shouldRegisterTraineeAndReturnCreatedUser() throws ServiceException {
        // Given
        TraineeCreateRequest request = new TraineeCreateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1995, 5, 5));
        request.setAddress("Almaty");
        request.setPassword("pass");

        doNothing().when(userService).save(any(User.class));
        doNothing().when(traineeService).save(any(Trainee.class));

        User expectedUser = new User(1L, "John", "Doe", "john.doe", "pass", true);
        when(userService.findAllUsers()).thenReturn(List.of(expectedUser));

        // When
        User result = profileService.registerTrainee(request);

        // Then
        verify(userService).save(any(User.class));
        verify(traineeService).save(any(Trainee.class));
        verify(userService).findAllUsers();
        assertEquals(expectedUser, result);
    }

    @Test
    void shouldThrowServiceExceptionWhenRegisteredUserNotFound() throws ServiceException {
        // Given
        TraineeCreateRequest request = new TraineeCreateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1995, 5, 5));
        request.setAddress("Almaty");
        request.setPassword("pass");

        doNothing().when(userService).save(any(User.class));
        doNothing().when(traineeService).save(any(Trainee.class));
        ;

        when(userService.findAllUsers()).thenReturn(Collections.emptyList());

        // When
        ServiceException ex = assertThrows(ServiceException.class,
                () -> profileService.registerTrainee(request));

        // Then
        verify(userService).save(any(User.class));
        verify(traineeService).save(any(Trainee.class));
        verify(userService).findAllUsers();
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals(USER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void shouldCreateTrainerProfileWhenUserAndTrainerValid() throws ServiceException {
        // Given
        User user = new User(2L, "Jane", "Smith", "jane.smith", null, true);
        Trainer trainer = new Trainer(2L, user, "Yoga", true, new HashSet<>());

        // When
        profileService.createTrainerProfile(user, trainer);

        // Then
        verify(userCredentialService).ensureUsernameExists(user);
        verify(userCredentialService).ensurePassword(user);
        verify(userService).save(user);
        verify(trainerService).save(trainer);
        assertEquals(user, trainer.getUser());
    }

    @Test
    void shouldThrowServiceExceptionWhenFirstNameIsNull() {
        // Given
        User user = new User(2L, null, "Smith", "jane.smith", null, true);
        Trainer trainer = new Trainer(2L, user, "Yoga", true, new HashSet<>());

        // When
        ServiceException ex = assertThrows(ServiceException.class,
                () -> profileService.createTrainerProfile(user, trainer)
        );

        //
        verifyNoInteractions(userCredentialService, userService, trainerService);
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(MISSING_USER_FIELD, ex.getMessage());
    }

    @Test
    void shouldThrowServiceExceptionWhenUserLastNameIsNull() {
        // Given
        User user = new User(2L, "Jane", null, "jane.smith", null, true);
        Trainer trainer = new Trainer(2L, user, "Yoga", true, new HashSet<>());

        // When
        ServiceException ex = assertThrows(
                ServiceException.class,
                () -> profileService.createTrainerProfile(user, trainer)
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(MISSING_USER_FIELD, ex.getMessage());

        verifyNoInteractions(userCredentialService, userService, trainerService);
    }

    @Test
    void shouldThrowServiceExceptionWhenTrainerSpecializationIsNull() {
        // Given
        User user = new User(2L, "Jane", "Smith", "Jane.Smith", null, true);
        Trainer trainer = new Trainer(2L, user, null, true, new HashSet<>());

        // When
        ServiceException ex = assertThrows(
                ServiceException.class,
                () -> profileService.createTrainerProfile(user, trainer)
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(MISSING_TRAINER_FIELD, ex.getMessage());

        verifyNoInteractions(userCredentialService, userService, trainerService);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUserIsNull() {
        // Given
        Trainer trainer = new Trainer(2L, null, null, true, new HashSet<>());

        // When
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> profileService.createTrainerProfile(null, trainer)
        );

        // Then
        assertEquals(NULL_EXCEPTION, ex.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTrainerIsNull() {
        // Given
        User user = new User(2L, "Jane", "Smith", "Jane.Smith", null, true);

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> profileService.createTrainerProfile(user, null)
        );

        // Then
        assertEquals(NULL_EXCEPTION, ex.getMessage());
    }

    @Test
    void shouldCreateTrainerProfileWhenRequestIsValid() throws ServiceException {
        // Given
        TrainerCreateRequest dto = new TrainerCreateRequest();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setSpecialization("Fitness");
        dto.setPassword("secret");

        doNothing().when(userService).save(any(User.class));
        doNothing().when(trainerService).save(any(Trainer.class));

        // When
        profileService.createTrainerProfile(dto);

        // Then
        verify(userService).save(any(User.class));
        verify(trainerService).save(any(Trainer.class));
        assertDoesNotThrow(() -> profileService);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTrainerDtoIsNull() {
        // Given

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> profileService.createTrainerProfile(null)
        );

        // Then
        assertEquals(NULL_EXCEPTION, ex.getMessage());
    }

    @Test
    void shouldPropagateServiceExceptionFromDelegatedMethod() throws ServiceException {
        // Given
        TrainerCreateRequest dto = new TrainerCreateRequest();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setSpecialization("Fitness");
        dto.setPassword("secret");

        doThrow(new ServiceException(HttpStatus.BAD_REQUEST, "Validation failed"))
                .when(userService)
                .save(any(User.class));

        // When
        ServiceException ex = assertThrows(ServiceException.class, () -> profileService.createTrainerProfile(dto));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Validation failed", ex.getMessage());
    }

    @Test
    void shouldReturnTrainerProfileWithTraineesWhenTrainerIsValid() throws ServiceException {
        // Given
        User trainerUser = new User(1L, "John", "Trainer", "john.trainer", "pass", true);
        Trainer trainer = new Trainer(10L, trainerUser, "Yoga", true);

        User traineeUser = new User(2L, "Jane", "Trainee", "jane.trainee", "pass", true);
        Trainee trainee = new Trainee(20L, traineeUser, "Some address", LocalDate.of(2000, 1, 1), true);

        when(userService.findById(1L)).thenReturn(Optional.of(trainerUser));
        when(trainerService.getTraineeIdsForTrainer(10L))
                .thenReturn(Set.of(20L));
        when(traineeService.findAllTrainee())
                .thenReturn(List.of(trainee));
        when(userService.findById(2L))
                .thenReturn(Optional.of(traineeUser));

        // When
        TrainerProfileResponse response = profileService.getTrainerProfile(trainer);

        // Then
        verify(trainerService).getTraineeIdsForTrainer(10L);
        verify(traineeService).findAllTrainee();
        assertEquals("john.trainer", response.getUsername());
        assertEquals("Yoga", response.getSpecialization());
        assertEquals(1, response.getTrainees().size());
        TraineeProfileResponse traineeResponse = response.getTrainees().get(0);
        assertEquals("jane.trainee", traineeResponse.getUsername());
    }

    @Test
    void shouldReturnTrainerProfileWithoutTraineesWhenIdsDoNotMatch() throws ServiceException {
        // Given
        User trainerUser = new User(1L, "John", "Trainer", "john.trainer", "pass", true);
        Trainer trainer = new Trainer(10L, trainerUser, "Yoga", true);

        User traineeUser = new User(2L, "Jane", "Trainee", "jane.trainee", "pass", true);
        Trainee trainee = new Trainee(20L, traineeUser, "Address", LocalDate.of(2000, 1, 1), true);

        when(userService.findById(1L)).thenReturn(Optional.of(trainerUser));
        when(trainerService.getTraineeIdsForTrainer(10L))
                .thenReturn(Set.of(999L));
        when(traineeService.findAllTrainee())
                .thenReturn(List.of(trainee));

        // When
        TrainerProfileResponse response = profileService.getTrainerProfile(trainer);

        // Then
        verify(userService, never()).findById(2L);
        assertTrue(response.getTrainees().isEmpty());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTrainerNull() {
        // Given

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> profileService.getTrainerProfile(null)
        );

        // Then
        assertEquals(NULL_EXCEPTION, ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileUpdatingTraineeProfile() {
        // Given
        String username = "john.doe";
        TraineeProfileResponse response = new TraineeProfileResponse();

        when(userService.findByUsername(username)).thenReturn(Optional.empty());

        // When
        ServiceException ex = assertThrows(ServiceException.class,
                () -> profileService.updateTraineeProfile(username, response)
        );

        // Then
        verify(userService).findByUsername(username);
        verifyNoInteractions(traineeService);
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("User not found for trainee", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTraineeNotFoundWhileUpdatingProfile() {
        // Given
        String username = "john.doe";
        User user = new User(1L, "John", "Doe", username, "pass", true);
        TraineeProfileResponse response = new TraineeProfileResponse();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(traineeService.findTraineeByUsername(username)).thenReturn(Optional.empty());

        // When
        ServiceException ex = assertThrows(ServiceException.class,
                () -> profileService.updateTraineeProfile(username, response)
        );

        // Then
        verify(traineeService).findTraineeByUsername(username);
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Trainee not found with username: " + username, ex.getMessage());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenTrainerNotFoundInProfileUpdate() {
        // Given
        String username = "John.Doe";

        User user = new User(1L, "John", "Doe", username, "pass", true);
        Trainee trainee = new Trainee(10L, user, "Address", LocalDate.of(1990, 1, 1), true);

        TrainerProfileResponse trainerProfileResponse =
                new TrainerProfileResponse();
        trainerProfileResponse.setUsername("missing.trainer");

        TraineeProfileResponse response = new TraineeProfileResponse()
                .firstName("John")
                .lastName("Doe")
                .address("New address")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .active(true)
                .trainers(List.of(trainerProfileResponse));

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(traineeService.findTraineeByUsername(username)).thenReturn(Optional.of(trainee));
        when(trainerService.findTrainerByUsername("missing.trainer"))
                .thenReturn(Optional.empty());

        // When
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.updateTraineeProfile(username, response)
        );

        // Then
        assertTrue(exception.getCause() instanceof ServiceException);
        assertEquals("Trainer was not found", exception.getCause().getMessage());
    }

    @Test
    void shouldDeleteTrainerProfileWhenUsernameProvided() throws ServiceException {
        // Given
        String username = "trainer.username";
        doNothing().when(trainerService).deleteTrainerByUsername(username);

        // When
        profileService.deleteTrainerProfile(username);

        // Then
        verify(trainerService).deleteTrainerByUsername(username);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDeletingTrainerProfileWithNullUsername() {
        // Given

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> profileService.deleteTrainerProfile(null)
        );

        // Then
        verifyNoInteractions(trainerService);
        assertEquals("Argument is null ", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringPasswordChange() {
        // Given
        String username = "john.doe";
        Authentication auth =
                new UsernamePasswordAuthenticationToken(username, "password");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userService.findByUsername(username)).thenReturn(Optional.empty());

        // When
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> profileService.changePassword("password", "newPass")
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found. Check if firstname and lastname exist.",
                exception.getMessage());

        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
    }


    @Test
    void shouldThrowExceptionWhenOldPasswordDoesNotMatch() {
        // Given
        String username = "john.doe";
        Authentication auth =
                new UsernamePasswordAuthenticationToken(username, "password");
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User(1L, "John", "Doe", username, "encodedOld", true);

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "encodedOld")).thenReturn(false);

        // When
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> profileService.changePassword("wrongOld", "newPass")
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Invalid user credentials", exception.getMessage());

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordIsBlank() {
        // Given
        String username = "john.doe";
        Authentication auth =
                new UsernamePasswordAuthenticationToken(username, "encodedOld");
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User(1L, "John", "Doe", username, "encodedOld", true);

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOld")).thenReturn(true);

        // When
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> profileService.changePassword("oldPass", "")
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("New password required", exception.getMessage());

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordIsNull() {
        // Given

        // When
        ServiceException ex = assertThrows(ServiceException.class,
                () -> profileService.changePassword("oldPass", null)
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("New password required", ex.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenOldPasswordIsNull() {
        // Given

        // When
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> profileService.changePassword(null, "newPass")
        );

        // Then
        assertEquals(NULL_EXCEPTION, ex.getMessage());
    }

}
