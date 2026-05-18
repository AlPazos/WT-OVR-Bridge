package pazos.tkStrike.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for the JSON:API payload TKStrike sends to POST /matches/{id}/actions.
 * <p>
 * JSON structure:
 * {
 * "data": {
 * "type": "match-actions",
 * "attributes": {
 * "action": "MATCH_LOADED",   ← Action enum as String
 * "round": 1,
 * "roundTime": "02:00",
 * "position": 1,
 * "score": { "home": 0, "away": 0 },
 * "penalties": { "home": 0, "away": 0 },
 * "hitlevel": null,
 * "source": null, ← ActionSource enum as String or null
 * "description": "MATCH_LOADED",
 * "timestamp": "2026-05-17T..."
 * },
 * "relationships": {
 * "match": { "data": { "type": "matches", "id": "M001" } },
 * "homeCompetitor": { "data": { "type": "competitors", "id": "home-M001" } },
 * "awayCompetitor": { "data": { "type": "competitors", "id": "away-M001" } }
 * }
 * }
 * }
 * <p>
 * Action enum values confirmed from source:
 * MATCH_LOADED, MATCH_START, ROUND_START, MATCH_TIME, MATCH_TIMEOUT,
 * MATCH_RESUME, ROUND_END, MATCH_END,
 * SCORE_HOME_PUNCH, SCORE_HOME_KICK, SCORE_HOME_TKICK, SCORE_HOME_SKICK,
 * SCORE_HOME_HEAD, SCORE_HOME_THEAD,
 * PENALTY_HOME, PENALTY_AWAY,
 * SCORE_AWAY_PUNCH, SCORE_AWAY_KICK, SCORE_AWAY_TKICK, SCORE_AWAY_SKICK,
 * SCORE_AWAY_HEAD, SCORE_AWAY_THEAD,
 * INVALIDATE_SCORE, ADJUST_SCORE, ADJUST_PENALTY,
 * INVALIDATE_SCORE_HOME_PUNCH, INVALIDATE_SCORE_HOME_KICK, ...
 * VR_HOME_REQUEST, VR_HOME_ACCEPTED, VR_HOME_REJECTED,
 * VR_AWAY_REQUEST, VR_AWAY_ACCEPTED, VR_AWAY_REJECTED
 * <p>
 * ActionSource enum values: HOME, AWAY, CR
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WtOvrActionDto {

    public enum Action {
        MATCH_LOADED, MATCH_START, MATCH_END,
        ROUND_START, ROUND_END,
        MATCH_TIME, MATCH_TIMEOUT, MATCH_RESUME,
        SCORE_HOME_PUNCH, SCORE_HOME_KICK, SCORE_HOME_TKICK, SCORE_HOME_SKICK,
        SCORE_HOME_HEAD, SCORE_HOME_THEAD,
        SCORE_AWAY_PUNCH, SCORE_AWAY_KICK, SCORE_AWAY_TKICK, SCORE_AWAY_SKICK,
        SCORE_AWAY_HEAD, SCORE_AWAY_THEAD,
        PENALTY_HOME, PENALTY_AWAY,
        INVALIDATE_SCORE, ADJUST_SCORE, ADJUST_PENALTY,
        INVALIDATE_SCORE_HOME_PUNCH, INVALIDATE_SCORE_HOME_KICK,
        INVALIDATE_SCORE_AWAY_PUNCH, INVALIDATE_SCORE_AWAY_KICK,
        VR_HOME_REQUEST, VR_HOME_ACCEPTED, VR_HOME_REJECTED,
        VR_AWAY_REQUEST, VR_AWAY_ACCEPTED, VR_AWAY_REJECTED,

        @JsonEnumDefaultValue
        UNKNOWN
    }

    private WtOvrDataDto data;

    public WtOvrDataDto getData() {
        return data;
    }

    public void setData(WtOvrDataDto v) {
        this.data = v;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WtOvrDataDto {
        private String type;
        private String id;
        private AttributesDto attributes;

        public String getType() {
            return type;
        }

        public void setType(String v) {
            this.type = v;
        }

        public String getId() {
            return id;
        }

        public void setId(String v) {
            this.id = v;
        }

        public AttributesDto getAttributes() {
            return attributes;
        }

        public void setAttributes(AttributesDto v) {
            this.attributes = v;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttributesDto {
        private Action action;
        private Integer hitlevel;
        private Integer round;
        private String roundTime;   // "mm:ss" format
        private Integer position;
        private ScoreDto score;
        private ScoreDto penalties;
        private String description;
        private String source;      // ActionSource enum as String or null
        private String timestamp;

        public Action getAction() {
            return action;
        }

        public void setAction(Action v) {
            this.action = v;
        }

        public Integer getHitlevel() {
            return hitlevel;
        }

        public void setHitlevel(Integer v) {
            this.hitlevel = v;
        }

        public Integer getRound() {
            return round;
        }

        public void setRound(Integer v) {
            this.round = v;
        }

        public String getRoundTime() {
            return roundTime;
        }

        public void setRoundTime(String v) {
            this.roundTime = v;
        }

        public Integer getPosition() {
            return position;
        }

        public void setPosition(Integer v) {
            this.position = v;
        }

        public ScoreDto getScore() {
            return score;
        }

        public void setScore(ScoreDto v) {
            this.score = v;
        }

        public ScoreDto getPenalties() {
            return penalties;
        }

        public void setPenalties(ScoreDto v) {
            this.penalties = v;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String v) {
            this.description = v;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String v) {
            this.source = v;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String v) {
            this.timestamp = v;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScoreDto {
        private Integer home;
        private Integer away;

        public Integer getHome() {
            return home;
        }

        public void setHome(Integer v) {
            this.home = v;
        }

        public Integer getAway() {
            return away;
        }

        public void setAway(Integer v) {
            this.away = v;
        }
    }
}
