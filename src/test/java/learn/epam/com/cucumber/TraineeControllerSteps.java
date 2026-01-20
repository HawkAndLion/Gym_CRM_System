package learn.epam.com.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import learn.epam.com.api.model.*;
import learn.epam.com.controller.TraineeController;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TraineeControllerSteps {

    @Autowired
    private TraineeController traineeController;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private TrainingService trainingService;

    private TraineeCreateRequest createRequest;
    private TraineeStatusRequest statusRequest;
    private TraineeTrainersRequest trainersRequest;
    private TraineeProfileResponse profileResponse;

    private ResponseEntity<?> response;
    private Exception thrownException;
    private TrainingRequest trainingRequest;
    private List<TrainingResponse> trainingResponses;
    private List<Training> trainings;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String trainerName;
    private String trainingTypeId;


    @Before
    public void setupSecurityContext() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("trainee1");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    @Before
    public void resetMocks() {
        reset(traineeService, profileService, trainerService, trainingService);
    }

    /* ================= REGISTRATION ================= */

    @Given("a valid trainee registration request")
    public void a_valid_trainee_registration_request() {
        createRequest = new TraineeCreateRequest()
                .firstName("John")
                .lastName("Doe")
                .password("secret")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Almaty");
    }

    @Given("the profile service throws an error during trainee registration")
    public void the_profile_service_throws_an_error_during_trainee_registration() {
        doThrow(new RuntimeException("Registration failed"))
                .when(profileService)
                .registerTrainee(any(TraineeCreateRequest.class));
    }

    @When("the trainee registration is submitted")
    public void the_trainee_registration_is_submitted() {
        try {
            response = traineeController.registerTrainee(createRequest);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the trainee response status should be {int}")
    public void the_trainee_response_status_should_be(int status) {
        assertNotNull(response, "Response should not be null");
        assertEquals(status, response.getStatusCodeValue());
    }

    @Then("the trainee registration should fail with status {int}")
    public void the_trainee_registration_should_fail_with_status(Integer status) {
        assertNotNull(response, "Response should not be null");
        assertEquals(status.intValue(), response.getStatusCodeValue());
    }


    /* ================= GET PROFILE ================= */

    @Given("a trainee with username {string} exists")
    public void a_trainee_with_username_exists(String username) {
        Trainee trainee = new Trainee("Almaty", LocalDate.of(2000, 1, 1), true);
        User user = new User("Alice", "Brown", username, "password", true);
        trainee.setUser(user);

        when(traineeService.findTraineeByUsername(username))
                .thenReturn(Optional.of(trainee));

        TrainerProfileResponse trainerProfileResponse = getTrainerProfileResponse();

        List<TrainerProfileResponse> trainerProfileResponses = new ArrayList<>();
        trainerProfileResponses.add(trainerProfileResponse);

        TraineeProfileResponse traineeProfileResponse = getTraineeProfileResponse(user, trainee, trainerProfileResponses);

        when(profileService.getTraineeProfile(trainee))
                .thenReturn(traineeProfileResponse);
    }

    private static TrainerProfileResponse getTrainerProfileResponse() {
        TrainerProfileResponse trainerProfileResponse = new TrainerProfileResponse();
        trainerProfileResponse.setUsername("John.Beckham");
        trainerProfileResponse.setFirstName("John");
        trainerProfileResponse.setLastName("Beckham");
        trainerProfileResponse.setSpecialization("Cardio Trainer");
        trainerProfileResponse.setActive(true);
        trainerProfileResponse.setTrainees(List.of());
        return trainerProfileResponse;
    }

    private static TraineeProfileResponse getTraineeProfileResponse(User user, Trainee trainee, List<TrainerProfileResponse> trainerProfileResponses) {
        TraineeProfileResponse traineeProfileResponse = new TraineeProfileResponse();
        traineeProfileResponse.setUsername(user.getUsername());
        traineeProfileResponse.setFirstName(user.getFirstName());
        traineeProfileResponse.setLastName(user.getLastName());
        traineeProfileResponse.setDateOfBirth(trainee.getDateOfBirth());
        traineeProfileResponse.setAddress(trainee.getAddress());
        traineeProfileResponse.setActive(trainee.isActive());
        traineeProfileResponse.setTrainers(trainerProfileResponses);
        return traineeProfileResponse;
    }

    @Given("no trainee exists with username {string}")
    public void no_trainee_exists_with_username(String username) {
        when(traineeService.findTraineeByUsername(username))
                .thenReturn(Optional.empty());
    }

    @When("the trainee profile is requested for username {string}")
    public void the_trainee_profile_is_requested_for_username(String username) {
        try {
            response = traineeController.getTraineeProfile(username);
//        } catch (Exception e) {
//            thrownException = e;
//        }
        } catch (ServiceException e) {
            thrownException = e;
            response = ResponseEntity
                    .status(e.getStatus())
                    .body(e.getMessage());
        }
    }

    @Then("the successful response status should be {int}")
    public void the_successful_response_status_should_be(int status) {
        assertNotNull(response);
        assertEquals(status, response.getStatusCodeValue());
    }

    @Then("the trainee profile should be returned")
    public void the_trainee_profile_should_be_returned() {
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof TraineeProfileResponse);
        TraineeProfileResponse body = (TraineeProfileResponse) response.getBody();
        assertEquals("Alice.Brown", body.getUsername());
    }

    @Then("the error message should contain {string}")
    public void the_error_message_should_contain(String message) {
        assertNotNull(thrownException);
        assertTrue(thrownException.getMessage().contains(message));
    }

    @Then("the response status when fail should be {int}")
    public void the_response_status_when_fail_should_be(int status) {
        assertNotNull(response, "Response must not be null");
        assertEquals(status, response.getStatusCodeValue());
    }


    /* ================= UPDATE PROFILE ================= */

    @Given("the authenticated user is {string}")
    public void the_authenticated_user_is(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    @Given("a valid trainee profile update request")
    public void a_valid_trainee_profile_update_request() {
        profileResponse = new TraineeProfileResponse()
                .firstName("Alice")
                .lastName("Brown")
                .address("Almaty")
                .dateOfBirth(LocalDate.of(2000, 1, 1));

        when(profileService.updateTraineeProfile(any(String.class), any(TraineeProfileResponse.class))).thenReturn(profileResponse);
    }

    @When("the trainee profile update is requested")
    public void the_trainee_profile_update_is_requested() {
        try {
            response = traineeController.updateTraineeProfile(profileResponse);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    /* ================= STATUS UPDATE ================= */

    @Given("a trainee status request for username {string} with active status true")
    public void a_trainee_status_request_for_username_with_active_status_true(String username) {
        statusRequest = new TraineeStatusRequest(username, true);
    }

    @Given("a trainee status request for username {string} with active status false")
    public void a_trainee_status_request_for_username_with_active_status_false(String username) {
        statusRequest = new TraineeStatusRequest(username, false);
    }

    @When("the trainee status is updated")
    public void the_trainee_status_is_updated() {
        response = traineeController.updateTraineeStatus(statusRequest);
    }

    @Given("a trainee trainers update request with valid trainer usernames")
    public void a_trainee_trainers_update_request_with_valid_trainer_usernames() {
        trainersRequest = new TraineeTrainersRequest(
                List.of("Jack.Brown", "Patrick.Snow")
        );

        Set<Trainer> trainers = new HashSet<>(List.of(
                new Trainer("Cardio", true),
                new Trainer("Fitness", true)
        ));

        when(trainerService.getTrainersByUsername(any()))
                .thenReturn(trainers);

        when(trainerService.getTrainerProfileResponse(trainers))
                .thenReturn(List.of(
                        new TrainerProfileResponse().username("Jack.Brown"),
                        new TrainerProfileResponse().username("Patrick.Snow")
                ));
    }

    @When("the trainee trainers are updated")
    public void the_trainee_trainers_are_updated() throws ServiceException {
        response = traineeController.updateTraineeTrainers(trainersRequest);
    }

    @Then("the updated trainer list should be returned")
    public void the_updated_trainer_list_should_be_returned() {
        assertEquals(200, response.getStatusCodeValue());

        List<TrainerProfileResponse> body =
                (List<TrainerProfileResponse>) response.getBody();

        assertNotNull(body);
        assertEquals(2, body.size());

        verify(trainerService).getTrainersByUsername(trainersRequest);
        verify(traineeService).update(anyString(), anySet());
        verify(trainingService).updateTrainingsByTrainee(anyString(), anySet());
        verify(trainerService).getTrainerProfileResponse(anySet());
    }

    /* ================= DELETE ================= */

    @Given("a trainee profile exists for username {string}")
    public void a_trainee_profile_exists_for_username(String username) {
        doNothing().when(profileService).deleteTraineeProfile(username);
    }

    @Given("the trainee profile does not exist for username {string}")
    public void the_trainee_profile_does_not_exist_for_username(String username) {
        doThrow(new ServiceException(HttpStatus.NOT_FOUND, "Trainee not found"))
                .when(profileService)
                .deleteTraineeProfile(username);
    }

    @When("the trainee profile is deleted for username {string}")
    public void the_trainee_profile_is_deleted_for_username(String username) {
        try {
            response = traineeController.deleteTraineeProfile(username);
        } catch (ServiceException e) {
            thrownException = e;
            response = ResponseEntity
                    .status(e.getStatus())
                    .body(e.getMessage());
        }
    }

    /* ================= GET TRAINEE TRAININGS ================= */

    @Given("training data exists for trainee {string}")
    public void training_data_exists_for_trainee(String username) throws ServiceException {

        Training t1 = new Training();
        t1.setName("Cardio");
        t1.setTrainingDate(LocalDate.of(2024, 1, 10));
        t1.setDuration(60);
        t1.setTrainingTypeId(1L);
        t1.setTrainerId(100L);
        t1.setTraineeId(200L);

        Training t2 = new Training();
        t2.setName("Fitness");
        t2.setTrainingDate(LocalDate.of(2024, 1, 12));
        t2.setDuration(45);
        t2.setTrainingTypeId(1L);
        t2.setTrainerId(101L);
        t2.setTraineeId(200L);

        trainings = List.of(t1, t2);

        when(trainingService.findTrainingsForTraineeByCriteria(
                eq(username),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(trainings);

        trainingResponses = List.of(
                new TrainingResponse()
                        .name("Cardio")
                        .date(LocalDate.of(2024, 1, 10))
                        .duration(1.5)
                        .trainingType("Cardio"),
                new TrainingResponse()
                        .name("Fitness")
                        .date(LocalDate.of(2024, 1, 12))
                        .duration(0.5)
                        .trainingType("Fitness")
        );

        when(trainingService.getTrainingResponseList(trainings))
                .thenReturn(trainingResponses);
    }

    @Given("training filters are provided")
    public void training_filters_are_provided() {
        fromDate = LocalDate.of(2024, 1, 1);
        toDate = LocalDate.of(2024, 12, 31);
        trainerName = null;
        trainingTypeId = null;
    }

    @Given("no optional training filters are provided")
    public void no_optional_training_filters_are_provided() {
        fromDate = null;
        toDate = null;
        trainerName = null;
        trainingTypeId = null;
    }

    @Given("training data exists for trainee {string} but no trainings match filters")
    public void training_data_exists_but_no_trainings_match(String username) throws ServiceException {
        when(trainingService.findTrainingsForTraineeByCriteria(
                eq(username),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(Collections.emptyList());

        when(trainingService.getTrainingResponseList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());
    }

    @When("trainee trainings are requested")
    public void trainee_trainings_are_requested() {
        response = traineeController.getTraineeTrainings(
                "Alice.Brown",
                fromDate,
                toDate,
                trainerName,
                trainingTypeId
        );
    }

    @Then("the training list should be returned")
    public void the_training_list_should_be_returned() {
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        assertTrue(response.getBody() instanceof List<?>);

        List<?> body = (List<?>) response.getBody();
        assertEquals(2, body.size());

        verify(trainingService).findTrainingsForTraineeByCriteria(
                eq("Alice.Brown"),
                any(),
                any(),
                any(),
                any()
        );

        verify(trainingService).getTrainingResponseList(trainings);
    }

    @Then("the training list should be empty")
    public void the_training_list_should_be_empty() {
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List<?>);

        List<?> body = (List<?>) response.getBody();
        assertTrue(body.isEmpty());
    }
}
