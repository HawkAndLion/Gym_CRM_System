package learn.epam.com.controller;

import learn.epam.com.api.AuthApi;
import learn.epam.com.api.model.ChangePasswordRequest;
import learn.epam.com.api.model.LoginRequest;
import learn.epam.com.api.model.LoginResponse;
import learn.epam.com.api.model.MessageResponse;
import learn.epam.com.exception.AccountLockedException;
import learn.epam.com.security.bruteforceprotector.LoginAttemptService;
import learn.epam.com.security.jwt.JwtTokenProvider;
import learn.epam.com.service.ProfileService;
import learn.epam.com.service.ServiceException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GymRestController implements AuthApi {
    private static final Logger LOG = LoggerFactory.getLogger(GymRestController.class);
    private static final String SUCCESS_PASSWORD_CHANGE = "Password was changed successfully.";
    private static final String ERROR_CHANGE_PASSWORD = "Change password error: {}";
    private static final String INVALID_CREDENTIALS = "Invalid credentials";
    private static final String BLOCK_MINUTES_LEFT = "Your account is blocked for %d minute(s). Please wait.";

    private final ProfileService profile;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        String username = request.getUsername();

        if (loginAttemptService.isBlocked(username)) {
            long minutesLeft = loginAttemptService.getRemainingLockMinutes(username);

            throw new AccountLockedException(
                    String.format(BLOCK_MINUTES_LEFT, minutesLeft)
            );
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            loginAttemptService.loginSucceeded(username);

            String jwtToken = jwtTokenProvider.generateToken(authentication);

            return ResponseEntity.ok(
                    new LoginResponse()
                            .token(jwtToken)
                            .type("Bearer")
                            .username(username)
            );

        } catch (AuthenticationException e) {

            return ResponseEntity.badRequest()
                    .body(new LoginResponse().token(INVALID_CREDENTIALS));
        }
    }

    @Override
    public ResponseEntity<MessageResponse> changePassword(ChangePasswordRequest request) {
        try {
            if (request.getOldPassword() != null && request.getNewPassword() != null) {
                String oldPassword = request.getOldPassword();
                String newPassword = request.getNewPassword();

                profile.changePassword(oldPassword, newPassword);

                LOG.info(SUCCESS_PASSWORD_CHANGE);

                return ResponseEntity.ok(
                        new MessageResponse().message(SUCCESS_PASSWORD_CHANGE)
                );
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse());
            }

        } catch (ServiceException e) {
            LOG.error(ERROR_CHANGE_PASSWORD, e.getMessage());

            return ResponseEntity.badRequest()
                    .body(new MessageResponse().message(e.getMessage()));
        }
    }
}
