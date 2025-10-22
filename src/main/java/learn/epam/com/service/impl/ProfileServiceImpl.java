package learn.epam.com.service.impl;

import learn.epam.com.dto.TraineeDto;
import learn.epam.com.dto.TraineeProfileDto;
import learn.epam.com.dto.TrainerDto;
import learn.epam.com.dto.TrainerProfileDto;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
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
    private static final String USER_NOT_FOUND = "User not found";
    private static final String INVALID_PASSWORD = "Invalid current password";
    private static final String NEW_PASSWORD_REQUIRED = "New password required";

    private final UserService userService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final UserCredentialService userCredentialService;

    @Autowired
    public ProfileServiceImpl(UserService userService,
                              TraineeService traineeService,
                              TrainerService trainerService,
                              UserCredentialService userCredentialService) {
        this.userService = userService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.userCredentialService = userCredentialService;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public Trainee createTraineeProfile(User user, Trainee trainee) throws ServiceException {
        if (user != null && trainee != null) {
            if (user.getFirstName() == null || user.getLastName() == null) {
                throw new ServiceException(MISSING_USER_FIELD);
            }

            userService.save(user);
            trainee.setUserId(user.getId());
            traineeService.save(trainee);

            LOG.info(CREATE_TRAINEE_PROFILE, user.getId(), trainee.getId());

            return trainee;

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public Trainer createTrainerProfile(User user, Trainer trainer) throws ServiceException {
        if (user != null && trainer != null) {
            if (user.getFirstName() == null || user.getLastName() == null) {
                throw new ServiceException(MISSING_USER_FIELD);
            }

            if (trainer.getSpecialization() == null) {
                throw new ServiceException(MISSING_TRAINER_FIELD);
            }

            userCredentialService.ensureUsernameExists(user);
            userCredentialService.ensurePassword(user);

            userService.save(user);
            trainer.setUserId(user.getId());
            trainerService.save(trainer);

            LOG.info(CREATE_TRAINER_PROFILE, user.getId(), trainer.getId());

            return trainer;

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public TraineeProfileDto getTraineeProfile(Trainee trainee) throws ServiceException {
        if (trainee != null) {
            User user = userService.findById(trainee.getUserId())
                    .orElseThrow(() -> new ServiceException("User not found for trainee"));

            List<TrainerDto> trainerDtos = new ArrayList<>();
            Set<Long> trainerIds = traineeService.getTrainerIdsForTrainee(trainee.getId());
            Set<Trainer> trainers = new HashSet<>(Set.of());
            List<Trainer> trainerList = trainerService.findAllTrainers();

            for (Trainer trainer : trainerList) {
                for (Long id : trainerIds) {
                    if (trainer.getId().equals(id)) {
                        trainers.add(trainer);
                    }
                }
            }

            if (!trainers.isEmpty()) {
                for (Trainer trainer : trainers) {
                    User trainerUser = userService.findById(trainer.getUserId()).orElse(null);
                    trainerDtos.add(new TrainerDto(
                            trainerUser != null ? trainerUser.getFirstName() : null,
                            trainerUser != null ? trainerUser.getLastName() : null,
                            trainer.getSpecialization()
                    ));
                }
            }

            TraineeProfileDto dto = new TraineeProfileDto();
            dto.setUsername(user.getUsername());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setDateOfBirth(trainee.getDateOfBirth());
            dto.setAddress(trainee.getAddress());
            dto.setActive(trainee.isActive());
            dto.setTrainers(trainerDtos);

            return dto;

        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public TrainerProfileDto getTrainerProfile(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            User user = userService.findById(trainer.getUserId())
                    .orElseThrow(() -> new ServiceException("User not found for trainer"));

            List<TraineeDto> traineeDtos = new ArrayList<>();
            Set<Long> trainerIds = trainerService.getTraineeIdsForTrainer(trainer.getId());
            Set<Trainee> trainees = new HashSet<>(Set.of());
            List<Trainee> traineeList = traineeService.findAllTrainee();

            for (Trainee trainee : traineeList) {
                for (Long id : trainerIds) {
                    if (trainee.getId().equals(id)) {
                        trainees.add(trainee);
                    }
                }
            }

            if (!trainees.isEmpty()) {
                for (Trainee trainee : trainees) {
                    User traineeUser = userService.findById(trainee.getUserId()).orElse(null);
                    traineeDtos.add(new TraineeDto(
                            traineeUser != null ? traineeUser.getFirstName() : null,
                            traineeUser != null ? traineeUser.getLastName() : null,
                            trainee.getDateOfBirth(),
                            trainee.getAddress()
                    ));
                }
            }

            TrainerProfileDto dto = new TrainerProfileDto();
            dto.setUsername(user.getUsername());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setSpecialization(trainer.getSpecialization());
            dto.setActive(trainer.isActive());
            dto.setTrainees(traineeDtos);

            return dto;
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public TraineeProfileDto updateTraineeProfile(String username, TraineeProfileDto updatedDto) throws ServiceException {
        if (username != null && updatedDto != null) {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new ServiceException("User not found for trainee"));

            Trainee existingTrainee = traineeService.findTraineeByUsername(username)
                    .orElseThrow(() -> new ServiceException("Trainee not found with username: " + username));

            user.setFirstName(updatedDto.getFirstName());
            user.setLastName(updatedDto.getLastName());
            user.setActive(updatedDto.isActive());
            userService.update(user);

            existingTrainee.setAddress(updatedDto.getAddress());
            existingTrainee.setDateOfBirth(updatedDto.getDateOfBirth());
            existingTrainee.setActive(updatedDto.isActive());

            Set<Trainer> trainers = updatedDto.getTrainers().stream()
                    .map(dto -> {
                        try {
                            return trainerService
                                    .findTrainerByUsername(makeUsername(dto.getFirstName(), dto.getLastName()))
                                    .orElseThrow(() -> new ServiceException("Trainer was not found"));
                        } catch (ServiceException e) {
                            LOG.warn("Trainer was not found");
                        }

                        return null;
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
    public TrainerProfileDto updateTrainerProfile(String username, TrainerProfileDto updatedDto) throws ServiceException {
        if (username != null && updatedDto != null) {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new ServiceException("User not found for trainer"));

            Trainer trainer = trainerService.findTrainerByUsername(username)
                    .orElseThrow(() -> new ServiceException("Trainer not found for username: " + username));

            user.setFirstName(updatedDto.getFirstName());
            user.setLastName(updatedDto.getLastName());
            user.setActive(updatedDto.isActive());
            userService.update(user);

            trainer.setSpecialization(updatedDto.getSpecialization());
            trainer.setActive(updatedDto.isActive());
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

            LOG.info("Trainee Profile was deleted successfully.");
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteTrainerProfile(String username) throws ServiceException {
        if (username != null) {
            trainerService.deleteTrainerByUsername(username);

            LOG.info("Trainer Profile was deleted successfully.");
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void changePassword(String username, String oldPassword, String newPassword) throws ServiceException {
        LOG.info("Request received: username={}, oldPassword={}, newPassword={}",
                username, oldPassword, newPassword);

        if (username != null && oldPassword != null && newPassword != null) {
            User user = userService.findAllUsers().stream()
                    .filter(user1 -> username.equalsIgnoreCase(user1.getUsername()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

            if (!user.getPassword().equals(oldPassword)) {
                throw new ServiceException(INVALID_PASSWORD);
            }

            if (newPassword.isBlank()) {
                throw new ServiceException(NEW_PASSWORD_REQUIRED);
            }

            user.setPassword(newPassword);
            userService.update(user);
        } else if (newPassword == null || newPassword.isBlank()) {
            throw new ServiceException(NEW_PASSWORD_REQUIRED);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    private String makeUsername(String firstName, String lastName) {
        StringBuilder builder = new StringBuilder();

        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            builder.append(firstName);
            builder.append(".");
            builder.append(lastName);
        }

        return builder.toString();
    }
}
