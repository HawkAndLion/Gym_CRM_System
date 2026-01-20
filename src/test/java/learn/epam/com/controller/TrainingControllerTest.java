package learn.epam.com.controller;

import learn.epam.com.api.model.MessageResponse;
import learn.epam.com.api.model.TrainingRequest;
import learn.epam.com.api.model.TrainingResponse;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingController controller;

    @Test
    void shouldAddTrainingSuccessfullyWhenMethodCalled() throws Exception {
        // Given
        TrainingRequest request = new TrainingRequest()
                .name("Morning Cardio")
                .date(LocalDate.of(2025, 10, 28))
                .trainingType("Cardio")
                .duration(60.0)
                .traineeUsername("Alice.Trainee")
                .trainerUsername("John.Trainer");

        User user = new User("John", "Doe", "John.Doe", "password", true);
        Trainer trainer = new Trainer(1L, user, "Cardio", true);

        User user2 = new User("Alice", "Brown", "Alice.Brown", "password", true);
        Trainee trainee = new Trainee(1L, user2, "address", LocalDate.of(2025, 10, 12), true);

        Training training = new Training();

        when(traineeService.findTraineeByUsername("Alice.Trainee")).thenReturn(Optional.of(trainee));
        when(trainerService.findTrainerByUsername("John.Trainer")).thenReturn(Optional.of(trainer));
        when(trainingTypeService.getTrainingTypeId("Cardio")).thenReturn(1L);
        when(trainingService.update(trainee, trainer, request, 1L)).thenReturn(training);

        // When
        ResponseEntity<MessageResponse> response = controller.addTraining(request);

        // Then
        verify(traineeService).findTraineeByUsername("Alice.Trainee");
        verify(trainerService).findTrainerByUsername("John.Trainer");
        verify(trainerService).assignTrainerToTrainee("John.Trainer", "Alice.Trainee");
        verify(trainingTypeService).getTrainingTypeId("Cardio");
        verify(trainingService).update(trainee, trainer, request, 1L);
        verify(trainingService).save(training);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Training added successfully",
                response.getBody().getMessage());
    }

    @Test
    void shouldReturnNotFoundWhenTraineeDoesNotExist() throws ServiceException {
        // Given
        TrainingRequest request = new TrainingRequest()
                .traineeUsername("Unknown.Trainee");

        when(traineeService.findTraineeByUsername("Unknown.Trainee"))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<MessageResponse> response = controller.addTraining(request);

        // Then
        verify(traineeService).findTraineeByUsername("Unknown.Trainee");
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Trainee not found: Unknown.Trainee",
                response.getBody().getMessage());
    }

    @Test
    void shouldReturnNotFoundWhenTrainerDoesNotExist() throws ServiceException {
        // Given
        TrainingRequest request = new TrainingRequest();
        request.setTraineeUsername("Alice.Trainee");
        request.setTrainerUsername("Unknown.Trainer");

        User user = new User("Alice", "Brown", "Alice.Brown", "password", true);
        Trainee trainee = new Trainee(1L, user, "address", LocalDate.of(2025, 10, 12), true);


        when(traineeService.findTraineeByUsername("Alice.Trainee"))
                .thenReturn(Optional.of(trainee));
        when(trainerService.findTrainerByUsername("Unknown.Trainer"))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<MessageResponse> response = controller.addTraining(request);

        // Then
        verify(traineeService).findTraineeByUsername("Alice.Trainee");
        verify(trainerService).findTrainerByUsername("Unknown.Trainer");
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Trainer not found: Unknown.Trainer",
                response.getBody().getMessage());
    }

    @Test
    void shouldReturnAllTrainingTypesWhenMethodCalled() {
        // Given
        List<Map<String, Object>> mockTrainingTypes = List.of(
                Map.of("id", 1, "name", "Cardio"),
                Map.of("id", 2, "name", "Strength")
        );

        when(trainingTypeService.getTrainingTypes()).thenReturn(mockTrainingTypes);

        // When
        ResponseEntity<List<Map<String, Object>>> response =
                controller.getTrainingTypes();

        // Then
        verify(trainingTypeService).getTrainingTypes();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockTrainingTypes, response.getBody());
    }

    @Test
    void shouldDeleteTrainingSuccessfullyWhenMethodCalled() throws Exception {
        // Given
        Training training = new Training();
        training.setId(1L);
        training.setTraineeId(10L);
        training.setTrainerId(20L);

        User user = new User("John", "Doe", "John.Doe", "password", true);
        Trainer trainer = new Trainer(20L, user, "Cardio", true);

        User user2 = new User("Alice", "Brown", "Alice.Brown", "password", true);
        Trainee trainee = new Trainee(10L, user2, "address", LocalDate.of(2025, 10, 12), true);

        when(trainingService.findById(1L)).thenReturn(Optional.of(training));
        when(traineeService.findById(10L)).thenReturn(Optional.of(trainee));
        when(trainerService.findById(20L)).thenReturn(Optional.of(trainer));
        when(traineeService.getTrainerIdsForTrainee(10L)).thenReturn(Set.of());

        // When
        ResponseEntity<MessageResponse> response =
                controller.deleteTraining(1L);

        // Then
        verify(trainingService).findById(1L);
        verify(trainingService).deleteById(1L);
        verify(traineeService).update(any(), any());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Training deleted successfully",
                response.getBody().getMessage());
    }

    @Test
    void shouldReturnNotFoundWhenDeleteTrainingNotExists() throws ServiceException {
        // Given
        when(trainingService.findById(123L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<MessageResponse> response =
                controller.deleteTraining(123L);

        // Then
        verify(trainingService).findById(123L);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Training not found ",
                response.getBody().getMessage());
    }

    @Test
    void shouldReturnAllTrainingsSuccessfullyWhenGetTrainings() {
        // Given
        Training training = new Training();
        List<Training> trainings = List.of(training);

        TrainingResponse responseDto = new TrainingResponse();
        List<TrainingResponse> responseList = List.of(responseDto);

        when(trainingService.findAllTrainings()).thenReturn(trainings);
        when(trainingService.getTrainingResponseList(trainings)).thenReturn(responseList);

        // When
        ResponseEntity<List<TrainingResponse>> response = controller.getTrainings();

        // Then
        verify(trainingService).findAllTrainings();
        verify(trainingService).getTrainingResponseList(trainings);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseList, response.getBody());
    }

    @Test
    void shouldReturnInternalServerErrorWhenExceptionOccurs() {
        // Given
        when(trainingService.findAllTrainings())
                .thenThrow(new RuntimeException("DB error"));

        // When
        ResponseEntity<List<TrainingResponse>> response = controller.getTrainings();

        // Then
        verify(trainingService).findAllTrainings();
        assertEquals(500, response.getStatusCodeValue());
        assertEquals(List.of(), response.getBody());
    }

    @Test
    void shouldUpdateTraineeWithRemainingTrainersWhenDeletingTraining() throws Exception {
        // Given
        Training training = new Training();
        training.setId(1L);
        training.setTraineeId(10L);
        training.setTrainerId(20L);

        User userTrainer = new User("John", "Doe", "John.Doe", "password", true);
        Trainer removedTrainer = new Trainer(20L, userTrainer, "Cardio", true);

        User userOtherTrainer = new User("Bob", "Smith", "Bob.Smith", "password", true);
        Trainer remainingTrainer = new Trainer(30L, userOtherTrainer, "Strength", true);

        User userTrainee = new User("Alice", "Brown", "Alice.Brown", "password", true);
        Trainee trainee = new Trainee(10L, userTrainee, "address",
                LocalDate.of(2025, 10, 12), true);

        when(trainingService.findById(1L)).thenReturn(Optional.of(training));
        when(traineeService.findById(10L)).thenReturn(Optional.of(trainee));
        when(trainerService.findById(20L)).thenReturn(Optional.of(removedTrainer));
        when(trainerService.findById(30L)).thenReturn(Optional.of(remainingTrainer));

        when(traineeService.getTrainerIdsForTrainee(10L))
                .thenReturn(Set.of(20L, 30L));

        // When
        ResponseEntity<MessageResponse> response =
                controller.deleteTraining(1L);

        // Then
        verify(trainingService).deleteById(1L);
        verify(traineeService).update(
                eq("Alice.Brown"),
                argThat(set ->
                        set.size() == 1 &&
                                set.iterator().next().getId().equals(30L)
                )
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Training deleted successfully",
                response.getBody().getMessage());
    }
}
