package learn.epam.com.service.impl;

import learn.epam.com.entity.TrainingType;
import learn.epam.com.repository.TrainingTypeRepository;
import learn.epam.com.service.ServiceException;
import learn.epam.com.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingTypeServiceImpl.class);
    private static final String NULL_EXCEPTION = "Argument is null ";
    private static final String TRAINING_TYPE_NOT_FOUND = "Training type not found: ";
    private static final String FIND_BY_ID = "Finding Training type by its Id";
    private static final String CHECK_ID_ON_NULL = "Check training type id. It might be null ";
    private static final String GET_TRAINING_TYPES = "Getting the list of training types";
    private static final String GET_TRAINING_TYPE_BY_NAME = "Getting Training type Id by trainingType name";
    private static final String ID = "id";
    private static final String NAME = "name";

    private final TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public TrainingTypeServiceImpl(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    @Transactional
    public Optional<TrainingType> findById(Long id) throws ServiceException {
        LOG.info(FIND_BY_ID);

        if (id != null) {
            return trainingTypeRepository.findById(id);
        } else {
            LOG.info(CHECK_ID_ON_NULL);

            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public List<TrainingType> findAllTrainingTypes() {
        LOG.info(GET_TRAINING_TYPES);

        return trainingTypeRepository.findAll();
    }

    @Override
    @Transactional
    public Long getTrainingTypeId(String trainingType) throws ServiceException {
        LOG.info(GET_TRAINING_TYPE_BY_NAME);

        Long trainingTypeId = null;

        if (trainingType != null && !trainingType.isBlank()) {
            trainingTypeId = findAllTrainingTypes().stream()
                    .filter(tt -> tt.getName().equalsIgnoreCase(trainingType))
                    .map(tt -> tt.getId())
                    .findFirst()
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, TRAINING_TYPE_NOT_FOUND + trainingType));
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
