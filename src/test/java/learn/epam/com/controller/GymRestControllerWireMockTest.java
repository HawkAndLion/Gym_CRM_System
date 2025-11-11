package learn.epam.com.controller;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import learn.epam.com.dto.ChangePasswordDto;
import learn.epam.com.dto.UserDetailsDto;
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
class GymRestControllerWireMockTest {

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
    void shouldLoginSuccessfully() {
        wireMockExtension.stubFor(post(urlEqualTo("/api/login"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "firstName": "John",
                                        "lastName": "Doe",
                                        "username": "John.Doe",
                                        "active": true
                                }
                                """)
                        .withStatus(200)));

        UserDetailsDto request = new UserDetailsDto();
        request.setUsername("John.Doe");
        request.setPassword("secret123");

        webTestClient.post()
                .uri("http://localhost:" + mockPort + "/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.lastName").isEqualTo("Doe")
                .jsonPath("$.username").isEqualTo("John.Doe")
                .jsonPath("$.active").isEqualTo(true);

        wireMockExtension.verify(postRequestedFor(urlEqualTo("/api/login")));
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        ChangePasswordDto request = new ChangePasswordDto();
        request.setOldPassword("secret123");
        request.setNewPassword("newSecret456");

        wireMockExtension.stubFor(put(urlEqualTo("/api/login"))
                .withHeader("Username", equalTo("John.Doe"))
                .withHeader("Password", equalTo("secret123"))
                .withRequestBody(containing("\"newPassword\":\"newSecret456\""))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                    {
                                      "username": "John.Doe",
                                      "password": "newSecret456"
                                    }
                                """)
                        .withStatus(200)));

        webTestClient.put()
                .uri("http://localhost:" + mockPort + "/api/login")
                .header("Username", "John.Doe")
                .header("Password", "secret123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.username").isEqualTo("John.Doe")
                .jsonPath("$.password").isEqualTo("newSecret456");

        wireMockExtension.verify(putRequestedFor(urlEqualTo("/api/login")));
    }

    @Test
    void shouldReturnUnauthorizedWhenInvalidLogin() {
        wireMockExtension.stubFor(post(urlEqualTo("/api/login"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBody("Invalid credentials")));

        UserDetailsDto request = new UserDetailsDto();
        request.setUsername("WrongUser");
        request.setPassword("wrongPassword");

        webTestClient.post()
                .uri("http://localhost:" + mockPort + "/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(String.class)
                .isEqualTo("Invalid credentials");

        wireMockExtension.verify(postRequestedFor(urlEqualTo("/api/login")));
    }

    @Test
    void shouldReturnBadRequestWhenMissingPasswordFields() {
        ChangePasswordDto request = new ChangePasswordDto();
//        request.setUsername("John.Doe");
        request.setOldPassword("secret123");

        wireMockExtension.stubFor(put(urlEqualTo("/api/login/password"))
                .withHeader("Username", equalTo("John.Doe"))
                .withHeader("Password", equalTo("secret123"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"error\": \"All fields are required\"}")));

        webTestClient.put()
                .uri("http://localhost:" + mockPort + "/api/login/password")
                .header("Username", "John.Doe")
                .header("Password", "secret123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("All fields are required");

        wireMockExtension.verify(putRequestedFor(urlEqualTo("/api/login/password")));
    }
}
