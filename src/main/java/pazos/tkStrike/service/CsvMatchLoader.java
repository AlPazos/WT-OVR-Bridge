package pazos.tkStrike.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.tkStrike.entity.Athlete;
import pazos.tkStrike.entity.Category;
import pazos.tkStrike.entity.Match;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CsvMatchLoader {

    private static final Logger log = LoggerFactory.getLogger(CsvMatchLoader.class);
    private final String csvPath;

    public CsvMatchLoader(String csvPath) {
        this.csvPath = csvPath;
    }

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
                    if (line.trim().isEmpty()) continue;
                    parseLine(line);
                    count++;
                }
                log.info("CSV cargado: {} combates", count);
            }
        } catch (Exception e) {
            log.error("Erro ao ler CSV: {}", e.getMessage());
        }
    }

    private void parseLine(String line) {
        try {
            String[] f = line.split(",", -1);

            String categoryName = f[3].trim();
            String categoryGender = f[4].trim();
            String categorySubCategory = f[5].trim();

            Category category = Category.findByNameGenderSubcategory(categoryName, categoryGender, categorySubCategory);
            if (category == null) {
                category = new Category(categoryName, categoryGender, categorySubCategory);
                category.bodyLevel = Integer.parseInt(f[6].trim());
                category.headLevel = Integer.parseInt(f[7].trim());
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
                category.persist();
            }

            String blueOvrId = f[8].trim();
            Athlete blueAthlete = Athlete.findById(blueOvrId);
            if (blueAthlete == null) {
                blueAthlete = new Athlete(blueOvrId, f[9].trim());
                blueAthlete.givenName = f[10].trim();
                blueAthlete.familyName = f[11].trim();
                blueAthlete.flagAbbreviation = f[12].trim();
                blueAthlete.rank = Integer.parseInt(f[13].trim());
                blueAthlete.seed = Integer.parseInt(f[14].trim());
                blueAthlete.gender = categoryGender;
                blueAthlete.wfId = blueOvrId;
                blueAthlete.persist();
            }

            String redOvrId = f[15].trim();
            Athlete redAthlete = Athlete.findById(redOvrId);
            if (redAthlete == null) {
                redAthlete = new Athlete(redOvrId, f[16].trim());
                redAthlete.givenName = f[17].trim();
                redAthlete.familyName = f[18].trim();
                redAthlete.flagAbbreviation = f[19].trim();
                redAthlete.rank = Integer.parseInt(f[20].trim());
                redAthlete.seed = Integer.parseInt(f[21].trim());
                redAthlete.gender = categoryGender;
                redAthlete.persist();
            }

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
            match.persist();

            log.info("Combate cargado: {} — Pista {} — {} vs {}",
                    matchNumber, match.mat, blueAthlete.scoreboardName, redAthlete.scoreboardName);

        } catch (Exception e) {
            log.error("Erro ao parsear liña CSV: {} — {}", line, e.getMessage());
        }
    }
}