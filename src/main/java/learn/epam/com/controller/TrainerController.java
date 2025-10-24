package learn.epam.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import learn.epam.com.dto.*;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainers API", description = "Operations related to trainers")
public class TrainerController {
    private static final Logger LOG = LoggerFactory.getLogger(GymRestController.class);
    private static final String USER_NOT_FOUND = "User was not found. Check if firstname and lastname exist.";
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
    private static final String UNKNOWN_TRAINEE = "Unknown Trainee";
    private static final String SPACE = " ";
    private static final String UNKNOWN_TRAINING_TYPE = "Unknown Training Type";
    private static final String ERROR_MAPPING_TRAINING = "Error mapping training: {}";
    private static final String ERROR_FETCH_TRAININGS = "Error fetching trainer trainings: {}";
    private static final String MESSAGE = "message";
    private static final String TRAINER = "Trainer ";
    private static final String ACTIVATED = "activated";
    private static final String DEACTIVATED = "deactivated";
    private static final String SUCCESS = " successfully";
    private static final String ERROR_UPDATE_STATUS = "Error updating trainer status: {}";

    private final ProfileService profile;
    private final UserService userService;
    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingTypeService trainingTypeService;
    private final TrainingService trainingService;

    public TrainerController(ProfileService profile, UserService userService, TrainerService trainerService, TraineeService traineeService, TrainingTypeService trainingTypeService, TrainingService trainingService) {
        this.profile = profile;
        this.userService = userService;
        this.trainerService = trainerService;
        this.traineeService = traineeService;
        this.trainingTypeService = trainingTypeService;
        this.trainingService = trainingService;
    }


    @PostMapping
    @Operation(summary = "Register new trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully registered"),
            @ApiResponse(responseCode = "400", description = "Missing required fields or validation failed")
    })
    public ResponseEntity<?> registerTrainer(@RequestBody TrainerDto request) {
        String firstName = request.getFirstName();
        String lastName = request.getLastName();
        String specialization = request.getSpecialization();

        try {
            User user = new User(firstName, lastName, null, null, true);
            Trainer trainer = new Trainer(specialization, true);

            profile.createTrainerProfile(user, trainer);

            User extractedUser = userService.findAllUsers().stream().filter(u -> u.getFirstName().equalsIgnoreCase(firstName) && u.getLastName().equalsIgnoreCase(lastName)).findFirst().orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
            UserDetailsDto response = new UserDetailsDto(extractedUser.getUsername(), extractedUser.getPassword());

            LOG.info(SUCCESS_REGISTER_TRAINER);

            return ResponseEntity.ok(response);

        } catch (ServiceException e) {
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
            @Parameter(description = "Header: Username", required = true)
            @RequestHeader("Username") String headerUsername,
            @Parameter(description = "Header: Password", required = true)
            @RequestHeader("Password") String headerPassword,
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
            @Parameter(description = "Header: Username", required = true)
            @RequestHeader("Username") String headerUsername,
            @Parameter(description = "Header: Password", required = true)
            @RequestHeader("Password") String headerPassword,
            @RequestBody TrainerProfileDto request) {
        try {
            if (request.getUsername() != null && request.getFirstName() != null && request.getLastName() != null) {

                TrainerProfileDto updatedProfile = profile.updateTrainerProfile(request.getUsername(), request);

                LOG.info(TRAINER_SUCCESS_UPDATE, request.getUsername());

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
            @Parameter(description = "Header: Username", required = true)
            @RequestHeader("Username") String headerUsername,
            @Parameter(description = "Header: Password", required = true)
            @RequestHeader("Password") String headerPassword,
            @RequestParam(name = "username") String username) {
        try {
            List<Trainer> trainers = trainerService.getUnassignedTrainersForTrainee(username);

            Set<TrainerProfileDto> profileDtos = new HashSet<>(Set.of());

            for (Trainer trainer : trainers) {
                User user = userService.findById(trainer.getUser().getId()).orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
                TrainerProfileDto profileDto = new TrainerProfileDto(user.getUsername(), user.getFirstName(), user.getLastName(), trainer.getSpecialization());
                profileDtos.add(profileDto);
            }

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
            @Parameter(description = "Header: Username", required = true)
            @RequestHeader("Username") String headerUsername,
            @Parameter(description = "Header: Password", required = true)
            @RequestHeader("Password") String headerPassword,
            @RequestParam(name = "Trainer's username") String username,
            @RequestParam(required = false, name = "Period from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, name = "Period to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, name = "Trainee's name") String traineeName) {

        try {
            List<Training> trainings = trainingService
                    .findTrainingsForTrainerByCriteria(username, fromDate, toDate, traineeName);

            List<TrainingDto> response = trainings.stream().map(training -> {
                try {
                    String traineeFullName = traineeService
                            .findById(training.getTraineeId())
                            .flatMap(trainee -> userService.findById(trainee.getUser().getId()))
                            .map(u -> u.getFirstName() + SPACE + u.getLastName())
                            .orElse(UNKNOWN_TRAINEE);

                    String trainingTypeName = trainingTypeService
                            .findById(training.getTrainingTypeId())
                            .map(tt -> tt.getName())
                            .orElse(UNKNOWN_TRAINING_TYPE);

                    return new TrainingDto(
                            training.getName(),
                            training.getTrainingDate(),
                            trainingTypeName,
                            training.getDuration(),
                            traineeFullName
                    );
                } catch (ServiceException e) {
                    LOG.error(ERROR_MAPPING_TRAINING, e.getMessage());

                    return null;
                }
            }).filter(Objects::nonNull).toList();

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
            @Parameter(description = "Header: Username", required = true)
            @RequestHeader("Username") String headerUsername,
            @Parameter(description = "Header: Password", required = true)
            @RequestHeader("Password") String headerPassword,
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
