package learn.epam.com.service.impl;

import learn.epam.com.dao.*;
import learn.epam.com.dto.TraineeTrainersDto;
import learn.epam.com.dto.TrainerProfileDto;
import learn.epam.com.dto.TrainingDto;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainerService;
import learn.epam.com.service.UserCredentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TrainerServiceImpl implements TrainerService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainerServiceImpl.class);
    private static final String SUCCESS_SAVE_TRAINER = "Trainer was created successfully";
    private static final String SUCCESS_UPDATE_TRAINER = "Trainer was updated successfully";
    private static final String SUCCESS_DELETE_TRAINER = "Trainer was deleted successfully";
    private static final String FAIL_FIND_TRAINER = "Trainer not found with id=";
    private static final String FAIL_LOAD_USER = "Failed to load user for trainer";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String AUTHENTICATION_FAIL = "Authentication failed";
    private static final String TRAINER_NOT_FOUND = "Trainee not found";
    private static final String TRAINER_ALREADY_ACTIVE = "Trainee already active";
    private static final String TRAINER_ALREADY_INACTIVE = "Trainee already inactive";
    private static final String NO_SUCH_USERNAME = "Trainer not found with username: ";
    private static final String USER_ID_REQUIRED = "Trainer.userId is required";
    private static final String SPECIALIZATION_REQUIRED = "Trainer.specialization is required";
    private static final String ID_REQUIRED = "Trainer.id is required for update";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String FETCH_UNASSIGNED_TRAINER = "Fetching unassigned trainers for traineeUsername={}";
    private static final String UPDATE_TRAINERS = "Updating trainers={} for trainee username={}";
    private static final String CHECK_TRAINEE_USERNAME = "Check if trainee username correct";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found: ";
    private static final String FETCH_TRAINEES_MESSAGE = "Fetching trainees for trainerId={}";
    private static final String EMPTY_TRAINER_LIST = "Please check the contents of trainer list. It might be empty.";
    private static final String UNKNOWN_TRAINER = "Unknown Trainer";
    private static final String UNKNOWN_TYPE = "Unknown Type";
    private static final String ERROR_TRAINING_MAPPING = "Error training mapping: {}";

    private final TrainerDao trainerDao;
    private final UserCredentialService userCredentialService;
    private final UserDao userDao;
    private final TrainingDao trainingDao;
    private final TrainingTypeDao trainingTypeDao;

    @Autowired
    public TrainerServiceImpl(TrainerDao trainerDao, UserCredentialService userCredentialService, UserDao userDao, TrainingDao trainingDao, TrainingTypeDao trainingTypeDao) {
        this.trainerDao = trainerDao;
        this.userCredentialService = userCredentialService;
        this.userDao = userDao;
        this.trainingDao = trainingDao;
        this.trainingTypeDao = trainingTypeDao;
    }


    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void save(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            validateTrainerForCreate(trainer);

            userCredentialService.ensureUsernameExists(trainer.getUser());
            userCredentialService.ensurePassword(trainer.getUser());

            trainerDao.save(trainer);

            LOG.info(SUCCESS_SAVE_TRAINER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Optional<Trainer> findById(Long id) throws ServiceException {
        return trainerDao.getById(id);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void update(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            validateTrainerForUpdate(trainer);

            trainerDao.update(trainer);

            LOG.info(SUCCESS_UPDATE_TRAINER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void delete(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            trainerDao.delete(trainer);

            LOG.info(SUCCESS_DELETE_TRAINER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public List<Trainer> findAllTrainers() {
        return trainerDao.getAll();
    }

    @Override
    @Transactional
    public boolean checkCredentials(Long trainerId, String username, String password) throws ServiceException {
        if (trainerId != null && username != null && password != null) {
            Trainer trainer = findById(trainerId)
                    .orElseThrow(() -> new ServiceException(FAIL_FIND_TRAINER + trainerId));

            Long userId = trainer.getUser().getId();
            User user = loadUser(userId);

            return username.equalsIgnoreCase(user.getUsername()) && password.equals(user.getPassword());

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public Optional<Trainer> findTrainerByCredentials(String username, String password) {
        List<User> users = userDao.getAll();
        return users.stream()
                .filter(u -> username.equalsIgnoreCase(u.getUsername()) && password.equals(u.getPassword()))
                .findFirst()
                .flatMap(u ->
                        trainerDao.getAll().stream()
                                .filter(t -> t.getUser().getId().equals(u.getId()))
                                .findFirst());
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public Optional<Trainer> findTrainerByUsername(String username) throws ServiceException {
        if (username != null) {
            return Optional.ofNullable(userDao.getAll().stream()
                    .filter(user -> user.getUsername().equalsIgnoreCase(username))
                    .findFirst()
                    .flatMap(user -> trainerDao.getAll().stream()
                            .filter(trainer -> trainer.getUser().getId().equals(user.getId()))
                            .findFirst())
                    .orElseThrow(() -> new ServiceException(NO_SUCH_USERNAME + username)));
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteTrainerByUsername(String username) throws ServiceException {
        if (username != null) {
            Trainer trainer = findTrainerByUsername(username)
                    .orElseThrow(() -> new ServiceException(AUTHENTICATION_FAIL));

            Long trainerId = trainer.getId();

            trainingDao.getAll().stream()
                    .filter(training -> training.getTrainerId().equals(trainerId))
                    .forEach(training -> {
                        trainingDao.delete(training);
                    });

            trainerDao.delete(trainer);

            userDao.getById(trainer.getUser().getId()).ifPresent(userDao::delete);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void activateTrainer(String username) throws ServiceException {
        Trainer trainer = findTrainerByUsername(username).orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND));
        User user = userDao.getById(trainer.getUser().getId()).orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

        if (trainer.isActive()) throw new ServiceException(TRAINER_ALREADY_ACTIVE);

        user.setActive(true);
        userDao.update(user);
        trainer.setActive(true);
        trainerDao.update(trainer);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deactivateTrainer(String username) throws ServiceException {
        Trainer trainer = findTrainerByUsername(username).orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND));
        User user = userDao.getById(trainer.getUser().getId()).orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

        if (!trainer.isActive()) throw new ServiceException(TRAINER_ALREADY_INACTIVE);

        user.setActive(false);
        userDao.update(user);
        trainer.setActive(false);
        trainerDao.update(trainer);
    }

    @Override
    @Transactional
    public List<Trainer> getUnassignedTrainersForTrainee(String username) throws ServiceException {
        LOG.debug(FETCH_UNASSIGNED_TRAINER, username);

        try {
            return trainerDao.getUnassignedTrainersForTrainee(username);
        } catch (DaoException exception) {
            throw new ServiceException(TRAINEE_NOT_FOUND, exception);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void updateTraineeTrainersList(String traineeUsername, Set<Long> trainerIds) throws ServiceException {
        LOG.info(UPDATE_TRAINERS, trainerIds, traineeUsername);

        try {
            trainerDao.updateTraineeTrainersList(traineeUsername, trainerIds);
        } catch (DaoException e) {
            throw new ServiceException(CHECK_TRAINEE_USERNAME, e);
        }
    }

    @Override
    @Transactional
    public List<TrainerProfileDto> getTrainerProfileDtos(Set<Trainer> trainers) throws ServiceException {
        if (!trainers.isEmpty()) {
            return trainers.stream()
                    .map(trainer -> {
                        User trainerUser = userDao.getById(trainer.getUser().getId()).orElse(null);
                        if (trainerUser != null) {
                            return new TrainerProfileDto(
                                    trainerUser.getUsername(),
                                    trainerUser.getFirstName(),
                                    trainerUser.getLastName(),
                                    trainer.getSpecialization()
                            );
                        }

                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } else {
            throw new ServiceException(EMPTY_TRAINER_LIST);
        }
    }

    @Override
    @Transactional
    public Set<TrainerProfileDto> getTrainerProfileDtoList(String username) throws ServiceException {
        List<Trainer> trainers = getUnassignedTrainersForTrainee(username);

        Set<TrainerProfileDto> profileDtos = new HashSet<>(Set.of());

        for (Trainer trainer : trainers) {
            User user = userDao.getById(trainer.getUser().getId()).orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
            TrainerProfileDto profileDto = new TrainerProfileDto(user.getUsername(), user.getFirstName(), user.getLastName(), trainer.getSpecialization());
            profileDtos.add(profileDto);
        }

        return profileDtos;
    }

    private User loadUser(Long userId) throws ServiceException {
        try {
            return userCredentialService
                    .loadUserOrThrow(userId);
        } catch (ServiceException exception) {
            throw new ServiceException(FAIL_LOAD_USER, exception);
        }
    }

    public static void validateTrainerForCreate(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            if (trainer.getUser() == null) throw new ServiceException(USER_ID_REQUIRED);

            if (isBlank(trainer.getSpecialization())) throw new ServiceException(SPECIALIZATION_REQUIRED);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    public static void validateTrainerForUpdate(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            if (trainer.getId() == null) throw new ServiceException(ID_REQUIRED);

            if (trainer.getUser() == null) throw new ServiceException(USER_ID_REQUIRED);

            validateTrainerForCreate(trainer);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Set<Long> getTraineeIdsForTrainer(Long trainerId) {
        LOG.debug(FETCH_TRAINEES_MESSAGE, trainerId);

        return trainerDao.getTraineeIdsForTrainer(trainerId);
    }

    @Override
    @Transactional
    public Set<Trainer> getTrainersByUsername(TraineeTrainersDto request) throws ServiceException {
        Set<Trainer> newTrainers = new HashSet<>();

        for (String trainerUsername : request.getTrainerUsernames()) {
            Trainer trainer = findTrainerByUsername(trainerUsername)
                    .orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND + trainerUsername));

            newTrainers.add(trainer);
        }

        return newTrainers;
    }

    @Override
    @Transactional
    public List<TrainingDto> getTrainingDtos(List<Training> trainings) {
        return trainings.stream().map(training -> {
            try {
                String trainerFullName = findById(training.getTrainerId())
                        .flatMap(trainer -> userDao.getById(trainer.getUser().getId()))
                        .map(u -> u.getFirstName() + " " + u.getLastName())
                        .orElse(UNKNOWN_TRAINER);

                String trainingTypeName = trainingTypeDao
                        .getById(training.getTrainingTypeId())
                        .map(tt -> tt.getName())
                        .orElse(UNKNOWN_TYPE);

                return new TrainingDto(
                        training.getName(),
                        training.getTrainingDate(),
                        trainingTypeName,
                        training.getDuration(),
                        trainerFullName
                );
            } catch (ServiceException e) {
                LOG.error(ERROR_TRAINING_MAPPING, e.getMessage());

                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
