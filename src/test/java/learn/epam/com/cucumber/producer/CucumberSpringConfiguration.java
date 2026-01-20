package learn.epam.com.cucumber.producer;

import io.cucumber.spring.CucumberContextConfiguration;
import jakarta.jms.ConnectionFactory;
import learn.epam.com.main.GymCrmSystemApplication;
import learn.epam.com.security.jwt.JwtTokenProvider;
import learn.epam.com.service.ProfileService;
import learn.epam.com.service.TraineeService;
import learn.epam.com.service.TrainerService;
import learn.epam.com.service.TrainingService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest(classes = GymCrmSystemApplication.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    @MockitoBean
    private JmsTemplate jmsTemplate;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private JmsListenerContainerFactory<?> jmsListenerContainerFactory;

    @MockitoBean
    private TrainerService trainerService;

    @MockitoBean
    private TraineeService traineeService;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private TrainingService trainingService;

}
