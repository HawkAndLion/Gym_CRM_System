package learn.epam.com.controller;

import learn.epam.com.dto.*;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.prometheusmetrics.CustomMetrics;
import learn.epam.com.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private UserService userService;

    @Mock
    private CustomMetrics customMetrics;

    @InjectMocks
    private TrainerController controller;

    @Test
    void shouldRegisterTrainerWhenMethodCalled() throws Exception {
        // Given
        TrainerDto dto = new TrainerDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setSpecialization("Cardio");

        UserDetailsDto userDetails = new UserDetailsDto();
        userDetails.setUsername("Alice.Brown");

        doNothing().when(profile).createTrainerProfile(dto);
        when(customMetrics.recordTrainerRegistration(Mockito.any()))
                .thenAnswer(invocation -> {
                    Object arg = invocation.getArgument(0);

                    return ((java.util.concurrent.Callable<?>) arg).call();
                });

        // When
        ResponseEntity<?> response = controller.registerTrainer(dto);

        // Then
        verify(profile).createTrainerProfile(dto);
        verify(customMetrics).incrementTrainerCreated();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Trainer was registered successfully", response.getBody());
    }

    @Test
    void shouldReturnTrainerProfileWhenMethodCalled() throws ServiceException {
        // Given
        User user = new User("John", "Doe", "John.Doe", "password", true);
        Trainer trainer = new Trainer(1L, user, "Cardio", true);

        TrainerProfileDto profileDto = new TrainerProfileDto();
        profileDto.setUsername("John.Doe");
        profileDto.setSpecialization("Cardio");

        when(trainerService.findTrainerByUsername("John.Doe"))
                .thenReturn(Optional.of(trainer));
        when(profile.getTrainerProfile(trainer))
                .thenReturn(profileDto);

        // When
        ResponseEntity<?> response = controller.getTrainerProfile("John.Doe");

        // Then
        verify(trainerService).findTrainerByUsername("John.Doe");
        verify(profile).getTrainerProfile(trainer);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("John.Doe",
                ((TrainerProfileDto) response.getBody()).getUsername());
    }

    @Test
    void shouldUpdateTrainerProfileWhenMethodCalled() throws ServiceException {
        // Given
        TrainerProfileDto request = new TrainerProfileDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setSpecialization("Strength");

        TrainerProfileDto responseDto = new TrainerProfileDto();
        responseDto.setUsername("John.Doe");
        responseDto.setSpecialization("Strength");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("John.Doe");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(profile.updateTrainerProfile("John.Doe", request))
                .thenReturn(responseDto);

        // When
        ResponseEntity<?> response =
                controller.updateTrainerProfile(request);

        // Then
        verify(profile).updateTrainerProfile("John.Doe", request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Strength",
                ((TrainerProfileDto) response.getBody()).getSpecialization());
    }

    @Test
    void shouldReturnUnassignedTrainersWhenMethodCalled() throws ServiceException {
        // Given
        TrainerProfileDto trainer1 = new TrainerProfileDto();
        trainer1.setUsername("Jack.London");
        TrainerProfileDto trainer2 = new TrainerProfileDto();
        trainer2.setUsername("Patrick.Willson");

        when(trainerService.getTrainerProfileDtoList("Alice.Brown"))
                .thenReturn(Set.of(trainer1, trainer2));

        // When
        ResponseEntity<?> response =
                controller.getNotAssignedOnTraineeActiveTrainers("Alice.Brown");

        // Then
        verify(trainerService).getTrainerProfileDtoList("Alice.Brown");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, ((Set<?>) response.getBody()).size());
    }

    @Test
    void shouldReturnTrainerTrainingsWhenMethodCalled() throws ServiceException {
        // Given
        Training training = new Training();
        TrainingDto dto = new TrainingDto();
        dto.setTrainingType("Cardio");

        when(trainingService.findTrainingsForTrainerByCriteria(
                eq("John.Doe"), any(), any(), any()))
                .thenReturn(List.of(training));
        when(trainingService.getTrainingDtoList(any()))
                .thenReturn(List.of(dto));

        // When
        ResponseEntity<?> response =
                controller.getTrainerTrainings("John.Doe", null, null, null);

        // Then
        verify(trainingService).findTrainingsForTrainerByCriteria(
                eq("John.Doe"), any(), any(), any());
        verify(trainingService).getTrainingDtoList(any());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Cardio",
                ((TrainingDto) ((List<?>) response.getBody()).get(0))
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
        assertTrue(response.getBody().toString()
                .contains("Trainer not found"));
    }

    @Test
    void shouldReturnNotFoundWhenTrainerTrainingsNotFound() throws ServiceException {
        // Given
        when(trainingService.findTrainingsForTrainerByCriteria(
                eq("Non.Existing"), any(), any(), any()))
                .thenThrow(new ServiceException("Error fetching trainings"));

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
        StatusDto dto =
                new StatusDto("John.Doe", true);

        // When
        ResponseEntity<?> response =
                controller.updateTrainerStatus(dto);

        // Then
        verify(trainerService).activateTrainer("John.Doe");
        assertEquals(200, response.getStatusCodeValue());
        Map<String, String> body = (Map<String, String>) response.getBody();
        String message = body.get("message");
        assertEquals("Trainer activated successfully", message);
    }
}

