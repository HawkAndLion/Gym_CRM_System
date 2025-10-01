package learn.epam.com.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Trainee {
    private Long id;
    private Long userId;
    private String address;
    private LocalDate dateOfBirth;
    private boolean isActive = true;

    public Trainee() {
        super();
    }

    public Trainee(Long id, Long userId, String address, LocalDate dateOfBirth) {
        this.id = id;
        this.userId = userId;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
    }

    public Trainee(Long id, Long userId, String address, LocalDate dateOfBirth, boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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
        Trainee trainee = (Trainee) object;
        return isActive == trainee.isActive && Objects.equals(id, trainee.id) && Objects.equals(userId, trainee.userId) && Objects.equals(address, trainee.address) && Objects.equals(dateOfBirth, trainee.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, address, dateOfBirth, isActive);
    }
}
