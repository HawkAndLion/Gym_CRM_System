package learn.epam.com.service.impl;

import learn.epam.com.dto.TraineeTrainersDto;
import learn.epam.com.dto.TrainerProfileDto;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.repository.*;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainerService;
import learn.epam.com.service.UserCredentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String UNKNOWN_TRAINER = "Unknown Trainer";
    private static final String UNKNOWN_TYPE = "Unknown Type";
    private static final String ERROR_TRAINING_MAPPING = "Error training mapping: {}";
    private static final String TRAINEE_NOT_FOUND_BY_USERNAME = "Trainee not found by username: %s";
    private static final String FAIL_UPDATE_TRAINERS = "Failed to update trainers for trainee: {}";
    private static final String FAIL_UPDATE_TRAINERS_MESSAGE = "Failed to update trainers for trainee: %s";
    private static final String SPACE = " ";

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final UserCredentialService userCredentialService;
    private final UserRepository userRepository;
    private final TrainingRepository trainingRepository;
//    private final TrainingTypeRepository trainingTypeRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public TrainerServiceImpl(TrainerRepository trainerRepository, TraineeRepository traineeRepository, UserCredentialService userCredentialService, UserRepository userRepository, TrainingRepository trainingRepository, TrainingTypeRepository trainingTypeRepository, PasswordEncoder passwordEncoder) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.userCredentialService = userCredentialService;
        this.userRepository = userRepository;
        this.trainingRepository = trainingRepository;
//        this.trainingTypeRepository = trainingTypeRepository;
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
                    .orElseThrow(() -> new ServiceException(String.format(FAIL_FIND_TRAINER, trainerId)));

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
        Trainer trainer = findTrainerByUsername(username).orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND));
        User user = userRepository.findById(trainer.getUser().getId()).orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

        if (trainer.isActive()) throw new ServiceException(TRAINER_ALREADY_ACTIVE);

        user.setActive(true);
        userRepository.save(user);
        trainer.setActive(true);
        trainerRepository.save(trainer);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deactivateTrainer(String username) throws ServiceException {
        Trainer trainer = findTrainerByUsername(username).orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND));
        User user = userRepository.findById(trainer.getUser().getId()).orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

        if (!trainer.isActive()) throw new ServiceException(TRAINER_ALREADY_INACTIVE);

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
                    .orElseThrow(() -> new ServiceException(String.format(TRAINEE_NOT_FOUND_BY_USERNAME, username)));

            return trainerRepository.findUnassignedTrainersForTrainee(trainee.getId());
        } catch (Exception exception) {
            throw new ServiceException(TRAINEE_NOT_FOUND, exception);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void updateTraineeTrainersList(String traineeUsername, Set<Long> trainerIds) throws ServiceException {
        LOG.info(UPDATE_TRAINERS, trainerIds, traineeUsername);

        try {
            Trainee trainee = traineeRepository.findByUsername(traineeUsername)
                    .orElseThrow(() -> new ServiceException(String.format(TRAINEE_NOT_FOUND_BY_USERNAME, traineeUsername)));

            traineeRepository.removeAllTrainerRelations(trainee.getId());

            if (trainerIds != null && !trainerIds.isEmpty()) {
                for (Long trainerId : trainerIds) {
                    traineeRepository.addTrainerRelation(trainee.getId(), trainerId);
                }
            }

        } catch (Exception e) {
            LOG.error(FAIL_UPDATE_TRAINERS, traineeUsername, e);

            throw new ServiceException(String.format(FAIL_UPDATE_TRAINERS_MESSAGE, traineeUsername), e);
        }
    }

    @Override
    @Transactional
    public List<TrainerProfileDto> getTrainerProfileDtos(Set<Trainer> trainers) throws ServiceException {
        if (!trainers.isEmpty()) {
            return trainers.stream()
                    .map(trainer -> {
                        User trainerUser = userRepository.findById(trainer.getUser().getId()).orElse(null);
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
            User user = userRepository.findById(trainer.getUser().getId()).orElseThrow(() -> new ServiceException(USER_NOT_FOUND));
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

        return trainerRepository.findTraineeIdsForTrainer(trainerId);
    }

    @Override
    @Transactional
    public Set<Trainer> getTrainersByUsername(TraineeTrainersDto request) throws ServiceException {
        Set<Trainer> newTrainers = new HashSet<>();

        for (String trainerUsername : request.getTrainerUsernames()) {
            Trainer trainer = findTrainerByUsername(trainerUsername)
                    .orElseThrow(() -> new ServiceException(TRAINEE_NOT_FOUND));

            newTrainers.add(trainer);
        }

        return newTrainers;
    }

//    @Override
//    @Transactional
//    public List<TrainingResponse> getTrainingResponses(List<Training> trainings) {
//        return trainings.stream().map(training -> {
//            try {
//                String trainerFullName = findById(training.getTrainerId())
//                        .flatMap(trainer -> userRepository.findById(trainer.getUser().getId()))
//                        .map(u -> u.getFirstName() + SPACE + u.getLastName())
//                        .orElse(UNKNOWN_TRAINER);
//
//                String trainingTypeName = trainingTypeRepository
//                        .findById(training.getTrainingTypeId())
//                        .map(tt -> tt.getName())
//                        .orElse(UNKNOWN_TYPE);
//
//                return new TrainingResponse()
//                        .name(training.getName())
//                        .date(training.getTrainingDate())
//                        .trainingType(trainingTypeName)
//                        .duration(training.getDuration())
//                        .traineeName(trainerFullName);
//            } catch (ServiceException e) {
//                LOG.error(ERROR_TRAINING_MAPPING, e.getMessage());
//
//                return null;
//            }
//        }).filter(Objects::nonNull).toList();
//    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void assignTrainerToTrainee(String trainerUsername, String traineeUsername)
            throws ServiceException {
        if (trainerUsername != null && traineeUsername != null) {
            Trainee trainee = traineeRepository.findByUsername(traineeUsername).orElseThrow(() ->
                    new ServiceException(String.format(TRAINEE_NOT_FOUND_BY_USERNAME, traineeUsername)));

            Trainer trainer = findTrainerByUsername(trainerUsername).orElseThrow(() ->
                    new ServiceException(NO_SUCH_USERNAME + trainerUsername));

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
