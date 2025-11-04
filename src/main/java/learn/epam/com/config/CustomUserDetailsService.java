package learn.epam.com.config;

import learn.epam.com.entity.User;
import learn.epam.com.security.bruteforceprotector.LoginAttemptService;
import learn.epam.com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    private static final String USER_LOCKED = "User %s is locked for %d more minutes";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String USER_NOT_FOUND = "User %s not found ";

    private final UserService userService;
    private final LoginAttemptService loginAttemptService;

    @Autowired
    public CustomUserDetailsService(UserService userService, LoginAttemptService loginAttemptService) {
        this.userService = userService;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (loginAttemptService.isBlocked(username)) {
            long minutes = loginAttemptService.getRemainingLockMinutes(username);

            throw new LockedException(String.format(USER_LOCKED, username, minutes));
        }

        Optional<User> optionalUser = userService.findByUsername(username);
        User user = optionalUser.orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND, username)));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(() -> ROLE_USER))
                .disabled(!user.isActive())
                .build();
    }
}
