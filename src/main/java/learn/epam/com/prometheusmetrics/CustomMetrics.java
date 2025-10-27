package learn.epam.com.prometheusmetrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class CustomMetrics {

    private final Counter traineeCreatedCounter;
    private final Counter trainerCreatedCounter;
    private final Timer traineeRegistrationTimer;
    private final Timer trainerRegistrationTimer;

    public CustomMetrics(MeterRegistry registry) {
        this.traineeCreatedCounter = Counter.builder("trainee.created.count")
                .description("Number of trainees created")
                .register(registry);

        this.trainerCreatedCounter = Counter.builder("trainer.created.count")
                .description("Number of trainers created")
                .register(registry);

        this.traineeRegistrationTimer = Timer.builder("trainee.registration.time")
                .description("Time taken to register a trainee")
                .register(registry);

        this.trainerRegistrationTimer = Timer.builder("trainer.registration.time")
                .description("Time taken to register a trainer")
                .register(registry);
    }

    public void incrementTraineeCreated() {
        traineeCreatedCounter.increment();
    }

    public <T> T recordTraineeRegistration(java.util.concurrent.Callable<T> callable) throws Exception {
        return traineeRegistrationTimer.recordCallable(callable);
    }

    public void incrementTrainerCreated() {
        trainerCreatedCounter.increment();
    }

    public <T> T recordTrainerRegistration(java.util.concurrent.Callable<T> callable) throws Exception {
        return trainerRegistrationTimer.recordCallable(callable);
    }
}