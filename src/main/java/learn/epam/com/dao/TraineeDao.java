package learn.epam.com.dao;

import learn.epam.com.entity.Trainee;

public interface TraineeDao extends GenericDao<Trainee> {
    Long getUserId(Trainee trainee);
}
