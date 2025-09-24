package learn.epam.com.service.impl;

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
            trainingDao.save(training);

            LOG.info(SUCCESS_SAVE_TRAINING);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Optional<Training> findById(Long id) throws ServiceException {
        return trainingDao.getById(id);
    }

    @Override
    public void update(Training training) throws ServiceException {
        if (training != null) {
            trainingDao.update(training);

            LOG.info(SUCCESS_UPDATE_TRAINING);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }

    }

    @Override
    public void delete(Training training) throws ServiceException {
        if (training != null) {
            trainingDao.delete(training);

            LOG.info(SUCCESS_DELETE_TRAINING);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public List<Training> findAllTrainings() throws ServiceException {
        return trainingDao.getAll();
    }
}
