package learn.epam.com.controller;

import learn.epam.com.api.model.ChangePasswordRequest;
import learn.epam.com.api.model.LoginRequest;
import learn.epam.com.api.model.LoginResponse;
import learn.epam.com.api.model.MessageResponse;
import learn.epam.com.security.bruteforceprotector.LoginAttemptService;
import learn.epam.com.security.jwt.JwtTokenProvider;
import learn.epam.com.service.ProfileService;
import learn.epam.com.service.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GymRestControllerTest {

    private ProfileService profileService;
    private AuthenticationManager authManager;
    private JwtTokenProvider jwtTokenProvider;
    private LoginAttemptService loginAttemptService;
    private GymRestController controller;

    @BeforeEach
    void setUp() {
        profileService = mock(ProfileService.class);
        authManager = mock(AuthenticationManager.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        loginAttemptService = mock(LoginAttemptService.class);

        controller = new GymRestController(profileService, authManager, jwtTokenProvider, loginAttemptService);
    }

    @Test
    void shouldLoginSuccessfullyWhenMethodCalled() {
        // Given
        LoginRequest request = new LoginRequest()
                .username("john")
                .password("secret");

        when(loginAttemptService.isBlocked("john")).thenReturn(false);
        when(authManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken("john", "secret"));
        when(jwtTokenProvider.generateToken(any())).thenReturn("fake-token");

        // When
        ResponseEntity<LoginResponse> response = controller.login(request);

        // Then
        verify(authManager).authenticate(any());
        verify(jwtTokenProvider).generateToken(any());
        assertEquals(200, response.getStatusCodeValue());
        LoginResponse body = response.getBody();
        assertEquals("fake-token", body.getToken());
        assertEquals("john", body.getUsername());
        assertEquals("Bearer", body.getType());
    }

    @Test
    void shouldreturnUnauthorizedWhenInvalidCredentialsOnLogin() {
        // Given
        LoginRequest request = new LoginRequest()
                .username("john")
                .password("wrong");

        when(loginAttemptService.isBlocked("john")).thenReturn(false);
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid"));

        // When
        ResponseEntity<LoginResponse> response = controller.login(request);

        // Then
        verify(loginAttemptService).isBlocked("john");
        verify(authManager).authenticate(any());
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid credentials", response.getBody().getToken());
    }

    @Test
    void shouldChangePasswordSuccessfullyWhenMethodCalled() throws ServiceException {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest()
                .oldPassword("old")
                .newPassword("new");

        // When
        ResponseEntity<MessageResponse> response = controller.changePassword(request);

        // Then
        verify(profileService).changePassword("old", "new");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Password was changed successfully.", response.getBody().getMessage());
    }

    @Test
    void shouldReturnBadRequestWhenMissingPasswordFields() throws ServiceException {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest()
                .oldPassword("old");

        // When
        ResponseEntity<MessageResponse> response = controller.changePassword(request);

        // Then
        verify(profileService, never()).changePassword(anyString(), anyString());
        assertEquals(400, response.getStatusCodeValue());
    }
}
