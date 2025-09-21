package learn.epam.com.main;

import learn.epam.com.service.TraineeService;
import learn.epam.com.service.TrainerService;
import learn.epam.com.service.TrainingService;
import learn.epam.com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GymFacade {
    private final UserService userService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public GymFacade(UserService userService, TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.userService = userService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
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
}
