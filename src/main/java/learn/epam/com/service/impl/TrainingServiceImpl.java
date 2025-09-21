package learn.epam.com.service.impl;

import learn.epam.com.dao.DaoException;
import learn.epam.com.dao.TrainingDao;
import learn.epam.com.entity.Training;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingServiceImpl.class);
    private static final String FAIL_SAVE_TRAINING = "Failed to save training";
    private static final String FAIL_UPDATE_TRAINING = "Failed to update training";
    private static final String FAIL_DELETE_TRAINING = "Failed to delete training";
    private static final String FAIL_GET_ALL_TRAINING = "Failed to get all trainings";
    private static final String FAIL_GET_BY_ID_TRAINING = "Failed to get training by id";
    private static final String SUCCESS_SAVE_TRAINING = "Training was created successfully";
    private static final String SUCCESS_UPDATE_TRAINING = "Training was updated successfully";
    private static final String SUCCESS_DELETE_TRAINING = "Training was deleted successfully";
    private static final String NULL_EXCEPTION = "Argument is null ";

    private final TrainingDao trainingDao;

    @Autowired
    public TrainingServiceImpl(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    public void save(Training training) throws ServiceException {
        if (training != null) {
            try {
                trainingDao.save(training);

                LOG.info(SUCCESS_SAVE_TRAINING);
            } catch (DaoException exception) {
                throw new ServiceException(FAIL_SAVE_TRAINING, exception);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Optional<Training> findById(Long id) throws ServiceException {
        try {
            return trainingDao.getById(id);

        } catch (DaoException exception) {
            throw new ServiceException(FAIL_GET_BY_ID_TRAINING, exception);
        }
    }

    @Override
    public void update(Training training) throws ServiceException {
        if (training != null) {
            try {
                trainingDao.update(training);

                LOG.info(SUCCESS_UPDATE_TRAINING);
            } catch (DaoException exception) {
                throw new ServiceException(FAIL_UPDATE_TRAINING, exception);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }

    }

    @Override
    public void delete(Training training) throws ServiceException {
        if (training != null) {
            try {
                trainingDao.delete(training);

                LOG.info(SUCCESS_DELETE_TRAINING);
            } catch (DaoException exception) {
                throw new ServiceException(FAIL_DELETE_TRAINING, exception);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public List<Training> findAllTrainings() throws ServiceException {
        try {
            return trainingDao.getAll();

        } catch (DaoException exception) {
            throw new ServiceException(FAIL_GET_ALL_TRAINING, exception);
        }
    }
}
