package pazos.wtovr.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.wtovr.entity.Athlete;
import pazos.wtovr.entity.Match;

@ApplicationScoped
public class TournamentService {

    private static final Logger log = LoggerFactory.getLogger(TournamentService.class);

    // winnerSide accepts "home"/"away" (WT OVR) or "BLUE"/"RED"
    @Transactional
    public void advance(String matchNumber, String winnerSide) {
        Match match = Match.findById(matchNumber);
        if (match == null || match.nextMatchNumber == null) return;

        boolean isHome = "home".equalsIgnoreCase(winnerSide) || "BLUE".equalsIgnoreCase(winnerSide);
        Athlete winner = isHome ? match.blueAthlete : match.redAthlete;
        if (winner == null) {
            log.warn("Null winner in match {}", matchNumber);
            return;
        }

        Match next = Match.findById(match.nextMatchNumber);
        if (next == null) {
            log.warn("Next match not found: {}", match.nextMatchNumber);
            return;
        }

        if ("BLUE".equalsIgnoreCase(match.nextMatchColor)) {
            next.blueAthlete = winner;
        } else {
            next.redAthlete = winner;
        }

        log.info("Winner {} advances to {} as {}",
                winner.scoreboardName, match.nextMatchNumber, match.nextMatchColor);
    }
}
