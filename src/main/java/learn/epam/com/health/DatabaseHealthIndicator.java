package learn.epam.com.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try {
            if (checkDatabaseConnection()) {
                return Health.up()
                        .withDetail("database", "PostgreSQL is reachable")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "Cannot reach PostgreSQL")
                        .build();
            }
        } catch (Exception e) {
            return Health.down(e).withDetail("database", "Connection error").build();
        }
    }

    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2); // 2 seconds timeout
        } catch (Exception e) {
            return false;
        }
    }
}
