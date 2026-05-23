package pazos.wtovr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Exact WtOvr DTO — based on the actual source of wtovr-gen3.jar.
 * Fields verified from MatchConfigurationDto.java and inner classes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchConfigurationDto {
    private String internalId;
    private Integer mat;
    private String matchNumber;
    private String phase;
    private String status;
    private CategoryDto category;
    private AthleteDto blueAthlete;
    private Integer blueAthleteVideoQuota;
    private AthleteDto redAthlete;
    private Integer redAthleteVideoQuota;
    private RoundsConfigDto roundsConfig;
    private Boolean isParaTkdMatch;
    private Integer differencialScore;
    private Integer maxAllowedGamJeoms;
    private Boolean wtCompetitionDataProtocol;
    private String matchVictoryCriteria; // "CONVENTIONAL" | "BESTOF3"
    @JsonIgnoreProperties(ignoreUnknown = true)
    private RefereeDto refereeCR;
    @JsonIgnoreProperties(ignoreUnknown = true)
    private RefereeDto refereeJ1;
    @JsonIgnoreProperties(ignoreUnknown = true)
    private RefereeDto refereeJ2;
    @JsonIgnoreProperties(ignoreUnknown = true)
    private RefereeDto refereeJ3;
    @JsonIgnoreProperties(ignoreUnknown = true)
    private RefereeDto refereeTA;
    @JsonIgnoreProperties(ignoreUnknown = true)
    private RefereeDto refereeRJ;

    // ── Main DTO getters/setters ──────────────────────────────────────────────
    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        this.status = v;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String v) {
        this.internalId = v;
    }

    public Integer getMat() {
        return mat;
    }

    public void setMat(Integer v) {
        this.mat = v;
    }

    public String getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(String v) {
        this.matchNumber = v;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String v) {
        this.phase = v;
    }

    public CategoryDto getCategory() {
        return category;
    }

    public void setCategory(CategoryDto v) {
        this.category = v;
    }

    public AthleteDto getBlueAthlete() {
        return blueAthlete;
    }

    public void setBlueAthlete(AthleteDto v) {
        this.blueAthlete = v;
    }

    public Integer getBlueAthleteVideoQuota() {
        return blueAthleteVideoQuota;
    }

    public void setBlueAthleteVideoQuota(Integer v) {
        this.blueAthleteVideoQuota = v;
    }

    public AthleteDto getRedAthlete() {
        return redAthlete;
    }

    public void setRedAthlete(AthleteDto v) {
        this.redAthlete = v;
    }

    public Integer getRedAthleteVideoQuota() {
        return redAthleteVideoQuota;
    }

    public void setRedAthleteVideoQuota(Integer v) {
        this.redAthleteVideoQuota = v;
    }

    public RoundsConfigDto getRoundsConfig() {
        return roundsConfig;
    }

    public void setRoundsConfig(RoundsConfigDto v) {
        this.roundsConfig = v;
    }

    public Boolean getIsParaTkdMatch() {
        return isParaTkdMatch;
    }

    public void setIsParaTkdMatch(Boolean v) {
        this.isParaTkdMatch = v;
    }

    // real getter is getParaTkdMatch(), not getIsParaTkdMatch()
    public Boolean getParaTkdMatch() {
        return isParaTkdMatch;
    }

    public void setParaTkdMatch(Boolean v) {
        this.isParaTkdMatch = v;
    }

    public Integer getDifferencialScore() {
        return differencialScore;
    }

    public void setDifferencialScore(Integer v) {
        this.differencialScore = v;
    }

    public Integer getMaxAllowedGamJeoms() {
        return maxAllowedGamJeoms;
    }

    public void setMaxAllowedGamJeoms(Integer v) {
        this.maxAllowedGamJeoms = v;
    }

    public Boolean getWtCompetitionDataProtocol() {
        return wtCompetitionDataProtocol;
    }

    public void setWtCompetitionDataProtocol(Boolean v) {
        this.wtCompetitionDataProtocol = v;
    }

    public String getMatchVictoryCriteria() {
        return matchVictoryCriteria;
    }

    public void setMatchVictoryCriteria(String v) {
        this.matchVictoryCriteria = v;
    }

    public RefereeDto getRefereeCR() {
        return refereeCR;
    }

    public void setRefereeCR(RefereeDto v) {
        this.refereeCR = v;
    }

    public RefereeDto getRefereeJ1() {
        return refereeJ1;
    }

    public void setRefereeJ1(RefereeDto v) {
        this.refereeJ1 = v;
    }

    public RefereeDto getRefereeJ2() {
        return refereeJ2;
    }

    public void setRefereeJ2(RefereeDto v) {
        this.refereeJ2 = v;
    }

    public RefereeDto getRefereeJ3() {
        return refereeJ3;
    }

    public void setRefereeJ3(RefereeDto v) {
        this.refereeJ3 = v;
    }

    public RefereeDto getRefereeTA() {
        return refereeTA;
    }

    public void setRefereeTA(RefereeDto v) {
        this.refereeTA = v;
    }

    public RefereeDto getRefereeRJ() {
        return refereeRJ;
    }

    public void setRefereeRJ(RefereeDto v) {
        this.refereeRJ = v;
    }

    // ── CategoryDto ──────────────────────────────────────────────────────────
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryDto {
        private String name;
        private String gender;
        private String subCategory;
        private Integer bodyLevel;
        private Integer headLevel;
        private Integer punchLevel;

        public String getName() {
            return name;
        }

        public void setName(String v) {
            this.name = v;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String v) {
            this.gender = v;
        }

        public String getSubCategory() {
            return subCategory;
        }

        public void setSubCategory(String v) {
            this.subCategory = v;
        }

        public Integer getBodyLevel() {
            return bodyLevel;
        }

        public void setBodyLevel(Integer v) {
            this.bodyLevel = v;
        }

        public Integer getHeadLevel() {
            return headLevel;
        }

        public void setHeadLevel(Integer v) {
            this.headLevel = v;
        }

        public Integer getPunchLevel() {
            return punchLevel;
        }

        public void setPunchLevel(Integer v) {
            this.punchLevel = v;
        }
    }

    // ── AthleteDto ───────────────────────────────────────────────────────────
    // birthDate is Date in the actual WtOvr code, not String
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AthleteDto {
        private String ovrInternalId;
        private String scoreboardName;
        private String wfId;
        private String flagAbbreviation;
        private String flagName;
        private Boolean flagShowName;
        private String givenName;
        private String familyName;
        private String passportGivenName;
        private String passportFamilyName;
        private String preferredGivenName;
        private String preferredFamilyName;
        private String printName;
        private String printInitialName;
        private String tvName;
        private String tvInitialName;
        private String gender;
        private Date birthDate;
        private String competitorType;
        private Integer rank;
        private Integer seed;

        public String getOvrInternalId() {
            return ovrInternalId;
        }

        public void setOvrInternalId(String v) {
            this.ovrInternalId = v;
        }

        public String getScoreboardName() {
            return scoreboardName;
        }

        public void setScoreboardName(String v) {
            this.scoreboardName = v;
        }

        public String getWfId() {
            return wfId;
        }

        public void setWfId(String v) {
            this.wfId = v;
        }

        public String getFlagAbbreviation() {
            return flagAbbreviation;
        }

        public void setFlagAbbreviation(String v) {
            this.flagAbbreviation = v;
        }

        public String getFlagName() {
            return flagName;
        }

        public void setFlagName(String v) {
            this.flagName = v;
        }

        public Boolean getFlagShowName() {
            return flagShowName;
        }

        public void setFlagShowName(Boolean v) {
            this.flagShowName = v;
        }

        public String getGivenName() {
            return givenName;
        }

        public void setGivenName(String v) {
            this.givenName = v;
        }

        public String getFamilyName() {
            return familyName;
        }

        public void setFamilyName(String v) {
            this.familyName = v;
        }

        public String getPassportGivenName() {
            return passportGivenName;
        }

        public void setPassportGivenName(String v) {
            this.passportGivenName = v;
        }

        public String getPassportFamilyName() {
            return passportFamilyName;
        }

        public void setPassportFamilyName(String v) {
            this.passportFamilyName = v;
        }

        public String getPreferredGivenName() {
            return preferredGivenName;
        }

        public void setPreferredGivenName(String v) {
            this.preferredGivenName = v;
        }

        public String getPreferredFamilyName() {
            return preferredFamilyName;
        }

        public void setPreferredFamilyName(String v) {
            this.preferredFamilyName = v;
        }

        public String getPrintName() {
            return printName;
        }

        public void setPrintName(String v) {
            this.printName = v;
        }

        public String getPrintInitialName() {
            return printInitialName;
        }

        public void setPrintInitialName(String v) {
            this.printInitialName = v;
        }

        public String getTvName() {
            return tvName;
        }

        public void setTvName(String v) {
            this.tvName = v;
        }

        public String getTvInitialName() {
            return tvInitialName;
        }

        public void setTvInitialName(String v) {
            this.tvInitialName = v;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String v) {
            this.gender = v;
        }

        public Date getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(Date v) {
            this.birthDate = v;
        }

        public String getCompetitorType() {
            return competitorType;
        }

        public void setCompetitorType(String v) {
            this.competitorType = v;
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer v) {
            this.rank = v;
        }

        public Integer getSeed() {
            return seed;
        }

        public void setSeed(Integer v) {
            this.seed = v;
        }
    }

    // ── RoundsConfigDto ──────────────────────────────────────────────────────
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoundsConfigDto {
        private Integer rounds;
        private Integer roundTimeMinutes;
        private Integer roundTimeSeconds;
        private Integer kyeShiTimeMinutes;
        private Integer kyeShiTimeSeconds;
        private Integer restTimeMinutes;
        private Integer restTimeSeconds;
        private Boolean goldenPointEnabled;
        private Integer goldenPointTimeMinutes;
        private Integer goldenPointTimeSeconds;

        public Integer getRounds() {
            return rounds;
        }

        public void setRounds(Integer v) {
            this.rounds = v;
        }

        public Integer getRoundTimeMinutes() {
            return roundTimeMinutes;
        }

        public void setRoundTimeMinutes(Integer v) {
            this.roundTimeMinutes = v;
        }

        public Integer getRoundTimeSeconds() {
            return roundTimeSeconds;
        }

        public void setRoundTimeSeconds(Integer v) {
            this.roundTimeSeconds = v;
        }

        public Integer getKyeShiTimeMinutes() {
            return kyeShiTimeMinutes;
        }

        public void setKyeShiTimeMinutes(Integer v) {
            this.kyeShiTimeMinutes = v;
        }

        public Integer getKyeShiTimeSeconds() {
            return kyeShiTimeSeconds;
        }

        public void setKyeShiTimeSeconds(Integer v) {
            this.kyeShiTimeSeconds = v;
        }

        public Integer getRestTimeMinutes() {
            return restTimeMinutes;
        }

        public void setRestTimeMinutes(Integer v) {
            this.restTimeMinutes = v;
        }

        public Integer getRestTimeSeconds() {
            return restTimeSeconds;
        }

        public void setRestTimeSeconds(Integer v) {
            this.restTimeSeconds = v;
        }

        public Boolean getGoldenPointEnabled() {
            return goldenPointEnabled;
        }

        public void setGoldenPointEnabled(Boolean v) {
            this.goldenPointEnabled = v;
        }

        public Integer getGoldenPointTimeMinutes() {
            return goldenPointTimeMinutes;
        }

        public void setGoldenPointTimeMinutes(Integer v) {
            this.goldenPointTimeMinutes = v;
        }

        public Integer getGoldenPointTimeSeconds() {
            return goldenPointTimeSeconds;
        }

        public void setGoldenPointTimeSeconds(Integer v) {
            this.goldenPointTimeSeconds = v;
        }

        // helper methods to format time as "mm:ss"
        public String getRoundTimeStr() {
            if (roundTimeMinutes == null || roundTimeSeconds == null) return "02:00";
            return String.format("%02d:%02d", roundTimeMinutes, roundTimeSeconds);
        }

        public String getKyeShiTimeStr() {
            if (kyeShiTimeMinutes == null || kyeShiTimeSeconds == null) return "01:00";
            return String.format("%02d:%02d", kyeShiTimeMinutes, kyeShiTimeSeconds);
        }
    }

    // ── RefereeDto ───────────────────────────────────────────────────────────
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RefereeDto {
        private String id;
        private String licenseNumber;
        private String scoreboardName;
        private String country;

        public String getId() {
            return id;
        }

        public void setId(String v) {
            this.id = v;
        }

        public String getLicenseNumber() {
            return licenseNumber;
        }

        public void setLicenseNumber(String v) {
            this.licenseNumber = v;
        }

        public String getScoreboardName() {
            return scoreboardName;
        }

        public void setScoreboardName(String v) {
            this.scoreboardName = v;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String v) {
            this.country = v;
        }
    }
}
