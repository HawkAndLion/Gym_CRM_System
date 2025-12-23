//package learn.epam.com.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
//import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
//import com.github.tomakehurst.wiremock.junit5.WireMockTest;
//import learn.epam.com.dto.TrainingDto;
//import learn.epam.com.main.GymCrmSystemApplication;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.RegisterExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.reactive.server.WebTestClient;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Map;
//
//import static com.github.tomakehurst.wiremock.client.WireMock.*;
//import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        classes = GymCrmSystemApplication.class)
//@ActiveProfiles("test")
//@WireMockTest
//class TrainingControllerWireMockTest {
//    private static final ObjectMapper mapper = new ObjectMapper()
//            .registerModule(new JavaTimeModule());
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @RegisterExtension
//    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
//            .options(WireMockConfiguration.options().dynamicPort())
//            .build();
//
//    private int mockPort;
//
//    @BeforeEach
//    void setUp() {
//        mockPort = wireMockExtension.getPort();
//    }
//
//    @Test
//    void shouldAddTrainingSuccessfullyWhenMethodCalled() throws Exception {
//        TrainingDto request = new TrainingDto();
//        request.setName("Morning Cardio");
//        request.setDate(LocalDate.of(2025, 10, 28));
//        request.setTrainingType("Cardio");
//        request.setDuration(60);
//        request.setTraineeUsername("Alice.Trainee");
//        request.setTrainerUsername("John.Trainer");
//
//        wireMockExtension.stubFor(post(urlEqualTo("/api/trainings"))
//                .willReturn(aResponse()
//                        .withHeader(CONTENT_TYPE, "application/json")
//                        .withStatus(200)
//                        .withBody(mapper.writeValueAsString(Map.of("message", "Training added successfully")))));
//
//        webTestClient.post()
//                .uri("http://localhost:" + mockPort + "/api/trainings")
//                .header("Username", "Admin")
//                .header("Password", "secret")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .bodyValue(mapper.writeValueAsString(request))
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("Training added successfully");
//
//        wireMockExtension.verify(postRequestedFor(urlEqualTo("/api/trainings")));
//    }
//
//    @Test
//    void shouldReturnAllTrainingTypesWhenMethodCalled() throws Exception {
//        List<Map<String, Object>> mockResponse = List.of(
//                Map.of("id", 1, "name", "Cardio"),
//                Map.of("id", 2, "name", "Strength")
//        );
//
//        wireMockExtension.stubFor(get(urlEqualTo("/api/trainings/training-types"))
//                .willReturn(aResponse()
//                        .withHeader(CONTENT_TYPE, "application/json")
//                        .withStatus(200)
//                        .withBody(mapper.writeValueAsString(mockResponse))));
//
//        webTestClient.get()
//                .uri("http://localhost:" + mockPort + "/api/trainings/training-types")
//                .header("Username", "Admin")
//                .header("Password", "secret")
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$[0].id").isEqualTo(1)
//                .jsonPath("$[0].name").isEqualTo("Cardio")
//                .jsonPath("$[1].id").isEqualTo(2)
//                .jsonPath("$[1].name").isEqualTo("Strength");
//
//        wireMockExtension.verify(getRequestedFor(urlEqualTo("/api/trainings/training-types")));
//    }
//
//    @Test
//    void shouldReturnBadRequestWhenAddTrainingWithMissingFields() throws Exception {
//        TrainingDto request = new TrainingDto();
//
//        wireMockExtension.stubFor(post(urlEqualTo("/api/trainings"))
//                .willReturn(aResponse()
//                        .withHeader(CONTENT_TYPE, "application/json")
//                        .withStatus(400)
//                        .withBody("""
//                                {"error": "Missing required training fields"}
//                                """)));
//
//        webTestClient.post()
//                .uri("http://localhost:" + mockPort + "/api/trainings")
//                .header("Username", "Admin")
//                .header("Password", "secret")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(mapper.writeValueAsString(request))
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody()
//                .jsonPath("$.error").isEqualTo("Missing required training fields");
//
//        wireMockExtension.verify(postRequestedFor(urlEqualTo("/api/trainings")));
//    }
//
//    @Test
//    void shouldReturnNotFoundWhenAddTrainingWithNonExistingTraineeOrTrainer() throws Exception {
//        TrainingDto request = new TrainingDto();
//        request.setName("Evening Yoga");
//        request.setDate(LocalDate.of(2025, 10, 30));
//        request.setTrainingType("Yoga");
//        request.setDuration(60);
//        request.setTraineeUsername("Unknown.Trainee");
//        request.setTrainerUsername("John.Trainer");
//
//        wireMockExtension.stubFor(post(urlEqualTo("/api/trainings"))
//                .willReturn(aResponse()
//                        .withHeader(CONTENT_TYPE, "application/json")
//                        .withStatus(404)
//                        .withBody("""
//                                {"error": "Trainee not found: Unknown.Trainee"}
//                                """)));
//
//        webTestClient.post()
//                .uri("http://localhost:" + mockPort + "/api/trainings")
//                .header("Username", "Admin")
//                .header("Password", "secret")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(mapper.writeValueAsString(request))
//                .exchange()
//                .expectStatus().isNotFound()
//                .expectBody()
//                .jsonPath("$.error").isEqualTo("Trainee not found: Unknown.Trainee");
//
//        wireMockExtension.verify(postRequestedFor(urlEqualTo("/api/trainings")));
//    }
//
//    @Test
//    void shouldReturnNotFoundWhenTrainerNotExists() throws Exception {
//        TrainingDto request = new TrainingDto();
//        request.setName("Evening Yoga");
//        request.setDate(LocalDate.of(2025, 10, 30));
//        request.setTrainingType("Yoga");
//        request.setDuration(60);
//        request.setTraineeUsername("Alice.Trainee");
//        request.setTrainerUsername("Unknown.Trainer");
//
//        wireMockExtension.stubFor(post(urlEqualTo("/api/trainings"))
//                .willReturn(aResponse()
//                        .withHeader(CONTENT_TYPE, "application/json")
//                        .withStatus(404)
//                        .withBody("""
//                                {"error": "Trainer not found: Unknown.Trainer"}
//                                """)));
//
//        webTestClient.post()
//                .uri("http://localhost:" + mockPort + "/api/trainings")
//                .header("Username", "Admin")
//                .header("Password", "secret")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(mapper.writeValueAsString(request))
//                .exchange()
//                .expectStatus().isNotFound()
//                .expectBody()
//                .jsonPath("$.error").isEqualTo("Trainer not found: Unknown.Trainer");
//
//        wireMockExtension.verify(postRequestedFor(urlEqualTo("/api/trainings")));
//    }
//
//    @Test
//    void shouldReturnInternalServerErrorWhenAddTrainingFailsUnexpectedly() throws Exception {
//        TrainingDto request = new TrainingDto();
//        request.setName("Crash Test");
//        request.setDate(LocalDate.now());
//        request.setTrainingType("Cardio");
//        request.setDuration(30);
//        request.setTraineeUsername("Alice.Trainee");
//        request.setTrainerUsername("John.Trainer");
//
//        wireMockExtension.stubFor(post(urlEqualTo("/api/trainings"))
//                .willReturn(aResponse()
//                        .withHeader(CONTENT_TYPE, "application/json")
//                        .withStatus(500)
//                        .withBody("""
//                                {"error": "Internal server error while saving training"}
//                                """)));
//
//        webTestClient.post()
//                .uri("http://localhost:" + mockPort + "/api/trainings")
//                .header("Username", "Admin")
//                .header("Password", "secret")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(mapper.writeValueAsString(request))
//                .exchange()
//                .expectStatus().is5xxServerError()
//                .expectBody()
//                .jsonPath("$.error").isEqualTo("Internal server error while saving training");
//
//        wireMockExtension.verify(postRequestedFor(urlEqualTo("/api/trainings")));
//    }
//
//    @Test
//    void shouldReturnInternalServerErrorWhenGetTrainingTypesFails() {
//        wireMockExtension.stubFor(get(urlEqualTo("/api/trainings/training-types"))
//                .willReturn(aResponse()
//                        .withHeader(CONTENT_TYPE, "application/json")
//                        .withStatus(500)
//                        .withBody("""
//                                {"error": "Internal server error while retrieving training types"}
//                                """)));
//
//        webTestClient.get()
//                .uri("http://localhost:" + mockPort + "/api/trainings/training-types")
//                .header("Username", "Admin")
//                .header("Password", "secret")
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().is5xxServerError()
//                .expectBody()
//                .jsonPath("$.error").isEqualTo("Internal server error while retrieving training types");
//
//        wireMockExtension.verify(getRequestedFor(urlEqualTo("/api/trainings/training-types")));
//    }
//}
