package pazos.tkStrike.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import pazos.tkStrike.entity.Match;
import java.util.List;

/**
 * Repository para combates usando Panache
 */
@ApplicationScoped
public class MatchRepository implements PanacheRepository<Match> {

    /**
     * Busca un combate por número de combate
     */
    public Match findByMatchNumber(String matchNumber) {
        return find("matchNumber", matchNumber).firstResult();
    }

    /**
     * Busca combates por número de pista/mat
     */
    public List<Match> findByMat(Integer mat) {
        return find("mat", mat).list();
    }

    /**
     * Busca combates por fase
     */
    public List<Match> findByPhase(String phase) {
        return find("phase", phase).list();
    }

    /**
     * Obtiene el siguiente combate disponible para una pista
     */
    public Match getNextMatchForMat(Integer mat) {
        return find("mat = ?1 ORDER BY matchNumber ASC", mat).firstResult();
    }

    /**
     * Obtiene TODOS los combates de una pista
     */
    public List<Match> findAllByMat(Integer mat) {
        return find("mat", mat).list();
    }

    /**
     * Cuenta combates en una pista
     */
    public long countByMat(Integer mat) {
        return find("mat", mat).count();
    }
}

