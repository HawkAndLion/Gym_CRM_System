package learn.epam.com.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import reactor.util.annotation.NonNull;

import java.util.UUID;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String TRANSACTION_ID = "transactionId";
    private static final String REQUEST_WITH_TRANSACTION = "Request [{}] {} from IP {} with transactionId {}";
    private static final String EXCEPTION = "Request ended with exception";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID, transactionId);
        log.info(REQUEST_WITH_TRANSACTION,
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), transactionId);

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        if (ex != null) {
            log.error(EXCEPTION, ex);
        }

        MDC.clear();
    }
}

