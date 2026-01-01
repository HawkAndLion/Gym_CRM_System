package learn.epam.com.controller;

import learn.epam.com.dto.StatusDto;
import learn.epam.com.dto.TraineeDto;
import learn.epam.com.dto.TraineeProfileDto;
import learn.epam.com.dto.UserDetailsDto;
import learn.epam.com.entity.Trainee;
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

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private ProfileService profile;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainingTypeService trainingTypeService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private CustomMetrics customMetrics;

    @InjectMocks
    private TraineeController controller;

    @Test
    void shouldRegisterTraineeSuccessfullyWhenMethodCalled() throws Exception {
        TraineeDto request = new TraineeDto();
        request.setFirstName("Alice");
        request.setLastName("Brown");

        UserDetailsDto userDetails = new UserDetailsDto();
        userDetails.setUsername("Alice.Brown");

        when(profile.registerTrainee(request)).thenReturn(userDetails);

        when(customMetrics.recordTraineeRegistration(Mockito.any()))
                .thenAnswer(invocation -> {
                    Object arg = invocation.getArgument(0);

                    return ((java.util.concurrent.Callable<?>) arg).call();
                });


        ResponseEntity<?> response = controller.registerTrainee(request);

        verify(customMetrics, times(1)).incrementTraineeCreated();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Trainee was successfully registered", response.getBody());
    }

    @Test
    void shouldReturnBadRequestWhenRegisterTraineeWithException() throws Exception {
        TraineeDto request = new TraineeDto();
        request.setFirstName("Alice");

        when(customMetrics.recordTraineeRegistration(any())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = controller.registerTrainee(request);

        assertEquals(400, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("error", body.keySet().iterator().next());
    }

    @Test
    void shouldGetTraineeProfileWhenRequestSuccessful() throws Exception {
        User user = new User("Alice", "Brown", "Alice.Brown", "password", true);
        Trainee trainee = new Trainee(1L, user, "address", LocalDate.of(2025, 10, 12), true);

        TraineeProfileDto profileDto = new TraineeProfileDto();
        profileDto.setUsername("Alice.Brown");

        when(traineeService.findTraineeByUsername("Alice.Brown")).thenReturn(Optional.of(trainee));
        when(profile.getTraineeProfile(trainee)).thenReturn(profileDto);

        ResponseEntity<?> response = controller.getTraineeProfile("Alice.Brown");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(profileDto, response.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenTraineeProfileNotFound() throws ServiceException {
        when(traineeService.findTraineeByUsername("Unknown")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getTraineeProfile("Unknown");

        assertEquals(404, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.get("error").toString().contains("Trainee not found"));
    }

    @Test
    void shouldDeleteTraineeProfileWhenRequestWasSuccessful() throws ServiceException {
        doNothing().when(profile).deleteTraineeProfile("Alice.Brown");

        ResponseEntity<?> response = controller.deleteTraineeProfile("Alice.Brown");

        verify(profile, times(1)).deleteTraineeProfile("Alice.Brown");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldReturnNotFoundWhenDeleteTraineeProfileFails() throws ServiceException {
        doThrow(new ServiceException("Trainee not found")).when(profile).deleteTraineeProfile("Ghost.User");

        ResponseEntity<?> response = controller.deleteTraineeProfile("Ghost.User");

        assertEquals(404, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Trainee not found", body.get("error"));
    }

    @Test
    void shouldUpdateTraineeStatusWhenChangedToActivate() throws ServiceException {
        StatusDto dto = new StatusDto();
        dto.setUsername("Alice.Brown");
        dto.setActive(true);

        doNothing().when(traineeService).activateTrainee("Alice.Brown");

        ResponseEntity<?> response = controller.updateTraineeStatus(dto);

        verify(traineeService, times(1)).activateTrainee("Alice.Brown");
        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Trainee activated successfully", body.get("message"));
    }

    @Test
    void shouldUpdateTraineeStatusWhenChangedToDeactivate() throws ServiceException {
        StatusDto dto = new StatusDto();
        dto.setUsername("Alice.Brown");
        dto.setActive(false);

        doNothing().when(traineeService).deactivateTrainee("Alice.Brown");

        ResponseEntity<?> response = controller.updateTraineeStatus(dto);

        verify(traineeService, times(1)).deactivateTrainee("Alice.Brown");
        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Trainee deactivated successfully", body.get("message"));
    }
}
