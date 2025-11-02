package learn.epam.com.repository;

import learn.epam.com.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @Query("SELECT t FROM Trainer t WHERE LOWER(t.user.username) = LOWER(:username)")
    Optional<Trainer> findByUsername(@Param("username") String username);

    @Query("""
            SELECT t FROM Trainer t 
            WHERE t.isActive = true 
            AND t.id NOT IN (
                SELECT tr.id FROM Trainer tr 
                JOIN tr.trainees trainee 
                WHERE trainee.id = :traineeId
            )
            """)
    List<Trainer> findUnassignedTrainersForTrainee(@Param("traineeId") Long traineeId);

    @Query("SELECT trainee.id FROM Trainer t JOIN t.trainees trainee WHERE t.id = :trainerId")
    Set<Long> findTraineeIdsForTrainer(@Param("trainerId") Long trainerId);
}
