package learn.epam.com.controller;

import jakarta.validation.Valid;
import learn.epam.com.api.TrainersApi;
import learn.epam.com.api.model.*;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.prometheusmetrics.CustomMetrics;
import learn.epam.com.service.ProfileService;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainerService;
import learn.epam.com.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrainerController implements TrainersApi {
    private static final Logger LOG = LoggerFactory.getLogger(TrainerController.class);
    private static final String SUCCESS_REGISTER_TRAINER = "Trainer was registered successfully";
    private static final String TRAINER_USERNAME_NOT_FOUND = "Trainer not found for username: ";
    private static final String TRAINER_SUCCESS_RETRIEVE = "Trainer Profile was retrieved successfully!";
    private static final String SUCCESS_STATUS_UPDATE = "Trainer status updated successfully";
    private static final String TRAINER_NOT_FOUND = "Trainer not found";
    private static final String ERROR_FETCH_TRAININGS = "Error fetching trainings";

    private final ProfileService profile;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final CustomMetrics customMetrics;

    @Override
    public ResponseEntity<MessageResponse> registerTrainer(
            @Valid @RequestBody TrainerCreateRequest request) throws ServiceException {

        profile.createTrainerProfile(request);
        customMetrics.incrementTrainerCreated();

        return ResponseEntity.ok(
                new MessageResponse().message(SUCCESS_REGISTER_TRAINER)
        );
    }

    @Override
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @RequestParam String username) {
        try {
            Trainer trainer = trainerService.findTrainerByUsername(username)
                    .orElseThrow(() ->
                            new ServiceException(HttpStatus.NOT_FOUND, TRAINER_USERNAME_NOT_FOUND + username));

            TrainerProfileResponse response = profile.getTrainerProfile(trainer);

            LOG.info(TRAINER_SUCCESS_RETRIEVE);

            return ResponseEntity.ok(response);
        } catch (ServiceException e) {
            LOG.error(TRAINER_NOT_FOUND, e);
            return ResponseEntity.status(404).build();
        }
    }

    @Override
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @RequestBody TrainerProfileResponse request) throws ServiceException {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        TrainerProfileResponse updated =
                profile.updateTrainerProfile(username, request);

        return ResponseEntity.ok(updated);
    }

    @Override
    public ResponseEntity<List<TrainerProfileResponse>> getNotAssignedTrainers(
            @RequestParam String username) throws ServiceException {

        List<TrainerProfileResponse> responses =
                List.copyOf(trainerService.getTrainerProfileDtoList(username));

        return ResponseEntity.ok(responses);
    }


    @Override
    public ResponseEntity<List<TrainingResponse>> getTrainerTrainings(
            @RequestParam String username,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String traineeName) {
        try {
            List<Training> trainings =
                    trainingService.findTrainingsForTrainerByCriteria(
                            username, fromDate, toDate, traineeName);

            List<TrainingResponse> responses =
                    trainingService.getTrainingResponseList(trainings);

            return ResponseEntity.ok(responses);
        } catch (ServiceException e) {
            LOG.error(ERROR_FETCH_TRAININGS, e);

            return ResponseEntity.status(404).build();
        }
    }

    @Override
    public ResponseEntity<MessageResponse> updateTrainerStatus(
            @Valid @RequestBody TrainerStatusRequest request) throws ServiceException {

        if (request.getActive()) {
            trainerService.activateTrainer(request.getUsername());
        } else {
            trainerService.deactivateTrainer(request.getUsername());
        }

        return ResponseEntity.ok(
                new MessageResponse().message(SUCCESS_STATUS_UPDATE)
        );
    }
}
