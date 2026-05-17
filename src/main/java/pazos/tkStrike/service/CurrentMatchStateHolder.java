package pazos.tkStrike.service;

import pazos.tkStrike.entity.Match;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona el estado actual del combate por cada pista (ring)
 * Compatibilidad temporal: usa Match pero debería ser MatchConfigurationDto
 */
public class CurrentMatchStateHolder {

    // Combate actualmente cargado en TKStrike por pista
    private final Map<String, Match> combateActual = new HashMap<>();

    /**
     * Obtiene el combate actual para una pista
     */
    public Match get(String ring) {
        return combateActual.get(ring);
    }

    /**
     * Establece el combate actual para una pista
     */
    public void set(String ring, Match match) {
        combateActual.put(ring, match);
    }

    /**
     * Limpia el combate actual de una pista
     */
    public void clear(String ring) {
        combateActual.remove(ring);
    }

    /**
     * Limpia todos los combates actuales
     */
    public void clearAll() {
        combateActual.clear();
    }
}

