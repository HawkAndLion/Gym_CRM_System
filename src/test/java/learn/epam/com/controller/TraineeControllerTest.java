package learn.epam.com.controller;

import learn.epam.com.api.model.*;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.prometheusmetrics.CustomMetrics;
import learn.epam.com.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private ProfileService profile;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private CustomMetrics customMetrics;

    @InjectMocks
    private TraineeController controller;

    @Test
    void shouldRegisterTraineeSuccessfullyWhenMethodCalled() {
        TraineeCreateRequest request = new TraineeCreateRequest();
        request.setFirstName("Alice");
        request.setLastName("Brown");
        request.setPassword("secret");

        when(profile.registerTrainee(request)).thenReturn(new User("Alice", "Brown", "Alice.Brown", "secret", true));

        ResponseEntity<Void> response = controller.registerTrainee(request);

        verify(customMetrics, times(1)).incrementTraineeCreated();
        verify(profile, times(1)).registerTrainee(request);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldReturnBadRequestWhenRegisterTraineeWithException() {
        TraineeCreateRequest request = new TraineeCreateRequest();
        request.setFirstName("Alice");
        request.setLastName("Brown");
        request.setPassword("secret");
        doThrow(new RuntimeException("Error")).when(profile).registerTrainee(request);

        ResponseEntity<Void> response = controller.registerTrainee(request);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void shouldGetTraineeProfileWhenRequestSuccessful() {
        User user = new User("Alice", "Brown", "Alice.Brown", "password", true);
        Trainee trainee = new Trainee(1L, user, "address", LocalDate.of(2025, 10, 12), true);

        TraineeProfileResponse profileResponse = new TraineeProfileResponse();
        profileResponse.setUsername("Alice.Brown");

        when(traineeService.findTraineeByUsername("Alice.Brown")).thenReturn(Optional.of(trainee));
        when(profile.getTraineeProfile(trainee)).thenReturn(profileResponse);

        ResponseEntity<TraineeProfileResponse> response = controller.getTraineeProfile("Alice.Brown");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(profileResponse, response.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenTraineeProfileNotFound() throws ServiceException {
        when(traineeService.findTraineeByUsername("Unknown")).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () ->
                controller.getTraineeProfile("Unknown")
        );

        assertTrue(exception.getMessage().contains("Trainee not found"));
    }

    @Test
    void shouldDeleteTraineeProfileWhenRequestWasSuccessful() throws ServiceException {
        doNothing().when(profile).deleteTraineeProfile("Alice.Brown");

        ResponseEntity<Void> response = controller.deleteTraineeProfile("Alice.Brown");

        verify(profile, times(1)).deleteTraineeProfile("Alice.Brown");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldReturnNotFoundWhenDeleteTraineeProfileFails() throws ServiceException {
        doThrow(new ServiceException(HttpStatus.BAD_REQUEST, "Trainee not found")).when(profile).deleteTraineeProfile("Ghost.User");

        ResponseEntity<?> response = controller.deleteTraineeProfile("Ghost.User");

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void shouldUpdateTraineeStatusWhenChangedToActivate() throws ServiceException {
        TraineeStatusRequest request = new TraineeStatusRequest();
        request.setUsername("Alice.Brown");
        request.setActive(true);

        doNothing().when(traineeService).activateTrainee("Alice.Brown");

        ResponseEntity<Void> response = controller.updateTraineeStatus(request);

        verify(traineeService, times(1)).activateTrainee("Alice.Brown");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldUpdateTraineeStatusWhenChangedToDeactivate() throws ServiceException {
        TraineeStatusRequest request = new TraineeStatusRequest();
        request.setUsername("Alice.Brown");
        request.setActive(false);

        doNothing().when(traineeService).deactivateTrainee("Alice.Brown");

        ResponseEntity<Void> response = controller.updateTraineeStatus(request);

        verify(traineeService, times(1)).deactivateTrainee("Alice.Brown");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldUpdateTraineeProfileWhenMethodCalled() throws ServiceException {
        // Given
        TraineeProfileResponse request = new TraineeProfileResponse();
        request.setUsername("Alice.Brown");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("Alice.Brown");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // When
        ResponseEntity<Void> response = controller.updateTraineeProfile(request);

        // Then
        verify(profile).updateTraineeProfile("Alice.Brown", request);
        assertEquals(200, response.getStatusCodeValue());

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUpdateTraineeTrainersSuccessfully() throws ServiceException {
        // Given
        TraineeTrainersRequest request = new TraineeTrainersRequest();
        request.setTrainerUsernames(List.of("Alice.Brown", "Tom.Hardy"));

        Trainer trainer1 = mock(Trainer.class);
        Trainer trainer2 = mock(Trainer.class);
        Set<Trainer> trainers = Set.of(trainer1, trainer2);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("Alice.Brown");

        var securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(trainerService.getTrainersByUsername(request)).thenReturn(trainers);
        doNothing().when(traineeService).update("Alice.Brown", trainers);
        doNothing().when(trainingService).updateTrainingsByTrainee("Alice.Brown", trainers);

        TrainerProfileResponse dto = new TrainerProfileResponse();
        when(trainerService.getTrainerProfileResponse(trainers))
                .thenReturn(List.of(dto));

        // When
        ResponseEntity<List<TrainerProfileResponse>> response =
                controller.updateTraineeTrainers(request);

        // Then
        verify(trainerService).getTrainersByUsername(request);
        verify(traineeService).update("Alice.Brown", trainers);
        verify(trainingService).updateTrainingsByTrainee("Alice.Brown", trainers);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void shouldGetTraineeTrainingsSuccessfullyWhenGetTraineeTrainings() throws ServiceException {
        // Given
        String username = "Alice.Brown";
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);
        String trainerName = "Bob.Trainer";
        String trainingType = "CARDIO";

        Long trainingTypeId = 1L;

        Training training = mock(Training.class);
        List<Training> trainings = List.of(training);

        TrainingResponse responseDto = new TrainingResponse();
        List<TrainingResponse> responseList = List.of(responseDto);

        when(trainingTypeService.getTrainingTypeId(trainingType))
                .thenReturn(trainingTypeId);

        when(trainingService.findTrainingsForTraineeByCriteria(
                username, from, to, trainerName, trainingTypeId))
                .thenReturn(trainings);

        when(trainingService.getTrainingResponseList(trainings))
                .thenReturn(responseList);

        // When
        ResponseEntity<List<TrainingResponse>> response =
                controller.getTraineeTrainings(username, from, to, trainerName, trainingType);

        // Then
        verify(trainingTypeService).getTrainingTypeId(trainingType);
        verify(trainingService).findTrainingsForTraineeByCriteria(
                username, from, to, trainerName, trainingTypeId);
        verify(trainingService).getTrainingResponseList(trainings);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseList, response.getBody());
    }

    @Test
    void shouldGetTraineeTrainingsWhenOptionalParamsAreNull() throws ServiceException {
        // Given
        String username = "Alice.Brown";

        when(trainingTypeService.getTrainingTypeId(null))
                .thenReturn(null);

        when(trainingService.findTrainingsForTraineeByCriteria(
                username, null, null, null, null))
                .thenReturn(List.of());

        when(trainingService.getTrainingResponseList(List.of()))
                .thenReturn(List.of());

        // When
        ResponseEntity<List<TrainingResponse>> response =
                controller.getTraineeTrainings(username, null, null, null, null);

        // Then
        verify(trainingTypeService).getTrainingTypeId(null);
        verify(trainingService).findTrainingsForTraineeByCriteria(username, null, null, null, null);
        verify(trainingService).getTrainingResponseList(List.of());

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
}
