package learn.epam.com.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import learn.epam.com.dao.TrainerDao;
import learn.epam.com.entity.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger LOG = LoggerFactory.getLogger(TrainerDaoImpl.class);
    private static final String SUCCESS_SAVE = "Saved trainer id={}";
    private static final String UPDATE_TRAINER = "Updated trainer id={}";
    private static final String DELETE_TRAINER = "Deleted trainer id={}";
    private static final String FROM_TRAINER = "from Trainer";
    private static final String NULL_EXCEPTION = "Argument is null ";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Trainer> getById(long id) {
        return Optional.ofNullable(entityManager.find(Trainer.class, id));
    }

    @Override
    public List<Trainer> getAll() {
        return entityManager.createQuery(FROM_TRAINER, Trainer.class).getResultList();
    }

    @Override
    public void save(Trainer trainer) {
        if (trainer != null) {
            entityManager.persist(trainer);

            LOG.info(SUCCESS_SAVE, trainer.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void update(Trainer trainer) {
        if (trainer != null) {
            entityManager.merge(trainer);

            LOG.info(UPDATE_TRAINER, trainer.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public void delete(Trainer trainer) {
        if (trainer != null) {
            entityManager.remove(entityManager.contains(trainer) ? trainer : entityManager.merge(trainer));

            LOG.info(DELETE_TRAINER, trainer.getId());
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }

    @Override
    public Long getUserId(Trainer trainer) {
        if (trainer != null) {

            return trainer.getUserId();
        } else {
            throw new IllegalArgumentException(NULL_EXCEPTION);
        }
    }
}
