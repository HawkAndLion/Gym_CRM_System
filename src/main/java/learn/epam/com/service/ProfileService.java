package learn.epam.com.service;

import learn.epam.com.api.model.*;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;

import java.time.LocalDate;

public interface ProfileService {
    void createTraineeProfile(User user, Trainee trainee) throws ServiceException;

    void createTrainerProfile(User user, Trainer trainer) throws ServiceException;

    void createTrainerProfile(TrainerCreateRequest trainerDto) throws ServiceException;

    void changePassword(String oldPassword, String newPassword) throws ServiceException;

    TraineeProfileResponse getTraineeProfile(Trainee trainee) throws ServiceException;

    TrainerProfileResponse getTrainerProfile(Trainer trainer) throws ServiceException;

    TraineeProfileResponse updateTraineeProfile(String username, TraineeProfileResponse updated) throws ServiceException;

    TrainerProfileResponse updateTrainerProfile(String username, TrainerProfileResponse updated) throws ServiceException;

    void deleteTraineeProfile(String username) throws ServiceException;

    void deleteTrainerProfile(String username) throws ServiceException;

    void createTraineeProfile(String firstName, String lastName, LocalDate date, String address, String password) throws ServiceException;

    UserDetailsResponse registerTrainee(TraineeCreateRequest request) throws ServiceException;
}
