package learn.epam.com.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "learn.epam.com")
public class GymCrmSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymCrmSystemApplication.class, args);
    }
}
