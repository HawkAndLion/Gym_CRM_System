package learn.epam.com.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

@Schema(
        description = "Registration response",
        requiredProperties = {"username", "password"}
)
public class UserDetailsDto {
    @Schema(description = "Generated username", example = "john.doe")
    @NotBlank
    private String username;

    @Schema(description = "Generated password", example = "A7f8dX#9kL")
    @NotBlank
    private String password;

    public UserDetailsDto(){}

    public UserDetailsDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        UserDetailsDto that = (UserDetailsDto) object;
        return Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
}
