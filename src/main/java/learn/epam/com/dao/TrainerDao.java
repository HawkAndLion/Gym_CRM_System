package learn.epam.com.dao;

import learn.epam.com.entity.Trainer;

public interface TrainerDao extends GenericDao<Trainer> {
    Long getUserId(Trainer trainer) throws DaoException;
}
