package pazos.tkStrike.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO exacto de TKStrike — baseado no código fonte real de tkStrike-gen3.jar
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TkStrikeEventDto {

    private String  matchNumber;
    private String  matchVMInternalId;
    private String  matchVMRingNumber;
    private String  matchCategoryName;
    private String  matchCategoryGender;
    private String  matchSubCategoryName;
    private Integer roundNumber;
    private String  roundNumberStr;
    private Long    tkStrikeSystemTimestamp;
    private String  tkStrikeSystemTimestampStr;
    private Long    roundTimestamp;
    private String  roundTimestampStr;
    private String  eventType;
    private boolean eventAddPoints;
    private boolean eventRemovePoints;
    private Integer bluePoints;
    private Integer bluePenalties;
    private Integer blueRoundWins;
    private Integer redPoints;
    private Integer redPenalties;
    private Integer redRoundWins;
    private String  matchWinner;
    private String  matchFinalDecision;
    private Integer blueBodyLevel;
    private Integer blueHeadLevel;
    private Integer redBodyLevel;
    private Integer redHeadLevel;
    private Integer calledByJudgeNumber;
    private Integer hitlevel;
    private Boolean isPARASpinning;

    public String getMatchNumber() { return matchNumber; }
    public void setMatchNumber(String v) { this.matchNumber = v; }
    public String getMatchVMInternalId() { return matchVMInternalId; }
    public void setMatchVMInternalId(String v) { this.matchVMInternalId = v; }
    public String getMatchVMRingNumber() { return matchVMRingNumber; }
    public void setMatchVMRingNumber(String v) { this.matchVMRingNumber = v; }
    public String getMatchCategoryName() { return matchCategoryName; }
    public void setMatchCategoryName(String v) { this.matchCategoryName = v; }
    public String getMatchCategoryGender() { return matchCategoryGender; }
    public void setMatchCategoryGender(String v) { this.matchCategoryGender = v; }
    public String getMatchSubCategoryName() { return matchSubCategoryName; }
    public void setMatchSubCategoryName(String v) { this.matchSubCategoryName = v; }
    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer v) { this.roundNumber = v; }
    public String getRoundNumberStr() { return roundNumberStr; }
    public void setRoundNumberStr(String v) { this.roundNumberStr = v; }
    public Long getTkStrikeSystemTimestamp() { return tkStrikeSystemTimestamp; }
    public void setTkStrikeSystemTimestamp(Long v) { this.tkStrikeSystemTimestamp = v; }
    public String getTkStrikeSystemTimestampStr() { return tkStrikeSystemTimestampStr; }
    public void setTkStrikeSystemTimestampStr(String v) { this.tkStrikeSystemTimestampStr = v; }
    public Long getRoundTimestamp() { return roundTimestamp; }
    public void setRoundTimestamp(Long v) { this.roundTimestamp = v; }
    public String getRoundTimestampStr() { return roundTimestampStr; }
    public void setRoundTimestampStr(String v) { this.roundTimestampStr = v; }
    public String getEventType() { return eventType; }
    public void setEventType(String v) { this.eventType = v; }
    public boolean isEventAddPoints() { return eventAddPoints; }
    public void setEventAddPoints(boolean v) { this.eventAddPoints = v; }
    public boolean isEventRemovePoints() { return eventRemovePoints; }
    public void setEventRemovePoints(boolean v) { this.eventRemovePoints = v; }
    public Integer getBluePoints() { return bluePoints; }
    public void setBluePoints(Integer v) { this.bluePoints = v; }
    public Integer getBluePenalties() { return bluePenalties; }
    public void setBluePenalties(Integer v) { this.bluePenalties = v; }
    public Integer getBlueRoundWins() { return blueRoundWins; }
    public void setBlueRoundWins(Integer v) { this.blueRoundWins = v; }
    public Integer getRedPoints() { return redPoints; }
    public void setRedPoints(Integer v) { this.redPoints = v; }
    public Integer getRedPenalties() { return redPenalties; }
    public void setRedPenalties(Integer v) { this.redPenalties = v; }
    public Integer getRedRoundWins() { return redRoundWins; }
    public void setRedRoundWins(Integer v) { this.redRoundWins = v; }
    public String getMatchWinner() { return matchWinner; }
    public void setMatchWinner(String v) { this.matchWinner = v; }
    public String getMatchFinalDecision() { return matchFinalDecision; }
    public void setMatchFinalDecision(String v) { this.matchFinalDecision = v; }
    public Integer getBlueBodyLevel() { return blueBodyLevel; }
    public void setBlueBodyLevel(Integer v) { this.blueBodyLevel = v; }
    public Integer getBlueHeadLevel() { return blueHeadLevel; }
    public void setBlueHeadLevel(Integer v) { this.blueHeadLevel = v; }
    public Integer getRedBodyLevel() { return redBodyLevel; }
    public void setRedBodyLevel(Integer v) { this.redBodyLevel = v; }
    public Integer getRedHeadLevel() { return redHeadLevel; }
    public void setRedHeadLevel(Integer v) { this.redHeadLevel = v; }
    public Integer getCalledByJudgeNumber() { return calledByJudgeNumber; }
    public void setCalledByJudgeNumber(Integer v) { this.calledByJudgeNumber = v; }
    public Integer getHitlevel() { return hitlevel; }
    public void setHitlevel(Integer v) { this.hitlevel = v; }
    // getter real: getPARASpinning() non isIsPARASpinning()
    public Boolean getPARASpinning() { return isPARASpinning; }
    public void setPARASpinning(Boolean v) { this.isPARASpinning = v; }
}
