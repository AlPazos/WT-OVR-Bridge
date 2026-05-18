package pazos.tkStrike.service;

import pazos.tkStrike.model.MatchConfigurationDto;

public class MatchIdentifierUtils {

    public static String competitorId(MatchConfigurationDto dto, boolean isBlue) {
        var athlete = isBlue ? dto.getBlueAthlete() : dto.getRedAthlete();
        if (athlete != null && athlete.getOvrInternalId() != null) {
            return athlete.getOvrInternalId();
        }
        return (isBlue ? "home" : "away") + "-" + dto.getMatchNumber();
    }

    public static String participantId(MatchConfigurationDto dto, boolean isBlue) {
        return competitorId(dto, isBlue) + "-part";
    }

    public static String eventId(MatchConfigurationDto dto) {
        return "event-" + dto.getMatchNumber();
    }

    public static String configId(MatchConfigurationDto dto) {
        return "config-" + dto.getMatchNumber();
    }
}

