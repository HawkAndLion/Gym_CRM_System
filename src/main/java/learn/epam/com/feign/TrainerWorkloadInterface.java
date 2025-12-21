package learn.epam.com.feign;

import learn.epam.com.dto.client.TrainingEventDto;
import learn.epam.com.dto.client.TrainingSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "trainer-workload-service")
public interface TrainerWorkloadInterface {

    @PostMapping("/api/v1/workload")
    ResponseEntity<Void> processTrainingEvent(@RequestBody TrainingEventDto dto,
                                              @RequestHeader("transactionId") String transactionId);

    @GetMapping("/api/v1/workload/{username}/{year}/{month}")
    ResponseEntity<TrainingSummaryDto> getMonthlySummary(@PathVariable String username,
                                                         @PathVariable int year,
                                                         @PathVariable int month,
                                                         @RequestHeader("transactionId") String transactionId);
}
