package pazos.tkStrike.service;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.tkStrike.websocket.PavillonWebSocket;

import java.util.Map;
import java.util.concurrent.*;

@ApplicationScoped
public class RoundClockManager {

    private static final Logger log = LoggerFactory.getLogger(RoundClockManager.class);

    @Inject
    PavillonWebSocket ws;

    private final Map<String, ScheduledExecutorService> schedulers = new ConcurrentHashMap<>();
    private final Map<String, Integer>                  segundos   = new ConcurrentHashMap<>();

    public void start(String ring, String timeStr) {
        segundos.put(ring, parseSeconds(timeStr));
        stopScheduler(ring);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        schedulers.put(ring, scheduler);

        scheduler.scheduleAtFixedRate(() -> {
            int remaining = segundos.merge(ring, -1, Integer::sum);
            ws.broadcast(ring, clockMsg(ring, "RUNNING", formatTime(remaining)));
            if (remaining <= 0) {
                stopScheduler(ring);
                ws.broadcast(ring, clockMsg(ring, "FINISHED", "00:00"));
            }
        }, 1, 1, TimeUnit.SECONDS);

        log.info("Pista {} — reloxo iniciado en {}", ring, timeStr);
    }

    public void stop(String ring, String timeStr) {
        stopScheduler(ring);
        segundos.put(ring, parseSeconds(timeStr));
        ws.broadcast(ring, clockMsg(ring, "STOPPED", timeStr));
        log.info("Pista {} — reloxo parado en {}", ring, timeStr);
    }

    public void resume(String ring, String timeStr) {
        if (timeStr != null) segundos.put(ring, parseSeconds(timeStr));
        start(ring, formatTime(segundos.getOrDefault(ring, 0)));
    }

    public void sync(String ring, String timeStr) {
        segundos.put(ring, parseSeconds(timeStr));
    }

    public void reset(String ring) {
        stopScheduler(ring);
        segundos.put(ring, 0);
        ws.broadcast(ring, clockMsg(ring, "RESET", "00:00"));
    }

    private void stopScheduler(String ring) {
        ScheduledExecutorService old = schedulers.remove(ring);
        if (old != null) old.shutdownNow();
    }

    private int parseSeconds(String timeStr) {
        if (timeStr == null || !timeStr.contains(":")) return 0;
        try {
            String[] p = timeStr.split(":");
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) { return 0; }
    }

    private String formatTime(int secs) {
        int s = Math.max(0, secs);
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    private String clockMsg(String ring, String action, String time) {
        return String.format("{\"type\":\"CLOCK\",\"ring\":\"%s\",\"action\":\"%s\",\"time\":\"%s\"}",
                ring, action, time);
    }
}
