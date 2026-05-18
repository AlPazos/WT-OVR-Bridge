package pazos.tkStrike.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.tkStrike.entity.Athlete;
import pazos.tkStrike.entity.Match;
import pazos.tkStrike.model.MatchResultDto;

@ApplicationScoped
public class TournamentService {

    private static final Logger log = LoggerFactory.getLogger(TournamentService.class);

    @Transactional
    public void advance(MatchResultDto dto) {
        Match match = Match.findById(dto.getMatchNumber());
        if (match == null || match.nextMatchNumber == null) return;

        Athlete winner = "BLUE".equalsIgnoreCase(dto.getMatchWinnerColor())
                ? match.blueAthlete : match.redAthlete;
        if (winner == null) {
            log.warn("Ganador nulo en combate {}", dto.getMatchNumber());
            return;
        }

        Match next = Match.findById(match.nextMatchNumber);
        if (next == null) {
            log.warn("Combate siguiente no encontrado: {}", match.nextMatchNumber);
            return;
        }

        if ("BLUE".equalsIgnoreCase(match.nextMatchColor)) {
            next.blueAthlete = winner;
        } else {
            next.redAthlete = winner;
        }

        log.info("Ganador {} ({}) avanza a {} como {}",
                winner.scoreboardName, dto.getMatchWinnerColor(),
                match.nextMatchNumber, match.nextMatchColor);
    }
}
