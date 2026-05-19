package pazos.wtovr.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ScoreboardBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(ScoreboardBroadcaster.class);

    private final Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();

    public void register(String matchId, Session session) {
        sessions.computeIfAbsent(matchId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(session);
        log.info("WS connected — match={} session={}", matchId, session.getId());
    }

    public void unregister(String matchId, Session session) {
        Set<Session> set = sessions.get(matchId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) sessions.remove(matchId);
        }
        log.info("WS disconnected — match={} session={}", matchId, session.getId());
    }

    public void broadcast(String matchId, String payload) {
        Set<Session> set = sessions.getOrDefault(matchId, Collections.emptySet());
        for (Session s : set) {
            if (s.isOpen()) {
                s.getAsyncRemote().sendText(payload);
            }
        }
    }
}