package learn.epam.com.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import learn.epam.com.dto.client.TrainingEventDto;
import learn.epam.com.dto.client.TrainingSummaryDto;
import learn.epam.com.feign.TrainerWorkloadInterface;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TrainerWorkloadClient {
    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadClient.class);
    private static final String CIRCUIT_BREAKER_NAME = "trainerWorkload";
    private static final String CIRCUIT_BREAKER_WARNING = "CircuitBreaker OPEN. Returning empty summary. user={}, transactionId={}";
    private static final String TRAINING_FALLBACK_MESSAGE = "CircuitBreaker OPEN. trainer-workload-service unavailable. Training event skipped. transactionId={}";

    private final TrainerWorkloadInterface feignClient;

    @CircuitBreaker(
            name = CIRCUIT_BREAKER_NAME,
            fallbackMethod = "processTrainingFallback"
    )
    public void processTrainingEvent(TrainingEventDto dto, String transactionId) {
        feignClient.processTrainingEvent(dto, transactionId);
    }

    @CircuitBreaker(
            name = CIRCUIT_BREAKER_NAME,
            fallbackMethod = "getSummaryFallback"
    )
    public TrainingSummaryDto getMonthlySummary(String username, int year, int month, String transactionId) {
        ResponseEntity<TrainingSummaryDto> response =
                feignClient.getMonthlySummary(username, year, month, transactionId);

        return response.getBody();
    }

    public void processTrainingFallback(TrainingEventDto dto,
                                        String transactionId,
                                        Throwable ex) {
        log.warn(
                TRAINING_FALLBACK_MESSAGE,
                transactionId,
                ex
        );
    }

    public TrainingSummaryDto getSummaryFallback(String username,
                                                 int year,
                                                 int month,
                                                 String transactionId,
                                                 Throwable ex) {

        log.warn(CIRCUIT_BREAKER_WARNING, username, transactionId, ex);

        return new TrainingSummaryDto(username, null, null, false, List.of());
    }
}
