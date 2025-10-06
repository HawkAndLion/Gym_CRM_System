package learn.epam.com.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "trainers")
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String specialization;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;

    public Trainer() {
        super();
    }

    public Trainer(Long id, Long userId, String specialization) {
        this.id = id;
        this.userId = userId;
        this.specialization = specialization;
    }

    public Trainer(Long id, Long userId, String specialization, boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.specialization = specialization;
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
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
        Trainer trainer = (Trainer) object;
        return isActive == trainer.isActive && Objects.equals(id, trainer.id) && Objects.equals(userId, trainer.userId) && Objects.equals(specialization, trainer.specialization);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, specialization, isActive);
    }
}
