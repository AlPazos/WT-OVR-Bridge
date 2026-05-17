package pazos.tkStrike.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket local para pantallas e tablets do pavillón.
 * URL: ws://192.168.1.50:8080/ws/pista/{ring}
 */
@ServerEndpoint("/ws/pista/{ring}")
@ApplicationScoped
public class PavillonWebSocket {

    private static final Logger log = LoggerFactory.getLogger(PavillonWebSocket.class);

    private final Map<String, Set<Session>> sesionsPorPista = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("ring") String ring) {
        sesionsPorPista.computeIfAbsent(ring, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("WebSocket conectado — pista {} (total: {})", ring, sesionsPorPista.get(ring).size());
    }

    @OnClose
    public void onClose(Session session, @PathParam("ring") String ring) {
        Set<Session> sesions = sesionsPorPista.get(ring);
        if (sesions != null) sesions.remove(session);
        log.info("WebSocket desconectado — pista {}", ring);
    }

    @OnError
    public void onError(Session session, @PathParam("ring") String ring, Throwable t) {
        log.error("Erro WebSocket pista {}: {}", ring, t.getMessage());
        Set<Session> sesions = sesionsPorPista.get(ring);
        if (sesions != null) sesions.remove(session);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("ring") String ring) {
        // Os clientes só escoitan, non envían
    }

    public void broadcast(String ring, String message) {
        Set<Session> sesions = sesionsPorPista.get(ring);
        if (sesions == null || sesions.isEmpty()) return;
        sesions.removeIf(s -> !s.isOpen());
        sesions.forEach(s -> {
            try {
                s.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                log.error("Erro ao enviar WebSocket pista {}", ring, e);
            }
        });
    }
}
