package pazos.tkStrike.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un combate
 * Relaciona:
 * - 2 atletas (blue y red)
 * - 1 categoría (configuration de rondas y reglas)
 */
@Entity
@Table(name = "matches", indexes = {
    @Index(name = "idx_match_number", columnList = "matchNumber", unique = true),
    @Index(name = "idx_mat", columnList = "mat"),
    @Index(name = "idx_phase", columnList = "phase"),
    @Index(name = "idx_category", columnList = "category_name,category_gender,category_sub_category")
})
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @Column(nullable = false, unique = true)
    public String matchNumber;

    public Integer mat;
    public String phase;

    // Relaciones con atletas
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "blue_athlete_id", nullable = false)
    public Athlete blueAthlete;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "red_athlete_id", nullable = false)
    public Athlete redAthlete;

    // Relación con categoría (por clave compuesta)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "category_name", referencedColumnName = "name"),
        @JoinColumn(name = "category_gender", referencedColumnName = "gender"),
        @JoinColumn(name = "category_sub_category", referencedColumnName = "subcategory")
    })
    public Category category;

    public String matchVictoryCriteria;

    // Video quotas
    public Integer blueAthleteVideoQuota;
    public Integer redAthleteVideoQuota;

    // Flag para indicar si usa protocolo WT
    public Boolean wtCompetitionDataProtocol;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;

    // Constructores
    public Match() {}

    public Match(String matchNumber) {
        this.matchNumber = matchNumber;
    }

    @Override
    public String toString() {
        return "Match{" +
               "matchNumber='" + matchNumber + '\'' +
               ", mat=" + mat +
               ", phase='" + phase + '\'' +
               '}';
    }
}

