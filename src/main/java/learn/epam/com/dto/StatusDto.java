package learn.epam.com.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public class StatusDto {

    @NotBlank
    private String username;

    private boolean isActive;

    public StatusDto() {
    }

    public StatusDto(String username, boolean isActive) {
        this.username = username;
        this.isActive = isActive;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        StatusDto that = (StatusDto) object;
        return isActive == that.isActive && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, isActive);
    }
}
