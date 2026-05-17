package pazos.tkStrike.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import pazos.tkStrike.model.MatchConfigurationDto;
import pazos.tkStrike.model.MatchResultDto;
import pazos.tkStrike.service.MatchStateService;
import pazos.tkStrike.websocket.PavillonWebSocket;

@Path("/venue-management")
public class VenueManagementResource {

    private static final Logger log = LoggerFactory.getLogger(VenueManagementResource.class);

    @Inject
    MatchStateService matchStateService;
    @Inject
    PavillonWebSocket ws;

    private final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {
        return Response.ok("{\"response\":true}").build();
    }

    /**
     * TKStrike pide o seguinte combate.
     * Devolve o seguinte combate dispoñible na cola en memoria para esa pista.
     */
    @GET
    @Path("/{ringNumber}/next-match")
    @Produces(MediaType.APPLICATION_JSON)
    public Response nextMatch(@PathParam("ringNumber") String ringNumber) {
        log.info("Pista {} — TKStrike pide seguinte combate", ringNumber);

        MatchConfigurationDto dto = matchStateService.getNextCombate(ringNumber);
        if (dto == null) {
            log.warn("Pista {} — sen combate en memoria", ringNumber);
            return Response.status(204).build();
        }

        return Response.ok(dto).build();
    }

    @GET
    @Path("/{ringNumber}/prev-match")
    @Produces(MediaType.APPLICATION_JSON)
    public Response prevMatch(@PathParam("ringNumber") String ringNumber) {
        MatchConfigurationDto dto = matchStateService.getCombateActual(ringNumber);
        if (dto == null) return Response.status(204).build();
        return Response.ok(dto).build();
    }

    @POST
    @Path("/{ringNumber}/match-result")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response matchResult(
            @PathParam("ringNumber") String ringNumber,
            MatchResultDto dto) {

        if (dto == null) return Response.status(400).build();

        log.info("Pista {} — resultado recibido por VenueManagement: gañador={}", 
                ringNumber, dto.getMatchWinnerColor());

        try {
            String json = String.format("{\"type\":\"MATCH_RESULT\",\"ring\":\"%s\",\"data\":%s}",
                    ringNumber, mapper.writeValueAsString(dto));
            ws.broadcast(ringNumber, json);
        } catch (Exception e) {
            log.error("Erro ao serializar resultado", e);
        }

        return Response.ok("{\"response\":true}").build();
    }
}
