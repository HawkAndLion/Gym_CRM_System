package learn.epam.com.dao;

import learn.epam.com.entity.Trainee;

import java.util.Optional;

public interface TraineeDao extends GenericDao<Trainee> {
    Long getUserId(Trainee trainee);
    Optional<Trainee> findTraineeByUsername(String username) throws DaoException;
}
