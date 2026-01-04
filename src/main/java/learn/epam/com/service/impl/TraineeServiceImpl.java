package learn.epam.com.service.impl;

import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.repository.TraineeRepository;
import learn.epam.com.repository.TrainerRepository;
import learn.epam.com.repository.TrainingRepository;
import learn.epam.com.repository.UserRepository;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TraineeService;
import learn.epam.com.service.UserCredentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TraineeServiceImpl implements TraineeService {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeServiceImpl.class);
    private static final String SUCCESS_SAVE_TRAINEE = "Trainee was created successfully";
    private static final String SUCCESS_UPDATE_TRAINEE = "Trainee was updated successfully";
    private static final String SUCCESS_DELETE_TRAINEE = "Trainee was deleted successfully";
    private static final String FAIL_FIND_TRAINEE = "Trainee not found with id=";
    private static final String FAIL_LOAD_USER = "Failed to load user for trainee";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String AUTHENTICATION_FAIL = "Authentication failed";
    private static final String TRAINEE_ALREADY_ACTIVE = "Trainee already active";
    private static final String TRAINEE_ALREADY_INACTIVE = "Trainee already inactive";
    private static final String USER_ID_REQUIRED = "Trainee.userId is required";
    private static final String ADDRESS_REQUIRED = "Trainee.address is required";
    private static final String DATE_OF_BIRTH_REQUIRED = "Trainee.dateOfBirth is required";
    private static final String DATE_OF_BIRTH_IN_PAST_REQUIRED = "Trainee.dateOfBirth must be in the past";
    private static final String ID_REQUIRED = "Trainee.id is required for update";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String FETCH_TRAINERS_MESSAGE = "Fetching trainers for traineeId={}";
    private static final String SET_TRAINERS_MESSAGE = "Setting trainers {} for traineeId={}";
    private static final String ASSIGN_TRAINER_MESSAGE = "Assigning trainerId={} to traineeId={}";
    private static final String UNASSIGN_TRAINER_MESSAGE = "Unassigning trainerId={} from traineeId={}";
    private static final String TRAINEE_NOT_FOUND_BY_ID = "Trainee not found for id %d";
    private static final String TRAINER_NOT_FOUND_BY_ID = "Trainer not found for id %d";

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserCredentialService userCredentialService;
    private final UserRepository userRepository;
    private final TrainingRepository trainingRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TraineeServiceImpl(TraineeRepository traineeRepository,
                              TrainerRepository trainerRepository,
                              UserCredentialService userCredentialService,
                              UserRepository userRepository,
                              TrainingRepository trainingRepository,
                              PasswordEncoder passwordEncoder) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.userCredentialService = userCredentialService;
        this.userRepository = userRepository;
        this.trainingRepository = trainingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void save(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            validateTraineeForCreate(trainee);

            userCredentialService.ensureUsernameExists(trainee.getUser());
            userCredentialService.ensurePassword(trainee.getUser());

            if (trainee.getUser() != null && trainee.getUser().getId() == null) {
                userRepository.save(trainee.getUser());
            }

            traineeRepository.save(trainee);

            LOG.info(SUCCESS_SAVE_TRAINEE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Optional<Trainee> findById(Long id) throws ServiceException {
        return traineeRepository.findById(id);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void update(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            validateTraineeForUpdate(trainee);

            traineeRepository.save(trainee);

            LOG.info(SUCCESS_UPDATE_TRAINEE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void update(String username, Set<Trainer> trainers) throws ServiceException {
        Trainee trainee = findTraineeByUsername(username)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_NOT_FOUND));

        trainee.setTrainers(trainers);
        update(trainee);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void delete(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            traineeRepository.delete(trainee);

            LOG.info(SUCCESS_DELETE_TRAINEE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public List<Trainee> findAllTrainee() {
        return traineeRepository.findAll();
    }

    @Override
    @Transactional
    public boolean checkCredentials(Long traineeId, String username, String password) throws ServiceException {
        if (traineeId != null && username != null && password != null) {
            Trainee trainee = findById(traineeId)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, FAIL_FIND_TRAINEE + traineeId));

            Long userId = trainee.getUser().getId();
            User user = loadUser(userId);

            return username.equalsIgnoreCase(user.getUsername())
                    && passwordEncoder.matches(password, user.getPassword());

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> findTraineeByCredentials(String username, String password) {
        if (username != null & password != null) {
            List<User> users = userRepository.findAll();

            return users.stream()
                    .filter(u -> username.equalsIgnoreCase(u.getUsername()) && password.equals(u.getPassword()))
                    .findFirst()
                    .flatMap(u ->
                            traineeRepository.findAll().stream()
                                    .filter(t -> t.getUser().getId().equals(u.getId()))
                                    .findFirst());

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public Optional<Trainee> findTraineeByUsername(String username) throws ServiceException {
        if (username != null) {
            try {
                return traineeRepository.findByUsername(username);
            } catch (Exception exception) {
                throw new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_NOT_FOUND);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void activateTrainee(String username) throws ServiceException {
        if (username != null) {
            Trainee trainee = findTraineeByUsername(username).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_NOT_FOUND));
            User user = userRepository.findById(trainee.getUser().getId()).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

            if (trainee.isActive()) throw new ServiceException(HttpStatus.BAD_REQUEST, TRAINEE_ALREADY_ACTIVE);

            user.setActive(true);
            userRepository.save(user);
            trainee.setActive(true);
            traineeRepository.save(trainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deactivateTrainee(String username) throws ServiceException {
        if (username != null) {
            Trainee trainee = findTraineeByUsername(username).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_NOT_FOUND));
            User user = userRepository.findById(trainee.getUser().getId()).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

            if (!trainee.isActive()) throw new ServiceException(HttpStatus.BAD_REQUEST, TRAINEE_ALREADY_INACTIVE);

            user.setActive(false);
            userRepository.save(user);
            trainee.setActive(false);
            traineeRepository.save(trainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteTraineeByUsername(String username) throws ServiceException {
        if (username != null) {
            Trainee trainee = findTraineeByUsername(username)
                    .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, AUTHENTICATION_FAIL));

            Long traineeId = trainee.getId();

            trainingRepository.findAll().stream()
                    .filter(training -> training.getTraineeId().equals(traineeId))
                    .forEach(training -> {
                        trainingRepository.delete(training);
                    });

            Set<Trainer> trainers = trainee.getTrainers();
            if (trainers != null && !trainers.isEmpty()) {
                for (Trainer trainer : trainers) {
                    trainer.getTrainees().remove(trainee);
                    trainerRepository.save(trainer);
                }
            }

            traineeRepository.delete(trainee);

            userRepository.findById(trainee.getUser().getId()).ifPresent(userRepository::delete);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Set<Long> getTrainerIdsForTrainee(Long traineeId) {
        LOG.debug(FETCH_TRAINERS_MESSAGE, traineeId);

        return traineeRepository.findTrainerIdsByTraineeId(traineeId);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds) throws ServiceException {
        LOG.info(SET_TRAINERS_MESSAGE, trainerIds, traineeId);

        if (traineeId != null && trainerIds != null) {
            Trainee trainee = traineeRepository.findById(traineeId).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, String.format(TRAINEE_NOT_FOUND_BY_ID, traineeId)));

            Set<Trainer> trainers = new HashSet<>(trainee.getTrainers());
            for (Trainer tr : trainers) {
                tr.getTrainees().remove(trainee);
                trainerRepository.save(tr);
            }
            trainee.getTrainers().clear();

            List<Trainer> trainerList = trainerRepository.findAllById(trainerIds);
            for (Trainer tr : trainerList) {
                trainee.getTrainers().add(tr);
                tr.getTrainees().add(trainee);
                trainerRepository.save(tr);
            }

            traineeRepository.save(trainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void assignTrainer(Long traineeId, Long trainerId) throws ServiceException {
        LOG.info(ASSIGN_TRAINER_MESSAGE, trainerId, traineeId);

        if (traineeId != null && trainerId != null) {
            Trainee trainee = traineeRepository.findById(traineeId).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, String.format(TRAINEE_NOT_FOUND_BY_ID, traineeId)));
            Trainer trainer = trainerRepository.findById(trainerId).orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, String.format(TRAINER_NOT_FOUND_BY_ID, trainerId)));

            trainee.getTrainers().add(trainer);
            trainer.getTrainees().add(trainee);

            trainerRepository.save(trainer);
            traineeRepository.save(trainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void unassignTrainer(Long traineeId, Long trainerId) {
        LOG.info(UNASSIGN_TRAINER_MESSAGE, trainerId, traineeId);

        if (traineeId != null && trainerId != null) {
            Trainee trainee = traineeRepository.findById(traineeId).orElseThrow(() -> new IllegalArgumentException(String.format(TRAINEE_NOT_FOUND_BY_ID, traineeId)));
            Trainer trainer = trainerRepository.findById(trainerId).orElseThrow(() -> new IllegalArgumentException(String.format(TRAINER_NOT_FOUND_BY_ID, trainerId)));

            trainee.getTrainers().remove(trainer);
            trainer.getTrainees().remove(trainee);

            trainerRepository.save(trainer);
            traineeRepository.save(trainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private User loadUser(Long userId) throws ServiceException {
        try {
            return userCredentialService.loadUserOrThrow(userId);
        } catch (ServiceException exception) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, FAIL_LOAD_USER, exception);
        }
    }

    private static void validateTraineeForCreate(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            if (trainee.getUser() == null) throw new ServiceException(HttpStatus.BAD_REQUEST, USER_ID_REQUIRED);

            if (isBlank(trainee.getAddress())) throw new ServiceException(HttpStatus.BAD_REQUEST, ADDRESS_REQUIRED);

            if (trainee.getDateOfBirth() == null) throw new ServiceException(HttpStatus.BAD_REQUEST, DATE_OF_BIRTH_REQUIRED);

            if (!trainee.getDateOfBirth().isBefore(LocalDate.now()))
                throw new ServiceException(HttpStatus.BAD_REQUEST, DATE_OF_BIRTH_IN_PAST_REQUIRED);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private static void validateTraineeForUpdate(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            if (trainee.getId() == null) throw new ServiceException(HttpStatus.BAD_REQUEST, ID_REQUIRED);

            if (trainee.getUser().getId() == null) throw new ServiceException(HttpStatus.BAD_REQUEST, USER_ID_REQUIRED);

            validateTraineeForCreate(trainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
