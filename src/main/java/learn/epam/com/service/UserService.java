package learn.epam.com.service;

import learn.epam.com.api.model.UserDetailsResponse;
import learn.epam.com.api.model.UserResponse;
import learn.epam.com.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void save(User user) throws ServiceException;

    Optional<User> findById(Long id);

    void update(User user) throws ServiceException;

    void delete(User user) throws ServiceException;

    List<User> findAllUsers();

    Optional<User> findByUsername(String username);

    UserResponse getUserDto(UserDetailsResponse request) throws ServiceException;

    UserDetailsResponse getUserDetailsDto(String username) throws ServiceException;

    UserDetailsResponse getUserDetailsDtoByCredentials(String firstName, String lastname) throws ServiceException;
}
