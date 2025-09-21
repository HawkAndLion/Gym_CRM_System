package learn.epam.com.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T> {
    Optional<T> getById(long id) throws DaoException;

    List<T> getAll() throws DaoException;

    void save(T t) throws DaoException;

    void update(T t) throws DaoException;

    void delete(T t) throws DaoException;
}
