package learn.epam.com.controller;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import learn.epam.com.dto.StatusDto;
import learn.epam.com.dto.TrainerDto;
import learn.epam.com.dto.TrainerProfileDto;
import learn.epam.com.main.GymCrmSystemApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = GymCrmSystemApplication.class)
@ActiveProfiles("test")
@WireMockTest
class TrainerControllerWireMockTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().dynamicPort())
            .build();

    @Autowired
    private WebTestClient webTestClient;

    private int mockPort;

    @BeforeEach
    void setUp() {
        mockPort = wireMockExtension.getPort();
    }

    @Test
    void shouldRegisterTrainerWhenMethodCalled() {
        TrainerDto request = new TrainerDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setSpecialization("Cardio");

        wireMockExtension.stubFor(post(urlEqualTo("/api/trainers"))
                .withRequestBody(containing("\"firstName\":\"John\""))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                    {
                                      "username": "John.Doe",
                                      "password": "securePass123"
                                    }
                                """)));

        webTestClient.post()
                .uri("http://localhost:" + mockPort + "/api/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("John.Doe")
                .jsonPath("$.password").isEqualTo("securePass123");

        wireMockExtension.verify(postRequestedFor(urlEqualTo("/api/trainers")));
    }

    @Test
    void shouldReturnTrainerProfileWhenMethodCalled() {

        wireMockExtension.stubFor(get(urlPathEqualTo("/api/trainers/profile"))
                .withQueryParam("username", equalTo("John.Doe"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                    {
                                      "username": "John.Doe",
                                      "firstName": "John",
                                      "lastName": "Doe",
                                      "specialization": "Cardio"
                                    }
                                """)));

        webTestClient.get()
                .uri("http://localhost:" + mockPort + "/api/trainers/profile?username=John.Doe")
                .header("Username", "admin")
                .header("Password", "1234")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("John.Doe")
                .jsonPath("$.specialization").isEqualTo("Cardio");

        wireMockExtension.verify(getRequestedFor(urlPathEqualTo("/api/trainers/profile")));
    }

    @Test
    void shouldUpdateTrainerProfileWhenMethodCalled() {
        TrainerProfileDto request = new TrainerProfileDto();
        request.setUsername("John.Doe");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setSpecialization("Strength");

        wireMockExtension.stubFor(put(urlEqualTo("/api/trainers/profile"))
                .withHeader("Username", equalTo("admin"))
                .withHeader("Password", equalTo("1234"))
                .withRequestBody(containing("\"specialization\":\"Strength\""))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                    {
                                      "username": "John.Doe",
                                      "firstName": "John",
                                      "lastName": "Doe",
                                      "specialization": "Strength"
                                    }
                                """)));

        webTestClient.put()
                .uri("http://localhost:" + mockPort + "/api/trainers/profile")
                .header("Username", "admin")
                .header("Password", "1234")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.specialization").isEqualTo("Strength");

        wireMockExtension.verify(putRequestedFor(urlEqualTo("/api/trainers/profile")));
    }

    @Test
    void shouldReturnUnassignedTrainersWhenMethodCalled() {

        wireMockExtension.stubFor(get(urlPathEqualTo("/api/trainers/profile/unassigned"))
                .withQueryParam("username", equalTo("Alice.Brown"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                    [
                                      {"username":"trainer1","firstName":"Anna","lastName":"Smith","specialization":"Yoga"},
                                      {"username":"trainer2","firstName":"Mark","lastName":"Brown","specialization":"Cardio"}
                                    ]
                                """)));

        webTestClient.get()
                .uri("http://localhost:" + mockPort + "/api/trainers/profile/unassigned?username=Alice.Brown")
                .header("Username", "Alice.Brown")
                .header("Password", "1234")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].username").isEqualTo("trainer1")
                .jsonPath("$[1].specialization").isEqualTo("Cardio");

        wireMockExtension.verify(getRequestedFor(urlPathEqualTo("/api/trainers/profile/unassigned")));
    }

    @Test
    void shouldReturnTrainerTrainingsWhenMethodCalled() {

        wireMockExtension.stubFor(get(urlPathEqualTo("/api/trainers/trainings"))
                .withQueryParam("Trainer's username", equalTo("John.Doe"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                    [
                                      {
                                        "trainingName": "Morning Cardio",
                                        "trainingDate": "2024-05-05",
                                        "trainingType": "Cardio",
                                        "trainingDuration": 60,
                                        "traineeName": "Alice Brown"
                                      }
                                    ]
                                """)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("localhost")
                        .port(mockPort)
                        .path("/api/trainers/trainings")
                        .queryParam("Trainer's username", "John.Doe")
                        .build())
                .header("Username", "admin")
                .header("Password", "1234")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].trainingType").isEqualTo("Cardio")
                .jsonPath("$[0].traineeName").isEqualTo("Alice Brown");

        wireMockExtension.verify(getRequestedFor(urlPathEqualTo("/api/trainers/trainings")));
    }

    @Test
    void shouldUpdateTrainerStatusWhenMethodCalled() {
        StatusDto request = new StatusDto();
        request.setUsername("John.Doe");
        request.setActive(true);

        wireMockExtension.stubFor(patch(urlEqualTo("/api/trainers/status"))
                .withHeader("Username", equalTo("admin"))
                .withHeader("Password", equalTo("1234"))
                .withRequestBody(containing("\"username\":\"John.Doe\""))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                    {"message": "Trainer activated successfully"}
                                """)));

        webTestClient.patch()
                .uri("http://localhost:" + mockPort + "/api/trainers/status")
                .header("Username", "admin")
                .header("Password", "1234")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Trainer activated successfully");

        wireMockExtension.verify(patchRequestedFor(urlEqualTo("/api/trainers/status")));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterTrainerWithMissingFields() {
        TrainerDto request = new TrainerDto();

        wireMockExtension.stubFor(post(urlEqualTo("/api/trainers"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {"error": "Missing required fields"}
                                """)));

        webTestClient.post()
                .uri("http://localhost:" + mockPort + "/api/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Missing required fields");

        wireMockExtension.verify(postRequestedFor(urlEqualTo("/api/trainers")));
    }

    @Test
    void shouldReturnNotFoundWhenTrainerProfileNotExists() {
        wireMockExtension.stubFor(get(urlPathEqualTo("/api/trainers/profile"))
                .withQueryParam("username", equalTo("Non.Existing"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {"error": "Trainer not found for username: Non.Existing"}
                                """)));

        webTestClient.get()
                .uri("http://localhost:" + mockPort + "/api/trainers/profile?username=Non.Existing")
                .header("Username", "admin")
                .header("Password", "1234")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Trainer not found for username: Non.Existing");

        wireMockExtension.verify(getRequestedFor(urlPathEqualTo("/api/trainers/profile")));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateTrainerProfileWithMissingFields() {
        TrainerProfileDto request = new TrainerProfileDto();

        wireMockExtension.stubFor(put(urlEqualTo("/api/trainers/profile"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {"error": "Username, first name, and last name are required"}
                                """)));

        webTestClient.put()
                .uri("http://localhost:" + mockPort + "/api/trainers/profile")
                .header("Username", "admin")
                .header("Password", "1234")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Username, first name, and last name are required");

        wireMockExtension.verify(putRequestedFor(urlEqualTo("/api/trainers/profile")));
    }

    @Test
    void shouldReturnNotFoundWhenTrainerTrainingsNotFound() {
        wireMockExtension.stubFor(get(urlPathEqualTo("/api/trainers/trainings"))
                .withQueryParam("Trainer's username", equalTo("Non.Existing"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {"error": "Error fetching trainings for trainer: Non.Existing"}
                                """)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("localhost")
                        .port(mockPort)
                        .path("/api/trainers/trainings")
                        .queryParam("Trainer's username", "Non.Existing")
                        .build())
                .header("Username", "admin")
                .header("Password", "1234")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Error fetching trainings for trainer: Non.Existing");

        wireMockExtension.verify(getRequestedFor(urlPathEqualTo("/api/trainers/trainings")));
    }

    @Test
    void shouldReturnNotFoundWhenUpdateTrainerStatusWithInvalidUsername() {
        StatusDto request = new StatusDto();
        request.setUsername("Unknown.User");
        request.setActive(false);

        wireMockExtension.stubFor(patch(urlEqualTo("/api/trainers/status"))
                .withRequestBody(containing("\"username\":\"Unknown.User\""))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {"error": "Trainer not found: Unknown.User"}
                                """)));

        webTestClient.patch()
                .uri("http://localhost:" + mockPort + "/api/trainers/status")
                .header("Username", "admin")
                .header("Password", "1234")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Trainer not found: Unknown.User");

        wireMockExtension.verify(patchRequestedFor(urlEqualTo("/api/trainers/status")));
    }
}
