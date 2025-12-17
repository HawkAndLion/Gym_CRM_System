package learn.epam.com.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainees")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Trainee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;

    @ManyToMany
    @JoinTable(name = "trainee_trainers", joinColumns = @JoinColumn(name = "trainee_id"), inverseJoinColumns = @JoinColumn(name = "trainer_id"))
    private Set<Trainer> trainers = new HashSet<>();

    public Trainee(String address, LocalDate dateOfBirth, boolean isActive) {
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.isActive = isActive;
    }

    public Trainee(Long id, User user, String address, LocalDate dateOfBirth, boolean isActive) {
        this.id = id;
        this.user = user;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.isActive = isActive;
    }

    public Trainee(Long id, User user, String address, LocalDate dateOfBirth, boolean isActive, Set<Trainer> trainers) {
        this.id = id;
        this.user = user;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.isActive = isActive;
        this.trainers = trainers;
    }
}
