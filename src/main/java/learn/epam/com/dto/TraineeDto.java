package learn.epam.com.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Objects;

@Schema(
        description = "Trainee registration request",
        requiredProperties = {"firstName", "lastName"}
)
public class TraineeDto {
    @Schema(description = "First name", example = "John")
    @NotBlank
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    @NotBlank
    private String lastName;

    @Schema(description = "Date of birth (optional)", example = "1998-05-23")
    private LocalDate dateOfBirth;

    @Schema(description = "Address (optional)", example = "123 Main St, New York")
    private String address;

    @NotBlank
    private String password;

    public TraineeDto() {
    }

    public TraineeDto(String firstName, String lastName, LocalDate dateOfBirth, String address, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
        TraineeDto that = (TraineeDto) object;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(dateOfBirth, that.dateOfBirth) && Objects.equals(address, that.address) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, dateOfBirth, address, password);
    }
}
