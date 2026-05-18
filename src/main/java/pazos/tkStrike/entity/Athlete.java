package pazos.tkStrike.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "athletes", indexes = {
        @Index(name = "idx_wf_id", columnList = "wfId")
})
public class Athlete extends PanacheEntityBase {

    @Id
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

    public Athlete() {
    }

    public Athlete(String ovrInternalId, String scoreboardName) {
        this.ovrInternalId = ovrInternalId;
        this.scoreboardName = scoreboardName;
    }

    public static Athlete findByWfId(String wfId) {
        return find("wfId", wfId).firstResult();
    }
}