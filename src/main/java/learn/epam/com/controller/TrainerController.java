package learn.epam.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import learn.epam.com.dto.StatusDto;
import learn.epam.com.dto.TrainerDto;
import learn.epam.com.dto.TrainerProfileDto;
import learn.epam.com.dto.TrainingDto;
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
@RequestMapping("/api/trainers")
@Tag(name = "Trainers API", description = "Operations related to trainers")
public class TrainerController {
    private static final Logger LOG = LoggerFactory.getLogger(TrainerController.class);
    private static final String ERROR = "error";
    private static final String SUCCESS_REGISTER_TRAINER = "Trainer was registered successfully";
    private static final String ERROR_REGISTER_TRAINER = "Error in registerTrainer: {}";
    private static final String TRAINER_USERNAME_NOT_FOUND = "Trainer not found for username: ";
    private static final String TRAINER_SUCCESS_RETRIEVE = "Trainer Profile was retrieved successfully!";
    private static final String ERROR_GET_TRAINER_PROFILE = "Error in getTrainerProfile: {}";
    private static final String TRAINER_SUCCESS_UPDATE = "Trainer profile updated successfully for username: {}";
    private static final String LACK_OF_ARGUMENTS = "Username, first name, and last name are required";
    private static final String ERROR_UPDATE_TRAINER_PROFILE = "Error in updateTrainerProfile: {}";
    private static final String TRAINER_LIST_SUCCESS_RETRIEVE = "Not assigned on trainee active trainers were retrieved successfully!";
    private static final String ERROR_GET_NOT_ASSIGNED_TRAINERS = "Error in getting not assigned on trainee active trainers: {}";
    private static final String ERROR_FETCH_TRAININGS = "Error fetching trainer trainings: {}";
    private static final String MESSAGE = "message";
    private static final String TRAINER = "Trainer ";
    private static final String ACTIVATED = "activated";
    private static final String DEACTIVATED = "deactivated";
    private static final String SUCCESS = " successfully";
    private static final String ERROR_UPDATE_STATUS = "Error updating trainer status: {}";

    private final ProfileService profile;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final CustomMetrics customMetrics;

    @PostMapping
    @Operation(summary = "Register new trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully registered"),
            @ApiResponse(responseCode = "400", description = "Missing required fields or validation failed")
    })
    public ResponseEntity<?> registerTrainer(@RequestBody TrainerDto request) {
        try {
            return customMetrics.recordTrainerRegistration(() -> {
                profile.createTrainerProfile(request);
                customMetrics.incrementTrainerCreated();

                LOG.info(SUCCESS_REGISTER_TRAINER);

                return ResponseEntity.ok(SUCCESS_REGISTER_TRAINER);
            });
        } catch (Exception e) {
            LOG.error(ERROR_REGISTER_TRAINER, e.getMessage(), e);

            return ResponseEntity.badRequest().body(Map.of(ERROR, e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "Get trainer profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee profile returned"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @ResponseBody
    public ResponseEntity<?> getTrainerProfile(
            @RequestParam(name = "username") String username) {
        try {
            Trainer trainer = trainerService.findTrainerByUsername(username)
                    .orElseThrow(() -> new ServiceException(TRAINER_USERNAME_NOT_FOUND + username));

            TrainerProfileDto profileDto = profile.getTrainerProfile(trainer);

            LOG.info(TRAINER_SUCCESS_RETRIEVE);

            return ResponseEntity.ok(profileDto);

        } catch (ServiceException e) {
            LOG.error(ERROR_GET_TRAINER_PROFILE, e.getMessage());

            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }


    @PutMapping("/profile")
    @Operation(summary = "Update trainer profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Validation failed")
    })
    public ResponseEntity<?> updateTrainerProfile(
            @RequestBody TrainerProfileDto request) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            if (username != null && request.getFirstName() != null && request.getLastName() != null) {

                TrainerProfileDto updatedProfile = profile.updateTrainerProfile(username, request);

                LOG.info(TRAINER_SUCCESS_UPDATE, username);

                return ResponseEntity.ok(updatedProfile);
            } else {
                throw new ServiceException(LACK_OF_ARGUMENTS);
            }
        } catch (ServiceException e) {
            LOG.error(ERROR_UPDATE_TRAINER_PROFILE, e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(ERROR, e.getMessage()));
        }
    }

    @GetMapping("/profile/unassigned")
    @Operation(summary = "Get not assigned on trainee active trainers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee profile returned"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    public ResponseEntity<?> getNotAssignedOnTraineeActiveTrainers(
            @RequestParam(name = "username") String username) {
        try {
            Set<TrainerProfileDto> profileDtos = trainerService.getTrainerProfileDtoList(username);

            LOG.info(TRAINER_LIST_SUCCESS_RETRIEVE);

            return ResponseEntity.ok(profileDtos);

        } catch (ServiceException e) {
            LOG.error(ERROR_GET_NOT_ASSIGNED_TRAINERS, e.getMessage());

            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }

    @GetMapping("/trainings")
    @Operation(summary = "Get trainer's trainings list by criteria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of trainings returned successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> getTrainerTrainings(
            @RequestParam(name = "Trainer's username") String username,
            @RequestParam(required = false, name = "Period from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, name = "Period to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, name = "Trainee's name") String traineeName) {

        try {
            List<Training> trainings = trainingService
                    .findTrainingsForTrainerByCriteria(username, fromDate, toDate, traineeName);

            List<TrainingDto> response = trainingService.getTrainingDtoList(trainings);

            return ResponseEntity.ok(response);

        } catch (ServiceException e) {
            LOG.error(ERROR_FETCH_TRAININGS, e.getMessage());

            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }

    @PatchMapping("/status")
    @Operation(summary = "Activate or deactivate a trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainer status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> updateTrainerStatus(
            @Valid @RequestBody StatusDto request) {
        try {
            if (!request.isActive()) {
                trainerService.deactivateTrainer(request.getUsername());
            } else {
                trainerService.activateTrainer(request.getUsername());
            }

            return ResponseEntity.ok(Map.of(MESSAGE, TRAINER + (request.isActive() ? ACTIVATED : DEACTIVATED) + SUCCESS));
        } catch (ServiceException e) {
            LOG.error(ERROR_UPDATE_STATUS, e.getMessage());

            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }
}
