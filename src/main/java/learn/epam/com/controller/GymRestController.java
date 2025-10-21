package learn.epam.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import learn.epam.com.dto.*;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.main.GymFacade;
import learn.epam.com.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Gym API", description = "Endpoints for testing Gym CRM system") // just for testing
public class GymRestController {
    private static final Logger LOG = LoggerFactory.getLogger(GymRestController.class);
    private static final String USER_NOT_FOUND = "User was not found. Check if firstname and lastname exist.";
    private static final String LOGIN_SUCCESSFUL = "Login was successful. STATUS 200 OK";
    private static final String LOGIN_ERROR = "Login error: {}";
    private static final String INVALID_CREDENTIALS = "Invalid credentials";
    private static final String SUCCESS_REGISTRATION_TRAINEE = "Trainee was registered successfully: username={}";
    private static final String ERROR_REGISTER_TRAINEE = "Error in registerTrainee: {}";
    private static final String ERROR = "error";
    private static final String SUCCESS_REGISTER_TRAINER = "Trainer was registered successfully";
    private static final String ERROR_REGISTER_TRAINER = "Error in registerTrainer: {}";
    private static final String MISSING_USER_DETAILS = "Username or password is missing";
    private static final String SUCCESS_PASSWORD_CHANGE = "Password was changed successfully.";
    private static final String REQUIRE_FIELDS = "All fields are required";
    private static final String ERROR_CHANGE_PASSWORD = "Change password error: {}";
    private static final String SUCCESS_RETRIEVE_TRAINEE_PROFILE = "Trainee Profile was retrieved successfully!";
    private static final String ERROR_GET_TRAINEE_PROFILE = "Error in getTraineeProfile: {}";
    private static final String SUCCESS_TRAINEE_UPDATE = "Trainee profile updated successfully for username: {}";
    private static final String ERROR_UPDATE_TRAINEE_PROFILE = "Error in updateTraineeProfile: {}";

    private final GymFacade facade;

    public GymRestController(GymFacade facade) {
        this.facade = facade;
    }

    @PostMapping("/trainees")
    @Operation(summary = "Register new trainee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully registered"),
            @ApiResponse(responseCode = "400", description = "Missing required fields or validation failed")
    })
    public ResponseEntity<?> registerTrainee(@RequestBody TraineeDto request) {
        try {
            String firstName = request.getFirstName();
            String lastName = request.getLastName();
            LocalDate date = request.getDateOfBirth();
            String address = request.getAddress();

            User user = new User(firstName, lastName, null, null, true);
            Trainee trainee = new Trainee(address, date, true);

            facade.profile().createTraineeProfile(user, trainee);

            User extractedUser = facade.user().findAllUsers().stream().filter(u -> u.getFirstName().equals(firstName) && u.getLastName().equals(lastName)).findFirst().orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
            UserDetailsDto response = new UserDetailsDto(extractedUser.getUsername(), extractedUser.getPassword());

            LOG.info(SUCCESS_REGISTRATION_TRAINEE, extractedUser.getUsername());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOG.error(ERROR_REGISTER_TRAINEE, e.getMessage(), e);

            return ResponseEntity.badRequest().body(Map.of(ERROR, e.getMessage()));
        }
    }


    @PostMapping("/trainers")
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

            facade.profile().createTrainerProfile(user, trainer);

            User extractedUser = facade.user().findAllUsers().stream().filter(u -> u.getFirstName().equalsIgnoreCase(firstName) && u.getLastName().equalsIgnoreCase(lastName)).findFirst().orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
            UserDetailsDto response = new UserDetailsDto(extractedUser.getUsername(), extractedUser.getPassword());

            LOG.info(SUCCESS_REGISTER_TRAINER);

            return ResponseEntity.ok(response);

        } catch (ServiceException e) {
            LOG.error(ERROR_REGISTER_TRAINER, e.getMessage(), e);

            return ResponseEntity.badRequest().body(Map.of(ERROR, e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "404", description = "Invalid credentials")
    })
    public ResponseEntity<?> login(@RequestBody UserDetailsDto request) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();

            if (username != null && password != null && !username.isBlank() && !password.isBlank()) {

                User user = facade.user().findAllUsers().stream()
                        .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                        .findFirst()
                        .orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

                UserDto userDto = new UserDto(user.getFirstName(), user.getLastName(), user.getUsername(), user.isActive());
                return ResponseEntity.ok(userDto);

            } else {
                return ResponseEntity.badRequest().body(Map.of(ERROR, INVALID_CREDENTIALS));
            }
        } catch (ServiceException e) {
            LOG.error(LOGIN_ERROR, e.getMessage());
            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }

    @PutMapping("/login")
    @Operation(summary = "Change password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "404", description = "Invalid input or user")
    })
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto request) {
        try {
            String username = request.getUsername();
            String oldPassword = request.getOldPassword();
            String newPassword = request.getNewPassword();

            if (username != null && oldPassword != null && newPassword != null) {
                facade.profile().changePassword(username, oldPassword, newPassword);

                LOG.info(SUCCESS_PASSWORD_CHANGE);

                User user = facade.user().findAllUsers().stream().filter(u -> u.getUsername().equals(username)).findFirst().orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
                UserDetailsDto response = new UserDetailsDto(user.getUsername(), user.getPassword());

                return ResponseEntity.ok(response);
            }

            throw new ServiceException(REQUIRE_FIELDS);

        } catch (ServiceException e) {
            LOG.error(ERROR_CHANGE_PASSWORD, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/trainees/profile")
    @Operation(summary = "Get trainee profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee profile returned"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @ResponseBody
    public ResponseEntity<?> getTraineeProfile(
            @RequestParam(name = "username") String username) {
        try {
            Trainee trainee = facade.trainee().findTraineeByUsername(username)
                    .orElseThrow(() -> new ServiceException("Trainee not found for username: " + username));

            TraineeProfileDto profileDto = facade.profile().getTraineeProfile(trainee);

            LOG.info(SUCCESS_RETRIEVE_TRAINEE_PROFILE);

            return ResponseEntity.ok(profileDto);

        } catch (ServiceException e) {
            LOG.error(ERROR_GET_TRAINEE_PROFILE, e.getMessage());
            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }


    @PutMapping("/trainees/profile")
    @Operation(summary = "Update trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Validation failed")
    })
    public ResponseEntity<?> updateTraineeProfile(@RequestBody TraineeProfileDto request) {
        try {
            if (request.getUsername() != null && request.getFirstName() != null && request.getLastName() != null) {

                TraineeProfileDto updatedProfile = facade.profile().updateTraineeProfile(request.getUsername(), request);

                LOG.info(SUCCESS_TRAINEE_UPDATE, request.getUsername());

                return ResponseEntity.ok(updatedProfile);
            } else {
                throw new ServiceException(REQUIRE_FIELDS);
            }
        } catch (ServiceException e) {
            LOG.error(ERROR_UPDATE_TRAINEE_PROFILE, e.getMessage());

            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/trainees/profile")
    @Operation(summary = "Delete trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile deleted"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    public ResponseEntity<?> deleteTraineeProfile(@RequestParam(name = "username") String username) {
        try {
            facade.profile().deleteTraineeProfile(username);

            LOG.info("TraineeProfile was deleted successfully.");

            return ResponseEntity.ok().build();

        } catch (ServiceException e) {
            LOG.error("Error in deleteTraineeProfile: {}", e.getMessage());

            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/trainers/profile")
    @Operation(summary = "Get trainer profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee profile returned"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @ResponseBody
    public ResponseEntity<?> getTrainerProfile(
            @RequestParam(name = "username") String username) {
        try {
            Trainer trainer = facade.trainer().findTrainerByUsername(username)
                    .orElseThrow(() -> new ServiceException("Trainer not found for username: " + username));

            TrainerProfileDto profileDto = facade.profile().getTrainerProfile(trainer);

            LOG.info("Trainer Profile was retrieved successfully!");

            return ResponseEntity.ok(profileDto);

        } catch (ServiceException e) {
            LOG.error("Error in getTrainerProfile: {}", e.getMessage());

            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/trainers/profile")
    @Operation(summary = "Update trainer profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Validation failed")
    })
    public ResponseEntity<?> updateTrainerProfile(@RequestBody TrainerProfileDto request) {
        try {
            if (request.getUsername() != null && request.getFirstName() != null && request.getLastName() != null) {

                TrainerProfileDto updatedProfile = facade.profile().updateTrainerProfile(request.getUsername(), request);

                LOG.info("Trainer profile updated successfully for username: {}", request.getUsername());

                return ResponseEntity.ok(updatedProfile);
            } else {
                throw new ServiceException("Username, first name, and last name are required");
            }
        } catch (ServiceException e) {
            LOG.error("Error in updateTrainerProfile: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/trainers/profile/unassigned")
    @Operation(summary = "Get not assigned on trainee active trainers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee profile returned"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    public ResponseEntity<?> getNotAssignedOnTraineeActiveTrainers(
            @RequestParam(name = "username") String username) {
        try {
            List<Trainer> trainers = facade.trainer().getUnassignedTrainersForTrainee(username);

            Set<TrainerProfileDto> profileDtos = new HashSet<>(Set.of());

            for (Trainer trainer : trainers) {
                User user = facade.user().findById(trainer.getUserId()).orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
                TrainerProfileDto profileDto = new TrainerProfileDto(user.getUsername(), user.getFirstName(), user.getLastName(), trainer.getSpecialization());
                profileDtos.add(profileDto);
            }

            LOG.info("Not assigned on trainee active trainers were retrieved successfully!");

            return ResponseEntity.ok(profileDtos);

        } catch (ServiceException e) {
            LOG.error("Error in getting not assigned on trainee active trainers: {}", e.getMessage());

            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/trainees/trainers")
    @Operation(summary = "Update trainee's trainer list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee's trainers updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> updateTraineeTrainers(@Valid @RequestBody TraineeTrainersDto request) {
        try {
            Trainee trainee = facade.trainee().findTraineeByUsername(request.getTraineeUsername())
                    .orElseThrow(() -> new ServiceException("Trainee entity not found for username: " + request.getTraineeUsername()));

            Set<Trainer> newTrainers = new HashSet<>();
            for (String trainerUsername : request.getTrainerUsernames()) {
                Trainer trainer = facade.trainer().findTrainerByUsername(trainerUsername)
                        .orElseThrow(() -> new ServiceException("Trainer entity not found for username: " + trainerUsername));

                newTrainers.add(trainer);
            }

            trainee.setTrainers(newTrainers);
            facade.trainee().save(trainee);

            List<TrainerProfileDto> trainerList = newTrainers.stream()
                    .map(trainer -> {
                        User trainerUser = facade.user().findById(trainer.getUserId()).orElse(null);
                        if (trainerUser != null) {
                            return new TrainerProfileDto(
                                    trainerUser.getUsername(),
                                    trainerUser.getFirstName(),
                                    trainerUser.getLastName(),
                                    trainer.getSpecialization()
                            );
                        }

                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();

            return ResponseEntity.ok(trainerList);

        } catch (ServiceException e) {
            LOG.error("Error in updating Trainee's trainerList: {}", e.getMessage());

            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/trainees/trainings")
    @Operation(summary = "Get trainee's trainings list by criteria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of trainings returned successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> getTraineeTrainings(
            @RequestParam(name = "Trainee's username") String username,
            @RequestParam(required = false, name = "Period from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false, name = "Period to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, name = "Trainer's name") String trainerName,
            @RequestParam(required = false, name = "Training type") String trainingType) {

        try {
            Long trainingTypeId = null;
            if (trainingType != null && !trainingType.isBlank()) {
                trainingTypeId = facade.trainingType().findAllTrainingTypes().stream()
                        .filter(tt -> tt.getName().equalsIgnoreCase(trainingType))
                        .map(tt -> tt.getId())
                        .findFirst()
                        .orElse(null);
            }

            List<Training> trainings = facade.training()
                    .findTrainingsForTraineeByCriteria(username, fromDate, toDate, trainerName, trainingTypeId);

            List<TrainingDto> response = trainings.stream().map(training -> {
                try {
                    String trainerFullName = facade.trainer()
                            .findById(training.getTrainerId())
                            .flatMap(trainer -> facade.user().findById(trainer.getUserId()))
                            .map(u -> u.getFirstName() + " " + u.getLastName())
                            .orElse("Unknown Trainer");

                    String trainingTypeName = facade.trainingType()
                            .findById(training.getTrainingTypeId())
                            .map(tt -> tt.getName())
                            .orElse("Unknown Type");

                    return new TrainingDto(
                            training.getName(),
                            training.getTrainingDate(),
                            trainingTypeName,
                            training.getDuration(),
                            trainerFullName
                    );
                } catch (ServiceException e) {
                    LOG.error("Error training mapping: {}", e.getMessage());

                    return null;
                }
            }).filter(Objects::nonNull).toList();

            return ResponseEntity.ok(response);

        } catch (ServiceException e) {
            LOG.error("Error fetching trainee trainings: {}", e.getMessage());

            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/trainers/trainings")
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
            List<Training> trainings = facade.training()
                    .findTrainingsForTrainerByCriteria(username, fromDate, toDate, traineeName);

            List<TrainingDto> response = trainings.stream().map(training -> {
                try {
                    String traineeFullName = facade.trainee()
                            .findById(training.getTraineeId())
                            .flatMap(trainee -> facade.user().findById(trainee.getUserId()))
                            .map(u -> u.getFirstName() + " " + u.getLastName())
                            .orElse("Unknown Trainee");

                    String trainingTypeName = facade.trainingType()
                            .findById(training.getTrainingTypeId())
                            .map(tt -> tt.getName())
                            .orElse("Unknown Training Type");

                    return new TrainingDto(
                            training.getName(),
                            training.getTrainingDate(),
                            trainingTypeName,
                            training.getDuration(),
                            traineeFullName
                    );
                } catch (ServiceException e) {
                    LOG.error("Error mapping training: {}", e.getMessage());

                    return null;
                }
            }).filter(Objects::nonNull).toList();

            return ResponseEntity.ok(response);

        } catch (ServiceException e) {
            LOG.error("Error fetching trainer trainings: {}", e.getMessage());

            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/trainings")
    @Operation(summary = "Add new training")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training added successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee, trainer, or training type not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> addTraining(@Valid @RequestBody TrainingDto request) {
        try {
            Trainee trainee = facade.trainee().findTraineeByUsername(request.getTraineeUsername())
                    .orElseThrow(() -> new ServiceException("Trainee not found: " + request.getTraineeUsername()));

            Trainer trainer = facade.trainer().findTrainerByUsername(request.getTrainerUsername())
                    .orElseThrow(() -> new ServiceException("Trainer not found: " + request.getTrainerUsername()));

            Long trainingTypeId = facade.trainingType()
                    .findAllTrainingTypes().stream()
                    .filter(tt -> tt.getName().equalsIgnoreCase(request.getTrainingType()))
                    .map(tt -> tt.getId())
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("Training type not found: " + request.getTrainingType()));

            Training training = new Training();
            training.setTraineeId(trainee.getId());
            training.setTrainerId(trainer.getId());
            training.setName(request.getName());
            training.setTrainingTypeId(trainingTypeId);
            training.setTrainingDate(request.getDate());
            training.setDuration(request.getDuration());

            facade.training().save(training);

            return ResponseEntity.ok(Map.of("message", "Training added successfully"));
        } catch (ServiceException e) {
            LOG.error("Error adding training: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/trainees/status")
    @Operation(summary = "Activate or deactivate a trainee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> updateTraineeStatus(@Valid @RequestBody UpdateStatusDto request) {
        try {
            Trainee trainee = facade.trainee()
                    .findTraineeByUsername(request.getUsername())
                    .orElseThrow(() -> new ServiceException("Trainee not found: " + request.getUsername()));

            User user = facade.user().findById(trainee.getUserId())
                    .orElseThrow(() -> new ServiceException("User not found for trainee: " + request.getUsername()));

            user.setActive(request.isActive());
            trainee.setActive(request.isActive());
            facade.user().update(user);
            facade.trainee().update(trainee);

            return ResponseEntity.ok(Map.of("message", "Trainee " + (request.isActive() ? "activated" : "deactivated") + " successfully"));
        } catch (ServiceException e) {
            LOG.error("Error updating trainee status: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/trainers/status")
    @Operation(summary = "Activate or deactivate a trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainer status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> updateTrainerStatus(@Valid @RequestBody UpdateStatusDto request) {
        try {
            Trainer trainer = facade.trainer()
                    .findTrainerByUsername(request.getUsername())
                    .orElseThrow(() -> new ServiceException("Trainer not found: " + request.getUsername()));

            if (trainer.getUserId() == null) {
                throw new ServiceException("Trainer has no linked user: " + request.getUsername());
            }

            User user = facade.user().findById(trainer.getUserId())
                    .orElseThrow(() -> new ServiceException("User not found for trainer: " + request.getUsername()));

            user.setActive(request.isActive());
            trainer.setActive(request.isActive());
            facade.user().update(user);
            facade.trainer().update(trainer);

            return ResponseEntity.ok(Map.of("message", "Trainer " + (request.isActive() ? "activated" : "deactivated") + " successfully"));
        } catch (ServiceException e) {
            LOG.error("Error updating trainer status: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/training-types")
    @Operation(summary = "Get all available training types")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training types retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getTrainingTypes() {
        List<Map<String, Object>> trainingTypes = facade.trainingType()
                .findAllTrainingTypes()
                .stream()
                .map(tt -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", tt.getId());
                    map.put("name", tt.getName());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(trainingTypes);
    }
}
