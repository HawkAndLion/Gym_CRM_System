package learn.epam.com.service.impl;

import learn.epam.com.dao.TrainingTypeDao;
import learn.epam.com.entity.TrainingType;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingTypeServiceImpl.class);
    private static final String SUCCESS_SAVE_TRAINING_TYPE = "TrainingType was created successfully";
    private static final String SUCCESS_UPDATE_TRAINING_TYPE = "TrainingType was updated successfully";
    private static final String SUCCESS_DELETE_TRAINING_TYPE = "TrainingType was deleted successfully";
    private static final String INVALID_TRAINING_TYPE = "Invalid training type";
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String TRAINING_TYPE_NOT_FOUND = "Training type not found: ";
    private static final String ID = "id";
    private static final String NAME = "name";

    private final TrainingTypeDao trainingTypeDao;

    @Autowired
    public TrainingTypeServiceImpl(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void save(TrainingType trainingType) throws ServiceException {
        if (trainingType != null) {
            if (trainingType.getName() != null) {
                trainingTypeDao.save(trainingType);

                LOG.info(SUCCESS_SAVE_TRAINING_TYPE);
            } else {
                throw new ServiceException(INVALID_TRAINING_TYPE);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Optional<TrainingType> findById(Long id) throws ServiceException {
        if (id != null) {
            return trainingTypeDao.getById(id);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void update(TrainingType trainingType) throws ServiceException {
        if (trainingType != null) {
            if (trainingType.getName() != null) {
                trainingTypeDao.update(trainingType);

                LOG.info(SUCCESS_UPDATE_TRAINING_TYPE);
            } else {
                throw new ServiceException(INVALID_TRAINING_TYPE);
            }
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void delete(TrainingType trainingType) throws ServiceException {
        if (trainingType != null) {
            trainingTypeDao.delete(trainingType);

            LOG.info(SUCCESS_DELETE_TRAINING_TYPE);
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public List<TrainingType> findAllTrainingTypes() {
        return trainingTypeDao.getAll();
    }

    @Override
    @Transactional
    public Long getTrainingTypeId(String trainingType) throws ServiceException {
        Long trainingTypeId = null;

        if (trainingType != null && !trainingType.isBlank()) {
            trainingTypeId = findAllTrainingTypes().stream()
                    .filter(tt -> tt.getName().equalsIgnoreCase(trainingType))
                    .map(tt -> tt.getId())
                    .findFirst()
                    .orElseThrow(() -> new ServiceException(TRAINING_TYPE_NOT_FOUND + trainingType));
        }

        return trainingTypeId;
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getTrainingTypes() {
        return findAllTrainingTypes()
                .stream()
                .map(tt -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put(ID, tt.getId());
                    map.put(NAME, tt.getName());
                    return map;
                })
                .toList();
    }
}
