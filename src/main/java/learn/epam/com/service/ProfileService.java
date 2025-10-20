package learn.epam.com.service;

import learn.epam.com.dto.TraineeProfileDto;
import learn.epam.com.dto.TrainerProfileDto;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;

public interface ProfileService {
    Trainee createTraineeProfile(User user, Trainee trainee) throws ServiceException;

    Trainer createTrainerProfile(User user, Trainer trainer) throws ServiceException;

    void changePassword(String username, String oldPassword, String newPassword) throws ServiceException;

    TraineeProfileDto getTraineeProfile(Trainee trainee) throws ServiceException;

    TrainerProfileDto getTrainerProfile(Trainer trainer) throws ServiceException;

    TraineeProfileDto updateTraineeProfile(String username, TraineeProfileDto updated) throws ServiceException;

    TrainerProfileDto updateTrainerProfile(String username, TrainerProfileDto updated) throws ServiceException;

    void deleteTraineeProfile(String username) throws ServiceException;

    void deleteTrainerProfile(String username) throws ServiceException;
}
