package pazos.tkStrike.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un atleta/competidor
 */
@Entity
@Table(name = "athletes", indexes = {
    @Index(name = "idx_ovr_id", columnList = "ovrInternalId", unique = true),
    @Index(name = "idx_wf_id", columnList = "wfId")
})
public class Athlete {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @Column(nullable = false, unique = true)
    public String ovrInternalId;

    @Column(nullable = false)
    public String scoreboardName;

    public String givenName;
    public String familyName;

    @Column(length = 3)
    public String flagAbbreviation;

    public Integer rank;
    public Integer seed;
    public String gender;
    public String wfId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;

    // Constructores
    public Athlete() {}

    public Athlete(String ovrInternalId, String scoreboardName) {
        this.ovrInternalId = ovrInternalId;
        this.scoreboardName = scoreboardName;
    }
}

