package pazos.tkStrike.resource;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.tkStrike.entity.Athlete;
import pazos.tkStrike.entity.Category;
import pazos.tkStrike.entity.Match;
import pazos.tkStrike.entity.MatchEvent;
import pazos.tkStrike.service.MatchStateService;

@Path("/admin")
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    @Inject
    MatchStateService matchStateService;

    @DELETE
    @Path("/reset")
    @Transactional
    public Response reset() {
        MatchEvent.deleteAll();
        Match.deleteAll();
        Athlete.deleteAll();
        Category.deleteAll();
        log.info("Base de datos vaciada");
        return Response.noContent().build();
    }

    @GET
    @Path("/reload")
    @Transactional
    public Response reload() {
        MatchEvent.deleteAll();
        Match.deleteAll();
        Athlete.deleteAll();
        Category.deleteAll();
        long total = matchStateService.cargarCsv();
        log.info("Recarga completada — {} combates", total);
        return Response.ok("{\"combates\":" + total + "}").build();
    }
}
