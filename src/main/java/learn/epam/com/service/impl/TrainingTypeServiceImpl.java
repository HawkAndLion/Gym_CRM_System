package learn.epam.com.service.impl;

import learn.epam.com.dao.TrainingTypeDao;
import learn.epam.com.entity.TrainingType;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingTypeServiceImpl.class);
    private static final String SUCCESS_SAVE_TRAINING_TYPE = "TrainingType was created successfully";
    private static final String SUCCESS_UPDATE_TRAINING_TYPE = "TrainingType was updated successfully";
    private static final String SUCCESS_DELETE_TRAINING_TYPE = "TrainingType was deleted successfully";
    private static final String NULL_EXCEPTION = "Argument is null ";

    private final TrainingTypeDao trainingTypeDao;

    @Autowired
    public TrainingTypeServiceImpl(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    public void save(TrainingType trainingType) throws ServiceException {
        if (trainingType != null) {
            trainingTypeDao.save(trainingType);
            LOG.info(SUCCESS_SAVE_TRAINING_TYPE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Optional<TrainingType> findById(Long id) throws ServiceException {
        return trainingTypeDao.getById(id);
    }

    @Override
    public void update(TrainingType trainingType) throws ServiceException {
        if (trainingType != null) {
            trainingTypeDao.update(trainingType);
            LOG.info(SUCCESS_UPDATE_TRAINING_TYPE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(TrainingType trainingType) throws ServiceException {
        if (trainingType != null) {
            trainingTypeDao.delete(trainingType);
            LOG.info(SUCCESS_DELETE_TRAINING_TYPE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public List<TrainingType> findAllTrainingTypes() throws ServiceException {
        return trainingTypeDao.getAll();
    }
}
