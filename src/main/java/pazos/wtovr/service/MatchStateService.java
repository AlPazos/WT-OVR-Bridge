package pazos.wtovr.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.wtovr.entity.Athlete;
import pazos.wtovr.entity.Match;
import pazos.wtovr.model.MatchConfigurationDto;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class MatchStateService {

    private static final Logger log = LoggerFactory.getLogger(MatchStateService.class);

    void onStart(@Observes StartupEvent ev) {
        if (Match.count() == 0) {
            loadCsv();
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public long loadCsv() {
        new CsvMatchLoader().load();
        long total = Match.count();
        log.info("CSV cargado en BD — {} combates", total);
        return total;
    }

    public MatchConfigurationDto getNextCombat(String ring) {
        try {
            Match match = Match.getNextForMat(Integer.parseInt(ring));
            return match != null ? convertMatchToDto(match) : null;
        } catch (NumberFormatException e) {
            log.warn("Pista inválida: {}", ring);
            return null;
        }
    }

    public List<MatchConfigurationDto> getAllMatches(String ring, String status) {
        try {
            int mat = Integer.parseInt(ring);
            List<Match> matches = status != null
                    ? Match.findAllByMatAndStatus(mat, status)
                    : Match.findAllByMat(mat);
            return matches.stream().map(this::convertMatchToDto).collect(Collectors.toList());
        } catch (NumberFormatException e) {
            log.warn("Pista inválida: {}", ring);
            return List.of();
        }
    }

    public List<MatchConfigurationDto> getIndistinctMatches() {
        try {
            List<Match> matches = Match.findAll().list();
            return matches.stream().map(this::convertMatchToDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener combates indistintos", e);
            return List.of();
        }
    }

    public MatchConfigurationDto getCombateByCompetitorId(String competitorId) {
        Athlete athlete = Athlete.findById(competitorId);
        if (athlete != null) {
            Match found = Match.findByCompetitor(athlete);
            if (found != null) return convertMatchToDto(found);
        }
        return null;
    }

    public MatchConfigurationDto.AthleteDto getAthleteByParticipantId(String participantId) {
        String competitorId = participantId.replace("-part", "");
        Athlete athlete = Athlete.findById(competitorId);
        if (athlete == null) return null;

        Match match = Match.find("blueAthlete", athlete).firstResult();
        if (match == null) return null;

        MatchConfigurationDto.AthleteDto dto = new MatchConfigurationDto.AthleteDto();
        dto.setOvrInternalId(athlete.ovrInternalId);
        dto.setScoreboardName(athlete.scoreboardName);
        dto.setGivenName(athlete.givenName);
        dto.setFamilyName(athlete.familyName);
        dto.setFlagAbbreviation(athlete.flagAbbreviation);
        dto.setRank(athlete.rank);
        dto.setSeed(athlete.seed);
        dto.setGender(athlete.gender);
        dto.setWfId(athlete.wfId);
        return dto;
    }

    public MatchConfigurationDto convertMatchToDto(Match match) {
        MatchConfigurationDto dto = new MatchConfigurationDto();
        dto.setMatchNumber(match.matchNumber);
        dto.setMat(match.mat);
        dto.setPhase(match.phase);
        dto.setStatus(match.status);

        if (match.category != null) {
            MatchConfigurationDto.CategoryDto cat = new MatchConfigurationDto.CategoryDto();
            cat.setName(match.category.name);
            cat.setGender(match.category.gender);
            cat.setSubCategory(match.category.subCategory);
            cat.setBodyLevel(match.category.bodyLevel);
            cat.setHeadLevel(match.category.headLevel);
            dto.setCategory(cat);

            MatchConfigurationDto.RoundsConfigDto rc = new MatchConfigurationDto.RoundsConfigDto();
            rc.setRounds(match.category.rounds);
            rc.setRoundTimeMinutes(match.category.roundTimeMinutes);
            rc.setRoundTimeSeconds(match.category.roundTimeSeconds);
            rc.setKyeShiTimeMinutes(match.category.kyeShiTimeMinutes);
            rc.setKyeShiTimeSeconds(match.category.kyeShiTimeSeconds);
            rc.setGoldenPointEnabled(match.category.goldenPointEnabled);
            rc.setGoldenPointTimeMinutes(match.category.goldenPointTimeMinutes);
            rc.setGoldenPointTimeSeconds(match.category.goldenPointTimeSeconds);
            dto.setRoundsConfig(rc);

            dto.setDifferencialScore(match.category.differentialScore);
            dto.setMaxAllowedGamJeoms(match.category.maxAllowedGamJeoms);
        } else {
            dto.setDifferencialScore(12);
            dto.setMaxAllowedGamJeoms(10);
        }

        if (match.blueAthlete != null) {
            MatchConfigurationDto.AthleteDto blue = new MatchConfigurationDto.AthleteDto();
            blue.setOvrInternalId(match.blueAthlete.ovrInternalId);
            blue.setScoreboardName(match.blueAthlete.scoreboardName);
            blue.setGivenName(match.blueAthlete.givenName);
            blue.setFamilyName(match.blueAthlete.familyName);
            blue.setFlagAbbreviation(match.blueAthlete.flagAbbreviation);
            blue.setRank(match.blueAthlete.rank);
            blue.setSeed(match.blueAthlete.seed);
            blue.setGender(match.blueAthlete.gender);
            blue.setWfId(match.blueAthlete.wfId);
            dto.setBlueAthlete(blue);
        }

        if (match.redAthlete != null) {
            MatchConfigurationDto.AthleteDto red = new MatchConfigurationDto.AthleteDto();
            red.setOvrInternalId(match.redAthlete.ovrInternalId);
            red.setScoreboardName(match.redAthlete.scoreboardName);
            red.setGivenName(match.redAthlete.givenName);
            red.setFamilyName(match.redAthlete.familyName);
            red.setFlagAbbreviation(match.redAthlete.flagAbbreviation);
            red.setRank(match.redAthlete.rank);
            red.setSeed(match.redAthlete.seed);
            red.setGender(match.redAthlete.gender);
            dto.setRedAthlete(red);
        }

        dto.setBlueAthleteVideoQuota(match.videoQuota);
        dto.setRedAthleteVideoQuota(match.videoQuota);
        dto.setMatchVictoryCriteria(match.matchVictoryCriteria);
        dto.setWtCompetitionDataProtocol(match.wtCompetitionDataProtocol);

        return dto;
    }
}