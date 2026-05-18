package pazos.tkStrike.resource;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.tkStrike.entity.Match;
import pazos.tkStrike.entity.MatchEvent;
import pazos.tkStrike.model.MatchConfigurationDto;
import pazos.tkStrike.model.MatchResultDto;
import pazos.tkStrike.model.TkStrikeEventDto;
import pazos.tkStrike.service.MatchStateService;

@Path("/{ring}/events-listener")
public class EventsListenerResource {

    private static final Logger log = LoggerFactory.getLogger(EventsListenerResource.class);

    @Inject
    MatchStateService matchStateService;

    @GET
    @Path("/ping")
    public Response ping() {
        log.debug("Ping de TKStrike");
        return Response.ok().build();
    }

    @POST
    @Path("/new-match-configured")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newMatchConfigured(MatchConfigurationDto dto,
                                       @PathParam("ring") String ring) {
        if (dto == null) return Response.status(400).build();
        log.info("Pista {} — combate configurado: {}", ring, dto.getMatchNumber());
        return Response.ok().build();
    }

    @POST
    @Path("/new-match-event")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response newMatchEvent(TkStrikeEventDto dto,
                                  @PathParam("ring") String ring) {
        if (dto == null || dto.getEventType() == null) return Response.status(400).build();

        Match match = Match.findById(dto.getMatchNumber());
        if (match == null) {
            log.warn("Pista {} — combate no encontrado: {}", ring, dto.getMatchNumber());
            return Response.status(404).build();
        }

        new MatchEvent(match, dto.getRoundNumber(), dto.getRoundTimestamp(), dto.getEventType(),
                dto.getBluePoints(), dto.getBluePenalties(),
                dto.getRedPoints(), dto.getRedPenalties()).persist();

        log.debug("Evento guardado: {} — Pista {} — Round {} — {}/{} vs {}/{}",
                dto.getEventType(), ring, dto.getRoundNumber(),
                dto.getBluePoints(), dto.getBluePenalties(),
                dto.getRedPoints(), dto.getRedPenalties());

        return Response.ok().build();
    }

    @POST
    @Path("/match-result")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response matchResult(MatchResultDto dto,
                                @PathParam("ring") String ring) {
        if (dto == null) return Response.status(400).build();
        log.info("Pista {} - Resultado: gañador={} decisión={}", ring, dto.getMatchWinnerColor(), dto.getMatchFinalDecision());
        return Response.ok().build();
    }
}