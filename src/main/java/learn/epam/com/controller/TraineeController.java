package learn.epam.com.controller;

import jakarta.validation.Valid;
import learn.epam.com.api.TraineesApi;
import learn.epam.com.api.model.*;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.prometheusmetrics.CustomMetrics;
import learn.epam.com.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class TraineeController implements TraineesApi {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeController.class);
    private static final String TRAINEE_NOT_FOUND = "Trainee not found for username: ";
    private static final String TRAINEE_STATUS_UPDATED = "Trainee status updated";
    private static final String SUCCESS_REGISTER_TRAINEE = "Trainee registered successfully";
    private static final String DELETE_TRAINEE_PROFILE = "Trainee profile deleted";
    private static final String ERROR_REGISTER_TRAINEE = "Error registering trainee";
    private static final String ERROR_DELETE_TRAINEE = "Error deleting trainee profile";

    private final ProfileService profile;
    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingTypeService trainingTypeService;
    private final TrainingService trainingService;
    private final CustomMetrics customMetrics;

    @Override
    public ResponseEntity<Void> registerTrainee(
            @Valid @RequestBody TraineeCreateRequest request) {
        try {
            customMetrics.incrementTraineeCreated();
            profile.registerTrainee(request);

            LOG.info(SUCCESS_REGISTER_TRAINEE);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LOG.error(ERROR_REGISTER_TRAINEE, e);

            return ResponseEntity.badRequest().build();
        }
    }


    @Override
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(@RequestParam String username) throws ServiceException {
        Trainee trainee = traineeService.findTraineeByUsername(username)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_NOT_FOUND + username));

        TraineeProfileResponse response = profile.getTraineeProfile(trainee);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> updateTraineeProfile(
            @Valid @RequestBody TraineeProfileResponse request) throws ServiceException {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        profile.updateTraineeProfile(username, request);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteTraineeProfile(@RequestParam String username) throws ServiceException {
        try {
            profile.deleteTraineeProfile(username);

            LOG.info(DELETE_TRAINEE_PROFILE);

            return ResponseEntity.ok().build();
        } catch (ServiceException e) {
            LOG.error(ERROR_DELETE_TRAINEE, e);

            return ResponseEntity.status(404).build();
        }
    }

    @Override
    public ResponseEntity<List<TrainerProfileResponse>> updateTraineeTrainers(
            @Valid @RequestBody TraineeTrainersRequest request) throws ServiceException {

        Set<Trainer> newTrainers = trainerService.getTrainersByUsername(request);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        traineeService.update(username, newTrainers);

        trainingService.updateTrainingsByTrainee(username, newTrainers);

        List<TrainerProfileResponse> trainerList = trainerService.getTrainerProfileResponse(newTrainers);

        return ResponseEntity.ok(trainerList);
    }

    @Override
    public ResponseEntity<List<TrainingResponse>> getTraineeTrainings(
            @RequestParam(name = "traineeUsername") String username,
            @RequestParam(required = false, name = "periodFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, name = "periodTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, name = "trainerName") String trainerName,
            @RequestParam(required = false, name = "trainingType") String trainingType) throws ServiceException {

        Long trainingTypeId = trainingTypeService.getTrainingTypeId(trainingType);

        List<Training> trainings = trainingService.findTrainingsForTraineeByCriteria(username, fromDate, toDate,
                trainerName, trainingTypeId);

        List<TrainingResponse> response = trainingService.getTrainingResponseList(trainings);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> updateTraineeStatus(
            @Valid @RequestBody TraineeStatusRequest request) throws ServiceException {

        if (!request.getActive()) {
            traineeService.deactivateTrainee(request.getUsername());
        } else {
            traineeService.activateTrainee(request.getUsername());
        }

        LOG.info(TRAINEE_STATUS_UPDATED);

        return ResponseEntity.ok().build();
    }
}
