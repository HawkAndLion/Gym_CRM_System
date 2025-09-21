package learn.epam.com.service;

import learn.epam.com.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void save(User user) throws ServiceException;

    Optional<User> findById(Long id) throws ServiceException;

    void update(User user) throws ServiceException;

    void delete(User user) throws ServiceException;

    List<User> findAllUsers() throws ServiceException;
}
