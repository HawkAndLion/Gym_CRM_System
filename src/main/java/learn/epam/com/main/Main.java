package learn.epam.com.main;

import learn.epam.com.config.ApplicationConfig;
import learn.epam.com.entity.*;
import learn.epam.com.service.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws ServiceException {
        ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);

        GymFacade facade = context.getBean(GymFacade.class);
        TraineeService traineeService = facade.trainee();
        TrainerService trainerService = facade.trainer();
        TrainingService trainingService = facade.training();
        UserService userService = facade.user();
        TrainingTypeService trainingTypeService = facade.trainingType();
        ProfileService profileService = facade.profile();

        List<User> users;
        users = userService.findAllUsers();


        for (User user : users) {
            System.out.println("USER #" + user.getId() + ") " + user.getFirstName() + " " + user.getLastName() + "; Username: " + user.getUsername());
        }
        System.out.println("**********************************");
        System.out.println("SERVICE: getting a list of trainees");
        List<Trainee> trainees;

        trainees = traineeService.findAllTrainee();

        for (Trainee trainee : trainees) {
            System.out.println("TRAINEE #" + trainee.getId() + ") " + trainee.getAddress() + "; USER #" + trainee.getUserId());
        }
        System.out.println("**********************************");

        // 3) Print list of trainers
        System.out.println("\n=== Trainers ===");
        List<Trainer> trainers;

        trainers = trainerService.findAllTrainers();

        for (Trainer trainer : trainers) {
            System.out.println("TRAINER #" + trainer.getId() + " Specialization is " + trainer.getSpecialization());
        }

        // 4) Get trainee by id = 1
        System.out.println("\n=== Trainee with id=3 ===");
        Optional<Trainee> trainee1;
        try {
            trainee1 = traineeService.findById(3L);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        Trainee targetTrainee = trainee1.orElseThrow();
        System.out.println("SEARCHING TRAINEE BY ID=3; ACTUAL #" + targetTrainee.getId());

        // 5) Get trainer by id = 3
        System.out.println("\n=== Trainer with id=2 ===");
        Optional<Trainer> trainer3;
        try {
            trainer3 = trainerService.findById(2L);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
        Trainer trainer = trainer3.get();
        System.out.println("SEARCHING TRAINER BY ID=2; ACTUAL #" + trainer.getId());

        // 6) Add new user + trainee
        System.out.println("\n=== Add new trainee ===");
        User user = userService.findById(6L).orElseThrow();
        Set<Trainer> listOfEmptyTrainer = new HashSet<>();

        Trainee newTrainee = new Trainee(
//                4L, // trainee id
                null,
                user.getId(),
                "Almaty, Arbat Str. 777",
                LocalDate.parse("1991-07-07"),
                true,
                listOfEmptyTrainer
        );

        traineeService.save(newTrainee);

        // Check again
//        try {
//            traineeService.findAllTrainee().forEach(System.out::println);
        List<Trainee> traineeList;

        traineeList = traineeService.findAllTrainee();

        for (Trainee trainee : traineeList) {
            System.out.println("TRAINEE #" + trainee.getId() + ") " + trainee.getAddress() + "; USER #" + trainee.getUserId());
        }

        /////////////////////
//        User newUser = new User(null, "Kim", "Kimberly", "Kim.Kimberly", "qwertyuiop", false);
        User newUser = new User(null, "John", "Brown", "John.Brown", "qwertyuiop", false);


        try {
            facade.user().save(newUser);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

//        User anotherUser = new User(null, "Maggy", "Maggy", "Maggy.Maggy", "qwertyuiop", false);
//        User anotherUser = new User(null,"John", "Brown", "John.Brown", "qwertyuiop", false);
        User anotherUser = new User(null, "John", "Brown", "", "", false);

        try {
            facade.user().save(anotherUser);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }


        for (User obj : userService.findAllUsers()) {
            System.out.println("USER #" + obj.getId() + ") " + obj.getFirstName() + " " + obj.getLastName() + "; Username: " + obj.getUsername() + "; Password: " + obj.getPassword());
        }


        /////////////////////
        // 7) Add new training
        System.out.println("\n=== Add new training ===");

        Training newTraining = new Training(
                null,  // training id
                4L,  // trainee id
                1L,  // trainer id
                "Cardio",
                1L,
                LocalDate.parse("2025-10-03"),
                1.0
        );
        try {
            facade.training().save(newTraining);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        // Print trainings
        List<Training> trainings;

        trainings = trainingService.findAllTrainings();


        for (Training training : trainings) {
            System.out.println("TRAINING #" + training.getId() + " " + training.getName() + "; " + training.getTrainingTypeId());
        }


        System.out.println("\n=== Add new trainingType ===");
        TrainingType trainingType = new TrainingType("Flexibility Training");

//        facade.trainingType().save(trainingType);


        List<TrainingType> list;

        list = trainingTypeService.findAllTrainingTypes();


        for (TrainingType type : list) {
            System.out.println("TRAINING TYPE #" + type.getId() + ") " + type.getName());
        }

        System.out.println("******************************************");
        System.out.println("CHECK USERNAME & PASSWORD");

        System.out.println("*****TRAINEE*****");
        boolean ok = false;
        try {
            ok = traineeService.checkCredentials(3L, "Nick.Carter", "qwertyuiop");
//            ok = traineeService.checkCredentials(3L, "Jessica.Parker", "qwertyuiop");
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
        if (ok) {
            System.out.println(" Credentials match existing trainee");
        } else {
            System.out.println(" Wrong username/password for trainee");
        }

        Optional<Trainee> trainee;
        try {
            trainee = traineeService.findTraineeByCredentials("Nick.Carter", "qwertyuiop");
            System.out.println("Trainee's user #" + trainee.get().getUserId());
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        try {
            User currentUser = userService.findById(trainee.get().getUserId()).orElseThrow();
            String oldPassword = userService.findById(trainee.get().getUserId()).get().getPassword();
            System.out.println("TRAINEE's password before change: " + oldPassword);
//            UserDao userDao = context.getBean(UserDao.class);
//            UserCredentialService credentialService = new UserCredentialService(userDao);
            traineeService.changePasswordForTrainee(currentUser.getUsername(), oldPassword, "secret777");
            System.out.println("TRAINEE's password after change: " + userService.findById(trainee.get().getUserId()).get().getPassword());
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        User someUser = new User(null, "Alicia", "Keys", "Alicia.Keys", "qwertyuiop", true);
        Trainee trainee2 = new Trainee(null, null, "Qonaev, Seifullin Str. 153", LocalDate.parse("1991-07-07"), true, listOfEmptyTrainer);

        Trainee traineeProfile = profileService.createTraineeProfile(someUser, trainee2);
        System.out.println("TRAINEE PROFILE: id=" + traineeProfile.getId() + ") " + traineeProfile.getAddress());


        traineeService.update(new Trainee(1L, 4L, "Moscow, some street", LocalDate.parse("1998-04-15"), true, listOfEmptyTrainer));
        Optional<Trainee> updatedTrainee = traineeService.findById(1L);

        traineeService.updateTraineeProfile(userService.findById(4L).get().getUsername(), "qwertyuiop", updatedTrainee.get());
        System.out.println("UPDATE TRAINEE PROFILE: " + traineeService.findTraineeByCredentials(userService.findById(4L).get().getUsername(), "qwertyuiop").get().getAddress());


        System.out.println("FIND TRAINEE BY USERNAME: " + traineeService.findTraineeByUsername("Jacob.Franklin").get().getId());

        Trainee traineeToBeDeleted = traineeService.findById(1L).get();
        System.out.println("TRAINEE PROFILE BEFORE being deleted: id=" + traineeService.findById(traineeToBeDeleted.getId()).get().getId() + ") " + traineeService.findById(traineeToBeDeleted.getId()).get().getAddress());
        traineeService.deleteTraineeByUsername(userService.findById(traineeToBeDeleted.getUserId()).get().getUsername());
        System.out.println("TRAINEE PROFILE AFTER being deleted: id=" + traineeService.findById(traineeToBeDeleted.getId()) + ") " + traineeService.findById(traineeToBeDeleted.getId()));

        System.out.println("TRAINEE #3 is BEFORE being deactivated: " + traineeService.findById(3L).get().isActive());
        traineeService.deactivateTrainee(userService.findById(trainee1.get().getUserId()).get().getUsername());
        System.out.println("TRAINEE #3 is AFTER being deactivated: " + traineeService.findById(3L).get().isActive());

        System.out.println("GET TRAINEE's List of Trainers: ");
//        TraineeTrainerDao traineeTrainerDao = context.getBean(TraineeTrainerDao.class);
        for (Long trainerId : traineeService.getTrainerIdsForTrainee(2L)) {
            System.out.println("Trainer #" + trainerId);
        }

        System.out.println("GET unassigned Trainers for TRAINEE before assigning: ");
        for (Trainer unassingedTrainer : trainerService.getUnassignedTrainersForTrainee("Jessica.Parker")) {
            System.out.println("Trainer #" + unassingedTrainer.getId());
        }

        traineeService.assignTrainer(2L, 2L);

        System.out.println("GET unassigned Trainers for TRAINEE after assigning: ");
        for (Trainer unassingedTrainer : trainerService.getUnassignedTrainersForTrainee("Jessica.Parker")) {
            System.out.println("Trainer #" + unassingedTrainer.getId());
        }

        traineeService.unassignTrainer(2L, 2L);
        traineeService.unassignTrainer(2L, 3L);

        System.out.println("GET unassigned Trainers for TRAINEE after unassigning trainer(s): ");
        for (Trainer unassingedTrainer : trainerService.getUnassignedTrainersForTrainee("Jessica.Parker")) {
            System.out.println("Trainer #" + unassingedTrainer.getId());
        }

        System.out.println("TRAINEE's List of Trainers before Set<Long> trainersIds: ");
        for (Long trainerId : traineeService.getTrainerIdsForTrainee(2L)) {
            System.out.println("Trainer #" + trainerId);
        }

        Set<Long> newTrainersIds = new java.util.HashSet<>(Set.of());
        newTrainersIds.add(2L);
        newTrainersIds.add(3L);
        trainerService.updateTraineeTrainersList("Jessica.Parker", newTrainersIds);

        System.out.println("UPDATED TRAINEE's Trainers: " + traineeService.getTrainerIdsForTrainee(2L));
        System.out.println("Remark: as long as Set<Long> contains only ids #2 and #3, it should print only these two trainers without #1.");
        System.out.println("Because there is \"trainee.getTrainers().clear();\" in method");

        System.out.println("GET TRAINEE's TRAININGS: ");
//        List<Training> trainingList = trainingService.findTrainingsForTraineeByCriteria("Jessica.Parker", LocalDate.parse("2025-10-02"), LocalDate.parse("2025-10-03"), "Lindsey.Adams", null);
        List<Training> trainingList = trainingService.findTrainingsForTraineeByCriteria("Nick.Carter", LocalDate.parse("2025-10-03"), LocalDate.parse("2025-10-05"), null, null);
        for (Training training : trainingList) {
            System.out.println("Training ID=" + training.getId() + "; Trainee's ID=" + training.getTraineeId()
                    + "; username=" + training.getName() + "; TrainingDate=" + training.getTrainingDate()
                    + "; Trainer ID=" + training.getTrainerId() + "; TrainingType ID=" + training.getTrainingTypeId());
        }

        User findUser = userService.findById(6L).get();
        System.out.println("Username to be deleted: " + findUser.getUsername());
        Trainee traineeForDelete = traineeService.findById(3L).get();
        traineeService.delete(traineeForDelete);

        userService.delete(userService.findById(6L).get());


        System.out.println("*****TRAINER*****");
        boolean fine = false;
        try {
            fine = trainerService.checkCredentials(1L, "John.Brown", "qwertyuiop");
//            fine = trainerService.checkCredentials(1L, "Lindsey.Adams", "qwertyuiop");
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
        if (fine) {
            System.out.println(" Credentials match existing trainee");
        } else {
            System.out.println(" Wrong username/password for trainee");
        }


        Optional<Trainer> trainer2 = trainerService.findTrainerByCredentials("John.Brown", "qwertyuiop");
//            Optional<Trainer> trainer2 = trainerService.findTrainerByCredentials("Nick.Carter", "qwertyuiop");
        System.out.println("Trainer's user #" + trainer2.get().getUserId());


        User user2 = new User(null, "Patrick", "Keys", "Patrick.Keys", "qwertyuiop", true);
        Set<Trainee> listOfEmptyTrainee = new HashSet<>();
        listOfEmptyTrainee.add(new Trainee());

        Trainer trainer1 = new Trainer(null, null, "Aerobics", true, listOfEmptyTrainee);
        try {
            System.out.println("TRAINER'S PROFILE: " + profileService.createTrainerProfile(user2, trainer1).getUserId());
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        for (User key : userService.findAllUsers()) {
            System.out.println("USER #" + key.getId() + ") " + key.getFirstName() + " " + key.getLastName() + "; Username: " + key.getUsername() + "; Password: " + key.getPassword());
        }


        try {
            System.out.println("FIND TRAINER BY USERNAME: " + trainerService.findTrainerByUsername("Patrick.Keys").get().getSpecialization());
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        System.out.println("TRAINER #2 is BEFORE being deactivated: " + trainerService.findById(2L).get().isActive());
        trainerService.deactivateTrainer(userService.findById(2L).get().getUsername());
        System.out.println("TRAINER #2 is AFTER being deactivated: " + trainerService.findById(2L).get().isActive());
    }


}
