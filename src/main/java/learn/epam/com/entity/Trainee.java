package learn.epam.com.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "trainees")
public class Trainee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;

    @ManyToMany
    @JoinTable(name = "trainee_trainers", joinColumns = @JoinColumn(name = "trainee_id"), inverseJoinColumns = @JoinColumn(name = "trainer_id"))
    private Set<Trainer> trainers = new HashSet<>();

    public Trainee() {
        super();
    }

    public Trainee(String address, LocalDate dateOfBirth, boolean isActive) {
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.isActive = isActive;
    }

    public Trainee(Long id, Long userId, String address, LocalDate dateOfBirth, boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.isActive = isActive;
    }

    public Trainee(Long id, Long userId, String address, LocalDate dateOfBirth, boolean isActive, Set<Trainer> trainers) {
        this.id = id;
        this.userId = userId;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.isActive = isActive;
        this.trainers = trainers;
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

    public Set<Trainer> getTrainers() {
        return trainers;
    }

    public void setTrainers(Set<Trainer> trainers) {
        this.trainers = trainers;
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
