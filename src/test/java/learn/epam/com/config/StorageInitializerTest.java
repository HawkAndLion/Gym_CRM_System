package learn.epam.com.config;

import learn.epam.com.entity.Trainee;
import learn.epam.com.entity.Trainer;
import learn.epam.com.entity.Training;
import learn.epam.com.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageInitializerTest {
    private static final String USERS_FILE_CONTENT = """
            1|John|Brown|john.brown|secret|true
            2|Alice|Smith|alice.smith|password|false
            """;

    @Mock
    private Map<Long, User> userStorage;
    @Mock
    private Map<Long, Trainee> traineeStorage;
    @Mock
    private Map<Long, Trainer> trainerStorage;
    @Mock
    private Map<Long, Training> trainingStorage;

    @Mock
    private Resource userData;
    @Mock
    private Resource traineeData;
    @Mock
    private Resource trainerData;
    @Mock
    private Resource trainingData;

    @InjectMocks
    private StorageInitializer initializer;

    @BeforeEach
    void setUp() throws Exception {
        initializer = new StorageInitializer(traineeData, trainerData, trainingData, userData);

        Field field = StorageInitializer.class.getDeclaredField("userStorage");
        field.setAccessible(true);
        field.set(initializer, userStorage);
    }


    @Test
    void shouldLoadValidUserWhenInitIsCalled() throws Exception {
        // Given
        when(userData.exists()).thenReturn(true);
        when(userData.getInputStream())
                .thenReturn(new ByteArrayInputStream(USERS_FILE_CONTENT.getBytes(StandardCharsets.UTF_8)));

        // When
        initializer.init();

        // Then
        verify(userStorage).put(eq(1L), any(User.class));
        verifyNoInteractions(traineeStorage, trainerStorage, trainingStorage);
    }

    @Test
    void shouldDoNothingWhenResourcesDONotExist() {
        // Given
        when(userData.exists()).thenReturn(false);
        when(traineeData.exists()).thenReturn(false);
        when(trainerData.exists()).thenReturn(false);
        when(trainingData.exists()).thenReturn(false);

        // When
        initializer.init();

        // Then
        verifyNoInteractions(userStorage, traineeStorage, trainerStorage, trainingStorage);
    }
}
