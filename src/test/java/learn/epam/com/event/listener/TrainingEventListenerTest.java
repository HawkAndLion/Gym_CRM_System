package learn.epam.com.event.listener;

import learn.epam.com.client.TrainingWorkloadEventProducer;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.event.TrainingCreatedEvent;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingEventListenerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingWorkloadEventProducer workloadProducer;

    @InjectMocks
    private TrainingEventListener listener;

    @Test
    void shouldPublishEventWhenTrainingCreated() throws ServiceException {
        // Given
        Training training = new Training(1L, 1L, 2L, "Workout", 2L, LocalDate.of(2025, 10, 1), 1.5);
        Trainer trainer = new Trainer(2L, new User(10L, "John", "Doe", "trainer", "pass", true), "Gym", true, new HashSet<>());
        when(trainerService.findById(2L)).thenReturn(Optional.of(trainer));

        TrainingCreatedEvent event = new TrainingCreatedEvent(training, "trainee", "trainer");

        // When
        listener.handleTrainingCreated(event);

        // Then
        verify(workloadProducer).send(argThat(dto ->
                dto.getTrainingId().equals(1L) &&
                        dto.getUsername().equals("trainer") &&
                        dto.getDurationMinutes() == 90L
        ));
    }

    @Test
    void shouldThrowExceptionIfTrainerNotFound() throws ServiceException {
        Training training = new Training(1L, 1L, 2L, "Workout", 2L, LocalDate.of(2025, 10, 1), 1.5);
        when(trainerService.findById(2L)).thenReturn(Optional.empty());
        TrainingCreatedEvent event = new TrainingCreatedEvent(training, "trainee", "trainer");

        assertThrows(ServiceException.class, () -> listener.handleTrainingCreated(event));
    }

    @Test
    void shouldThrowIfDurationNonPositive() throws ServiceException {
        Training training = new Training(1L, 1L, 2L, "Workout", 2L, LocalDate.of(2025, 10, 1), 0.0);

        Trainer trainer = new Trainer(2L, new User(10L, "John", "Doe", "trainer", "pass", true), "Gym", true, new HashSet<>());
        when(trainerService.findById(2L)).thenReturn(Optional.of(trainer));

        TrainingCreatedEvent event = new TrainingCreatedEvent(training, "trainee", "trainer");

        assertThrows(IllegalArgumentException.class, () -> listener.handleTrainingCreated(event));
    }

}
