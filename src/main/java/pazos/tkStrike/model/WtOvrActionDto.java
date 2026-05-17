package pazos.tkStrike.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO que representa o JSON:API que TKStrike envía a
 * POST /matches/{id}/actions
 *
 * O JSON ten esta estrutura:
 * {
 *   "data": {
 *     "type": "match-actions",
 *     "attributes": {
 *       "action": "MATCH_LOADED",   ← enum Action como String
 *       "round": 1,
 *       "roundTime": "02:00",
 *       "position": 1,
 *       "score": { "home": 0, "away": 0 },
 *       "penalties": { "home": 0, "away": 0 },
 *       "hitlevel": null,
 *       "source": null,             ← enum ActionSource como String ou null
 *       "description": "MATCH_LOADED",
 *       "timestamp": "2026-05-17T..."
 *     },
 *     "relationships": {
 *       "match": { "data": { "type": "matches", "id": "M001" } },
 *       "homeCompetitor": { "data": { "type": "competitors", "id": "home-M001" } },
 *       "awayCompetitor": { "data": { "type": "competitors", "id": "away-M001" } }
 *     }
 *   }
 * }
 *
 * Enums Action confirmados no código fonte:
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
 *
 * Enums ActionSource: HOME, AWAY, CR
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WtOvrActionDto {

    private WtOvrDataDto data;

    public WtOvrDataDto getData() { return data; }
    public void setData(WtOvrDataDto v) { this.data = v; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WtOvrDataDto {
        private String type;
        private String id;
        private AttributesDto attributes;

        public String getType() { return type; }
        public void setType(String v) { this.type = v; }
        public String getId() { return id; }
        public void setId(String v) { this.id = v; }
        public AttributesDto getAttributes() { return attributes; }
        public void setAttributes(AttributesDto v) { this.attributes = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttributesDto {
        private String      action;      // enum Action como String
        private Integer     hitlevel;
        private Integer     round;
        private String      roundTime;   // "02:00" formato mm:ss
        private Integer     position;
        private ScoreDto    score;
        private ScoreDto    penalties;
        private String      description;
        private String      source;      // enum ActionSource como String ou null
        private String      timestamp;

        public String getAction() { return action; }
        public void setAction(String v) { this.action = v; }
        public Integer getHitlevel() { return hitlevel; }
        public void setHitlevel(Integer v) { this.hitlevel = v; }
        public Integer getRound() { return round; }
        public void setRound(Integer v) { this.round = v; }
        public String getRoundTime() { return roundTime; }
        public void setRoundTime(String v) { this.roundTime = v; }
        public Integer getPosition() { return position; }
        public void setPosition(Integer v) { this.position = v; }
        public ScoreDto getScore() { return score; }
        public void setScore(ScoreDto v) { this.score = v; }
        public ScoreDto getPenalties() { return penalties; }
        public void setPenalties(ScoreDto v) { this.penalties = v; }
        public String getDescription() { return description; }
        public void setDescription(String v) { this.description = v; }
        public String getSource() { return source; }
        public void setSource(String v) { this.source = v; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String v) { this.timestamp = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScoreDto {
        private Integer home;
        private Integer away;

        public Integer getHome() { return home; }
        public void setHome(Integer v) { this.home = v; }
        public Integer getAway() { return away; }
        public void setAway(Integer v) { this.away = v; }
    }
}
