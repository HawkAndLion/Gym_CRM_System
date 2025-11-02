package learn.epam.com.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalApiHealthIndicator implements HealthIndicator {

    private static final String TEST_URL = "https://api.github.com"; //correct
//    private static final String TEST_URL = "https://api.invalid-endpoint-12345.com";  //incorrect


    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Health health() {
        boolean reachable = checkApi();

        return reachable
                ? Health.up().withDetail("externalApi", "OK").build()
                : Health.down().withDetail("externalApi", "Not reachable").build();
    }

    private boolean checkApi() {
        try {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(TEST_URL, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
