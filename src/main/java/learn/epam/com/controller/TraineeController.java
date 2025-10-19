package learn.epam.com.controller;

//import io.swagger.annotations.*;
//import learn.epam.com.dto.request.TraineeRegistrationRequest;
//import learn.epam.com.dto.response.RegistrationResponse;
//import learn.epam.com.main.GymFacade;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/trainees")
//@Api(tags = "Trainee Management")
//public class TraineeController {
//
//    private final GymFacade facade;
//
//    public TraineeController(GymFacade facade) {
//        this.facade = facade;
//    }
//
//    @ApiOperation(value = "Register a new trainee", notes = "Creates a trainee profile and returns username and password.")
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "Trainee successfully registered"),
//            @ApiResponse(code = 400, message = "Invalid input data")
//    })
//    @PostMapping("/register")
//    public ResponseEntity<RegistrationResponse> registerTrainee(
//            @ApiParam(value = "Trainee registration data", required = true)
//            @RequestBody TraineeRegistrationRequest request) {
//        var result = facade.trainee().registerTrainee(request);
//        return ResponseEntity.ok(result);
//    }
//}
//
