package pazos.tkStrike.service;

import pazos.tkStrike.model.MatchConfigurationDto;

/**
 * Utilidades para generar y manipular identificadores de competidores y eventos
 */
public class MatchIdentifierUtils {

    /**
     * Genera el ID del competidor (azul o rojo) para un combate
     */
    public static String competitorId(MatchConfigurationDto dto, boolean isBlue) {
        var athlete = isBlue ? dto.getBlueAthlete() : dto.getRedAthlete();
        if (athlete != null && athlete.getOvrInternalId() != null) {
            return athlete.getOvrInternalId();
        }
        return (isBlue ? "home" : "away") + "-" + dto.getMatchNumber();
    }

    /**
     * Genera el ID de participante (competidor con sufijo "-part")
     */
    public static String participantId(MatchConfigurationDto dto, boolean isBlue) {
        return competitorId(dto, isBlue) + "-part";
    }

    /**
     * Genera el ID de evento
     */
    public static String eventId(MatchConfigurationDto dto) {
        return "event-" + dto.getMatchNumber();
    }

    /**
     * Genera el ID de configuración
     */
    public static String configId(MatchConfigurationDto dto) {
        return "config-" + dto.getMatchNumber();
    }
}

