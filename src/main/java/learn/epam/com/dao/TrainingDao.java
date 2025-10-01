package learn.epam.com.dao;

import learn.epam.com.entity.Training;

import java.util.List;

public interface TrainingDao extends GenericDao<Training> {
    List<Training> findTrainingsByTraineeId(Long traineeId);
    List<Training> findTrainingsByTrainerId(Long traineeId);
}
