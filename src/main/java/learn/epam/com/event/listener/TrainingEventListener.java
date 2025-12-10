package learn.epam.com.event.listener;

import learn.epam.com.client.TrainerWorkloadClient;
import learn.epam.com.dto.client.ActionType;
import learn.epam.com.dto.client.TrainingEventDto;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.event.TrainingCreatedEvent;
import learn.epam.com.event.TrainingDeletedEvent;
import learn.epam.com.repository.UserRepository;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
public class TrainingEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingEventListener.class);
    private static final String TRAINER_NOT_FOUND = "Trainer not found";

    private final TrainerWorkloadClient trainerWorkload;
    private final TrainerService trainerService;
    private final UserRepository userRepository;

    public TrainingEventListener(
            TrainerWorkloadClient trainerWorkload,
            TrainerService trainerService,
            UserRepository userRepository) {
        this.trainerWorkload = trainerWorkload;
        this.trainerService = trainerService;
        this.userRepository = userRepository;
    }

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
            Trainer currentTrainer = trainerService.findById(training.getTrainerId()).orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND));
            String trainerUsername = trainerService.findById(training.getTrainerId())
                    .flatMap(trainer -> userRepository.findById(trainer.getUser().getId()))
                    .map(user -> user.getUsername())
                    .orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND));

            TrainingEventDto dto = new TrainingEventDto();
            dto.setUsername(trainerUsername);
            dto.setFirstName(currentTrainer.getUser().getFirstName());
            dto.setLastName(currentTrainer.getUser().getLastName());
            dto.setActive(currentTrainer.isActive());
            dto.setTrainingDate(training.getTrainingDate());
            dto.setDurationMinutes(toMinutes(training.getDuration()));
            dto.setActionType(type);

            trainerWorkload.processTrainingEvent(dto, UUID.randomUUID().toString());

            LOG.info("Workload service notified. EventType={}", type);

        } catch (ServiceException e) {
            LOG.error("Failed to notify workload service", e);
        }
    }

    private long toMinutes(double durationInHours) {
        if (durationInHours <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        return Math.round(durationInHours * 60);
    }
}
