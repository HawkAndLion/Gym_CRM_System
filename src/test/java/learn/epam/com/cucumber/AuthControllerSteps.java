package learn.epam.com.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import learn.epam.com.api.model.ChangePasswordRequest;
import learn.epam.com.api.model.LoginRequest;
import learn.epam.com.api.model.LoginResponse;
import learn.epam.com.api.model.MessageResponse;
import learn.epam.com.controller.GymRestController;
import learn.epam.com.exception.AccountLockedException;
import learn.epam.com.security.bruteforceprotector.LoginAttemptService;
import learn.epam.com.security.jwt.JwtTokenProvider;
import learn.epam.com.service.ProfileService;
import learn.epam.com.service.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthControllerSteps {

    private ProfileService profileService;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private LoginAttemptService loginAttemptService;

    private GymRestController controller;

    private ResponseEntity<?> response;
    private Exception thrownException;

    private LoginRequest loginRequest;
    private ChangePasswordRequest changePasswordRequest;

    public AuthControllerSteps() {
        profileService = mock(ProfileService.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        loginAttemptService = mock(LoginAttemptService.class);

        controller = new GymRestController(
                profileService,
                authenticationManager,
                jwtTokenProvider,
                loginAttemptService
        );
    }

    @Given("user {string} is not blocked")
    public void user_is_not_blocked(String username) {
        when(loginAttemptService.isBlocked(username)).thenReturn(false);
    }

    @Given("authentication succeeds for username {string} and password {string}")
    public void authentication_succeeds_for_username_and_password(String username, String password) {
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(username, password));
    }

    @Given("a JWT token can be generated")
    public void a_jwt_token_can_be_generated() {
        when(jwtTokenProvider.generateToken(any())).thenReturn("fake-jwt-token");
    }

    @When("the user logs in with username {string} and password {string}")
    public void the_user_logs_in_with_username_and_password(String username, String password) {
        loginRequest = new LoginRequest()
                .username(username)
                .password(password);

        try {
            response = controller.login(loginRequest);
        } catch (Exception ex) {
            thrownException = ex;
        }
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(Integer status) {
        assertNotNull(response);
        assertEquals(status.intValue(), response.getStatusCodeValue());
    }

    @Then("the response should contain a JWT token")
    public void the_response_should_contain_a_jwt_token() {
        LoginResponse body = (LoginResponse) response.getBody();
        assertNotNull(body);
        assertNotNull(body.getToken());
        assertFalse(body.getToken().isBlank());
    }

    @Then("the response type should be {string}")
    public void the_response_type_should_be(String type) {
        LoginResponse body = (LoginResponse) response.getBody();
        assertEquals(type, body.getType());
    }

    @Then("the response username should be {string}")
    public void the_response_username_should_be(String username) {
        LoginResponse body = (LoginResponse) response.getBody();
        assertEquals(username, body.getUsername());
    }

    @Given("authentication fails for username {string} and password {string}")
    public void authentication_fails_for_username_and_password(String username, String password) {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid"));
    }

    @Then("the response token should be {string}")
    public void the_response_token_should_be(String token) {
        LoginResponse body = (LoginResponse) response.getBody();
        assertEquals(token, body.getToken());
    }

    @Given("user {string} is blocked")
    public void user_is_blocked(String username) {
        when(loginAttemptService.isBlocked(username)).thenReturn(true);
    }

    @Given("the remaining block time is {int} minutes")
    public void the_remaining_block_time_is_minutes(Integer minutes) {
        when(loginAttemptService.getRemainingLockMinutes(any()))
                .thenReturn(minutes.longValue());
    }

    @Then("an AccountLockedException should be thrown")
    public void an_account_locked_exception_should_be_thrown() {
        assertNotNull(thrownException);
        assertTrue(thrownException instanceof AccountLockedException);
    }

    @Then("the error message should be {string}")
    public void the_error_message_should_be(String message) {
        assertEquals(message, thrownException.getMessage());
    }

    @Given("the current password is {string} and the new password is {string}")
    public void the_current_password_is_and_the_new_password_is(String oldPwd, String newPwd) {
        changePasswordRequest = new ChangePasswordRequest()
                .oldPassword(oldPwd)
                .newPassword(newPwd);
    }

    @Given("the profile service allows password change")
    public void the_profile_service_allows_password_change() {
        // default mock behavior = success
    }

    @When("the user requests to change password")
    public void the_user_requests_to_change_password() {
        try {
            response = controller.changePassword(changePasswordRequest);
        } catch (Exception ex) {
            thrownException = ex;
        }
    }

    @Then("the response message should be {string}")
    public void the_response_message_should_be(String message) {
        MessageResponse body = (MessageResponse) response.getBody();
        assertEquals(message, body.getMessage());
    }

    @Given("the profile service throws a service exception with message {string}")
    public void the_profile_service_throws_a_service_exception_with_message(String message)
            throws ServiceException {

        doThrow(new ServiceException(HttpStatus.BAD_REQUEST, message))
                .when(profileService)
                .changePassword(anyString(), anyString());
    }

    @Given("the new password is {string}")
    public void the_new_password_is(String newPassword) {
        changePasswordRequest = new ChangePasswordRequest()
                .newPassword(newPassword);
    }

    @Given("the current password is {string}")
    public void the_current_password_is(String oldPassword) {
        changePasswordRequest = new ChangePasswordRequest()
                .oldPassword(oldPassword);
    }

    @Given("no password values are provided")
    public void no_password_values_are_provided() {
        changePasswordRequest = new ChangePasswordRequest();
    }
}
