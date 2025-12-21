package learn.epam.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import learn.epam.com.dto.TrainingDto;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.event.TrainingDeletedEvent;
import learn.epam.com.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trainings")
@Tag(name = "Trainings API", description = "Operations related to trainings")
public class TrainingController {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingController.class);
    private static final String TRAINING_NOT_FOUND = "Training not found ";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found: ";
    private static final String TRAINER_NOT_FOUND = "Trainer not found: ";
    private static final String MESSAGE = "message";
    private static final String SUCCESS_MESSAGE = "Training added successfully";
    private static final String ERROR = "error";
    private static final String ERROR_ADD_TRAINING = "Error adding training: {}";

    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingTypeService trainingTypeService;
    private final TrainingService trainingService;

    @PostMapping
    @Operation(summary = "Add new training")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training added successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee, trainer, or training type not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<?> addTraining(
            @RequestBody TrainingDto request) {
        try {
            Trainee trainee = traineeService.findTraineeByUsername(request.getTraineeUsername())
                    .orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND + request.getTraineeUsername()));

            Trainer trainer = trainerService.findTrainerByUsername(request.getTrainerUsername())
                    .orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND + request.getTrainerUsername()));

            trainerService.assignTrainerToTrainee(
                    request.getTrainerUsername(),
                    request.getTraineeUsername()
            );

            Long trainingTypeId = trainingTypeService.getTrainingTypeId(request.getTrainingType());

            Training training = trainingService.update(trainee, trainer, request, trainingTypeId);

            trainingService.save(training);

            return ResponseEntity.ok(Map.of(MESSAGE, SUCCESS_MESSAGE));
        } catch (ServiceException e) {
            LOG.error(ERROR_ADD_TRAINING, e.getMessage());

            return ResponseEntity.status(404).body(Map.of(ERROR, e.getMessage()));
        }
    }

    @GetMapping("/training-types")
    @Operation(summary = "Get all available training types")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training types retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getTrainingTypes(
    ) {
        List<Map<String, Object>> trainingTypes = trainingTypeService.getTrainingTypes();

        return ResponseEntity.ok(trainingTypes);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete training by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Training not found")
    })
    public ResponseEntity<?> deleteTraining(@PathVariable Long id) {
        try {
            Training training = trainingService.findById(id).orElseThrow(() -> new org.hibernate.service.spi.ServiceException(TRAINING_NOT_FOUND));

            Trainee trainee = traineeService.findById(training.getTraineeId())
                    .orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND + training.getTraineeId()));

            Trainer trainer = trainerService.findById(training.getTrainerId())
                    .orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND + training.getTrainerId()));

            TrainingDeletedEvent deletedEvent = new TrainingDeletedEvent(training);

            Set<Long> trainerIds = traineeService.getTrainerIdsForTrainee(trainee.getId());
            Set<Trainer> newTrainers = new HashSet<>();
            for (Long trainerId : trainerIds) {
                if (!trainerId.equals(trainer.getId())) {
                    Trainer tr = trainerService.findById(trainerId)
                            .orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND + trainerId));
                    newTrainers.add(tr);
                }
            }

            trainingService.deleteById(id);
            traineeService.update(trainee.getUser().getUsername(), newTrainers);

            return ResponseEntity.ok(Map.of("message", "Training deleted successfully"));
        } catch (ServiceException e) {
            LOG.error("Error deleting training: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
