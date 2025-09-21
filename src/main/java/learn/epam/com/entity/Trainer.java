package learn.epam.com.entity;

import java.util.Objects;

public class Trainer {
    private Long id;
    private Long userId;
    private String specialization;

    public Trainer() {
        super();
    }

    public Trainer(Long id, Long userId, String specialization) {
        this.id = id;
        this.userId = userId;
        this.specialization = specialization;
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

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Trainer trainer = (Trainer) object;
        return Objects.equals(id, trainer.id) && Objects.equals(userId, trainer.userId) && Objects.equals(specialization, trainer.specialization);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, specialization);
    }
}
