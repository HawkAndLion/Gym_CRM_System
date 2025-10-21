package learn.epam.com.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import learn.epam.com.logging.LoggingInterceptor;
import learn.epam.com.main.GymFacade;
import learn.epam.com.security.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoggingInterceptor loggingInterceptor;
    private final GymFacade gymFacade;

    @Autowired
    public WebMvcConfig(LoggingInterceptor loggingInterceptor, GymFacade gymFacade){
        this.loggingInterceptor = loggingInterceptor;
        this.gymFacade = gymFacade;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor(gymFacade))
                .addPathPatterns("/api/**");

        registry.addInterceptor(loggingInterceptor);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule()) // чтобы работали LocalDate и т.д.
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        return converter;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        boolean hasJackson = converters.stream()
                .anyMatch(c -> c instanceof MappingJackson2HttpMessageConverter);
        if (!hasJackson) {
            converters.add(mappingJackson2HttpMessageConverter(objectMapper()));
        }
    }
}
