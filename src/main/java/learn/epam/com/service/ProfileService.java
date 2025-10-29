package learn.epam.com.service;

import learn.epam.com.dto.*;
import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.User;

import java.time.LocalDate;

public interface ProfileService {
    void createTraineeProfile(User user, Trainee trainee) throws ServiceException;

    void createTrainerProfile(User user, Trainer trainer) throws ServiceException;

    void createTrainerProfile(TrainerDto trainerDto) throws ServiceException;

    void changePassword(String username, String oldPassword, String newPassword) throws ServiceException;

    TraineeProfileDto getTraineeProfile(Trainee trainee) throws ServiceException;

    TrainerProfileDto getTrainerProfile(Trainer trainer) throws ServiceException;

    TraineeProfileDto updateTraineeProfile(String username, TraineeProfileDto updated) throws ServiceException;

    TrainerProfileDto updateTrainerProfile(String username, TrainerProfileDto updated) throws ServiceException;

    void deleteTraineeProfile(String username) throws ServiceException;

    void deleteTrainerProfile(String username) throws ServiceException;

    void createTraineeProfile(String firstName, String lastName, LocalDate date, String address) throws ServiceException;

    UserDetailsDto registerTrainee(TraineeDto traineeDto) throws ServiceException;
}
