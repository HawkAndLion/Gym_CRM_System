package learn.epam.com.main;

import learn.epam.com.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GymFacade {
    private final UserService userService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final ProfileService profileService;

    @Autowired
    public GymFacade(UserService userService,
                     TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     TrainingTypeService trainingTypeService,
                     ProfileService profileService) {
        this.userService = userService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.profileService = profileService;
    }

    public TraineeService trainee() {
        return traineeService;
    }

    public TrainerService trainer() {
        return trainerService;
    }

    public UserService user() {
        return userService;
    }

    public TrainingService training() {
        return trainingService;
    }

    public TrainingTypeService trainingType() {
        return trainingTypeService;
    }

    public ProfileService profile() {
        return profileService;
    }
}
