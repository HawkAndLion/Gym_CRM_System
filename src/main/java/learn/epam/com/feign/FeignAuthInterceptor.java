package learn.epam.com.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import learn.epam.com.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeignAuthInterceptor implements RequestInterceptor {

    private static final String SERVICE_NAME = "gym-crm-system";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void apply(RequestTemplate template) {
        String token = jwtTokenProvider.generateServiceToken(SERVICE_NAME);

        template.header(AUTHORIZATION, BEARER + token);
    }
}
