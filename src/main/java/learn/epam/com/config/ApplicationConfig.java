package learn.epam.com.config;

import learn.epam.com.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Configuration
@ComponentScan(basePackages = "learn.epam.com")
@PropertySource("classpath:application.properties")
public class ApplicationConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final String CREATE_TRAINEE_STORAGE = "Creating traineeStorage bean";
    private static final String CREATE_TRAINER_STORAGE = "Creating trainerStorage bean";
    private static final String CREATE_TRAINING_STORAGE = "Creating trainingStorage bean";
    private static final String CREATE_USER_STORAGE = "Creating trainingStorage bean";
    private static final String CREATE_TRAINING_TYPE_STORAGE = "Creating trainingTypeStorage bean";
    private static final String CREATE_TRAINEE_TRAINER_STORAGE = "Creating traineeTrainerStorage bean";

    @Value("${trainee.data.path:classpath:trainees.txt}")
    private Resource traineeData;
    @Value("${trainer.data.path:classpath:trainers.txt}")
    private Resource trainerData;
    @Value("${training.data.path:classpath:trainings.txt}")
    private Resource trainingData;
    @Value("${user.data.path:classpath:users.txt}")
    private Resource userData;
    @Value("${trainingtype.data.path:classpath:trainingtype.txt}")
    private Resource trainingTypeData;
    @Value("${traineetrainer.data.path:classpath:traineetrainer.txt}")
    private Resource traineeTrainerData;

    @Bean(name = "traineeStorage")
    public Map<Long, Trainee> traineeStorage() {
        LOG.info(CREATE_TRAINEE_STORAGE);

        return new HashMap<>();
    }


    @Bean(name = "trainerStorage")
    public Map<Long, Trainer> trainerStorage() {
        LOG.info(CREATE_TRAINER_STORAGE);

        return new HashMap<>();
    }


    @Bean(name = "trainingStorage")
    public Map<Long, Training> trainingStorage() {
        LOG.info(CREATE_TRAINING_STORAGE);

        return new HashMap<>();
    }

    @Bean(name = "userStorage")
    public Map<Long, User> userStorage() {
        LOG.info(CREATE_USER_STORAGE);

        return new HashMap<>();
    }

    @Bean(name = "trainingTypeStorage")
    public Map<Long, TrainingType> trainingTypeStorage() {
        LOG.info(CREATE_TRAINING_TYPE_STORAGE);

        return new HashMap<>();
    }

    @Bean(name = "traineeTrainerStorage")
    public Map<Long, Set<Long>> traineeTrainerStorage() {
        LOG.info(CREATE_TRAINEE_TRAINER_STORAGE);

        return new HashMap<>();
    }

    @Bean
    public StorageInitializer storageInitializer() {
        return new StorageInitializer(traineeData, trainerData, trainingData, userData, trainingTypeData);
    }
}
