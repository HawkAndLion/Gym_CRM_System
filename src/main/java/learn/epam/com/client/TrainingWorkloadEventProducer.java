package learn.epam.com.client;

import learn.epam.com.dto.client.TrainingEventDto;
import learn.epam.com.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainingWorkloadEventProducer {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private final JmsTemplate jmsTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.service.name}")
    private String serviceName;

    @Value("${app.jms.destination}")
    private String destination;

    public void send(TrainingEventDto dto) {
        String serviceToken =
                jwtTokenProvider.generateServiceToken(serviceName);

        jmsTemplate.convertAndSend(destination, dto, message -> {
            message.setStringProperty(
                    AUTHORIZATION,
                    BEARER + serviceToken
            );
            return message;
        });
    }
}

