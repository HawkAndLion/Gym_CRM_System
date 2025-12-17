package learn.epam.com.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String specialization;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;

    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new HashSet<>();

    public Trainer(String specialization, boolean isActive) {
        this.specialization = specialization;
        this.isActive = isActive;
    }

    public Trainer(Long id, User user, String specialization, boolean isActive, Set<Trainee> trainees) {
        this.id = id;
        this.user = user;
        this.specialization = specialization;
        this.isActive = isActive;
        this.trainees = trainees;
    }

    public Trainer(Long id, User user, String specialization, boolean isActive) {
        this.id = id;
        this.user = user;
        this.specialization = specialization;
        this.isActive = isActive;
    }
}
