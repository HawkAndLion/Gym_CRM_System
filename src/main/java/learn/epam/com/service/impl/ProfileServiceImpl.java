package learn.epam.com.service.impl;

import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;
import learn.epam.com.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {
    private static final Logger LOG = LoggerFactory.getLogger(ProfileServiceImpl.class);
    private static final String MISSING_USER_FIELD = "User missing required fields: firstName/lastName";
    private static final String MISSING_TRAINEE_FIELD = "Trainee missing required fields: address/dateOfBirth";
    private static final String CREATE_TRAINEE_PROFILE = "Created trainee profile: userId={}, traineeId={}";
    private static final String MISSING_TRAINER_FIELD = "Trainer missing required fields: specialization";
    private static final String CREATE_TRAINER_PROFILE = "Created trainer profile: userId={}, trainerId={}";
    private static final String NULL_EXCEPTION = "Argument is null ";

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

            if (trainee.getAddress() == null || trainee.getDateOfBirth() == null) {
                throw new ServiceException(MISSING_TRAINEE_FIELD);
            }

            userCredentialService.ensureUsernameExists(user);
            userCredentialService.ensurePassword(user);

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
}
