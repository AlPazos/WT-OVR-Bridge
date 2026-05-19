package pazos.wtovr.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import pazos.wtovr.service.MatchStateService;

@Path("/manager")
public class ManagerResource {
    @Inject
    MatchStateService matchStateService;

    @GET
    @Path("/matches")
    @Produces("application/json")
    public Response getMatches() {
        return Response.ok(matchStateService.getIndistinctMatches()).build();
    }
}
