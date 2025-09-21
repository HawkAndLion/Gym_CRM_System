package learn.epam.com.service;

import learn.epam.com.entity.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeService {
    void save(Trainee trainee) throws ServiceException;

    Optional<Trainee> findById(Long id) throws ServiceException;

    void update(Trainee trainee) throws ServiceException;

    void delete(Trainee trainee) throws ServiceException;

    List<Trainee> findAllTrainee() throws ServiceException;

    boolean checkCredentials(Long traineeId, String username, String password) throws ServiceException;

    Optional<Trainee> findTraineeByCredentials(String username, String password) throws ServiceException;
}
