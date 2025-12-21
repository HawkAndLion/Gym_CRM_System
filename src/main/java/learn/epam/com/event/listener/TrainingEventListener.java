package learn.epam.com.event.listener;

import learn.epam.com.client.TrainerWorkloadClient;
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

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TrainingEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingEventListener.class);
    private static final String TRAINER_NOT_FOUND = "Trainer not found";
    private static final String WORKLOAD_SERVICE_NOTIFIED = "Workload service notified. EventType={}";
    private static final String FAIL_TO_NOTIFY = "Failed to notify workload service";
    private static final String DURATION_MUST_BE_POSITIVE = "Duration must be positive";

    private final TrainerWorkloadClient trainerWorkload;
    private final TrainerService trainerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTrainingCreated(TrainingCreatedEvent event) {
        notifyWorkloadService(event.getTraining(), ActionType.ADD);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTrainingDeleted(TrainingDeletedEvent event) {
        notifyWorkloadService(event.getTraining(), ActionType.DELETE);
    }

    private void notifyWorkloadService(Training training, ActionType type) {
        try {
            Trainer trainer = trainerService.findById(training.getTrainerId())
                    .orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND));

            TrainingEventDto dto = new TrainingEventDto();
            dto.setUsername(trainer.getUser().getUsername());
            dto.setFirstName(trainer.getUser().getFirstName());
            dto.setLastName(trainer.getUser().getLastName());
            dto.setActive(trainer.isActive());
            dto.setTrainingDate(training.getTrainingDate());

            long durationMinutes = toMinutes(training.getDuration());

            if (type == ActionType.DELETE) {
                durationMinutes = -durationMinutes;
            }
            dto.setDurationMinutes(durationMinutes);

            dto.setActionType(type);

            trainerWorkload.processTrainingEvent(dto, UUID.randomUUID().toString());

            LOG.info(WORKLOAD_SERVICE_NOTIFIED, type);

        } catch (ServiceException e) {
            LOG.error(FAIL_TO_NOTIFY, e);
        }
    }

    private long toMinutes(double durationInHours) {
        if (durationInHours <= 0) {
            throw new IllegalArgumentException(DURATION_MUST_BE_POSITIVE);
        }
        return Math.round(durationInHours * 60);
    }
}
