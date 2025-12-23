package learn.epam.com.client;

import learn.epam.com.dto.client.TrainingEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainingWorkloadEventProducer {

    private static final String DESTINATION = "trainer.workload.queue";

    @Autowired
    private final JmsTemplate jmsTemplate;

    public void send(TrainingEventDto dto) {
        jmsTemplate.convertAndSend(DESTINATION, dto);
    }
}

