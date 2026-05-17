package pazos.tkStrike.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.tkStrike.entity.Athlete;
import pazos.tkStrike.entity.Category;
import pazos.tkStrike.entity.CategoryId;
import pazos.tkStrike.entity.Match;
import pazos.tkStrike.repository.AthleteRepository;
import pazos.tkStrike.repository.CategoryRepository;
import pazos.tkStrike.repository.MatchRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Responsable de cargar y parsear el fichero CSV de combates hacia la BD.
 * Separa:
 * - Atletas (tabla athletes)
 * - Categorías (tabla categories con PK compuesta)
 * - Combates (tabla matches)
 */
public class CsvMatchLoader {

    private static final Logger log = LoggerFactory.getLogger(CsvMatchLoader.class);
    private final String csvPath;
    private final AthleteRepository athleteRepository;
    private final CategoryRepository categoryRepository;
    private final MatchRepository matchRepository;

    public CsvMatchLoader(String csvPath, AthleteRepository athleteRepository,
                         CategoryRepository categoryRepository, MatchRepository matchRepository) {
        this.csvPath = csvPath;
        this.athleteRepository = athleteRepository;
        this.categoryRepository = categoryRepository;
        this.matchRepository = matchRepository;
    }

    /**
     * Carga todos los datos del CSV hacia la BD
     */
    public void loadMatches() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(csvPath);
            if (is == null) {
                log.error("Arquivo CSV no encontrado: {}", csvPath);
                return;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String line;
                boolean first = true;
                int count = 0;
                while ((line = br.readLine()) != null) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    parseLine(line);
                    count++;
                }
                log.info("CSV cargado: {} combates", count);
            }
        } catch (Exception e) {
            log.error("Erro ao ler CSV: {}", e.getMessage());
        }
    }

    /**
     * Parsea una línea CSV y guarda los datos en la BD
     */
    private void parseLine(String line) {
        try {
            String[] f = line.split(",", -1);

            // ===== PARSEAR CATEGORÍA =====
            String categoryName = f[3].trim();
            String categoryGender = f[4].trim();
            String categorySubCategory = f[5].trim();
            Integer bodyLevel = Integer.parseInt(f[6].trim());
            Integer headLevel = Integer.parseInt(f[7].trim());

            CategoryId categoryId = new CategoryId(categoryName, categoryGender, categorySubCategory);
            Category category = categoryRepository.findById(categoryId);

            if (category == null) {
                category = new Category(categoryId);
                category.bodyLevel = bodyLevel;
                category.headLevel = headLevel;
                category.rounds = Integer.parseInt(f[22].trim());
                category.roundTimeMinutes = Integer.parseInt(f[23].trim());
                category.roundTimeSeconds = Integer.parseInt(f[24].trim());
                category.kyeShiTimeMinutes = Integer.parseInt(f[25].trim());
                category.kyeShiTimeSeconds = Integer.parseInt(f[26].trim());
                category.goldenPointEnabled = Boolean.parseBoolean(f[27].trim());
                category.goldenPointTimeMinutes = Integer.parseInt(f[28].trim());
                category.goldenPointTimeSeconds = Integer.parseInt(f[29].trim());
                category.differentialScore = Integer.parseInt(f[30].trim());
                category.maxAllowedGamJeoms = Integer.parseInt(f[31].trim());

                categoryRepository.persist(category);
                log.debug("Categoría creada: {}", categoryId);
            }

            // ===== PARSEAR ATLETA AZUL =====
            String blueOvrId = f[8].trim();
            Athlete blueAthlete = athleteRepository.findByOvrInternalId(blueOvrId);
            if (blueAthlete == null) {
                blueAthlete = new Athlete(blueOvrId, f[9].trim());
                blueAthlete.givenName = f[10].trim();
                blueAthlete.familyName = f[11].trim();
                blueAthlete.flagAbbreviation = f[12].trim();
                blueAthlete.rank = Integer.parseInt(f[13].trim());
                blueAthlete.seed = Integer.parseInt(f[14].trim());
                blueAthlete.gender = categoryGender;
                blueAthlete.wfId = f[8].trim();
                athleteRepository.persist(blueAthlete);
                log.debug("Atleta AZUL creado: {} — {}", blueOvrId, blueAthlete.scoreboardName);
            }

            // ===== PARSEAR ATLETA ROJO =====
            String redOvrId = f[15].trim();
            Athlete redAthlete = athleteRepository.findByOvrInternalId(redOvrId);
            if (redAthlete == null) {
                redAthlete = new Athlete(redOvrId, f[16].trim());
                redAthlete.givenName = f[17].trim();
                redAthlete.familyName = f[18].trim();
                redAthlete.flagAbbreviation = f[19].trim();
                redAthlete.rank = Integer.parseInt(f[20].trim());
                redAthlete.seed = Integer.parseInt(f[21].trim());
                redAthlete.gender = categoryGender;
                athleteRepository.persist(redAthlete);
                log.debug("Atleta ROJO creado: {} — {}", redOvrId, redAthlete.scoreboardName);
            }

            // ===== PARSEAR COMBATE =====
            String matchNumber = f[0].trim();
            Match match = new Match(matchNumber);
            match.mat = Integer.parseInt(f[1].trim());
            match.phase = f[2].trim();
            match.category = category;
            match.blueAthlete = blueAthlete;
            match.redAthlete = redAthlete;
            match.blueAthleteVideoQuota = Integer.parseInt(f[32].trim());
            match.redAthleteVideoQuota = Integer.parseInt(f[32].trim());
            match.matchVictoryCriteria = f[33].trim();
            match.wtCompetitionDataProtocol = true;

            matchRepository.persist(match);
            log.info("Combate cargado: {} — Pista {} — {} vs {}",
                     matchNumber, match.mat, blueAthlete.scoreboardName, redAthlete.scoreboardName);

        } catch (Exception e) {
            log.error("Erro ao parsear liña CSV: {} — {}", line, e.getMessage());
        }
    }
}

