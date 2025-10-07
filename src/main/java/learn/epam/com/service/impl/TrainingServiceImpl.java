package learn.epam.com.service.impl;

import learn.epam.com.dao.TrainingDao;
import learn.epam.com.dao.UserDao;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TraineeService;
import learn.epam.com.service.TrainerService;
import learn.epam.com.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainingServiceImpl implements TrainingService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingServiceImpl.class);
    private static final String SUCCESS_SAVE_TRAINING = "Training was created successfully";
    private static final String SUCCESS_UPDATE_TRAINING = "Training was updated successfully";
    private static final String SUCCESS_DELETE_TRAINING = "Training was deleted successfully";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found for username: ";
    private static final String TRAINER_NOT_FOUND = "Trainer not found for username: ";
    private static final String DURATION_MUST_BE_POSITIVE = "duration must be positive";
    private static final String TRAINEE_ID_REQUIRED = "Training.traineeId is required";
    private static final String TRAINER_ID_REQUIRED = "Training.trainerId is required";
    private static final String NAME_IS_REQUIRED = "Training.name is required";
    private static final String TRAINING_TYPE_REQUIRED = "Training.trainingTypeId is required";
    private static final String TRAINING_DATE_REQUIRED = "trainingDate required";
    private static final String ID_REQUIRED = "Training.id is required for update";
    private static final String NULL_EXCEPTION = "Argument is null ";

    private final TrainingDao trainingDao;
    private final UserDao userDao;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    @Autowired
    public TrainingServiceImpl(TrainingDao trainingDao, UserDao userDao, TraineeService traineeService, TrainerService trainerService) {
        this.trainingDao = trainingDao;
        this.userDao = userDao;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void save(Training training) throws ServiceException {
        if (training != null) {
            validateTrainingForCreate(training);

            trainingDao.save(training);

            LOG.info(SUCCESS_SAVE_TRAINING);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Optional<Training> findById(Long id) throws ServiceException {
        return trainingDao.getById(id);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void update(Training training) throws ServiceException {
        if (training != null) {
            validateTrainingForUpdate(training);

            trainingDao.update(training);

            LOG.info(SUCCESS_UPDATE_TRAINING);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }

    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void delete(Training training) throws ServiceException {
        if (training != null) {
            trainingDao.delete(training);

            LOG.info(SUCCESS_DELETE_TRAINING);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public List<Training> findAllTrainings() {
        return trainingDao.getAll();
    }

    @Override
    @Transactional
    public List<Training> findTrainingsForTraineeByCriteria(String traineeUsername, LocalDate fromDate, LocalDate toDate, String trainerName, Long trainingTypeId) throws ServiceException {
        if (traineeUsername != null) {
            Trainee trainee = traineeService.findTraineeByUsername(traineeUsername).orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND + traineeUsername));

            Long traineeId = trainee.getId();

            return trainingDao.findTrainingsByTraineeId(traineeId).stream()
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
                                    .flatMap(trainer -> userDao.getById(trainer.getUserId()))
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

            return trainingDao.findTrainingsByTrainerId(trainerId).stream()
                    .filter(training -> {
                        LocalDate date = training.getTrainingDate();

                        return (fromDate == null || !date.isBefore(fromDate)) &&
                                (toDate == null || !date.isAfter(toDate));
                    })
                    .filter(training -> {
                        if (traineeName == null || traineeName.isBlank()) {
                            return true;
                        }

                        return userDao.getById(training.getTrainerId())
                                .map(user -> traineeName.equalsIgnoreCase(user.getUsername()))
                                .orElse(false);
                    })
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
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
