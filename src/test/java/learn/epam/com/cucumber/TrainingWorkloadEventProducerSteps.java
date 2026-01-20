package learn.epam.com.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import learn.epam.com.client.TrainingWorkloadEventProducer;
import learn.epam.com.dto.client.TrainingEventDto;
import learn.epam.com.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TrainingWorkloadEventProducerSteps {
    private static final String FAKE_TOKEN = "fake-jwt-token";

    private TrainingEventDto dto;
    private Exception thrownException;

    @Autowired
    private TrainingWorkloadEventProducer producer;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JmsTemplate jmsTemplate;


    @Given("a TrainingEventDto is created")
    public void a_training_event_dto_is_created() {
        dto = new TrainingEventDto();
        dto.setUsername("trainer1");
    }

    @Given("JwtTokenProvider generates a service token")
    public void jwt_token_provider_generates_a_service_token() {
        when(jwtTokenProvider.generateServiceToken(anyString())).thenReturn(FAKE_TOKEN);
    }

    @Given("JwtTokenProvider fails to generate a token")
    public void jwt_token_provider_fails_to_generate_a_token() {
        when(jwtTokenProvider.generateServiceToken(anyString()))
                .thenThrow(new RuntimeException("JWT generation failed"));
    }

    @Given("the JMS template fails to send the message")
    public void the_jms_template_fails_to_send_the_message() {
        doThrow(new RuntimeException("JMS send failed"))
                .when(jmsTemplate)
                .convertAndSend(anyString(), any(TrainingEventDto.class), any(MessagePostProcessor.class));
    }

    @When("the training event is sent")
    public void the_training_event_is_sent() {
        try {
            producer.send(dto);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the JMS template should send the message to the correct destination")
    public void the_jms_template_should_send_the_message_to_the_correct_destination() {
        verify(jmsTemplate, times(1))
                .convertAndSend(anyString(), eq(dto), any(MessagePostProcessor.class));
    }

    @Then("the message should have an Authorization header with the JWT token")
    public void the_message_should_have_an_authorization_header_with_the_jwt_token() throws JMSException {
        // Capture the MessagePostProcessor and test it
        var mppCaptor = org.mockito.ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(anyString(), eq(dto), mppCaptor.capture());

        Message mockMessage = mock(Message.class);
        mppCaptor.getValue().postProcessMessage(mockMessage);

        verify(mockMessage).setStringProperty("Authorization", "Bearer " + FAKE_TOKEN);
    }

    @Then("an exception should be thrown")
    public void an_exception_should_be_thrown() {
        assertNotNull(thrownException);
    }

}
