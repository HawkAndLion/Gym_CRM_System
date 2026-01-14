package learn.epam.com.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;


@SpringBootApplication(scanBasePackages = "learn.epam.com")
@EnableJpaRepositories(basePackages = "learn.epam.com.repository")
@EntityScan(basePackages = "learn.epam.com.entity")
@EnableDiscoveryClient
@EnableJms
public class GymCrmSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymCrmSystemApplication.class, args);
    }
}
