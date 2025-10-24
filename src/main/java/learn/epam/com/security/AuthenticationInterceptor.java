package learn.epam.com.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import learn.epam.com.entity.User;
import learn.epam.com.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

public class AuthenticationInterceptor implements HandlerInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    private final UserService userService;

    public AuthenticationInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (path.contains("/trainees") && request.getMethod().equalsIgnoreCase("POST")) return true;
        if (path.contains("/trainers") && request.getMethod().equalsIgnoreCase("POST")) return true;
        if (path.contains("/login")) return true;

        String username = request.getHeader("Username");
        String password = request.getHeader("Password");

        if (username == null || password == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Missing Username or Password header\"}");

            return false;
        }

        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(password)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid credentials\"}");

            return false;
        }

        if (!userOpt.get().isActive()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"User account is deactivated\"}");
            return false;
        }

        LOG.info("Authenticated user: {}", username);

        return true;
    }
}
