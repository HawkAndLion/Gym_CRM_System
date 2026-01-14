package learn.epam.com.service.impl;

import learn.epam.com.api.model.TraineeTrainersRequest;
import learn.epam.com.api.model.TrainerProfileResponse;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.repository.TraineeRepository;
import learn.epam.com.repository.TrainerRepository;
import learn.epam.com.repository.TrainingRepository;
import learn.epam.com.repository.UserRepository;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainerService;
import learn.epam.com.service.UserCredentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TrainerServiceImpl implements TrainerService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainerServiceImpl.class);
    private static final String SUCCESS_SAVE_TRAINER = "Trainer was created successfully";
    private static final String SUCCESS_UPDATE_TRAINER = "Trainer was updated successfully";
    private static final String SUCCESS_DELETE_TRAINER = "Trainer was deleted successfully";
    private static final String FAIL_FIND_TRAINER = "Trainer not found with id %d";
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
    private static final String TRAINEE_NOT_FOUND = "Trainee not found ";
    private static final String FETCH_TRAINEES_MESSAGE = "Fetching trainees for trainerId={}";
    private static final String EMPTY_TRAINER_LIST = "Please check the contents of trainer list. It might be empty.";
    private static final String TRAINEE_NOT_FOUND_BY_USERNAME = "Trainee not found by username: %s";
    private static final String FAIL_UPDATE_TRAINERS = "Failed to update trainers for trainee: {}";
    private static final String FAIL_UPDATE_TRAINERS_MESSAGE = "Failed to update trainers for trainee: %s";

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final UserCredentialService userCredentialService;
    private final UserRepository userRepository;
    private final TrainingRepository trainingRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public TrainerServiceImpl(TrainerRepository trainerRepository, TraineeRepository traineeRepository, UserCredentialService userCredentialService, UserRepository userRepository, TrainingRepository trainingRepository, PasswordEncoder passwordEncoder) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.userCredentialService = userCredentialService;
        this.userRepository = userRepository;
        this.trainingRepository = trainingRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void save(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            validateTrainerForCreate(trainer);

            userCredentialService.ensureUsernameExists(trainer.getUser());
            userCredentialService.ensurePassword(trainer.getUser());

            trainerRepository.save(trainer);

            LOG.info(SUCCESS_SAVE_TRAINER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Optional<Trainer> findById(Long id) throws ServiceException {
        return trainerRepository.findById(id);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void update(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            validateTrainerForUpdate(trainer);

            trainerRepository.save(trainer);

            LOG.info(SUCCESS_UPDATE_TRAINER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void delete(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            trainerRepository.delete(trainer);

            LOG.info(SUCCESS_DELETE_TRAINER);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public List<Trainer> findAllTrainers() {
        return trainerRepository.findAll();
    }

    @Override
    @Transactional
    public boolean checkCredentials(Long trainerId, String username, String password) throws ServiceException {
        if (trainerId != null && username != null && password != null) {
            Trainer trainer = findById(trainerId)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, String.format(FAIL_FIND_TRAINER, trainerId)));

            Long userId = trainer.getUser().getId();
            User user = loadUser(userId);

            return username.equalsIgnoreCase(user.getUsername())
                    && passwordEncoder.matches(password, user.getPassword());

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public Optional<Trainer> findTrainerByCredentials(String username, String password) {
        List<User> users = userRepository.findAll();
        return users.stream()
                .filter(u -> username.equalsIgnoreCase(u.getUsername()) && password.equals(u.getPassword()))
                .findFirst()
                .flatMap(u ->
                        trainerRepository.findAll().stream()
                                .filter(t -> t.getUser().getId().equals(u.getId()))
                                .findFirst());
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public Optional<Trainer> findTrainerByUsername(String username) throws ServiceException {
        if (username != null) {
            return Optional.ofNullable(userRepository.findAll().stream()
                    .filter(user -> user.getUsername().equalsIgnoreCase(username))
                    .findFirst()
                    .flatMap(user -> trainerRepository.findAll().stream()
                            .filter(trainer -> trainer.getUser().getId().equals(user.getId()))
                            .findFirst())
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, NO_SUCH_USERNAME + username)));
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteTrainerByUsername(String username) throws ServiceException {
        if (username != null) {
            Trainer trainer = findTrainerByUsername(username)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, AUTHENTICATION_FAIL));

            Long trainerId = trainer.getId();

            trainingRepository.findAll().stream()
                    .filter(training -> training.getTrainerId().equals(trainerId))
                    .forEach(training -> {
                        trainingRepository.delete(training);
                    });

            trainerRepository.delete(trainer);

            userRepository.findById(trainer.getUser().getId()).ifPresent(userRepository::delete);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void activateTrainer(String username) throws ServiceException {
        Trainer trainer = findTrainerByUsername(username).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINER_NOT_FOUND));
        User user = userRepository.findById(trainer.getUser().getId()).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        if (trainer.isActive()) throw new ServiceException(HttpStatus.BAD_REQUEST, TRAINER_ALREADY_ACTIVE);

        user.setActive(true);
        userRepository.save(user);
        trainer.setActive(true);
        trainerRepository.save(trainer);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deactivateTrainer(String username) throws ServiceException {
        Trainer trainer = findTrainerByUsername(username).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINER_NOT_FOUND));
        User user = userRepository.findById(trainer.getUser().getId()).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        if (!trainer.isActive()) throw new ServiceException(HttpStatus.BAD_REQUEST, TRAINER_ALREADY_INACTIVE);

        user.setActive(false);
        userRepository.save(user);
        trainer.setActive(false);
        trainerRepository.save(trainer);
    }

    @Override
    @Transactional
    public List<Trainer> getUnassignedTrainersForTrainee(String username) throws ServiceException {
        try {
            LOG.debug(FETCH_UNASSIGNED_TRAINER, username);

            Trainee trainee = traineeRepository.findByUsername(username)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, String.format(TRAINEE_NOT_FOUND_BY_USERNAME, username)));

            return trainerRepository.findUnassignedTrainersForTrainee(trainee.getId());
        } catch (Exception exception) {
            throw new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_NOT_FOUND, exception);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void updateTraineeTrainersList(String traineeUsername, Set<Long> trainerIds) throws ServiceException {
        LOG.info(UPDATE_TRAINERS, trainerIds, traineeUsername);

        try {
            Trainee trainee = traineeRepository.findByUsername(traineeUsername)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, String.format(TRAINEE_NOT_FOUND_BY_USERNAME, traineeUsername)));

            traineeRepository.removeAllTrainerRelations(trainee.getId());

            if (trainerIds != null && !trainerIds.isEmpty()) {
                for (Long trainerId : trainerIds) {
                    traineeRepository.addTrainerRelation(trainee.getId(), trainerId);
                }
            }

        } catch (Exception e) {
            LOG.error(FAIL_UPDATE_TRAINERS, traineeUsername, e);

            throw new ServiceException(HttpStatus.BAD_REQUEST, String.format(FAIL_UPDATE_TRAINERS_MESSAGE, traineeUsername), e);
        }
    }

    @Override
    @Transactional
    public List<TrainerProfileResponse> getTrainerProfileResponse(Set<Trainer> trainers) throws ServiceException {
        if (!trainers.isEmpty()) {
            return trainers.stream()
                    .map(trainer -> {
                        User trainerUser = userRepository.findById(trainer.getUser().getId()).orElse(null);
                        if (trainerUser != null) {
                            return new TrainerProfileResponse()
                                    .username(trainerUser.getUsername())
                                    .firstName(trainerUser.getFirstName())
                                    .lastName(trainerUser.getLastName())
                                    .specialization(trainer.getSpecialization());
                        }

                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } else {
            throw new ServiceException(HttpStatus.BAD_REQUEST, EMPTY_TRAINER_LIST);
        }
    }

    @Override
    @Transactional
    public Set<TrainerProfileResponse> getTrainerProfileDtoList(String username) throws ServiceException {
        List<Trainer> trainers = getUnassignedTrainersForTrainee(username);

        Set<TrainerProfileResponse> profileResponses = new HashSet<>();

        for (Trainer trainer : trainers) {
            User user = userRepository.findById(trainer.getUser().getId())
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

            TrainerProfileResponse profileResponse = new TrainerProfileResponse()
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .specialization(trainer.getSpecialization())
                    .active(trainer.isActive())
                    .trainees(Collections.emptyList());

            profileResponses.add(profileResponse);
        }

        return profileResponses;
    }

    private User loadUser(Long userId) throws ServiceException {
        try {
            return userCredentialService
                    .loadUserOrThrow(userId);
        } catch (ServiceException exception) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, FAIL_LOAD_USER, exception);
        }
    }

    public static void validateTrainerForCreate(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            if (trainer.getUser() == null) throw new ServiceException(HttpStatus.BAD_REQUEST, USER_ID_REQUIRED);

            if (isBlank(trainer.getSpecialization())) throw new ServiceException(HttpStatus.BAD_REQUEST, SPECIALIZATION_REQUIRED);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    public static void validateTrainerForUpdate(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            if (trainer.getId() == null) throw new ServiceException(HttpStatus.BAD_REQUEST, ID_REQUIRED);

            if (trainer.getUser() == null) throw new ServiceException(HttpStatus.BAD_REQUEST, USER_ID_REQUIRED);

            validateTrainerForCreate(trainer);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Set<Long> getTraineeIdsForTrainer(Long trainerId) {
        LOG.debug(FETCH_TRAINEES_MESSAGE, trainerId);

        return trainerRepository.findTraineeIdsForTrainer(trainerId);
    }

    @Override
    @Transactional
    public Set<Trainer> getTrainersByUsername(TraineeTrainersRequest request) throws ServiceException {
        Set<Trainer> newTrainers = new HashSet<>();

        for (String trainerUsername : request.getTrainerUsernames()) {
            Trainer trainer = findTrainerByUsername(trainerUsername)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_NOT_FOUND));

            newTrainers.add(trainer);
        }

        return newTrainers;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void assignTrainerToTrainee(String trainerUsername, String traineeUsername)
            throws ServiceException {
        if (trainerUsername != null && traineeUsername != null) {
            Trainee trainee = traineeRepository.findByUsername(traineeUsername).orElseThrow(() ->
                    new ServiceException(HttpStatus.NOT_FOUND, String.format(TRAINEE_NOT_FOUND_BY_USERNAME, traineeUsername)));

            Trainer trainer = findTrainerByUsername(trainerUsername).orElseThrow(() ->
                    new ServiceException(HttpStatus.NOT_FOUND, NO_SUCH_USERNAME + trainerUsername));

            Long traineeId = trainee.getId();
            Long trainerId = trainer.getId();

            traineeRepository.addTrainerRelation(traineeId, trainerId);

            LOG.info("Trainer {} successfully assigned to trainee {}", trainerUsername, traineeUsername);

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
