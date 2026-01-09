package learn.epam.com.controller;

import learn.epam.com.api.model.TraineeCreateRequest;
import learn.epam.com.api.model.TraineeProfileResponse;
import learn.epam.com.api.model.TraineeStatusRequest;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.User;
import learn.epam.com.prometheusmetrics.CustomMetrics;
import learn.epam.com.service.ProfileService;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TraineeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private ProfileService profile;

    @Mock
    private TraineeService traineeService;

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
}
