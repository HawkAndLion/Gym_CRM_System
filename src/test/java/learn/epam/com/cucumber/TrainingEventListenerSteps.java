package learn.epam.com.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import learn.epam.com.client.TrainingWorkloadEventProducer;
import learn.epam.com.dto.client.ActionType;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.event.TrainingCreatedEvent;
import learn.epam.com.event.TrainingDeletedEvent;
import learn.epam.com.event.listener.TrainingEventListener;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TrainingEventListenerSteps {

    private Training training;
    private Trainer trainer;
    private TrainingCreatedEvent createdEvent;
    private TrainingDeletedEvent deletedEvent;
    private Exception thrownException;

    @Autowired
    private TrainerService trainerService;

    private TrainingWorkloadEventProducer workloadProducer;

    @Autowired
    private TrainingEventListener listener;

    @Before
    public void setup() {
        workloadProducer = mock(TrainingWorkloadEventProducer.class);
        listener = new TrainingEventListener(trainerService, workloadProducer);
    }


    @Given("a Training with id {int}, trainerId {int}, and duration {double} hours")
    public void a_training_with_id_trainer_id_and_duration_hours(
            Integer id, Integer trainerId, Double duration) {

        training = new Training(id.longValue(), 1L, trainerId.longValue(), "Workout", 2L, LocalDate.of(2025, 10, 1), duration);
    }


    @Given("the Trainer with id {int} exists")
    public void the_trainer_with_id_exists(Integer trainerId) {
        trainer = new Trainer(trainerId.longValue(), new User(10L, "John", "Doe", "trainer", "pass", true), "Gym", true, new HashSet<>());

        when(trainerService.findById(trainerId.longValue()))
                .thenReturn(Optional.of(trainer));
    }

    @Given("the Trainer with id {int} does not exist")
    public void the_trainer_with_id_does_not_exist(Integer trainerId) {
        when(trainerService.findById(trainerId.longValue()))
                .thenReturn(Optional.empty());
    }

    @When("the TrainingCreatedEvent is published")
    public void the_training_created_event_is_published() {
        createdEvent = new TrainingCreatedEvent(training, "trainee", "trainer");
        try {
            listener.handleTrainingCreated(createdEvent);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("the TrainingDeletedEvent is published")
    public void the_training_deleted_event_is_published() {
        deletedEvent = new TrainingDeletedEvent(training);
        try {
            listener.handleTrainingDeleted(deletedEvent);
        } catch (Exception e) {
            thrownException = e;
        }
    }


    @Then("the workload producer should send a training event with trainingId {int}")
    public void the_workload_producer_should_send_a_training_event_with_training_id(Integer id) {
        verify(workloadProducer).send(
                argThat(dto -> dto.getTrainingId().equals(id.longValue()))
        );
    }

    @Then("the action type should be ADD")
    public void the_action_type_should_be_add() {
        verify(workloadProducer).send(argThat(dto -> dto.getActionType() == ActionType.ADD));
    }

    @Then("the action type should be DELETE")
    public void the_action_type_should_be_delete() {
        verify(workloadProducer).send(argThat(dto -> dto.getActionType() == ActionType.DELETE));
    }

    @Then("a ServiceException should be thrown")
    public void a_service_exception_should_be_thrown() {
        assertNotNull(thrownException);
        assertTrue(thrownException instanceof ServiceException);
    }

    @Then("an IllegalArgumentException should be thrown")
    public void an_illegal_argument_exception_should_be_thrown() {
        assertNotNull(thrownException);
        assertTrue(thrownException instanceof IllegalArgumentException);
    }
}

