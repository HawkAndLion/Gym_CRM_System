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
    void shouldRegisterTrainerSuccessfully() {
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
    void shouldReturnTrainerProfileSuccessfully() {

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
    void shouldUpdateTrainerProfileSuccessfully() {
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
    void shouldReturnUnassignedTrainersSuccessfully() {

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
    void shouldReturnTrainerTrainingsSuccessfully() {

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
    void shouldUpdateTrainerStatusSuccessfully() {
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
}
