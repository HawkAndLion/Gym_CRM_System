package learn.epam.com.repository;

import learn.epam.com.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {
    @Query("SELECT t FROM Trainee t WHERE t.user.username = :username")
    Optional<Trainee> findByUsername(@Param("username") String username);

    @Query("SELECT tr.id FROM Trainee t JOIN t.trainers tr WHERE t.id = :traineeId")
    Set<Long> findTrainerIdsByTraineeId(@Param("traineeId") Long traineeId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM trainee_trainers WHERE trainee_id = :traineeId", nativeQuery = true)
    void removeAllTrainerRelations(@Param("traineeId") Long traineeId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO trainee_trainers (trainee_id, trainer_id) VALUES (:traineeId, :trainerId)", nativeQuery = true)
    void addTrainerRelation(@Param("traineeId") Long traineeId, @Param("trainerId") Long trainerId);
}
