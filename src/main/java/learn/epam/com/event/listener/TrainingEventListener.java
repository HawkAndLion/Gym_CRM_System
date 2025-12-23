package learn.epam.com.event.listener;

import learn.epam.com.client.TrainingWorkloadEventProducer;
import learn.epam.com.dto.client.ActionType;
import learn.epam.com.dto.client.TrainingEventDto;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.event.TrainingCreatedEvent;
import learn.epam.com.event.TrainingDeletedEvent;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TrainingEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingEventListener.class);
    private static final String TRAINER_NOT_FOUND = "Trainer not found";
    private static final String DURATION_MUST_BE_POSITIVE = "Duration must be positive";
    private static final String TRAINING_EVENT_SENT_TO_QUEUE = "Training event sent to queue: trainingId={}, action={}";

    private final TrainerService trainerService;
    private final TrainingWorkloadEventProducer workloadProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTrainingCreated(TrainingCreatedEvent event) throws ServiceException {
        publish(event.getTraining(), ActionType.ADD);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTrainingDeleted(TrainingDeletedEvent event) throws ServiceException {
        publish(event.getTraining(), ActionType.DELETE);
    }

    private void publish(Training training, ActionType type) throws ServiceException {

        Trainer trainer = trainerService.findById(training.getTrainerId())
                .orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND));

        TrainingEventDto dto = new TrainingEventDto(
                training.getId(),
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.isActive(),
                training.getTrainingDate(),
                toMinutes(training.getDuration()),
                type
        );

        workloadProducer.send(dto);

        LOG.info(TRAINING_EVENT_SENT_TO_QUEUE,
                training.getId(), type);
    }

    private long toMinutes(double durationInHours) {
        if (durationInHours <= 0) {
            throw new IllegalArgumentException(DURATION_MUST_BE_POSITIVE);
        }

        return Math.round(durationInHours * 60);
    }
}
