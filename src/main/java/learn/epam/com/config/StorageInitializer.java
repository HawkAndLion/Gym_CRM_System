package learn.epam.com.config;

import jakarta.annotation.PostConstruct;
import learn.epam.com.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Map;

@Component
public class StorageInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(StorageInitializer.class);
    private static final String SPLITTER = "\\|";
    private static final String HASH_SIGN = "#";
    private static final String LOAD_TRAINEE_SUCCESS = "Loaded {} trainees";
    private static final String FAIL_LOAD_TRAINEE_MESSAGE = "Failed to load trainees: {}";
    private static final String NO_TRAINEE_DATA_FOUND = "No trainee data resource found";
    private static final String LOAD_TRAINER_SUCCESS = "Loaded {} trainers";
    private static final String FAIL_LOAD_TRAINER_MESSAGE = "Failed to load trainers: {}";
    private static final String NO_TRAINER_DATA_FOUND = "No trainer data resource found";
    private static final String LOAD_TRAINING_SUCCESS = "Loaded {} trainings";
    private static final String FAIL_LOAD_TRAINING_MESSAGE = "Failed to load trainings: {}";
    private static final String NO_TRAINING_DATA_FOUND = "No training data resource found";
    private static final String LOAD_USER_SUCCESS = "Loaded {} users";
    private static final String FAIL_LOAD_USER_MESSAGE = "Failed to load users: {}";
    private static final String NO_USER_DATA_FOUND = "No user data resource found";
    private static final String LOAD_TRAINING_TYPE_SUCCESS = "Loaded {} trainingType list";
    private static final String FAIL_LOAD_TRAINING_TYPE_MESSAGE = "Failed to load training type list: {}";
    private static final String NO_TRAINING_TYPE_DATA_FOUND = "No training type data resource found";

    private final Resource traineeData;
    private final Resource trainerData;
    private final Resource trainingData;
    private final Resource userData;
    private final Resource trainingTypeData;

    @Autowired
    @Qualifier("traineeStorage")
    private Map<Long, Trainee> traineeStorage;

    @Autowired
    @Qualifier("trainerStorage")
    private Map<Long, Trainer> trainerStorage;

    @Autowired
    @Qualifier("trainingStorage")
    private Map<Long, Training> trainingStorage;

    @Autowired
    @Qualifier("userStorage")
    private Map<Long, User> userStorage;

    @Autowired
    @Qualifier("trainingTypeStorage")
    private Map<Long, TrainingType> trainingTypeStorage;

    public StorageInitializer(Resource traineeData,
                              Resource trainerData,
                              Resource trainingData,
                              Resource userData,
                              Resource trainingTypeData) {
        this.traineeData = traineeData;
        this.trainerData = trainerData;
        this.trainingData = trainingData;
        this.userData = userData;
        this.trainingTypeData = trainingTypeData;
    }

    @PostConstruct
    public void init() {
        loadTrainees();
        loadTrainers();
        loadTrainings();
        loadUsers();
        loadTrainingTypes();
    }

    private void loadTrainees() {
        if (traineeData != null && traineeData.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(traineeData.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith(HASH_SIGN)) continue;

                    String[] splitLine = line.split(SPLITTER);

                    Trainee trainee = new Trainee();
                    long id = Long.parseLong(splitLine[0].trim());
                    trainee.setId(id);
                    trainee.setUserId(Long.parseLong(splitLine[1].trim()));
                    trainee.setAddress(splitLine[2].trim());
                    LocalDate birthDate = LocalDate.parse(splitLine[3].trim());
                    trainee.setDateOfBirth(birthDate);
                    traineeStorage.put(trainee.getId(), trainee);
                }
                LOG.info(LOAD_TRAINEE_SUCCESS, traineeStorage.size());

            } catch (IOException e) {
                LOG.warn(FAIL_LOAD_TRAINEE_MESSAGE, e.getMessage());
            }
        }

        LOG.info(NO_TRAINEE_DATA_FOUND);
    }

    private void loadTrainers() {
        if (trainerData != null && trainerData.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(trainerData.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith(HASH_SIGN)) continue;

                    String[] splitText = line.split(SPLITTER);

                    Trainer trainer = new Trainer();
                    long id = Long.parseLong(splitText[0].trim());
                    trainer.setId(id);
                    trainer.setUserId(Long.parseLong(splitText[1].trim()));
                    trainer.setSpecialization(splitText[2].trim());

                    trainerStorage.put(trainer.getId(), trainer);
                }
                LOG.info(LOAD_TRAINER_SUCCESS, trainerStorage.size());

            } catch (IOException exception) {
                LOG.warn(FAIL_LOAD_TRAINER_MESSAGE, exception.getMessage());
            }
        }

        LOG.info(NO_TRAINER_DATA_FOUND);
    }

    private void loadTrainings() {
        if (trainingData != null && trainingData.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(trainingData.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith(HASH_SIGN)) continue;

                    String[] splitText = line.split(SPLITTER);

                    Training training = new Training();
                    Long id = Long.parseLong(splitText[0].trim());
                    training.setId(id);
                    training.setTraineeId(Long.parseLong(splitText[1].trim()));
                    training.setTrainerId(Long.parseLong(splitText[2].trim()));
                    training.setName(splitText[3].trim());
                    training.setTrainingTypeId(Long.valueOf(splitText[4].trim()));
                    LocalDate date = LocalDate.parse(splitText[5].trim());
                    training.setTrainingDate(date);
                    training.setDuration(Double.parseDouble(splitText[6].trim()));

                    trainingStorage.put(training.getId(), training);
                }
                LOG.info(LOAD_TRAINING_SUCCESS, trainingStorage.size());

            } catch (IOException exception) {
                LOG.warn(FAIL_LOAD_TRAINING_MESSAGE, exception.getMessage());
            }
        }

        LOG.info(NO_TRAINING_DATA_FOUND);
    }

    private void loadUsers() {
        if (userData != null && userData.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(userData.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith(HASH_SIGN)) continue;

                    String[] splitLine = line.split(SPLITTER);

                    User user = new User();
                    long id = Long.parseLong(splitLine[0].trim());
                    user.setId(id);
                    user.setFirstName(splitLine[1].trim());
                    user.setLastName(splitLine[2].trim());
                    user.setUsername(splitLine[3].trim());
                    user.setPassword(splitLine[4].trim());
                    user.setActive(Boolean.parseBoolean(splitLine[5].trim()));

                    userStorage.put(user.getId(), user);
                }
                LOG.info(LOAD_USER_SUCCESS, userStorage.size());

            } catch (IOException e) {
                LOG.warn(FAIL_LOAD_USER_MESSAGE, e.getMessage());
            }
        }
        LOG.info(NO_USER_DATA_FOUND);
    }

    private void loadTrainingTypes() {
        if (trainingTypeData != null && trainingTypeData.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(trainingTypeData.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith(HASH_SIGN)) continue;

                    String[] splitText = line.split(SPLITTER);

                    TrainingType type = new TrainingType();
                    Long id = Long.parseLong(splitText[0].trim());
                    type.setId(id);
                    type.setName(splitText[1].trim());

                    trainingTypeStorage.put(type.getId(), type);
                }
                LOG.info(LOAD_TRAINING_TYPE_SUCCESS, trainingTypeStorage.size());

            } catch (IOException exception) {
                LOG.warn(FAIL_LOAD_TRAINING_TYPE_MESSAGE, exception.getMessage());
            }
        }

        LOG.info(NO_TRAINING_TYPE_DATA_FOUND);
    }
}
