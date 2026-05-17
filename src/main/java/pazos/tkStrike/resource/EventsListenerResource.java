package pazos.tkStrike.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.tkStrike.model.MatchConfigurationDto;
import pazos.tkStrike.model.MatchResultDto;
import pazos.tkStrike.model.TkStrikeEventDto;
import pazos.tkStrike.service.MatchStateService;
import pazos.tkStrike.service.RoundClockManager;
import pazos.tkStrike.websocket.PavillonWebSocket;

@Path("/events-listener")
public class EventsListenerResource {

    private static final Logger log = LoggerFactory.getLogger(EventsListenerResource.class);

    @Inject
    MatchStateService matchStateService;
    @Inject
    RoundClockManager clockManager;
    @Inject
    PavillonWebSocket ws;

    private final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("/ping")
    public Response ping() {
        log.debug("Ping de TKStrike");
        return Response.ok().build();
    }

    @POST
    @Path("/new-match-configured")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newMatchConfigured(MatchConfigurationDto dto) {
        if (dto == null) return Response.status(400).build();

        String ring = String.valueOf(dto.getMat());
        log.info("Pista {} — combate configurado: {}", ring, dto.getMatchNumber());

        matchStateService.setCombateActual(ring, dto);
        broadcast(ring, "MATCH_CONFIGURED", dto);

        return Response.ok().build();
    }

    @POST
    @Path("/new-match-event")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newMatchEvent(TkStrikeEventDto dto) {
        if (dto == null || dto.getEventType() == null) return Response.status(400).build();

        String ring = dto.getMatchVMRingNumber() != null ? dto.getMatchVMRingNumber() : "1";
        log.debug("Pista {} — evento: {}", ring, dto.getEventType());

        procesarReloxo(ring, dto);
        broadcast(ring, "MATCH_EVENT", dto);

        return Response.ok().build();
    }

    @POST
    @Path("/match-result")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response matchResult(MatchResultDto dto) {
        if (dto == null) return Response.status(400).build();

        String ring = dto.getVmRingNumber() != null ? dto.getVmRingNumber() : "1";
        log.info("Pista {} — resultado: gañador={} decisión={}", ring,
                dto.getMatchWinnerColor(), dto.getMatchFinalDecision());

        clockManager.reset(ring);
        broadcast(ring, "MATCH_RESULT", dto);
        matchStateService.clear(ring);

        return Response.ok().build();
    }

    // ─── Privados ─────────────────────────────────────────────────────────────

    private void procesarReloxo(String ring, TkStrikeEventDto dto) {
        String timeStr = dto.getRoundTimestampStr();
        switch (dto.getEventType()) {
            case "START_ROUND"                 -> clockManager.start(ring, timeStr != null ? timeStr : "02:00");
            case "TIMEOUT"                     -> clockManager.stop(ring, timeStr != null ? timeStr : "00:00");
            case "RESUME"                      -> clockManager.resume(ring, timeStr);
            case "ROUNDCOUNTDOWN_CHANGE",
                 "GOLDENPOINTCOUNTDOWN_CHANGE" -> clockManager.sync(ring, timeStr);
            case "END_ROUND",
                 "MATCH_FINISHED"              -> clockManager.reset(ring);
            default                            -> { /* golpes, penalizacións... non afectan ao reloxo */ }
        }
    }

    private void broadcast(String ring, String type, Object payload) {
        try {
            String json = String.format("{\"type\":\"%s\",\"ring\":\"%s\",\"data\":%s}",
                    type, ring, mapper.writeValueAsString(payload));
            ws.broadcast(ring, json);
        } catch (Exception e) {
            log.error("Erro ao serializar para WebSocket", e);
        }
    }
}
