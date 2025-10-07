package learn.epam.com.service;

import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;

public interface ProfileService {
    Trainee createTraineeProfile(User user, Trainee trainee) throws ServiceException;

    Trainer createTrainerProfile(User user, Trainer trainer) throws ServiceException;
}
