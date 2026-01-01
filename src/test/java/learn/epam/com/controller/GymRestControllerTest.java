package learn.epam.com.controller;

import learn.epam.com.dto.ChangePasswordDto;
import learn.epam.com.dto.UserDetailsDto;
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

import java.util.Map;

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
        UserDetailsDto dto = new UserDetailsDto();
        dto.setUsername("john");
        dto.setPassword("secret");

        when(loginAttemptService.isBlocked("john")).thenReturn(false);
        when(authManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken("john", "secret"));
        when(jwtTokenProvider.generateToken(any())).thenReturn("fake-token");

        ResponseEntity<?> response = controller.login(dto);

        verify(authManager).authenticate(any());
        verify(jwtTokenProvider).generateToken(any());
        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("fake-token", body.get("token"));
        assertEquals("john", body.get("username"));
    }

    @Test
    void shouldreturnUnauthorizedWhenInvalidCredentialsOnLogin() {
        UserDetailsDto dto = new UserDetailsDto();
        dto.setUsername("john");
        dto.setPassword("wrong");

        when(loginAttemptService.isBlocked("john")).thenReturn(false);
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid"));

        ResponseEntity<?> response = controller.login(dto);

        verify(loginAttemptService).isBlocked("john");
        verify(authManager).authenticate(any());
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void shouldChangePasswordSuccessfullyWhenMethodCalled() throws ServiceException {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("old");
        dto.setNewPassword("new");

        ResponseEntity<?> response = controller.changePassword(dto);

        verify(profileService).changePassword("old", "new");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Password was changed successfully.", response.getBody());
    }

    @Test
    void shouldReturnBadRequestWhenMissingPasswordFields() throws ServiceException {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("old");

        ResponseEntity<?> response = controller.changePassword(dto);

        verify(profileService, never()).changePassword(anyString(), anyString());
        assertEquals(400, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("All fields are required", body.get("error"));
    }
}
