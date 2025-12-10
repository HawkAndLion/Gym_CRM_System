package learn.epam.com.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import learn.epam.com.dto.StatusDto;
import learn.epam.com.dto.TraineeDto;
import learn.epam.com.dto.TraineeProfileDto;
import learn.epam.com.dto.TraineeTrainersDto;
import learn.epam.com.main.GymCrmSystemApplication;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TraineeService;
import learn.epam.com.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = GymCrmSystemApplication.class)
@ActiveProfiles("test")
@WireMockTest
class TraineeControllerWireMockTest {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Autowired
    WebTestClient webTestClient;

    private final UserService userService = Mockito.mock(UserService.class);
    private final TraineeService traineeService = Mockito.mock(TraineeService.class);

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().dynamicPort())
            .build();

    private static int mockPort;

    @BeforeEach
    void setupMocks() {
        mockPort = wireMockExtension.getPort();
    }

    @Test
    void shouldRegisterAndRetrieveTraineeProfile() throws Exception {
        wireMockExtension.stubFor(post(urlEqualTo("/api/trainees"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"username": "Alice.Brown", "password": "secret"}
                                """)));

        TraineeDto request = new TraineeDto();
        request.setFirstName("Alice");
        request.setLastName("Brown");
        request.setAddress("Paris");

        webTestClient.post()
                .uri("http://localhost:" + mockPort + "/api/trainees")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(request))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.username").isEqualTo("Alice.Brown")
                .jsonPath("$.password").isEqualTo("secret");

        wireMockExtension.verify(postRequestedFor(urlEqualTo("/api/trainees")));
    }

    @Test
    void shouldGetTraineeProfileWhenMethodCalled() {
        wireMockExtension.stubFor(get(urlPathEqualTo("/api/trainees/profile"))
                .withQueryParam("username", equalTo("Alice.Brown"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "username": "Alice.Brown",
                                    "firstName": "Alice",
                                    "lastName": "Brown",
                                    "address": "Paris"
                                }
                                """)));

        webTestClient.get()
                .uri("http://localhost:" + mockPort + "/api/trainees/profile?username=Alice.Brown")
                .header("Username", "Alice.Brown")
                .header("Password", "secret")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("Alice.Brown")
                .jsonPath("$.firstName").isEqualTo("Alice")
                .jsonPath("$.lastName").isEqualTo("Brown")
                .jsonPath("$.address").isEqualTo("Paris");

        wireMockExtension.verify(getRequestedFor(urlPathEqualTo("/api/trainees/profile"))
                .withQueryParam("username", equalTo("Alice.Brown")));
    }

    @Test
    void shouldUpdateTraineeProfileWhenMethodCalled() throws Exception {
        wireMockExtension.stubFor(put(urlEqualTo("/api/trainees/profile"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "username": "Alice.Brown",
                                    "firstName": "Alice",
                                    "lastName": "Brown",
                                    "dateOfBirth": "1995-04-23",
                                    "address": "Paris"
                                }
                                """)));

        TraineeProfileDto request = new TraineeProfileDto();
        request.setUsername("Alice.Brown");
        request.setFirstName("Alice");
        request.setLastName("Brown");
        request.setDateOfBirth(LocalDate.of(1995, 4, 23));
        request.setAddress("Paris");

        webTestClient.put()
                .uri("http://localhost:" + mockPort + "/api/trainees/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(request))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.username").isEqualTo("Alice.Brown")
                .jsonPath("$.firstName").isEqualTo("Alice")
                .jsonPath("$.lastName").isEqualTo("Brown")
                .jsonPath("$.dateOfBirth").isEqualTo("1995-04-23")
                .jsonPath("$.address").isEqualTo("Paris");

        wireMockExtension.verify(putRequestedFor(urlEqualTo("/api/trainees/profile")));
    }

    @Test
    void shouldDeleteTraineeProfileWhenMethodCalled() {
        wireMockExtension.stubFor(delete(urlPathEqualTo("/api/trainees/profile"))
                .withQueryParam("username", equalTo("Alice.Brown"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));

        webTestClient.delete()
                .uri("http://localhost:" + mockPort + "/api/trainees/profile?username=Alice.Brown")
                .header("Username", "Alice.Brown")
                .header("Password", "secret")
                .exchange()
                .expectStatus().isOk();

        wireMockExtension.verify(deleteRequestedFor(urlPathEqualTo("/api/trainees/profile"))
                .withQueryParam("username", equalTo("Alice.Brown")));
    }

    @Test
    void shouldUpdateTraineeTrainersWhenMethodCalled() throws Exception {
        wireMockExtension.stubFor(put(urlEqualTo("/api/trainees/trainers"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {"username": "John.Trainer", "firstName": "John", "lastName": "Trainer", "specialization": "Cardio"}
                                ]
                                """)));

        TraineeTrainersDto request = new TraineeTrainersDto();
        request.setTrainerUsernames(List.of("John.Trainer"));

        webTestClient.put()
                .uri("http://localhost:" + mockPort + "/api/trainees/trainers")
                .header("Username", "Alice.Brown")
                .header("Password", "secret")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(request))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].username").isEqualTo("John.Trainer")
                .jsonPath("$[0].firstName").isEqualTo("John")
                .jsonPath("$[0].lastName").isEqualTo("Trainer")
                .jsonPath("$[0].specialization").isEqualTo("Cardio");

        wireMockExtension.verify(putRequestedFor(urlEqualTo("/api/trainees/trainers")));
    }

    @Test
    void shouldUpdateTraineeStatusWhenMethodCalled() throws Exception {
        wireMockExtension.stubFor(patch(urlEqualTo("/api/trainees/status"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"message": "Trainee activated successfully"}
                                """)));

        StatusDto request = new StatusDto();
        request.setUsername("Alice.Brown");
        request.setActive(true);

        webTestClient.patch()
                .uri("http://localhost:" + mockPort + "/api/trainees/status")
                .header("Username", "Admin")
                .header("Password", "admin123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(request))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Trainee activated successfully");

        wireMockExtension.verify(patchRequestedFor(urlEqualTo("/api/trainees/status")));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterTraineeWithMissingFields() throws Exception {
        wireMockExtension.stubFor(post(urlEqualTo("/api/trainees"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"error": "Missing required fields"}
                                """)));

        TraineeDto request = new TraineeDto();

        webTestClient.post()
                .uri("http://localhost:" + mockPort + "/api/trainees")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(request))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Missing required fields");
    }

    @Test
    void shouldReturnNotFoundWhenGetTraineeProfileForUnknownUser() {
        wireMockExtension.stubFor(get(urlPathEqualTo("/api/trainees/profile"))
                .withQueryParam("username", equalTo("Unknown.User"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"error": "Trainee not found for username: Unknown.User"}
                                """)));

        webTestClient.get()
                .uri("http://localhost:" + mockPort + "/api/trainees/profile?username=Unknown.User")
                .header("Username", "Unknown.User")
                .header("Password", "secret")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Trainee not found for username: Unknown.User");
    }

    @Test
    void shouldReturnBadRequestWhenUpdateTraineeProfileWithMissingFields() throws Exception {
        wireMockExtension.stubFor(put(urlEqualTo("/api/trainees/profile"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"error": "All fields are required"}
                                """)));

        TraineeProfileDto request = new TraineeProfileDto();

        webTestClient.put()
                .uri("http://localhost:" + mockPort + "/api/trainees/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(request))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("All fields are required");
    }


    @Test
    void shouldReturnNotFoundWhenDeleteNonexistentTraineeProfile() {
        wireMockExtension.stubFor(delete(urlPathEqualTo("/api/trainees/profile"))
                .withQueryParam("username", equalTo("Ghost.User"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"error": "Trainee not found for username: Ghost.User"}
                                """)));

        webTestClient.delete()
                .uri("http://localhost:" + mockPort + "/api/trainees/profile?username=Ghost.User")
                .header("Username", "Ghost.User")
                .header("Password", "secret")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Trainee not found for username: Ghost.User");
    }

    @Test
    void shouldReturnNotFoundWhenUpdateTraineeTrainersWithInvalidTrainer() throws Exception {
        wireMockExtension.stubFor(put(urlEqualTo("/api/trainees/trainers"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"error": "Trainer not found"}
                                """)));

        TraineeTrainersDto request = new TraineeTrainersDto();
        request.setTrainerUsernames(List.of("Nonexistent.Trainer"));

        webTestClient.put()
                .uri("http://localhost:" + mockPort + "/api/trainees/trainers")
                .header("Username", "Alice.Brown")
                .header("Password", "secret")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(request))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Trainer not found");
    }

    @Test
    void shouldReturnBadRequestWhenGetTrainingsWithInvalidDates() {
        wireMockExtension.stubFor(get(urlPathEqualTo("/api/trainees/trainings"))
                .withQueryParam("traineeUsername", equalTo("Alice.Brown"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"error": "Invalid date range"}
                                """)));

        webTestClient.get()
                .uri("http://localhost:" + mockPort + "/api/trainees/trainings?traineeUsername=Alice.Brown&periodFrom=2025-12-01&periodTo=2025-01-01")
                .header("Username", "Alice.Brown")
                .header("Password", "secret")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Invalid date range");
    }

    @Test
    void shouldReturnNotFoundWhenUpdateStatusOfNonexistentTrainee() throws Exception {
        wireMockExtension.stubFor(patch(urlEqualTo("/api/trainees/status"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"error": "Trainee not found"}
                                """)));

        StatusDto request = new StatusDto();
        request.setUsername("Ghost.User");
        request.setActive(true);

        webTestClient.patch()
                .uri("http://localhost:" + mockPort + "/api/trainees/status")
                .header("Username", "Admin")
                .header("Password", "admin123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(request))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Trainee not found");
    }
}