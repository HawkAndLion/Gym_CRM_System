package learn.epam.com.service.impl;

import learn.epam.com.dto.TrainingDto;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.event.TrainingCreatedEvent;
import learn.epam.com.event.TrainingDeletedEvent;
import learn.epam.com.repository.TrainingRepository;
import learn.epam.com.repository.TrainingTypeRepository;
import learn.epam.com.repository.UserRepository;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TraineeService;
import learn.epam.com.service.TrainerService;
import learn.epam.com.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TrainingServiceImpl implements TrainingService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingServiceImpl.class);
    private static final String SUCCESS_SAVE_TRAINING = "Training was created successfully";
    private static final String SUCCESS_UPDATE_TRAINING = "Training was updated successfully";
    private static final String SUCCESS_DELETE_TRAINING = "Training was deleted successfully";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found for username: ";
    private static final String TRAINER_NOT_FOUND = "Trainer not found for username: ";
    private static final String ENTITY_NOT_FOUND = "Entity not found. Check its existence. ";
    private static final String DURATION_MUST_BE_POSITIVE = "duration must be positive";
    private static final String TRAINEE_ID_REQUIRED = "Training.traineeId is required";
    private static final String TRAINER_ID_REQUIRED = "Training.trainerId is required";
    private static final String NAME_IS_REQUIRED = "Training.name is required";
    private static final String TRAINING_TYPE_REQUIRED = "Training.trainingTypeId is required";
    private static final String TRAINING_DATE_REQUIRED = "trainingDate required";
    private static final String ID_REQUIRED = "Training.id is required for update";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String UNKNOWN_TRAINEE = "Unknown Trainee";
    private static final String SPACE = " ";
    private static final String UNKNOWN_TRAINING_TYPE = "Unknown Training Type";
    private static final String ERROR_MAPPING_TRAINING = "Error mapping training: {}";
    private static final String ASSIGN_TRAINER_TO_TRAINING = "Assign trainer to a training";
    private static final String TRAINING_NOT_FOUND = "Training not found: ";

    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingTypeRepository trainingTypeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public TrainingServiceImpl(TrainingRepository trainingRepository, UserRepository userRepository,
                               TraineeService traineeService, TrainerService trainerService,
                               TrainingTypeRepository trainingTypeRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.trainingRepository = trainingRepository;
        this.userRepository = userRepository;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingTypeRepository = trainingTypeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void save(Training training) throws ServiceException {
        if (training != null) {
            validateTrainingForCreate(training);

            trainingRepository.save(training);

            String traineeUsername = traineeService.findById(training.getTraineeId())
                    .map(trainee -> trainee.getUser().getUsername())
                    .orElseThrow(() -> new ServiceException(ENTITY_NOT_FOUND));

            String trainerUsername = trainerService.findById(training.getTrainerId())
                    .map(trainer -> trainer.getUser().getUsername())
                    .orElseThrow(() -> new ServiceException(ENTITY_NOT_FOUND));

            eventPublisher.publishEvent(
                    new TrainingCreatedEvent(training, traineeUsername, trainerUsername)
            );

            LOG.info(SUCCESS_SAVE_TRAINING);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Optional<Training> findById(Long id) throws ServiceException {
        return trainingRepository.findById(id);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void update(Training training) throws ServiceException {
        if (training != null) {
            validateTrainingForUpdate(training);

            trainingRepository.save(training);

            LOG.info(SUCCESS_UPDATE_TRAINING);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public Training update(Trainee trainee, Trainer trainer, TrainingDto trainingDto, Long trainingTypeId) throws ServiceException {
        Training training = new Training();
        training.setTraineeId(trainee.getId());
        training.setTrainerId(trainer.getId());
        training.setName(trainingDto.getName());
        training.setTrainingTypeId(trainingTypeId);
        training.setTrainingDate(trainingDto.getDate());
        training.setDuration(trainingDto.getDuration());

        return training;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void delete(Training training) throws ServiceException {
        if (training != null) {
            eventPublisher.publishEvent(new TrainingDeletedEvent(training));

            trainingRepository.delete(training);

            LOG.info(SUCCESS_DELETE_TRAINING);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteById(Long id) throws ServiceException {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new ServiceException(TRAINING_NOT_FOUND + id));

        eventPublisher.publishEvent(new TrainingDeletedEvent(training));

        trainingRepository.delete(training);

        LOG.info(SUCCESS_DELETE_TRAINING);
    }

    @Override
    @Transactional
    public List<Training> findAllTrainings() {
        return trainingRepository.findAll();
    }

    @Override
    @Transactional
    public List<Training> findTrainingsForTraineeByCriteria(String traineeUsername, LocalDate fromDate, LocalDate toDate, String trainerName, Long trainingTypeId) throws ServiceException {
        if (traineeUsername != null) {
            Trainee trainee = traineeService.findTraineeByUsername(traineeUsername).orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND + traineeUsername));

            Long traineeId = trainee.getId();

            return trainingRepository.findTrainingsByTraineeId(traineeId).stream()
                    .filter(training -> {
                        LocalDate date = training.getTrainingDate();

                        return (fromDate == null || !date.isBefore(fromDate)) &&
                                (toDate == null || !date.isAfter(toDate));
                    })
                    .filter(training -> {
                        if (trainerName == null || trainerName.isBlank()) {
                            return true;
                        }

                        try {
                            return trainerService.findById(training.getTrainerId())
                                    .flatMap(trainer -> userRepository.findById(trainer.getUser().getId()))
                                    .map(user -> trainerName.equalsIgnoreCase(user.getUsername()))
                                    .orElse(false);
                        } catch (ServiceException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(training -> trainingTypeId == null || training.getTrainingTypeId().equals(trainingTypeId))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public List<Training> findTrainingsForTrainerByCriteria(String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeName) throws ServiceException {
        if (trainerUsername != null) {
            Trainer trainer = trainerService.findTrainerByUsername(trainerUsername).orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND + trainerUsername));

            Long trainerId = trainer.getId();

            return trainingRepository.findTrainingsByTrainerId(trainerId).stream()
                    .filter(training -> {
                        LocalDate date = training.getTrainingDate();

                        return (fromDate == null || !date.isBefore(fromDate)) &&
                                (toDate == null || !date.isAfter(toDate));
                    })
                    .filter(training -> {
                        if (traineeName == null || traineeName.isBlank()) {
                            return true;
                        }

                        return userRepository.findById(training.getTrainerId())
                                .map(user -> traineeName.equalsIgnoreCase(user.getUsername()))
                                .orElse(false);
                    })
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public List<TrainingDto> getTrainingDtoList(List<Training> trainings) {
        return trainings.stream().map(training -> {
            try {
                String traineeFullName = traineeService
                        .findById(training.getTraineeId())
                        .flatMap(trainee -> userRepository.findById(trainee.getUser().getId()))
                        .map(u -> u.getFirstName() + SPACE + u.getLastName())
                        .orElse(UNKNOWN_TRAINEE);

                String trainingTypeName = trainingTypeRepository
                        .findById(training.getTrainingTypeId())
                        .map(tt -> tt.getName())
                        .orElse(UNKNOWN_TRAINING_TYPE);

                return new TrainingDto(
                        training.getName(),
                        training.getTrainingDate(),
                        trainingTypeName,
                        training.getDuration(),
                        traineeFullName
                );
            } catch (ServiceException e) {
                LOG.error(ERROR_MAPPING_TRAINING, e.getMessage());

                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    @Transactional(rollbackFor = ServiceException.class)
    public void updateTrainingsByTrainee(String username, Set<Trainer> trainers) throws ServiceException {
        Trainee trainee = traineeService.findTraineeByUsername(username)
                .orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND + username));

        List<Training> traineeTrainings = trainingRepository.findTrainingsByTraineeId(trainee.getId());

        traineeTrainings.forEach(t -> System.out.println(t.getName() + " " + t.getTrainingDate()));

        for (Trainer trainer : trainers) {
            Long trainerId = trainer.getId();

            List<Training> trainerTrainings = trainingRepository.findTrainingsByTrainerId(trainerId);

            if (trainerTrainings.isEmpty()) {
                throw new ServiceException(ASSIGN_TRAINER_TO_TRAINING);
            }

            for (Training trainerTraining : trainerTrainings) {
                boolean alreadyAssigned = traineeTrainings.stream()
                        .anyMatch(t -> t.getTrainerId().equals(trainerId)
                                && t.getName().equalsIgnoreCase(trainerTraining.getName())
                                && Objects.equals(t.getTrainingTypeId(), trainerTraining.getTrainingTypeId())
                                && t.getTrainingDate().equals(trainerTraining.getTrainingDate()));

                if (!alreadyAssigned) {
                    Training newTraining = new Training();
                    newTraining.setTraineeId(trainee.getId());
                    newTraining.setTrainerId(trainerId);
                    newTraining.setName(trainerTraining.getName());
                    newTraining.setTrainingTypeId(trainerTraining.getTrainingTypeId());
                    newTraining.setTrainingDate(LocalDate.now());
                    newTraining.setDuration(trainerTraining.getDuration());

                    trainingRepository.save(newTraining);
                }
            }
        }
    }

    @Override
    @Transactional
    public double getTotalDurationForTrainer(Long trainerId) throws ServiceException {
        if (trainerId != null) {
            return trainingRepository.findTrainingsByTrainerId(trainerId)
                    .stream()
                    .mapToDouble(Training::getDuration)
                    .sum();
        } else {
            throw new IllegalArgumentException("trainerId is required");
        }
    }

    private static void validateTrainingForCreate(Training training) throws ServiceException {
        if (training != null) {
            if (training.getTraineeId() == null) throw new ServiceException(TRAINEE_ID_REQUIRED);
            if (training.getTrainerId() == null) throw new ServiceException(TRAINER_ID_REQUIRED);
            if (isBlank(training.getName())) throw new ServiceException(NAME_IS_REQUIRED);
            if (training.getTrainingTypeId() == null) throw new ServiceException(TRAINING_TYPE_REQUIRED);
            if (training.getTrainingDate() == null) throw new ServiceException(TRAINING_DATE_REQUIRED);
            if (training.getDuration() <= 0) throw new ServiceException(DURATION_MUST_BE_POSITIVE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private static void validateTrainingForUpdate(Training training) throws ServiceException {
        if (training != null) {
            if (training.getId() == null) throw new ServiceException(ID_REQUIRED);
            validateTrainingForCreate(training);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
