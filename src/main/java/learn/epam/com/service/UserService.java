package learn.epam.com.service;

import learn.epam.com.dto.UserDetailsDto;
import learn.epam.com.dto.UserDto;
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

    UserDto getUserDto(UserDetailsDto request) throws ServiceException;

    UserDetailsDto getUserDetailsDto(String username) throws ServiceException;

    UserDetailsDto getUserDetailsDtoByCredentials(String firstName, String lastname) throws ServiceException;
}
