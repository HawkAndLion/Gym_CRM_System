package learn.epam.com.controller;

import learn.epam.com.api.model.*;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private ProfileService profile;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private CustomMetrics customMetrics;

    @InjectMocks
    private TrainerController controller;

    @Test
    void shouldRegisterTrainerWhenMethodCalled() throws Exception {
        // Given
        TrainerCreateRequest request = new TrainerCreateRequest()
                .firstName("John")
                .lastName("Doe")
                .specialization("Fitness")
                .password("pass");

        // When
        ResponseEntity<MessageResponse> response = controller.registerTrainer(request);

        // Then
        verify(profile).createTrainerProfile(request);
        verify(customMetrics).incrementTrainerCreated();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Trainer was registered successfully", response.getBody().getMessage());
    }

    @Test
    void shouldReturnTrainerProfileWhenMethodCalled() throws ServiceException {
        // Given
        String username = "John.Doe";
        Trainer trainer = mock(Trainer.class);

        TrainerProfileResponse profileResponse = new TrainerProfileResponse()
                .username(username)
                .firstName("John")
                .lastName("Doe")
                .specialization("Fitness")
                .active(true);

        when(trainerService.findTrainerByUsername(username)).thenReturn(Optional.of(trainer));
        when(profile.getTrainerProfile(trainer)).thenReturn(profileResponse);

        // When
        ResponseEntity<TrainerProfileResponse> response = controller.getTrainerProfile(username);

        // Then
        verify(trainerService).findTrainerByUsername("John.Doe");
        verify(profile).getTrainerProfile(trainer);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(profileResponse, response.getBody());
    }

    @Test
    void shouldUpdateTrainerProfileWhenMethodCalled() throws ServiceException {
        // Given
        TrainerProfileResponse request = new TrainerProfileResponse()
                .username("John.Doe")
                .firstName("John")
                .lastName("Doe")
                .specialization("Fitness")
                .active(true);

        TrainerProfileResponse updatedResponse = new TrainerProfileResponse()
                .username("John.Doe")
                .firstName("John")
                .lastName("Doe")
                .specialization("Fitness")
                .active(false);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("John.Doe");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(profile.updateTrainerProfile("John.Doe", request)).thenReturn(updatedResponse);

        // When
        ResponseEntity<TrainerProfileResponse> response = controller.updateTrainerProfile(request);

        // Then
        verify(profile).updateTrainerProfile("John.Doe", request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(updatedResponse, response.getBody());
    }

    @Test
    void shouldReturnUnassignedTrainersWhenMethodCalled() throws ServiceException {
        // Given

        TrainerProfileResponse trainer1 = new TrainerProfileResponse().username("Jack.London");
        TrainerProfileResponse trainer2 = new TrainerProfileResponse().username("Patrick.Willson");

        when(trainerService.getTrainerProfileDtoList("Alice.Brown"))
                .thenReturn(Set.of(trainer1, trainer2));

        // When
        ResponseEntity<List<TrainerProfileResponse>> response = controller.getNotAssignedTrainers("Alice.Brown");

        // Then
        verify(trainerService).getTrainerProfileDtoList("Alice.Brown");
        List<TrainerProfileResponse> body = response.getBody();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, body.size());
        assertTrue(response.getBody().contains(trainer1));
        assertTrue(response.getBody().contains(trainer2));
    }

    @Test
    void shouldReturnTrainerTrainingsWhenMethodCalled() throws ServiceException {
        // Given
        Training training = new Training();
        TrainingResponse responseDto = new TrainingResponse();
        responseDto.setTrainingType("Cardio");

        when(trainingService.findTrainingsForTrainerByCriteria(
                eq("John.Doe"), any(), any(), any()))
                .thenReturn(List.of(training));
        when(trainingService.getTrainingResponseList(any()))
                .thenReturn(List.of(responseDto));

        // When
        ResponseEntity<?> response =
                controller.getTrainerTrainings("John.Doe", null, null, null);

        // Then
        verify(trainingService).findTrainingsForTrainerByCriteria(
                eq("John.Doe"), any(), any(), any());
        verify(trainingService).getTrainingResponseList(any());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Cardio",
                ((TrainingResponse) ((List<?>) response.getBody()).get(0))
                        .getTrainingType());
    }


    @Test
    void shouldReturnNotFoundWhenTrainerProfileNotExists() throws ServiceException {
        // Given
        when(trainerService.findTrainerByUsername("Non.Existing"))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response =
                controller.getTrainerProfile("Non.Existing");

        // Then
        verify(trainerService).findTrainerByUsername("Non.Existing");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void shouldReturnNotFoundWhenTrainerTrainingsNotFound() throws ServiceException {
        // Given
        when(trainingService.findTrainingsForTrainerByCriteria(
                eq("Non.Existing"), any(), any(), any()))
                .thenThrow(new ServiceException(HttpStatus.BAD_REQUEST, "Error fetching trainings"));

        // When
        ResponseEntity<?> response =
                controller.getTrainerTrainings("Non.Existing", null, null, null);

        // Then
        verify(trainingService).findTrainingsForTrainerByCriteria(
                eq("Non.Existing"), any(), any(), any());
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void shouldUpdateTrainerStatusWhenMethodCalled() throws ServiceException {
        // Given
        TrainerStatusRequest request = new TrainerStatusRequest()
                .username("John.Doe")
                .active(true);
        // When
        ResponseEntity<MessageResponse> response = controller.updateTrainerStatus(request);

        // Then
        verify(trainerService).activateTrainer("John.Doe");
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Trainer status updated successfully", response.getBody().getMessage());
    }
}

