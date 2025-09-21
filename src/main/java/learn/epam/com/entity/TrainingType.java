package learn.epam.com.entity;

import java.util.Objects;

public class TrainingType {
    private Long id;
    private Training type;
    private Training name;

    public TrainingType() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Training getType() {
        return type;
    }

    public void setType(Training type) {
        this.type = type;
    }

    public Training getName() {
        return name;
    }

    public void setName(Training name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TrainingType that = (TrainingType) object;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, name);
    }
}
