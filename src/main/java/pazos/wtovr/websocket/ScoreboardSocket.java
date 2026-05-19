package pazos.wtovr.websocket;

import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/mats/{mat}")
public class ScoreboardSocket {

    @Inject
    ScoreboardBroadcaster broadcaster;

    @OnOpen
    public void onOpen(Session session, @PathParam("mat") String mat) {
        broadcaster.register(mat, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("mat") String mat) {
        broadcaster.unregister(mat, session);
    }

    @OnError
    public void onError(Session session, @PathParam("mat") String mat, Throwable t) {
        broadcaster.unregister(mat, session);
    }
}