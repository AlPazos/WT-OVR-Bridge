package pazos.tkStrike.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_events", indexes = {
        @Index(name = "idx_event_match", columnList = "match_number"),
        @Index(name = "idx_event_type", columnList = "eventType")
})
public class MatchEvent extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_number", nullable = false)
    public Match match;

    public Integer round;
    public Long roundTimestamp;

    @Column(nullable = false)
    public String eventType;

    public Integer bluePoints;
    public Integer bluePenalties;
    public Integer redPoints;
    public Integer redPenalties;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    public MatchEvent() {
    }

    public MatchEvent(Match match, Integer round, Long roundTimestamp, String eventType,
                      Integer bluePoints, Integer bluePenalties,
                      Integer redPoints, Integer redPenalties) {
        this.match = match;
        this.round = round;
        this.roundTimestamp = roundTimestamp;
        this.eventType = eventType;
        this.bluePoints = bluePoints;
        this.bluePenalties = bluePenalties;
        this.redPoints = redPoints;
        this.redPenalties = redPenalties;
    }
}