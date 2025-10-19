package learn.epam.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import learn.epam.com.dto.TraineeProfileDto;
import learn.epam.com.dto.TraineeRegistrationRequestDto;
import learn.epam.com.dto.TraineeRegistrationResponseDto;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Gym API", description = "Endpoints for testing Gym CRM system") // just for testing
public class GymRestController {
    private static final Logger LOG = LoggerFactory.getLogger(GymRestController.class);
    private static final String NULL_EXCEPTION = "Argument is null ";

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
    public ResponseEntity<?> registerTrainee(@RequestBody TraineeRegistrationRequestDto request) {
        try {
            LOG.info("Request received: firstName={}, lastName={}, dateOfBirth={}, address={}",
                    request.getFirstName(), request.getLastName(), request.getDateOfBirth(), request.getAddress());

            User user = new User(request.getFirstName(), request.getLastName(), null, null, true);
            Trainee trainee = new Trainee(request.getAddress(), request.getDateOfBirth(), true);

            facade.profile().createTraineeProfile(user, trainee);

            User extractedUser = facade.user().findAllUsers().stream().filter(u -> u.getFirstName().equals(request.getFirstName()) && u.getLastName().equals(request.getLastName())).findFirst().orElseThrow(() -> new org.hibernate.service.spi.ServiceException("User was not found. Check if firstname and lastname exist."));
            LOG.info("Creating trainee for user: {} {}", extractedUser.getFirstName(), extractedUser.getLastName());

            TraineeRegistrationResponseDto response = new TraineeRegistrationResponseDto(extractedUser.getUsername(), extractedUser.getPassword());

            LOG.info("Trainee was registered successfully: username={}", extractedUser.getUsername());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOG.error("Error in registerTrainee: {}", e.getMessage(), e);

            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/trainers")
    @Operation(summary = "Register new trainer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully registered"),
            @ApiResponse(responseCode = "400", description = "Missing required fields or validation failed")
    })
    public ResponseEntity<?> registerTrainer(@RequestBody Map<String, String> request) {
        Map<String, String> response;

        try {
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");
            String specialization = request.get("specialization");

            if (firstName != null && lastName != null && specialization != null) {
                if (firstName.isBlank() || lastName.isBlank() || specialization.isBlank()) {
                    throw new ServiceException("First name, last name, and specialization are required");
                }

                User user = new User(firstName, lastName, null, null, true);
                facade.user().save(user);

                Trainer trainer = new Trainer(null, user.getId(), specialization, true, null);
                facade.trainer().save(trainer);

                User targetUser = facade.user().findById(user.getId()).orElseThrow(() -> new ServiceException("User not found after save"));

                response = Map.of(
                        "username", targetUser.getUsername(),
                        "password", targetUser.getPassword()
                );

            } else {
                throw new ServiceException("First name, last name, and specialization are required");
            }

            LOG.info("Trainer was registered successfully");

            return ResponseEntity.ok(response);

        } catch (ServiceException e) {
            LOG.error("Error in registerTrainer: {}", e.getMessage(), e);

            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/login")
    @Operation(summary = "Login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "404", description = "Invalid credentials")
    })
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password) {

        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            boolean success = false;
            try {
                List<User> users = facade.user().findAllUsers();
                for (User user : users) {
                    if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                        success = true;
                    }
                }

                if (!success) {
                    throw new ServiceException("Invalid credentials");
                }

                LOG.info("Login was successful. STATUS 200 OK");

                return ResponseEntity.ok().build();

            } catch (ServiceException e) {
                LOG.error("Login error: {}", e.getMessage());

                return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
            }
        } else {
            LOG.error("Username or password is missing");

            return ResponseEntity.badRequest().body(Map.of("error", "Username and password must not be null"));
        }
    }

    @PutMapping("/login")
    @Operation(summary = "Change password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "404", description = "Invalid input or user")
    })
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            if (username == null || oldPassword == null || newPassword == null) {
                throw new ServiceException("All fields are required");
            }

            facade.profile().changePassword(username, oldPassword, newPassword);

            LOG.info("Password was changed successfully. STATUS 200 OK");

            return ResponseEntity.ok().build();

        } catch (ServiceException e) {
            LOG.error("Change password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/trainees/profile")
    @Operation(summary = "Get trainee profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee profile returned"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    public ResponseEntity<?> getTraineeProfile(
            @Parameter(description = "Username of the trainee to retrieve profile for")
            @RequestParam String username) {

        try {
            Trainee trainee = facade.trainee().findTraineeByUsername(username)
                    .orElseThrow(() -> new ServiceException("Trainee not found for username: " + username));

            TraineeProfileDto profileDto = facade.profile().getTraineeProfile(trainee);

            LOG.info("Trainee Profile was retrieved successfully!");

            return ResponseEntity.ok(profileDto);

        } catch (ServiceException e) {
            LOG.error("Error in getTraineeProfile: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
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

                LOG.info("Trainee profile updated successfully for username: {}", request.getUsername());

                return ResponseEntity.ok(updatedProfile);
            } else {
                throw new ServiceException("Username, first name, and last name are required");
            }
        } catch (ServiceException e) {
            LOG.error("Error in updateTraineeProfile: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/trainees/profile")
    @Operation(summary = "Delete trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile deleted"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    public ResponseEntity<?> deleteTraineeProfile(@RequestParam String username) {
        try {
            facade.profile().deleteTraineeProfile(username);

            LOG.info("TraineeProfile was deleted successfully.");

            return ResponseEntity.ok().build();

        } catch (ServiceException e) {
            LOG.error("Error in deleteTraineeProfile: {}", e.getMessage());

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

    // Just for test =)))
    @Operation(summary = "Health check", description = "Returns confirmation that REST API is running")
    @GetMapping("/test")
    public String test() {
        return "REST API is running!";
    }

}
