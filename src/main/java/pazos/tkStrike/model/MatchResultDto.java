package pazos.tkStrike.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * DTO exacto de TKStrike — baseado no código fonte real de tkStrike-gen3.jar
 * ATENCIÓN: matchWinner é AthleteDto, non unha clase interna propia
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchResultDto {

    private String     vmMatchInternalId;
    private String     matchNumber;
    private String     vmRingNumber;
    private String     categoryName;
    private String     categoryGender;
    private String     subCategoryName;
    private String     phaseName;
    private Long       matchStartTime;
    private Long       matchEndTime;
    private Boolean    goldenPointTieBreakerHaveTieBreaker;
    private Integer    goldenPointTieBreakerBluePunches;
    private Integer    goldenPointTieBreakerBlueRoundWins;
    private Integer    goldenPointTieBreakerBlueHits;
    private Integer    goldenPointTieBreakerBluePenalties;
    private Integer    goldenPointTieBreakerRedPunches;
    private Integer    goldenPointTieBreakerRedRoundWins;
    private Integer    goldenPointTieBreakerRedHits;
    private Integer    goldenPointTieBreakerRedPenalties;
    private Integer    goldenPointTieBreakerBluePARATechPoints;
    private Integer    goldenPointTieBreakerRedPARATechPoints;
    private String     matchVictoryCriteria;
    private Boolean    paraTkdMatch;
    private String     matchWinnerColor;
    private MatchConfigurationDto.AthleteDto matchWinner;         // AthleteDto, non String
    private String     matchFinalDecision;
    private Integer    roundFinish;
    private Integer    bluePoints;
    private Integer    blueRoundWins;
    private Integer    bluePenalties;
    private Integer    blueVideoQuota;
    private Integer    redPoints;
    private Integer    redRoundWins;
    private Integer    redPenalties;
    private Integer    redVideoQuota;

    public String getVmMatchInternalId() { return vmMatchInternalId; }
    public void setVmMatchInternalId(String v) { this.vmMatchInternalId = v; }
    public String getMatchNumber() { return matchNumber; }
    public void setMatchNumber(String v) { this.matchNumber = v; }
    public String getVmRingNumber() { return vmRingNumber; }
    public void setVmRingNumber(String v) { this.vmRingNumber = v; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String v) { this.categoryName = v; }
    public String getCategoryGender() { return categoryGender; }
    public void setCategoryGender(String v) { this.categoryGender = v; }
    public String getSubCategoryName() { return subCategoryName; }
    public void setSubCategoryName(String v) { this.subCategoryName = v; }
    public String getPhaseName() { return phaseName; }
    public void setPhaseName(String v) { this.phaseName = v; }
    public Long getMatchStartTime() { return matchStartTime; }
    public void setMatchStartTime(Long v) { this.matchStartTime = v; }
    public Long getMatchEndTime() { return matchEndTime; }
    public void setMatchEndTime(Long v) { this.matchEndTime = v; }
    public Boolean getGoldenPointTieBreakerHaveTieBreaker() { return goldenPointTieBreakerHaveTieBreaker; }
    public void setGoldenPointTieBreakerHaveTieBreaker(Boolean v) { this.goldenPointTieBreakerHaveTieBreaker = v; }
    public Integer getGoldenPointTieBreakerBluePunches() { return goldenPointTieBreakerBluePunches; }
    public void setGoldenPointTieBreakerBluePunches(Integer v) { this.goldenPointTieBreakerBluePunches = v; }
    public Integer getGoldenPointTieBreakerBlueRoundWins() { return goldenPointTieBreakerBlueRoundWins; }
    public void setGoldenPointTieBreakerBlueRoundWins(Integer v) { this.goldenPointTieBreakerBlueRoundWins = v; }
    public Integer getGoldenPointTieBreakerBlueHits() { return goldenPointTieBreakerBlueHits; }
    public void setGoldenPointTieBreakerBlueHits(Integer v) { this.goldenPointTieBreakerBlueHits = v; }
    public Integer getGoldenPointTieBreakerBluePenalties() { return goldenPointTieBreakerBluePenalties; }
    public void setGoldenPointTieBreakerBluePenalties(Integer v) { this.goldenPointTieBreakerBluePenalties = v; }
    public Integer getGoldenPointTieBreakerRedPunches() { return goldenPointTieBreakerRedPunches; }
    public void setGoldenPointTieBreakerRedPunches(Integer v) { this.goldenPointTieBreakerRedPunches = v; }
    public Integer getGoldenPointTieBreakerRedRoundWins() { return goldenPointTieBreakerRedRoundWins; }
    public void setGoldenPointTieBreakerRedRoundWins(Integer v) { this.goldenPointTieBreakerRedRoundWins = v; }
    public Integer getGoldenPointTieBreakerRedHits() { return goldenPointTieBreakerRedHits; }
    public void setGoldenPointTieBreakerRedHits(Integer v) { this.goldenPointTieBreakerRedHits = v; }
    public Integer getGoldenPointTieBreakerRedPenalties() { return goldenPointTieBreakerRedPenalties; }
    public void setGoldenPointTieBreakerRedPenalties(Integer v) { this.goldenPointTieBreakerRedPenalties = v; }
    public Integer getGoldenPointTieBreakerBluePARATechPoints() { return goldenPointTieBreakerBluePARATechPoints; }
    public void setGoldenPointTieBreakerBluePARATechPoints(Integer v) { this.goldenPointTieBreakerBluePARATechPoints = v; }
    public Integer getGoldenPointTieBreakerRedPARATechPoints() { return goldenPointTieBreakerRedPARATechPoints; }
    public void setGoldenPointTieBreakerRedPARATechPoints(Integer v) { this.goldenPointTieBreakerRedPARATechPoints = v; }
    public String getMatchVictoryCriteria() { return matchVictoryCriteria; }
    public void setMatchVictoryCriteria(String v) { this.matchVictoryCriteria = v; }
    public Boolean getParaTkdMatch() { return paraTkdMatch; }
    public void setParaTkdMatch(Boolean v) { this.paraTkdMatch = v; }
    public String getMatchWinnerColor() { return matchWinnerColor; }
    public void setMatchWinnerColor(String v) { this.matchWinnerColor = v; }
    public MatchConfigurationDto.AthleteDto getMatchWinner() { return matchWinner; }
    public void setMatchWinner(MatchConfigurationDto.AthleteDto v) { this.matchWinner = v; }
    public String getMatchFinalDecision() { return matchFinalDecision; }
    public void setMatchFinalDecision(String v) { this.matchFinalDecision = v; }
    public Integer getRoundFinish() { return roundFinish; }
    public void setRoundFinish(Integer v) { this.roundFinish = v; }
    public Integer getBluePoints() { return bluePoints; }
    public void setBluePoints(Integer v) { this.bluePoints = v; }
    public Integer getBlueRoundWins() { return blueRoundWins; }
    public void setBlueRoundWins(Integer v) { this.blueRoundWins = v; }
    public Integer getBluePenalties() { return bluePenalties; }
    public void setBluePenalties(Integer v) { this.bluePenalties = v; }
    public Integer getBlueVideoQuota() { return blueVideoQuota; }
    public void setBlueVideoQuota(Integer v) { this.blueVideoQuota = v; }
    public Integer getRedPoints() { return redPoints; }
    public void setRedPoints(Integer v) { this.redPoints = v; }
    public Integer getRedRoundWins() { return redRoundWins; }
    public void setRedRoundWins(Integer v) { this.redRoundWins = v; }
    public Integer getRedPenalties() { return redPenalties; }
    public void setRedPenalties(Integer v) { this.redPenalties = v; }
    public Integer getRedVideoQuota() { return redVideoQuota; }
    public void setRedVideoQuota(Integer v) { this.redVideoQuota = v; }
}
