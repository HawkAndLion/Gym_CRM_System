package learn.epam.com.dto;

import java.util.Objects;

public class TrainerDto {
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;

    public TrainerDto() {}

    public TrainerDto(String username, String firstName, String lastName, String specialization) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TrainerDto that = (TrainerDto) object;
        return Objects.equals(username, that.username) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(specialization, that.specialization);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, firstName, lastName, specialization);
    }
}
