package pazos.tkStrike.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import pazos.tkStrike.entity.Athlete;

/**
 * Repository para agratletas usando Panache
 */
@ApplicationScoped
public class AthleteRepository implements PanacheRepository<Athlete> {

    /**
     * Busca un atleta por su ID interno de OVR
     */
    public Athlete findByOvrInternalId(String ovrInternalId) {
        return find("ovrInternalId", ovrInternalId).firstResult();
    }

    /**
     * Busca un atleta por su ID de WF
     */
    public Athlete findByWfId(String wfId) {
        return find("wfId", wfId).firstResult();
    }
}

