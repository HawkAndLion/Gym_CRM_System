package learn.epam.com.controller;

import learn.epam.com.api.TrainingsApi;
import learn.epam.com.api.model.MessageResponse;
import learn.epam.com.api.model.TrainingRequest;
import learn.epam.com.api.model.TrainingResponse;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class TrainingController implements TrainingsApi {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingController.class);

    private static final String TRAINING_NOT_FOUND = "Training not found ";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found: ";
    private static final String TRAINER_NOT_FOUND = "Trainer not found: ";

    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingTypeService trainingTypeService;
    private final TrainingService trainingService;

    @Override
    public ResponseEntity<MessageResponse> addTraining(TrainingRequest request) {
        try {
            Trainee trainee = traineeService.findTraineeByUsername(request.getTraineeUsername())
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_NOT_FOUND + request.getTraineeUsername()));

            Trainer trainer = trainerService.findTrainerByUsername(request.getTrainerUsername())
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINER_NOT_FOUND + request.getTrainerUsername()));

            trainerService.assignTrainerToTrainee(
                    request.getTrainerUsername(),
                    request.getTraineeUsername()
            );

            Long trainingTypeId =
                    trainingTypeService.getTrainingTypeId(request.getTrainingType());

            Training training =
                    trainingService.update(trainee, trainer, request, trainingTypeId);

            trainingService.save(training);

            return ResponseEntity.ok(
                    new MessageResponse().message("Training added successfully")
            );

        } catch (ServiceException e) {
            LOG.error("Error adding training: {}", e.getMessage());
            return ResponseEntity
                    .status(404)
                    .body(new MessageResponse().message(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> getTrainingTypes() {
        List<Map<String, Object>> trainingTypes =
                trainingTypeService.getTrainingTypes();

        return ResponseEntity.ok(trainingTypes);
    }

    @Override
    public ResponseEntity<List<TrainingResponse>> getTrainings() {
        try {
            List<Training> trainings = trainingService.findAllTrainings();

            List<TrainingResponse> responseList = trainingService.getTrainingResponseList(trainings);

            return ResponseEntity.ok(responseList);

        } catch (Exception e) {
            LOG.error("Error retrieving trainings: {}", e.getMessage());

            return ResponseEntity.status(500).body(List.of());
        }
    }


    @Override
    public ResponseEntity<MessageResponse> deleteTraining(Long id) {
        try {
            Training training = trainingService.findById(id)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINING_NOT_FOUND));

            Trainee trainee = traineeService.findById(training.getTraineeId())
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_NOT_FOUND + training.getTraineeId()));

            Trainer trainer = trainerService.findById(training.getTrainerId())
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINER_NOT_FOUND + training.getTrainerId()));

            Set<Long> trainerIds =
                    traineeService.getTrainerIdsForTrainee(trainee.getId());

            Set<Trainer> newTrainers = new HashSet<>();
            for (Long trainerId : trainerIds) {
                if (!trainerId.equals(trainer.getId())) {
                    Trainer tr = trainerService.findById(trainerId)
                            .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINER_NOT_FOUND + trainerId));
                    newTrainers.add(tr);
                }
            }

            trainingService.deleteById(id);
            traineeService.update(trainee.getUser().getUsername(), newTrainers);

            return ResponseEntity.ok(
                    new MessageResponse().message("Training deleted successfully")
            );

        } catch (ServiceException e) {
            LOG.error("Error deleting training: {}", e.getMessage());

            return ResponseEntity
                    .status(404)
                    .body(new MessageResponse().message(e.getMessage()));
        }
    }
}
