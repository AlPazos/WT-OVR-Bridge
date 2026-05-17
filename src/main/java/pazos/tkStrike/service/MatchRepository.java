package pazos.tkStrike.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.tkStrike.model.MatchConfigurationDto;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Repositorio de combates - permite buscar combates por diferentes criterios
 */
public class MatchRepository {

    private static final Logger log = LoggerFactory.getLogger(MatchRepository.class);

    private final List<MatchConfigurationDto> combates;
    private final Map<String, Integer> nextIndexPorPista = new HashMap<>();

    public MatchRepository(List<MatchConfigurationDto> combates) {
        this.combates = combates;
    }

    /**
     * Obtiene el siguiente combate disponible para una pista
     * Llamado por GET /matches?filter[mat]={ring}
     */
    public MatchConfigurationDto getNextMatch(String ring) {
        int mat = Integer.parseInt(ring);
        int idx = nextIndexPorPista.getOrDefault(ring, 0);

        // Busca el siguiente combate no completado de esta pista
        for (int i = idx; i < combates.size(); i++) {
            MatchConfigurationDto dto = combates.get(i);
            if (dto.getMat() != null && dto.getMat() == mat) {
                nextIndexPorPista.put(ring, i + 1);
                log.info("Pista {} — seguinte combate: {}", ring, dto.getMatchNumber());
                return dto;
            }
        }
        log.warn("Pista {} — sen máis combates dispoñibles", ring);
        return null;
    }

    /**
     * Busca combate por número de combate
     */
    public MatchConfigurationDto getByMatchNumber(String matchNumber) {
        return combates.stream()
                .filter(d -> matchNumber.equals(d.getMatchNumber()))
                .findFirst().orElse(null);
    }

    /**
     * Busca combate por ID de competidor
     */
    public MatchConfigurationDto getByCompetitorId(String competitorId) {
        return combates.stream()
                .filter(d -> competitorId.equals(MatchIdentifierUtils.competitorId(d, true))
                        || competitorId.equals(MatchIdentifierUtils.competitorId(d, false)))
                .findFirst().orElse(null);
    }

    /**
     * Obtiene el atleta por ID de participante
     */
    public MatchConfigurationDto.AthleteDto getAthleteByParticipantId(String participantId) {
        for (MatchConfigurationDto dto : combates) {
            if (participantId.equals(MatchIdentifierUtils.participantId(dto, true))) {
                return dto.getBlueAthlete();
            }
            if (participantId.equals(MatchIdentifierUtils.participantId(dto, false))) {
                return dto.getRedAthlete();
            }
        }
        return null;
    }

    /**
     * Busca combate por ID de evento
     */
    public MatchConfigurationDto getByEventId(String eventId) {
        return combates.stream()
                .filter(d -> eventId.equals(MatchIdentifierUtils.eventId(d)))
                .findFirst().orElse(null);
    }

    /**
     * Busca combate por ID de configuración
     */
    public MatchConfigurationDto getByConfigId(String configId) {
        return combates.stream()
                .filter(d -> configId.equals(MatchIdentifierUtils.configId(d)))
                .findFirst().orElse(null);
    }

    /**
     * Reinicia los índices de búsqueda
     */
    public void reset() {
        nextIndexPorPista.clear();
    }
}

