package pazos.wtovr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchInputDto {

    private String matchNumber;
    private Integer mat;
    private String phase;
    private String status;
    private Long categoryId;
    private MatchConfigurationDto.AthleteDto blueAthlete;
    private Integer blueAthleteVideoQuota;
    private MatchConfigurationDto.AthleteDto redAthlete;
    private Integer redAthleteVideoQuota;
    private String matchVictoryCriteria;
    private Boolean wtCompetitionDataProtocol;
    private String nextMatchNumber;
    private String nextMatchColor;

    public String getMatchNumber() { return matchNumber; }
    public void setMatchNumber(String v) { this.matchNumber = v; }

    public Integer getMat() { return mat; }
    public void setMat(Integer v) { this.mat = v; }

    public String getPhase() { return phase; }
    public void setPhase(String v) { this.phase = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long v) { this.categoryId = v; }

    public MatchConfigurationDto.AthleteDto getBlueAthlete() { return blueAthlete; }
    public void setBlueAthlete(MatchConfigurationDto.AthleteDto v) { this.blueAthlete = v; }

    public Integer getBlueAthleteVideoQuota() { return blueAthleteVideoQuota; }
    public void setBlueAthleteVideoQuota(Integer v) { this.blueAthleteVideoQuota = v; }

    public MatchConfigurationDto.AthleteDto getRedAthlete() { return redAthlete; }
    public void setRedAthlete(MatchConfigurationDto.AthleteDto v) { this.redAthlete = v; }

    public Integer getRedAthleteVideoQuota() { return redAthleteVideoQuota; }
    public void setRedAthleteVideoQuota(Integer v) { this.redAthleteVideoQuota = v; }

    public String getMatchVictoryCriteria() { return matchVictoryCriteria; }
    public void setMatchVictoryCriteria(String v) { this.matchVictoryCriteria = v; }

    public Boolean getWtCompetitionDataProtocol() { return wtCompetitionDataProtocol; }
    public void setWtCompetitionDataProtocol(Boolean v) { this.wtCompetitionDataProtocol = v; }

    public String getNextMatchNumber() { return nextMatchNumber; }
    public void setNextMatchNumber(String v) { this.nextMatchNumber = v; }

    public String getNextMatchColor() { return nextMatchColor; }
    public void setNextMatchColor(String v) { this.nextMatchColor = v; }
}
