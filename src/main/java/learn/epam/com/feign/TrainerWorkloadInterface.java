package learn.epam.com.feign;

import learn.epam.com.dto.client.TrainingEventDto;
import learn.epam.com.dto.client.TrainingSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient("TRAINER-WORKLOAD-SERVICE")
public interface TrainerWorkloadInterface {
    @PostMapping
    public ResponseEntity<Void> processTrainingEvent(@RequestBody TrainingEventDto dto,
                                                     @RequestHeader(value = "transactionId", required = false) String transactionId);


    @GetMapping("/{username}/{year}/{month}")
    public ResponseEntity<TrainingSummaryDto> getMonthlySummary(@PathVariable String username,
                                                                @PathVariable int year,
                                                                @PathVariable int month,
                                                                @RequestHeader(value = "transactionId", required = false) String transactionId);
}
