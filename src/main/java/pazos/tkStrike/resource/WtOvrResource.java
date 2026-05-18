package pazos.tkStrike.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.tkStrike.entity.Match;
import pazos.tkStrike.entity.MatchEvent;
import pazos.tkStrike.model.MatchConfigurationDto;
import pazos.tkStrike.model.WtOvrActionDto;
import pazos.tkStrike.service.MatchStateService;
import pazos.tkStrike.service.TournamentService;

import java.util.List;

/**
 * WT OVR protocol (JSON:API/crnk).
 * <p>
 * TKStrike requests ?include= with: homeCompetitor, homeCompetitor.participants,
 * awayCompetitor, awayCompetitor.participants, matchConfiguration, refereeAssignment,
 * refereeAssignment.refJ1/J2/J3/CR/RJ/TA, event
 * <p>
 * Therefore included must contain:
 * - competitors (home + away) with participants as an ARRAY of refs
 * - participants (home + away) with all Participant.java fields
 * - match-configurations with timing (Strings "mm:ss"), thresholds, goldenPoint, videoReplayQuota, rules
 * - match-referee-assignments (empty when no referees assigned)
 * - events with discipline, division, gender, name, abbreviation, weightCategory
 * <p>
 * GET /participants/{id} is also needed because crnk does a lazy fetch
 * when the participant is not found in the included array.
 */
@Path("/")
public class WtOvrResource {

    private static final Logger log = LoggerFactory.getLogger(WtOvrResource.class);
    private static final String JSONAPI = "application/vnd.api+json";
    private final ObjectMapper mapper = new ObjectMapper();
    @Inject
    MatchStateService matchStateService;

    @Inject
    TournamentService tournamentService;

    // ── /status ──────────────────────────────────────────────────────────────
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status() {
        return Response.ok("{\"status\":\"ok\"}").build();
    }

    // ── GET /matches ─────────────────────────────────────────────────────────
    @GET
    @Path("/matches")
    @Produces(JSONAPI)
    public Response getMatches(
            @QueryParam("filter[status]") String status,
            @QueryParam("filter[mat]") Integer mat,
            @QueryParam("include") String include) {

        log.info("WT OVR GET /matches — mat={}", mat);
        String ring = mat != null ? String.valueOf(mat) : "1";

        List<MatchConfigurationDto> dtos = matchStateService.getAllCombates(ring, status);

        if (dtos.isEmpty()) {
            return Response.ok("{\"data\":[],\"included\":[]}").type(JSONAPI).build();
        }
        return Response.ok(buildListResponseMultiple(dtos, mat)).type(JSONAPI).build();
    }

    // ── GET /matches/{id} ────────────────────────────────────────────────────
    @GET
    @Path("/matches/{id}")
    @Produces(JSONAPI)
    public Response getMatch(@PathParam("id") String id) {
        log.info("WT OVR GET /matches/{}", id);
        Match match = Match.findById(id);
        if (match == null) {
            return Response.status(404)
                    .entity("{\"errors\":[{\"status\":\"404\",\"title\":\"Not found\"}]}")
                    .type(JSONAPI).build();
        }
        return Response.ok(buildSingleResponse(matchStateService.convertMatchToDto(match))).type(JSONAPI).build();
    }

    // ── GET /participants/{id} ───────────────────────────────────────────────
    @GET
    @Path("/participants/{id}")
    @Produces(JSONAPI)
    public Response getParticipant(@PathParam("id") String id) {
        log.info("WT OVR GET /participants/{}", id);

        MatchConfigurationDto.AthleteDto athlete = matchStateService.getAthleteByParticipantId(id);

        ObjectNode root = mapper.createObjectNode();
        root.set("data", buildParticipantNode(id, athlete));
        return Response.ok(root.toString()).type(JSONAPI).build();
    }

    // ── GET /competitors/{id} ────────────────────────────────────────────────
    @GET
    @Path("/competitors/{id}")
    @Produces(JSONAPI)
    public Response getCompetitor(@PathParam("id") String id) {
        log.info("WT OVR GET /competitors/{}", id);
        MatchConfigurationDto dto = matchStateService.getCombateByCompetitorId(id);
        boolean isHome = dto != null && id.equals(competitorId(dto, true));
        MatchConfigurationDto.AthleteDto athlete = dto != null
                ? (isHome ? dto.getBlueAthlete() : dto.getRedAthlete()) : null;
        String partId = id + "-part";

        ObjectNode root = mapper.createObjectNode();
        root.set("data", buildCompetitorNode(id, athlete, partId));
        return Response.ok(root.toString()).type(JSONAPI).build();
    }

    // ── GET /events/{id} ─────────────────────────────────────────────────────
    @GET
    @Path("/events/{id}")
    @Produces(JSONAPI)
    public Response getEvent(@PathParam("id") String id) {
        log.info("WT OVR GET /events/{}", id);
        String matchId = id.replace("event-", "");
        Match match = Match.findById(matchId);
        MatchConfigurationDto dto = match != null ? matchStateService.convertMatchToDto(match) : null;
        ObjectNode root = mapper.createObjectNode();
        root.set("data", buildEventNode(id, dto));
        return Response.ok(root.toString()).type(JSONAPI).build();
    }

    // ── GET /match-referee-assignments/{id} ──────────────────────────────────
    @GET
    @Path("/match-referee-assignments/{id}")
    @Produces(JSONAPI)
    public Response getRefereeAssignment(@PathParam("id") String id) {
        log.info("WT OVR GET /match-referee-assignments/{}", id);
        ObjectNode root = mapper.createObjectNode();
        ObjectNode data = mapper.createObjectNode();
        data.put("type", "match-referee-assignments");
        data.put("id", id);
        data.set("attributes", mapper.createObjectNode());
        data.set("relationships", mapper.createObjectNode());
        root.set("data", data);
        return Response.ok(root.toString()).type(JSONAPI).build();
    }

    // ── POST /matches/{id}/actions ───────────────────────────────────────────
    @POST
    @Path("/matches/{id}/actions")
    @Consumes(JSONAPI)
    @Produces(JSONAPI)
    @Transactional
    public Response postAction(@PathParam("id") String matchId, String body) {
        try {
            WtOvrActionDto dto = mapper.readValue(body, WtOvrActionDto.class);
            var attrs = dto.getData() != null ? dto.getData().getAttributes() : null;
            WtOvrActionDto.Action action = attrs != null ? attrs.getAction() : WtOvrActionDto.Action.UNKNOWN;
            log.info("WT OVR POST /matches/{}/actions — action={}", matchId, action);

            Match match = Match.findById(matchId);
            if (match != null && attrs != null) {
                if (action == WtOvrActionDto.Action.MATCH_START) {
                    match.status = "started";
                } else if (action == WtOvrActionDto.Action.MATCH_END) {
                    match.status = "finished";
                } else if (action != WtOvrActionDto.Action.MATCH_TIME
                        && action != WtOvrActionDto.Action.UNKNOWN) {
                    Integer bluePoints = attrs.getScore() != null ? attrs.getScore().getHome() : null;
                    Integer redPoints = attrs.getScore() != null ? attrs.getScore().getAway() : null;
                    Integer bluePenalties = attrs.getPenalties() != null ? attrs.getPenalties().getHome() : null;
                    Integer redPenalties = attrs.getPenalties() != null ? attrs.getPenalties().getAway() : null;
                    new MatchEvent(match, attrs.getRound(), null, action.name(),
                            bluePoints, bluePenalties, redPoints, redPenalties).persist();
                }
            }
        } catch (Exception e) {
            log.warn("WT OVR action parse error: {}", e.getMessage());
        }

        // TKStrike ignores the response body
        ObjectNode root = mapper.createObjectNode();
        ObjectNode data = mapper.createObjectNode();
        data.put("type", "match-actions");
        data.put("id", matchId + "-" + System.currentTimeMillis());
        data.set("attributes", mapper.createObjectNode());
        root.set("data", data);
        return Response.status(201).entity(root.toString()).type(JSONAPI).build();
    }

    // ── POST /matches/{id}/results ───────────────────────────────────────────
    @POST
    @Path("/matches/{id}/results")
    @Consumes(JSONAPI)
    @Produces(JSONAPI)
    @Transactional
    public Response postResult(@PathParam("id") String matchId, String body) {
        log.info("WT OVR POST /matches/{}/results", matchId);
        try {
            com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(body);
            com.fasterxml.jackson.databind.JsonNode winner = json.path("data").path("attributes").path("winner");
            if (!winner.isMissingNode()) {
                tournamentService.advance(matchId, winner.asText());
            }
        } catch (Exception e) {
            log.warn("WT OVR result parse error: {}", e.getMessage());
        }
        ObjectNode root = mapper.createObjectNode();
        ObjectNode data = mapper.createObjectNode();
        data.put("type", "match-results");
        data.put("id", matchId + "-result");
        data.set("attributes", mapper.createObjectNode());
        root.set("data", data);
        return Response.status(201).entity(root.toString()).type(JSONAPI).build();
    }

    // ── Construtores JSON:API ─────────────────────────────────────────────────

    private String buildListResponse(MatchConfigurationDto dto, Integer mat) {
        try {
            ObjectNode root = mapper.createObjectNode();
            ArrayNode data = mapper.createArrayNode();
            data.add(buildMatchNode(dto, mat));
            root.set("data", data);
            root.set("included", buildIncluded(dto));
            return root.toString();
        } catch (Exception e) {
            log.error("Error buildListResponse", e);
            return "{\"data\":[],\"included\":[]}";
        }
    }

    /**
     * Construye una respuesta JSON:API con MÚLTIPLES combates
     */
    private String buildListResponseMultiple(List<MatchConfigurationDto> dtos, Integer mat) {
        try {
            ObjectNode root = mapper.createObjectNode();
            ArrayNode data = mapper.createArrayNode();
            ArrayNode included = mapper.createArrayNode();

            // Agregar cada combate a data
            for (MatchConfigurationDto dto : dtos) {
                data.add(buildMatchNode(dto, mat != null ? mat : dto.getMat()));
                // Agregar todos los included de cada combate
                included.addAll(buildIncluded(dto));
            }

            root.set("data", data);
            root.set("included", included);
            return root.toString();
        } catch (Exception e) {
            log.error("Error buildListResponseMultiple", e);
            return "{\"data\":[],\"included\":[]}";
        }
    }

    private String buildSingleResponse(MatchConfigurationDto dto) {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.set("data", buildMatchNode(dto, dto.getMat()));
            root.set("included", buildIncluded(dto));
            return root.toString();
        } catch (Exception e) {
            log.error("Error buildSingleResponse", e);
            return "{\"data\":null}";
        }
    }

    /**
     * Builds the full Match node in JSON:API format.
     * Fields from Match.java: id, status, mat, number, phase (String via toString()),
     * score, penalties, round, roundTime, result.
     * Relationships: homeCompetitor, awayCompetitor, matchConfiguration,
     * refereeAssignment, event, session.
     */
    private ObjectNode buildMatchNode(MatchConfigurationDto dto, Integer mat) {
        String matchId = s(dto.getMatchNumber());
        String homeId = competitorId(dto, true);
        String awayId = competitorId(dto, false);
        String configId = "config-" + matchId;
        String eventId = "event-" + matchId;
        String refAssId = "refass-" + matchId;

        ObjectNode node = mapper.createObjectNode();
        node.put("type", "matches");
        node.put("id", matchId);

        ObjectNode attrs = mapper.createObjectNode();
        attrs.put("status", dto.getStatus() != null ? dto.getStatus() : "available");
        attrs.put("mat", mat != null ? mat : 1);
        attrs.put("number", matchId);
        // phase como String — TKStrike usa match.getPhase().toString()
        // Valores: R256, R128, R64, R32, R16, QF, SF, F, BRZ, BMC
        attrs.put("phase", s(dto.getPhase()).isEmpty() ? "SF" : dto.getPhase());
        attrs.put("round", 1);
        attrs.put("roundTime", dto.getRoundsConfig() != null
                ? dto.getRoundsConfig().getRoundTimeStr() : "02:00");

        // score e penalties — MatchScore {home, away}
        ObjectNode score = mapper.createObjectNode();
        score.put("home", 0);
        score.put("away", 0);
        attrs.set("score", score);
        ObjectNode pen = mapper.createObjectNode();
        pen.put("home", 0);
        pen.put("away", 0);
        attrs.set("penalties", pen);

        // result — MatchInternalResult {status, decision, homeType, awayType}
        ObjectNode result = mapper.createObjectNode();
        result.put("status", "LIVE");
        result.putNull("decision");
        result.putNull("homeType");
        result.putNull("awayType");
        attrs.set("result", result);

        node.set("attributes", attrs);

        // Relationships — todos os que pide TKStrike no include=
        ObjectNode rels = mapper.createObjectNode();
        rels.set("homeCompetitor", relOne("competitors", homeId));
        rels.set("awayCompetitor", relOne("competitors", awayId));
        rels.set("matchConfiguration", relOne("match-configurations", configId));
        rels.set("refereeAssignment", relOne("match-referee-assignments", refAssId));
        rels.set("event", relOne("events", eventId));
        rels.set("session", relNull());
        rels.set("results", relMany());
        node.set("relationships", rels);

        return node;
    }

    /**
     * Builds the included array with all required resources.
     * crnk resolves relationships from included — if present here no extra GET is made.
     */
    private ArrayNode buildIncluded(MatchConfigurationDto dto) {
        ArrayNode inc = mapper.createArrayNode();

        String matchId = s(dto.getMatchNumber());
        String homeId = competitorId(dto, true);
        String awayId = competitorId(dto, false);
        String homePartId = homeId + "-part";
        String awayPartId = awayId + "-part";
        String configId = "config-" + matchId;
        String eventId = "event-" + matchId;
        String refAssId = "refass-" + matchId;

        // ── competitors ──────────────────────────────────────────────────────
        inc.add(buildCompetitorNode(homeId, dto.getBlueAthlete(), homePartId));
        inc.add(buildCompetitorNode(awayId, dto.getRedAthlete(), awayPartId));

        // ── participants ─────────────────────────────────────────────────────
        inc.add(buildParticipantNode(homePartId, dto.getBlueAthlete()));
        inc.add(buildParticipantNode(awayPartId, dto.getRedAthlete()));

        // ── match-configurations ─────────────────────────────────────────────
        inc.add(buildConfigNode(configId, dto));

        // ── events ───────────────────────────────────────────────────────────
        inc.add(buildEventNode(eventId, dto));

        // ── match-referee-assignments (baleiro — sen árbitros por defecto) ───
        inc.add(buildRefAssNode(refAssId, matchId));

        return inc;
    }

    /**
     * Competitor — Competitor.java:
     * id, competitorType, printName, printInitialName, tvName, tvInitialName,
     * scoreboardName, scoreboardInitialName, rank, seed, country.
     * Relationship participants: List<Participant> → ARRAY of refs.
     */
    private ObjectNode buildCompetitorNode(String id, MatchConfigurationDto.AthleteDto a, String partId) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "competitors");
        node.put("id", id);

        ObjectNode attrs = mapper.createObjectNode();
        String given = a != null ? s(a.getGivenName()) : "";
        String family = a != null ? s(a.getFamilyName()) : "";
        String full = (given + " " + family).trim();
        String sboard = a != null ? s(a.getScoreboardName()) : id;

        attrs.put("competitorType", "INDIVIDUAL");
        attrs.put("printName", full.isEmpty() ? sboard : full);
        attrs.put("printInitialName", sboard);
        attrs.put("tvName", full.isEmpty() ? sboard : full);
        attrs.put("tvInitialName", sboard);
        attrs.put("scoreboardName", sboard);
        attrs.put("scoreboardInitialName", sboard);
        attrs.put("rank", a != null && a.getRank() != null ? a.getRank() : 0);
        attrs.put("seed", a != null && a.getSeed() != null ? a.getSeed() : 0);
        attrs.put("country", a != null ? s(a.getFlagAbbreviation()) : "");
        node.set("attributes", attrs);

        // participants é List<Participant> → relationship como ARRAY
        ObjectNode rels = mapper.createObjectNode();
        ObjectNode partRel = mapper.createObjectNode();
        ArrayNode partData = mapper.createArrayNode();
        ObjectNode partRef = mapper.createObjectNode();
        partRef.put("type", "participants");
        partRef.put("id", partId);
        partData.add(partRef);
        partRel.set("data", partData);
        rels.set("participants", partRel);
        node.set("relationships", rels);

        return node;
    }

    /**
     * Participant — Participant.java:
     * id, licenseNumber, givenName, familyName, passportGivenName, passportFamilyName,
     * preferredGivenName, preferredFamilyName, printName, printInitialName,
     * tvName, tvInitialName, scoreboardName, scoreboardInitialName,
     * gender (enum: FEMALE/MALE/MIXED), birthDate (String), mainRole, country.
     */
    private ObjectNode buildParticipantNode(String id, MatchConfigurationDto.AthleteDto a) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "participants");
        node.put("id", id);

        ObjectNode attrs = mapper.createObjectNode();
        String given = a != null ? s(a.getGivenName()) : "";
        String family = a != null ? s(a.getFamilyName()) : "";
        String full = (given + " " + family).trim();
        String sboard = a != null ? s(a.getScoreboardName()) : id;
        String country = a != null ? s(a.getFlagAbbreviation()) : "";
        String gender = a != null && a.getGender() != null ? a.getGender() : "MALE";

        attrs.put("licenseNumber", a != null && !s(a.getWfId()).isEmpty()
                ? a.getWfId() : (a != null ? s(a.getOvrInternalId()) : id));
        attrs.put("givenName", given);
        attrs.put("familyName", family);
        attrs.put("passportGivenName", given);
        attrs.put("passportFamilyName", family);
        attrs.put("preferredGivenName", given);
        attrs.put("preferredFamilyName", family);
        attrs.put("printName", full.isEmpty() ? sboard : full);
        attrs.put("printInitialName", sboard);
        attrs.put("tvName", full.isEmpty() ? sboard : full);
        attrs.put("tvInitialName", sboard);
        attrs.put("scoreboardName", sboard);
        attrs.put("scoreboardInitialName", sboard);
        attrs.put("gender", gender);
        attrs.put("birthDate", "");
        attrs.put("mainRole", "ATHLETE");
        attrs.put("country", country);
        node.set("attributes", attrs);

        // organization relationship — null
        ObjectNode rels = mapper.createObjectNode();
        ObjectNode orgRel = mapper.createObjectNode();
        orgRel.set("data", mapper.nullNode());
        rels.set("organization", orgRel);
        node.set("relationships", rels);

        return node;
    }

    /**
     * MatchConfiguration — MatchConfiguration.java:
     * id, rules (enum: CONVENTIONAL/BESTOF3), rounds, maxDifference, maxPenalties.
     * timing: { round: "02:00", rest: "01:00", injury: "01:00" }  ← Strings
     * thresholds: { body: int, head: int }
     * goldenPoint: { enabled: bool, time: "01:00" }
     * videoReplayQuota: { home: int, away: int }
     */
    private ObjectNode buildConfigNode(String id, MatchConfigurationDto dto) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "match-configurations");
        node.put("id", id);

        ObjectNode attrs = mapper.createObjectNode();
        var rc = dto.getRoundsConfig();
        attrs.put("rules", s(dto.getMatchVictoryCriteria()).isEmpty() ? "CONVENTIONAL" : dto.getMatchVictoryCriteria());
        attrs.put("rounds", rc != null && rc.getRounds() != null ? rc.getRounds() : 3);
        attrs.put("maxDifference", dto.getDifferencialScore() != null ? dto.getDifferencialScore() : 12);
        attrs.put("maxPenalties", dto.getMaxAllowedGamJeoms() != null ? dto.getMaxAllowedGamJeoms() : 10);

        // timing — Strings in "mm:ss" format
        ObjectNode timing = mapper.createObjectNode();
        timing.put("round", rc != null ? rc.getRoundTimeStr() : "02:00");
        timing.put("rest", rc != null ? rc.getKyeShiTimeStr() : "01:00");
        timing.put("injury", rc != null ? rc.getKyeShiTimeStr() : "01:00");
        attrs.set("timing", timing);

        // thresholds
        ObjectNode thr = mapper.createObjectNode();
        thr.put("body", dto.getCategory() != null && dto.getCategory().getBodyLevel() != null
                ? dto.getCategory().getBodyLevel() : 25);
        thr.put("head", dto.getCategory() != null && dto.getCategory().getHeadLevel() != null
                ? dto.getCategory().getHeadLevel() : 5);
        attrs.set("thresholds", thr);

        // goldenPoint
        ObjectNode gp = mapper.createObjectNode();
        boolean gpEnabled = rc != null && Boolean.TRUE.equals(rc.getGoldenPointEnabled());
        gp.put("enabled", gpEnabled);
        String gpTime = "01:00";
        if (rc != null && rc.getGoldenPointTimeMinutes() != null) {
            gpTime = String.format("%02d:%02d",
                    rc.getGoldenPointTimeMinutes(),
                    rc.getGoldenPointTimeSeconds() != null ? rc.getGoldenPointTimeSeconds() : 0);
        }
        gp.put("time", gpTime);
        attrs.set("goldenPoint", gp);

        // videoReplayQuota — MatchScore {home, away}
        ObjectNode quota = mapper.createObjectNode();
        quota.put("home", dto.getBlueAthleteVideoQuota() != null ? dto.getBlueAthleteVideoQuota() : 2);
        quota.put("away", dto.getRedAthleteVideoQuota() != null ? dto.getRedAthleteVideoQuota() : 2);
        attrs.set("videoReplayQuota", quota);

        node.set("attributes", attrs);
        node.set("relationships", mapper.createObjectNode());
        return node;
    }

    /**
     * Event — Event.java:
     * id, discipline, division, gender (enum: FEMALE/MALE/MIXED),
     * name, abbreviation, weightCategory, sportClass, category.
     * Used by WtDataToTkStrikeConverter.convertEvent() to build CategoryDto.
     */
    private ObjectNode buildEventNode(String id, MatchConfigurationDto dto) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "events");
        node.put("id", id);

        ObjectNode attrs = mapper.createObjectNode();
        String catName = dto != null && dto.getCategory() != null ? s(dto.getCategory().getName()) : "";
        String gender = dto != null && dto.getCategory() != null ? s(dto.getCategory().getGender()) : "MALE";
        String subCat = dto != null && dto.getCategory() != null ? s(dto.getCategory().getSubCategory()) : "";

        attrs.put("discipline", "Taekwondo");
        attrs.put("division", subCat.isEmpty() ? catName : subCat);
        attrs.put("gender", gender.isEmpty() ? "MALE" : gender);
        attrs.put("name", catName.isEmpty() ? "Event" : catName);
        attrs.put("abbreviation", catName.isEmpty() ? "Event" : catName);
        attrs.put("weightCategory", catName.isEmpty() ? "Event" : catName);
        attrs.put("sportClass", "");
        attrs.put("category", catName.isEmpty() ? "SENIORS" : catName);
        node.set("attributes", attrs);

        ObjectNode rels = mapper.createObjectNode();
        rels.set("medalWinners", relMany());
        rels.set("matches", relMany());
        node.set("relationships", rels);

        return node;
    }

    /**
     * MatchRefereeAssignment — MatchRefereeAssignment.java:
     * id + relationships refJ1/J2/J3/CR/RJ/TA all with SerializeType.ONLY_ID.
     * No referees configured → all null.
     */
    private ObjectNode buildRefAssNode(String id, String matchId) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "match-referee-assignments");
        node.put("id", id);
        node.set("attributes", mapper.createObjectNode());

        ObjectNode rels = mapper.createObjectNode();
        rels.set("match", relOne("matches", matchId));
        rels.set("refJ1", relNull());
        rels.set("refJ2", relNull());
        rels.set("refJ3", relNull());
        rels.set("refCR", relNull());
        rels.set("refRJ", relNull());
        rels.set("refTA", relNull());
        node.set("relationships", rels);

        return node;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String competitorId(MatchConfigurationDto dto, boolean home) {
        var a = home ? dto.getBlueAthlete() : dto.getRedAthlete();
        if (a != null && a.getOvrInternalId() != null) return a.getOvrInternalId();
        return (home ? "home" : "away") + "-" + dto.getMatchNumber();
    }

    // to-one relationship
    private ObjectNode relOne(String type, String id) {
        ObjectNode rel = mapper.createObjectNode();
        ObjectNode d = mapper.createObjectNode();
        d.put("type", type);
        d.put("id", id);
        rel.set("data", d);
        return rel;
    }

    // null to-one relationship
    private ObjectNode relNull() {
        ObjectNode rel = mapper.createObjectNode();
        rel.set("data", mapper.nullNode());
        return rel;
    }

    // empty to-many relationship
    private ObjectNode relMany() {
        ObjectNode rel = mapper.createObjectNode();
        rel.set("data", mapper.createArrayNode());
        return rel;
    }

    // ── GET /match-configurations/{id} ───────────────────────────────────────
    @GET
    @Path("/match-configurations/{id}")
    @Produces(JSONAPI)
    public Response getMatchConfig(@PathParam("id") String id) {
        log.info("WT OVR GET /match-configurations/{}", id);
        String matchId = id.replace("config-", "");
        Match match = Match.findById(matchId);
        MatchConfigurationDto dto = match != null ? matchStateService.convertMatchToDto(match) : new MatchConfigurationDto();
        ObjectNode root = mapper.createObjectNode();
        root.set("data", buildConfigNode(id, dto));
        return Response.ok(root.toString()).type(JSONAPI).build();
    }

    // ── GET /sessions/{id} ────────────────────────────────────────────────────
    @GET
    @Path("/sessions/{id}")
    @Produces(JSONAPI)
    public Response getSession(@PathParam("id") String id) {
        log.info("WT OVR GET /sessions/{}", id);
        ObjectNode root = mapper.createObjectNode();
        ObjectNode data = mapper.createObjectNode();
        data.put("type", "sessions");
        data.put("id", id);
        ObjectNode attrs = mapper.createObjectNode();
        attrs.put("name", "Session");
        attrs.put("startTime", "");
        attrs.put("endTime", "");
        attrs.put("scheduleStatus", "ACTIVE");
        data.set("attributes", attrs);
        data.set("relationships", mapper.createObjectNode());
        root.set("data", data);
        return Response.ok(root.toString()).type(JSONAPI).build();
    }

    // ── GET /organizations/{id} ───────────────────────────────────────────────
    @GET
    @Path("/organizations/{id}")
    @Produces(JSONAPI)
    public Response getOrganization(@PathParam("id") String id) {
        log.info("WT OVR GET /organizations/{}", id);
        ObjectNode root = mapper.createObjectNode();
        ObjectNode data = mapper.createObjectNode();
        data.put("type", "organizations");
        data.put("id", id);
        ObjectNode attrs = mapper.createObjectNode();
        attrs.put("name", "FGT");
        attrs.put("country", "ESP");
        data.set("attributes", attrs);
        data.set("relationships", mapper.createObjectNode());
        root.set("data", data);
        return Response.ok(root.toString()).type(JSONAPI).build();
    }

    private String s(String v) {
        return v != null ? v : "";
    }
}
