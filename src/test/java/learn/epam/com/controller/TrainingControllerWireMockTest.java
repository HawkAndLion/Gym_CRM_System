package learn.epam.com.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import learn.epam.com.dto.TrainingDto;
import learn.epam.com.main.GymCrmSystemApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = GymCrmSystemApplication.class)
@ActiveProfiles("test")
@WireMockTest
class TrainingControllerWireMockTest {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Autowired
    private WebTestClient webTestClient;

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().dynamicPort())
            .build();

    private int mockPort;

    @BeforeEach
    void setUp() {
        mockPort = wireMockExtension.getPort();
    }

    @Test
    void shouldAddTrainingSuccessfully() throws Exception {
        TrainingDto request = new TrainingDto();
        request.setName("Morning Cardio");
        request.setDate(LocalDate.of(2025, 10, 28));
        request.setTrainingType("Cardio");
        request.setDuration(60);
        request.setTraineeUsername("Alice.Trainee");
        request.setTrainerUsername("John.Trainer");

        wireMockExtension.stubFor(post(urlEqualTo("/api/trainings"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, "application/json")
                        .withStatus(200)
                        .withBody(mapper.writeValueAsString(Map.of("message", "Training added successfully")))));

        webTestClient.post()
                .uri("http://localhost:" + mockPort + "/api/trainings")
                .header("Username", "Admin")
                .header("Password", "secret")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(request))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Training added successfully");

        wireMockExtension.verify(postRequestedFor(urlEqualTo("/api/trainings")));
    }

    @Test
    void shouldReturnAllTrainingTypesSuccessfully() throws Exception {
        // Arrange
        List<Map<String, Object>> mockResponse = List.of(
                Map.of("id", 1, "name", "Cardio"),
                Map.of("id", 2, "name", "Strength")
        );

        wireMockExtension.stubFor(get(urlEqualTo("/api/trainings/training-types"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, "application/json")
                        .withStatus(200)
                        .withBody(mapper.writeValueAsString(mockResponse))));

        // Act + Assert
        webTestClient.get()
                .uri("http://localhost:" + mockPort + "/api/trainings/training-types")
                .header("Username", "Admin")
                .header("Password", "secret")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Cardio")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].name").isEqualTo("Strength");

        // Verify
        wireMockExtension.verify(getRequestedFor(urlEqualTo("/api/trainings/training-types")));
    }
}
