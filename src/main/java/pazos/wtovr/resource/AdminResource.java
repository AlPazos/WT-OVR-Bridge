package pazos.wtovr.resource;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.wtovr.entity.Athlete;
import pazos.wtovr.entity.Match;
import pazos.wtovr.entity.MatchEvent;

@Path("/admin")
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    @DELETE
    @Path("/reset")
    @Transactional
    public Response reset() {
        MatchEvent.deleteAll();
        Match.deleteAll();
        Athlete.deleteAll();
        log.info("Base de datos vaciada");
        return Response.noContent().build();
    }
}
