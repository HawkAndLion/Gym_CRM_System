package learn.epam.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import learn.epam.com.dto.ChangePasswordDto;
import learn.epam.com.dto.UserDetailsDto;
import learn.epam.com.dto.UserDto;
import learn.epam.com.entity.User;
import learn.epam.com.service.ProfileService;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Gym API", description = "Endpoints for testing Gym CRM system")
public class GymRestController {
    private static final Logger LOG = LoggerFactory.getLogger(GymRestController.class);
    private static final String USER_NOT_FOUND = "User was not found. Check if username and password are correct";
    private static final String LOGIN_ERROR = "Login error: {}";
    private static final String INVALID_CREDENTIALS = "Invalid credentials";
    private static final String ERROR = "error";
    private static final String SUCCESS_PASSWORD_CHANGE = "Password was changed successfully.";
    private static final String REQUIRE_FIELDS = "All fields are required";
    private static final String ERROR_CHANGE_PASSWORD = "Change password error: {}";

    private final ProfileService profile;
    private final UserService userService;

    public GymRestController(ProfileService profile, UserService userService) {
        this.profile = profile;
        this.userService = userService;
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

                User user = userService.findAllUsers().stream()
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
            @ApiResponse(responseCode = "400", description = "Invalid input or user")
    })
    public ResponseEntity<?> changePassword(
            @Parameter(description = "Header: Username", required = true)
            @RequestHeader("Username") String headerUsername,
            @Parameter(description = "Header: Password", required = true)
            @RequestHeader("Password") String headerPassword,
            @RequestBody ChangePasswordDto request) {
        try {
            String username = request.getUsername();
            String oldPassword = request.getOldPassword();
            String newPassword = request.getNewPassword();

            if (username != null && oldPassword != null && newPassword != null) {
                profile.changePassword(username, oldPassword, newPassword);

                LOG.info(SUCCESS_PASSWORD_CHANGE);

                User user = userService.findAllUsers().stream().filter(u -> u.getUsername().equals(username)).findFirst().orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
                UserDetailsDto response = new UserDetailsDto(user.getUsername(), user.getPassword());

                return ResponseEntity.ok(response);
            }

            throw new ServiceException(REQUIRE_FIELDS);

        } catch (ServiceException e) {
            LOG.error(ERROR_CHANGE_PASSWORD, e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(ERROR, e.getMessage()));
        }
    }
}
