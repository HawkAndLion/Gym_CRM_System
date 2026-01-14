package learn.epam.com.service.impl;

import learn.epam.com.api.model.TraineeCreateRequest;
import learn.epam.com.api.model.TraineeProfileResponse;
import learn.epam.com.api.model.TrainerCreateRequest;
import learn.epam.com.api.model.TrainerProfileResponse;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {
    private static final Logger LOG = LoggerFactory.getLogger(ProfileServiceImpl.class);
    private static final String MISSING_USER_FIELD = "User missing required fields: firstName/lastName";
    private static final String CREATE_TRAINEE_PROFILE = "Created trainee profile: userId={}, traineeId={}";
    private static final String MISSING_TRAINER_FIELD = "Trainer missing required fields: specialization";
    private static final String CREATE_TRAINER_PROFILE = "Created trainer profile: userId={}, trainerId={}";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String USER_NOT_FOUND = "User not found. Check if firstname and lastname exist.";
    private static final String INVALID_CREDENTIALS = "Invalid user credentials";
    private static final String NEW_PASSWORD_REQUIRED = "New password required";
    private static final String TRAINEE_USER_NOT_FOUND = "User not found for trainee";
    private static final String TRAINER_USER_NOT_FOUND = "User not found for trainer";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found with username: ";
    private static final String TRAINER_NOT_FOUND = "Trainer was not found";
    private static final String TRAINER_USERNAME_NOT_FOUND = "Trainer not found for username: ";
    private static final String TRAINEE_SUCCESS_DELETE = "Trainee Profile was deleted successfully.";
    private static final String TRAINER_SUCCESS_DELETE = "Trainer Profile was deleted successfully.";
    private static final String RECEIVED_ARGUMENTS = "Request received: username={}, oldPassword={}, newPassword={}";

    private final UserService userService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final UserCredentialService userCredentialService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ProfileServiceImpl(UserService userService,
                              TraineeService traineeService,
                              TrainerService trainerService,
                              UserCredentialService userCredentialService,
                              PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.userCredentialService = userCredentialService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void createTraineeProfile(User user, Trainee trainee) throws ServiceException {
        if (user != null && trainee != null) {
            if (user.getFirstName() == null || user.getLastName() == null) {
                throw new ServiceException(HttpStatus.BAD_REQUEST, MISSING_USER_FIELD);
            }

            userService.save(user);
            trainee.setUser(user);
            traineeService.save(trainee);

            LOG.info(CREATE_TRAINEE_PROFILE, user.getId(), trainee.getId());

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void createTraineeProfile(String firstName, String lastName, LocalDate date, String address, String password) throws ServiceException {
        User user = new User(firstName, lastName, null, password, true);
        Trainee trainee = new Trainee(address, date, true);

        createTraineeProfile(user, trainee);
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public User registerTrainee(TraineeCreateRequest request) throws ServiceException {
        String firstName = request.getFirstName();
        String lastName = request.getLastName();
        LocalDate date = request.getDateOfBirth();
        String address = request.getAddress();
        String password = request.getPassword();

        createTraineeProfile(firstName, lastName, date, address, password);

        return userService.findAllUsers().stream()
                .filter(u -> u.getFirstName().equals(firstName) && u.getLastName().equals(lastName))
                .findFirst()
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public TraineeProfileResponse getTraineeProfile(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            User user = userService.findById(trainee.getUser().getId())
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_USER_NOT_FOUND));

            List<TrainerProfileResponse> trainerResponses = new ArrayList<>();
            Set<Long> trainerIds = traineeService.getTrainerIdsForTrainee(trainee.getId());
            List<Trainer> allTrainers = trainerService.findAllTrainers();

            for (Trainer trainer : allTrainers) {
                if (trainerIds.contains(trainer.getId())) {
                    User trainerUser = userService.findById(trainer.getUser().getId()).orElse(null);
                    if (trainerUser != null) {
                        TrainerProfileResponse trainerResponse = new TrainerProfileResponse()
                                .username(trainerUser.getUsername())
                                .firstName(trainerUser.getFirstName())
                                .lastName(trainerUser.getLastName())
                                .specialization(trainer.getSpecialization())
                                .active(trainer.isActive());
                        trainerResponses.add(trainerResponse);
                    }
                }
            }

            return new TraineeProfileResponse()
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .dateOfBirth(trainee.getDateOfBirth())
                    .address(trainee.getAddress())
                    .active(trainee.isActive())
                    .trainers(trainerResponses);

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void createTrainerProfile(User user, Trainer trainer) throws ServiceException {
        if (user != null && trainer != null) {
            if (user.getFirstName() == null || user.getLastName() == null) {
                throw new ServiceException(HttpStatus.BAD_REQUEST, MISSING_USER_FIELD);
            }

            if (trainer.getSpecialization() == null) {
                throw new ServiceException(HttpStatus.BAD_REQUEST, MISSING_TRAINER_FIELD);
            }

            userCredentialService.ensureUsernameExists(user);
            userCredentialService.ensurePassword(user);

            userService.save(user);
            trainer.setUser(user);

            trainerService.save(trainer);

            LOG.info(CREATE_TRAINER_PROFILE, user.getId(), trainer.getId());

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void createTrainerProfile(TrainerCreateRequest trainerDto) throws ServiceException {
        if (trainerDto != null) {
            String firstName = trainerDto.getFirstName();
            String lastName = trainerDto.getLastName();
            String specialization = trainerDto.getSpecialization();
            String password = trainerDto.getPassword();

            User user = new User(firstName, lastName, null, password, true);
            Trainer trainer = new Trainer(specialization, true);

            createTrainerProfile(user, trainer);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public TrainerProfileResponse getTrainerProfile(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            User user = userService.findById(trainer.getUser().getId())
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINER_USER_NOT_FOUND));

            List<TraineeProfileResponse> traineeResponses = new ArrayList<>();
            Set<Long> traineeIds = trainerService.getTraineeIdsForTrainer(trainer.getId());
            List<Trainee> allTrainees = traineeService.findAllTrainee();

            for (Trainee trainee : allTrainees) {
                if (traineeIds.contains(trainee.getId())) {
                    User traineeUser = userService.findById(trainee.getUser().getId())
                            .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_USER_NOT_FOUND));

                    TraineeProfileResponse traineeResponse = new TraineeProfileResponse()
                            .username(traineeUser.getUsername())
                            .firstName(traineeUser.getFirstName())
                            .lastName(traineeUser.getLastName())
                            .dateOfBirth(trainee.getDateOfBirth())
                            .address(trainee.getAddress())
                            .active(trainee.isActive());

                    traineeResponses.add(traineeResponse);
                }
            }

            return new TrainerProfileResponse()
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .specialization(trainer.getSpecialization())
                    .active(trainer.isActive())
                    .trainees(traineeResponses);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public TraineeProfileResponse updateTraineeProfile(String username, TraineeProfileResponse profileResponse) throws ServiceException {
        if (username != null && profileResponse != null) {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_USER_NOT_FOUND));

            Trainee existingTrainee = traineeService.findTraineeByUsername(username)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINEE_NOT_FOUND + username));

            user.setFirstName(profileResponse.getFirstName());
            user.setLastName(profileResponse.getLastName());
            user.setActive(profileResponse.getActive());
            userService.update(user);

            existingTrainee.setAddress(profileResponse.getAddress());
            existingTrainee.setDateOfBirth(profileResponse.getDateOfBirth());
            existingTrainee.setActive(profileResponse.getActive());

            Set<Trainer> trainers = profileResponse.getTrainers().stream()
                    .map(t -> {
                        try {
                            return trainerService.findTrainerByUsername(t.getUsername())
                                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINER_NOT_FOUND));
                        } catch (ServiceException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toSet());

            existingTrainee.setTrainers(trainers);
            traineeService.update(existingTrainee);

            return getTraineeProfile(existingTrainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public TrainerProfileResponse updateTrainerProfile(String username, TrainerProfileResponse profileResponse) throws ServiceException {
        if (username != null && profileResponse != null) {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINER_USER_NOT_FOUND));

            Trainer trainer = trainerService.findTrainerByUsername(username)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINER_USERNAME_NOT_FOUND + username));

            user.setFirstName(profileResponse.getFirstName());
            user.setLastName(profileResponse.getLastName());
            user.setActive(profileResponse.getActive());
            userService.update(user);

            trainer.setSpecialization(profileResponse.getSpecialization());
            trainer.setActive(profileResponse.getActive());
            trainerService.update(trainer);

            return getTrainerProfile(trainer);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }


    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteTraineeProfile(String username) throws ServiceException {
        if (username != null) {
            traineeService.deleteTraineeByUsername(username);

            LOG.info(TRAINEE_SUCCESS_DELETE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteTrainerProfile(String username) throws ServiceException {
        if (username != null) {
            trainerService.deleteTrainerByUsername(username);

            LOG.info(TRAINER_SUCCESS_DELETE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void changePassword(String oldPassword, String newPassword) throws ServiceException {
        if (oldPassword != null && newPassword != null) {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            LOG.info(RECEIVED_ARGUMENTS, username, oldPassword, newPassword);

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new ServiceException(HttpStatus.BAD_REQUEST, INVALID_CREDENTIALS);
            }

            if (newPassword.isBlank()) {
                throw new ServiceException(HttpStatus.BAD_REQUEST, NEW_PASSWORD_REQUIRED);
            }

            user.setPassword(passwordEncoder.encode(newPassword));

            userService.update(user);
        } else if (newPassword == null || newPassword.isBlank()) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, NEW_PASSWORD_REQUIRED);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }
}
