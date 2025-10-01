package learn.epam.com.dao.impl;

import learn.epam.com.dao.TraineeTrainerDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
public class TraineeTrainerDaoImpl implements TraineeTrainerDao {
    private static final Logger LOG = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private final Map<Long, Set<Long>> storage;

    public TraineeTrainerDaoImpl(@Qualifier("traineeTrainerStorage") Map<Long, Set<Long>> storage) {
        this.storage = storage;
    }

    @Override
    public Set<Long> getTrainerIdsForTrainee(Long traineeId) {
        return storage.getOrDefault(traineeId, Collections.EMPTY_SET);
    }

    @Override
    public void setTrainerIdsForTrainee(Long traineeId, Set<Long> trainerIds) {
        storage.put(traineeId, new HashSet<>(trainerIds));
    }

    @Override
    public void assignTrainer(Long traineeId, Long trainerId) {
        storage.computeIfAbsent(trainerId, k -> new HashSet<>()).add(trainerId);
    }

    @Override
    public void unassignTrainer(Long traineeId, Long trainerId) {
        storage.getOrDefault(traineeId, new HashSet<>()).remove(trainerId);
    }
//    private static final String
}
