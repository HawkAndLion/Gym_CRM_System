package learn.epam.com.service.impl;

import learn.epam.com.dto.TraineeProfileDto;
import learn.epam.com.dto.TrainerDto;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {
    private static final Logger LOG = LoggerFactory.getLogger(ProfileServiceImpl.class);
    private static final String MISSING_USER_FIELD = "User missing required fields: firstName/lastName";
    private static final String MISSING_TRAINEE_FIELD = "Trainee missing required fields: address/dateOfBirth";
    private static final String CREATE_TRAINEE_PROFILE = "Created trainee profile: userId={}, traineeId={}";
    private static final String MISSING_TRAINER_FIELD = "Trainer missing required fields: specialization";
    private static final String CREATE_TRAINER_PROFILE = "Created trainer profile: userId={}, trainerId={}";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String INVALID_PASSWORD = "Invalid current password";
    private static final String NEW_PASSWORD_REQUIRED = "New password required";
    private static final String AUTHENTICATION_FAIL = "Authentication failed";

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
            for (Trainer trainer : trainee.getTrainers()) {
                User trainerUser = userService.findById(trainer.getUserId()).orElse(null);
                trainerDtos.add(new TrainerDto(
                        trainerUser != null ? trainerUser.getUsername() : null,
                        trainerUser != null ? trainerUser.getFirstName() : null,
                        trainerUser != null ? trainerUser.getLastName() : null,
                        trainer.getSpecialization()
                ));
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
    public Trainer getTrainerProfile(Trainer trainer) throws ServiceException {
        if (trainer != null) {
            String username = userService.findById(trainer.getId()).get().getUsername();

            if(username != null && !username.isBlank()){
                return trainerService.findTrainerByUsername(username).orElseThrow();
            } else {
                throw new ServiceException("Username for the trainer was not found");
            }
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
            userService.save(user);

            existingTrainee.setAddress(updatedDto.getAddress());
            existingTrainee.setDateOfBirth(updatedDto.getDateOfBirth());
            existingTrainee.setActive(updatedDto.isActive());
            traineeService.save(existingTrainee);

            return getTraineeProfile(existingTrainee);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void updateTrainerProfile(String username, Trainer updated) throws ServiceException {
        if (username != null && updated != null) {
            Trainer trainer = trainerService.findTrainerByUsername(username).orElseThrow(() -> new ServiceException(AUTHENTICATION_FAIL));

            updated.setId(trainer.getId());
            updated.setUserId(trainer.getUserId());
            trainerService.update(updated);
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
}
