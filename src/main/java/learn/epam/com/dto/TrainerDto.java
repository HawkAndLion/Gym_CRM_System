package learn.epam.com.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

@Schema(
        description = "Trainee registration request",
        requiredProperties = {"firstName", "lastName", "specialization"}
)
public class TrainerDto {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String specialization;

    @NotBlank
    private String password;

    public TrainerDto() {
    }

    public TrainerDto(String firstName, String lastName, String specialization, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
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
        TrainerDto that = (TrainerDto) object;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(specialization, that.specialization) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, specialization, password);
    }
}
