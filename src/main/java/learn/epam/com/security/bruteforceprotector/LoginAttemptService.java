package learn.epam.com.security.bruteforceprotector;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_TIME_SECONDS = 20;

    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lockTimeCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
        lockTimeCache.remove(username);
    }

    public void loginFailed(String username) {
        int attempts = attemptsCache.getOrDefault(username, 0) + 1;
        attemptsCache.put(username, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            lockTimeCache.put(username, LocalDateTime.now().plusSeconds(LOCK_TIME_SECONDS));
        }
    }

    public boolean isBlocked(String username) {
        LocalDateTime lockUntil = lockTimeCache.get(username);

        if (lockUntil != null) {
            if (lockUntil.isBefore(LocalDateTime.now())) {
                lockTimeCache.remove(username);
                attemptsCache.remove(username);

                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public long getRemainingLockMinutes(String username) {
        LocalDateTime lockUntil = lockTimeCache.get(username);

        if (lockUntil != null) {
            return java.time.Duration.between(LocalDateTime.now(), lockUntil).toMinutes();
        } else {
            return 0;
        }
    }
}