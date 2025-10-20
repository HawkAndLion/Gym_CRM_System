package learn.epam.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import learn.epam.com.dto.*;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.main.GymFacade;
import learn.epam.com.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


    private String makeUsername(String firstName, String lastName) {
        StringBuilder builder = new StringBuilder();

        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            builder.append(firstName);
            builder.append(".");
            builder.append(lastName);
        }

        return builder.toString();
    }
}
