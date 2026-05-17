package pazos.tkStrike.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.tkStrike.entity.Match;
import pazos.tkStrike.model.MatchConfigurationDto;
import pazos.tkStrike.repository.AthleteRepository;
import pazos.tkStrike.repository.CategoryRepository;
import pazos.tkStrike.repository.MatchRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de orquestación para la gestión del estado de combates.
 * Coordina:
 * - Carga del CSV hacia la BD (normalizada)
 * - Búsquedas de combates por diversos criterios
 * - Gestión del estado actual de cada pista
 */
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class MatchStateService {

    private static final Logger log = LoggerFactory.getLogger(MatchStateService.class);

    @ConfigProperty(name = "campionato.csv.path", defaultValue = "campionato.csv")
    String csvPath;

    @Inject
    AthleteRepository athleteRepository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    MatchRepository matchRepository;

    private CurrentMatchStateHolder currentMatchStateHolder;

    void onStart(@Observes StartupEvent ev) {
        try {
            cargarCsv();
            currentMatchStateHolder = new CurrentMatchStateHolder();
        } catch (Exception e) {
            log.error("Erro ao cargar CSV", e);
        }
    }

    /**
     * Carga el CSV e inicializa la BD con todos los combates normalizados
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public void cargarCsv() {
        String path = csvPath != null ? csvPath : "campionato.csv";
        CsvMatchLoader csvMatchLoader = new CsvMatchLoader(path, athleteRepository, 
                                                           categoryRepository, matchRepository);
        csvMatchLoader.loadMatches();
        log.info("CSV cargado en BD");
    }

    /**
     * Obtiene el siguiente combate disponible para unha pista.
     */
    public MatchConfigurationDto getNextCombate(String ring) {
        try {
            Integer mat = Integer.parseInt(ring);
            Match match = matchRepository.getNextMatchForMat(mat);
            if (match != null) {
                return convertMatchToDto(match);
            }
            return null;
        } catch (NumberFormatException e) {
            log.warn("Pista inválida: {}", ring);
            return null;
        }
    }

    /**
     * Obtiene TODOS los combates disponibles para una pista
     */
    public List<MatchConfigurationDto> getAllCombates(String ring) {
        try {
            Integer mat = Integer.parseInt(ring);
            return matchRepository.findAllByMat(mat)
                    .stream()
                    .map(this::convertMatchToDto)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            log.warn("Pista inválida: {}", ring);
            return List.of();
        }
    }

    /**
     * Convierte una entidad Match a MatchConfigurationDto
     */
    private MatchConfigurationDto convertMatchToDto(Match match) {
        MatchConfigurationDto dto = new MatchConfigurationDto();
        dto.setMatchNumber(match.matchNumber);
        dto.setMat(match.mat);
        dto.setPhase(match.phase);
        
        // Categoría
        if (match.category != null) {
            MatchConfigurationDto.CategoryDto cat = new MatchConfigurationDto.CategoryDto();
            cat.setName(match.category.id.name);
            cat.setGender(match.category.id.gender);
            cat.setSubCategory(match.category.id.subCategory);
            cat.setBodyLevel(match.category.bodyLevel);
            cat.setHeadLevel(match.category.headLevel);
            dto.setCategory(cat);
        }
        
        // Atleta azul
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
        
        // Atleta rojo
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
        
        // Configuración de rondas
        if (match.category != null) {
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
        }
        
        dto.setDifferencialScore(match.category != null ? match.category.differentialScore : 12);
        dto.setMaxAllowedGamJeoms(match.category != null ? match.category.maxAllowedGamJeoms : 10);
        dto.setBlueAthleteVideoQuota(match.blueAthleteVideoQuota);
        dto.setRedAthleteVideoQuota(match.redAthleteVideoQuota);
        dto.setMatchVictoryCriteria(match.matchVictoryCriteria);
        dto.setWtCompetitionDataProtocol(match.wtCompetitionDataProtocol);
        
        return dto;
    }

    public MatchConfigurationDto getCombateActual(String ring) {
        // Compatibilidad temporal
        return null;
    }

    public void setCombateActual(String ring, MatchConfigurationDto dto) {
        // Compatibilidad temporal
    }

    public void clear(String ring) {
        currentMatchStateHolder.clear(ring);
    }

    public MatchConfigurationDto getCombateByMatchNumber(String matchNumber) {
        Match match = matchRepository.findByMatchNumber(matchNumber);
        return match != null ? convertMatchToDto(match) : null;
    }

    /**
     * Busca combate por cualquiera de los IDs de atleta (blue o red)
     */
    public MatchConfigurationDto getCombateByCompetitorId(String competitorId) {
        // Obtener directamente el atleta (si existe) evita crear una lista temporal
        var athlete = athleteRepository.find("ovrInternalId", competitorId).firstResult();
        if (athlete != null) {
            // Panache doesn't provide an `or` chaining method on the query returned by
            // `find(String, Object)`. Build a single JPQL-like query with an OR condition
            // to fetch matches where the athlete is either blue or red.
            Match found = matchRepository.find("blueAthlete = ?1 or redAthlete = ?1", athlete).firstResult();
            if (found != null) {
                return convertMatchToDto(found);
            }
        }
        return null;
    }

    /**
     * Busca combate por ID de evento
     */
    public MatchConfigurationDto getCombateByEventId(String eventId) {
        Match match = matchRepository.findByMatchNumber(eventId);
        return match != null ? convertMatchToDto(match) : null;
    }

    /**
     * Busca combate por ID de configuración
     */
    public MatchConfigurationDto getCombateByConfigId(String configId) {
        Match match = matchRepository.findByMatchNumber(configId);
        return match != null ? convertMatchToDto(match) : null;
    }

    /**
     * Busca un atleta por ID de participante
     */
    public MatchConfigurationDto.AthleteDto getAthleteByParticipantId(String participantId) {
        // participantId formato: "{competitorId}-part"
        String competitorId = participantId.replace("-part", "");
        var athlete = athleteRepository.find("ovrInternalId", competitorId).firstResult();
        if (athlete != null) {
            var matches = matchRepository.find("blueAthlete", athlete).list();
            if (!matches.isEmpty()) {
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
        }
        return null;
    }
}
