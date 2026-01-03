package learn.epam.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import learn.epam.com.api.model.TrainingResponse;
import learn.epam.com.dto.*;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.prometheusmetrics.CustomMetrics;
import learn.epam.com.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trainees")
@Tag(name = "Trainees API", description = "Operations related to trainees")
public class TraineeController {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeController.class);
    private static final String SUCCESS_REGISTRATION_TRAINEE = "Trainee was registered successfully: username={}";
    private static final String SUCCESS_REGISTRATION_MESSAGE = "Trainee was successfully registered";
    private static final String ERROR_REGISTER_TRAINEE = "Error in registerTrainee: {}";
    private static final String ERROR = "error";
    private static final String REQUIRE_FIELDS = "All fields are required";
    private static final String SUCCESS_RETRIEVE_TRAINEE_PROFILE = "Trainee Profile was retrieved successfully!";
    private static final String ERROR_GET_TRAINEE_PROFILE = "Error in getTraineeProfile: {}";
    private static final String SUCCESS_TRAINEE_UPDATE = "Trainee profile updated successfully for username: {}";
    private static final String ERROR_UPDATE_TRAINEE_PROFILE = "Error in updateTraineeProfile: {}";
    private static final String SUCCESS_DELETE = "TraineeProfile was deleted successfully.";
    private static final String ERROR_DELETE = "Error in deleteTraineeProfile: {}";
    private static final String ERROR_UPDATE_TRAINER_LIST = "Error in updating Trainee's trainerList: {}";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found for username: ";
    private static final String ERROR_FETCH_TRAININGS = "Error fetching trainee trainings: {}";
    private static final String MESSAGE = "message";
    private static final String TRAINEE = "Trainee ";
    private static final String ACTIVATED = "activated";
    private static final String DEACTIVATED = "deactivated";
    private static final String SUCCESS = " successfully";
    private static final String ERROR_UPDATE_STATUS = "Error updating trainee status: {}";

    private final ProfileService profile;
    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingTypeService trainingTypeService;
    private final TrainingService trainingService;
    private final CustomMetrics customMetrics;

    @PostMapping
    @Operation(summary = "Register new trainee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully registered"),
            @ApiResponse(responseCode = "400", description = "Missing required fields or validation failed")
    })
    public ResponseEntity<?> registerTrainee(@RequestBody TraineeDto request) {
        try {
            return customMetrics.recordTraineeRegistration(() -> {
                UserDetailsDto response = profile.registerTrainee(request);

                LOG.info(SUCCESS_REGISTRATION_TRAINEE, response.getUsername());

                customMetrics.incrementTraineeCreated();

                return ResponseEntity.ok(SUCCESS_REGISTRATION_MESSAGE);
            });
        } catch (Exception e) {
            LOG.error(ERROR_REGISTER_TRAINEE, e.getMessage(), e);

            return ResponseEntity.badRequest().body(Map.of(ERROR, e.getMessage()));
        }
    }


    @GetMapping("/profile")
    @Operation(summary = "Get trainee profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee profile returned"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @ResponseBody
    public ResponseEntity<?> getTraineeProfile(
            @RequestParam(name = "username") String username) {
        try {
            Trainee trainee = traineeService.findTraineeByUsername(username)
                    .orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND + username));

            TraineeProfileDto profileDto = profile.getTraineeProfile(trainee);

            LOG.info(SUCCESS_RETRIEVE_TRAINEE_PROFILE);

            return ResponseEntity.ok(profileDto);

        } catch (ServiceException e) {
            LOG.error(ERROR_GET_TRAINEE_PROFILE, e.getMessage());

            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @Operation(summary = "Update trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<?> updateTraineeProfile(
            @RequestBody TraineeProfileDto request) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            if (username != null && request.getFirstName() != null && request.getLastName() != null) {

                TraineeProfileDto updatedProfile = profile.updateTraineeProfile(username, request);

                LOG.info(SUCCESS_TRAINEE_UPDATE, username);

                return ResponseEntity.ok(updatedProfile);
            } else {
                throw new ServiceException(REQUIRE_FIELDS);
            }
        } catch (ServiceException e) {
            LOG.error(ERROR_UPDATE_TRAINEE_PROFILE, e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(ERROR, e.getMessage()));
        }
    }

    @DeleteMapping("/profile")
    @Operation(summary = "Delete trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile deleted"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    public ResponseEntity<?> deleteTraineeProfile(
            @RequestParam(name = "username") String username) {
        try {
            profile.deleteTraineeProfile(username);

            LOG.info(SUCCESS_DELETE);

            return ResponseEntity.ok().build();

        } catch (ServiceException e) {
            LOG.error(ERROR_DELETE, e.getMessage());

            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }

    @PutMapping("/trainers")
    @Operation(summary = "Update trainee's trainer list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee's trainers updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> updateTraineeTrainers(
            @Valid @RequestBody TraineeTrainersDto request) {
        try {
            Set<Trainer> newTrainers = trainerService.getTrainersByUsername(request);

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            traineeService.update(username, newTrainers);

            trainingService.updateTrainingsByTrainee(username, newTrainers);

            List<TrainerProfileDto> trainerList = trainerService.getTrainerProfileDtos(newTrainers);

            return ResponseEntity.ok(trainerList);

        } catch (ServiceException e) {
            LOG.error(ERROR_UPDATE_TRAINER_LIST, e.getMessage());

            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }

    @GetMapping("/trainings")
    @Operation(summary = "Get trainee's trainings list by criteria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of trainings returned successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> getTraineeTrainings(
            @RequestParam(name = "traineeUsername") String username,
            @RequestParam(required = false, name = "periodFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, name = "periodTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, name = "trainerName") String trainerName,
            @RequestParam(required = false, name = "trainingType") String trainingType) {

        try {
            Long trainingTypeId = trainingTypeService.getTrainingTypeId(trainingType);

            List<Training> trainings = trainingService.findTrainingsForTraineeByCriteria(username, fromDate, toDate,
                    trainerName, trainingTypeId);

            List<TrainingResponse> response = trainingService.getTrainingResponseList(trainings);

            return ResponseEntity.ok(response);

        } catch (ServiceException e) {
            LOG.error(ERROR_FETCH_TRAININGS, e.getMessage());

            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }

    @PatchMapping("/status")
    @Operation(summary = "Activate or deactivate a trainee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> updateTraineeStatus(
            @Valid @RequestBody StatusDto request) {
        try {
            if (!request.isActive()) {
                traineeService.deactivateTrainee(request.getUsername());
            } else {
                traineeService.activateTrainee(request.getUsername());
            }

            return ResponseEntity.ok(Map.of(MESSAGE, TRAINEE + (request.isActive() ? ACTIVATED : DEACTIVATED) + SUCCESS));
        } catch (ServiceException e) {
            LOG.error(ERROR_UPDATE_STATUS, e.getMessage());

            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }
}
