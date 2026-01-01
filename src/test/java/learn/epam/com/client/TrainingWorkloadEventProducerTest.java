package learn.epam.com.client;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import learn.epam.com.dto.client.TrainingEventDto;
import learn.epam.com.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TrainingWorkloadEventProducerTest {
    private static final String FAKE_JWT_TOKEN = "fake-jwt-token";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String QUEUE_NAME = "trainer.workload.queue";
    private static final String SERVICE_NAME = "serviceName";
    private static final String TEST_SERVICE = "test-service";

    private JmsTemplate jmsTemplate;
    private JwtTokenProvider jwtTokenProvider;
    private TrainingWorkloadEventProducer producer;

    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException {
        jmsTemplate = mock(JmsTemplate.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);

        producer = new TrainingWorkloadEventProducer(jmsTemplate, jwtTokenProvider);

        Field field = TrainingWorkloadEventProducer.class.getDeclaredField(SERVICE_NAME);
        field.setAccessible(true);
        field.set(producer, TEST_SERVICE);

        Field destinationField = TrainingWorkloadEventProducer.class.getDeclaredField("destination");
        destinationField.setAccessible(true);
        destinationField.set(producer, QUEUE_NAME);
    }

    @Test
    void send_shouldSendMessageWithAuthorizationHeader() throws JMSException {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        when(jwtTokenProvider.generateServiceToken(TEST_SERVICE)).thenReturn(FAKE_JWT_TOKEN);

        // When
        producer.send(dto);

        // Then
        ArgumentCaptor<TrainingEventDto> dtoCaptor = ArgumentCaptor.forClass(TrainingEventDto.class);
        ArgumentCaptor<MessagePostProcessor> mppCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        verify(jmsTemplate, times(1)).convertAndSend(eq(QUEUE_NAME), dtoCaptor.capture(), mppCaptor.capture());
        assertEquals(dto, dtoCaptor.getValue());

        Message mockMessage = mock(Message.class);
        mppCaptor.getValue().postProcessMessage(mockMessage);
        verify(mockMessage).setStringProperty(AUTHORIZATION, BEARER + FAKE_JWT_TOKEN);
    }
}
