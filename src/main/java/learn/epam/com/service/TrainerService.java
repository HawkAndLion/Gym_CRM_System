package learn.epam.com.service;

import learn.epam.com.entity.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    void save(Trainer trainer) throws ServiceException;

    Optional<Trainer> findById(Long id) throws ServiceException;

    void update(Trainer trainer) throws ServiceException;

    void delete(Trainer trainer) throws ServiceException;

    List<Trainer> findAllTrainers() throws ServiceException;

    boolean checkCredentials(Long trainerId, String username, String password) throws ServiceException;

    Optional<Trainer> findTrainerByCredentials(String username, String password) throws ServiceException;
}
