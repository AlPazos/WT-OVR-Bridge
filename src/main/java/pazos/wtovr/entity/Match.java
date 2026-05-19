package pazos.wtovr.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches", indexes = {
        @Index(name = "idx_mat", columnList = "mat"),
        @Index(name = "idx_phase", columnList = "phase"),
        @Index(name = "idx_category", columnList = "category_id")
})
public class Match extends PanacheEntityBase {

    @Id
    public String matchNumber;

    public Integer mat;
    public String phase;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "blue_athlete_id", nullable = true)
    public Athlete blueAthlete;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "red_athlete_id", nullable = true)
    public Athlete redAthlete;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    public Category category;

    public String matchVictoryCriteria;
    public Integer videoQuota;
    public Boolean wtCompetitionDataProtocol;

    public String nextMatchNumber;
    public String nextMatchColor;

    @Column(nullable = false, columnDefinition = "varchar(20) default 'available'")
    public String status = "available";

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;

    public Match() {
    }

    public Match(String matchNumber) {
        this.matchNumber = matchNumber;
    }

    public static Match getNextForMat(int mat) {
        return find("mat = ?1 ORDER BY matchNumber ASC", mat).firstResult();
    }

    public static List<Match> findAllByMat(int mat) {
        return find("mat", mat).list();
    }

    public static List<Match> findAllByMatAndStatus(int mat, String status) {
        return find("mat = ?1 and status = ?2", mat, status).list();
    }

    public static Match findByCompetitor(Athlete athlete) {
        return find("blueAthlete = ?1 or redAthlete = ?1", athlete).firstResult();
    }

    @Override
    public String toString() {
        return "Match{matchNumber='" + matchNumber + "', mat=" + mat + ", phase='" + phase + "'}";
    }
}