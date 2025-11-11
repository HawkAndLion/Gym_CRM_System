package learn.epam.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import learn.epam.com.dto.ChangePasswordDto;
import learn.epam.com.dto.UserDetailsDto;
import learn.epam.com.exception.AccountLockedException;
import learn.epam.com.security.bruteforceprotector.LoginAttemptService;
import learn.epam.com.security.jwt.JwtTokenProvider;
import learn.epam.com.service.ProfileService;
import learn.epam.com.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Gym API", description = "Endpoints for testing Gym CRM system")
public class GymRestController {
    private static final Logger LOG = LoggerFactory.getLogger(GymRestController.class);
    private static final String ERROR = "error";
    private static final String SUCCESS_PASSWORD_CHANGE = "Password was changed successfully.";
    private static final String REQUIRE_FIELDS = "All fields are required";
    private static final String ERROR_CHANGE_PASSWORD = "Change password error: {}";
    private static final String INVALID_CREDENTIALS = "Invalid credentials";

    private final ProfileService profile;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;

    public GymRestController(ProfileService profile, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, LoginAttemptService loginAttemptService) {
        this.profile = profile;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "404", description = "Invalid credentials")
    })
    public ResponseEntity<?> login(@RequestBody UserDetailsDto request) {
        String username = request.getUsername();

        if (loginAttemptService.isBlocked(username)) {
            long minutesLeft = loginAttemptService.getRemainingLockMinutes(username);

            throw new AccountLockedException(
                    "Your account is blocked for " + minutesLeft + " minute(s). Please wait."
            );
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            loginAttemptService.loginSucceeded(username);

            String jwtToken = jwtTokenProvider.generateToken(authentication);

            return ResponseEntity.ok(Map.of(
                    "token", jwtToken,
                    "type", "Bearer",
                    "username", request.getUsername()
            ));

        } catch (AuthenticationException e) {

            return ResponseEntity.badRequest().body(INVALID_CREDENTIALS);
        }
    }

    @PutMapping("/login/password")
    @Operation(summary = "Change password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user")
    })
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordDto request) {
        try {
            if (request.getOldPassword() != null && request.getNewPassword() != null) {
                String oldPassword = request.getOldPassword();
                String newPassword = request.getNewPassword();

                profile.changePassword(oldPassword, newPassword);

                LOG.info(SUCCESS_PASSWORD_CHANGE);

                return ResponseEntity.ok(SUCCESS_PASSWORD_CHANGE);
            } else {
                return ResponseEntity.badRequest().body(Map.of(ERROR, REQUIRE_FIELDS));
            }

        } catch (ServiceException e) {
            LOG.error(ERROR_CHANGE_PASSWORD, e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(ERROR, e.getMessage()));
        }
    }
}
