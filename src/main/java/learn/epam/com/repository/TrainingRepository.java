package learn.epam.com.repository;

import learn.epam.com.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("SELECT tr FROM Training tr WHERE tr.traineeId = :traineeId")
    List<Training> findTrainingsByTraineeId(@Param("traineeId") Long traineeId);

    @Query("SELECT tr FROM Training tr WHERE tr.trainerId = :trainerId")
    List<Training> findTrainingsByTrainerId(@Param("trainerId") Long trainerId);
}
