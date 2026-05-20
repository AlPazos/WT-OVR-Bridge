package pazos.wtovr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pazos.wtovr.entity.Athlete;
import pazos.wtovr.entity.Category;
import pazos.wtovr.entity.Match;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CsvMatchLoader {

    private static final Logger log = LoggerFactory.getLogger(CsvMatchLoader.class);

    public void load() {
        loadCategories();
        loadAthletes();
        loadMatches();
    }

    private void loadCategories() {
        int count = 0;
        for (String[] f : readCsv("categories.csv")) {
            String name = f[0].trim();
            String gender = f[1].trim();
            String subCategory = f[2].trim();
            if (Category.findByNameGenderSubcategory(name, gender, subCategory) != null) continue;
            Category cat = new Category(name, gender, subCategory);
            cat.bodyLevel = Integer.parseInt(f[3].trim());
            cat.headLevel = Integer.parseInt(f[4].trim());
            cat.rounds = Integer.parseInt(f[5].trim());
            cat.roundTimeMinutes = Integer.parseInt(f[6].trim());
            cat.roundTimeSeconds = Integer.parseInt(f[7].trim());
            cat.kyeShiTimeMinutes = Integer.parseInt(f[8].trim());
            cat.kyeShiTimeSeconds = Integer.parseInt(f[9].trim());
            cat.goldenPointEnabled = Boolean.parseBoolean(f[10].trim());
            cat.goldenPointTimeMinutes = Integer.parseInt(f[11].trim());
            cat.goldenPointTimeSeconds = Integer.parseInt(f[12].trim());
            cat.differentialScore = Integer.parseInt(f[13].trim());
            cat.maxAllowedGamJeoms = Integer.parseInt(f[14].trim());
            cat.persist();
            count++;
        }
        log.info("Categorías cargadas: {}", count);
    }

    private void loadAthletes() {
        int count = 0;
        for (String[] f : readCsv("athletes.csv")) {
            String ovrId = f[0].trim();
            if (Athlete.findById(ovrId) != null) continue;
            Athlete a = new Athlete(ovrId, f[1].trim());
            a.givenName = f[2].trim();
            a.familyName = f[3].trim();
            a.flagAbbreviation = f[4].trim();
            a.rank = 0;
            a.seed = Integer.parseInt(f[5].trim());
            a.gender = f[6].trim();
            a.wfId = ovrId;
            a.persist();
            count++;
        }
        log.info("Atletas cargados: {}", count);
    }

    private void loadMatches() {
        // [0]=matchNumber [1]=mat [2]=phase [3]=categoryName [4]=categoryGender [5]=categorySubCategory
        // [6]=blueOvrId [7]=redOvrId [8]=videoQuota [9]=matchVictoryCriteria [10]=nextMatchNumber [11]=nextMatchColor
        int count = 0;
        for (String[] f : readCsv("matches.csv")) {
            String matchNumber = f[0].trim();
            if (Match.findById(matchNumber) != null) continue;

            Category category = Category.findByNameGenderSubcategory(f[3].trim(), f[4].trim(), f[5].trim());

            String blueOvrId = f[6].trim();
            String redOvrId = f[7].trim();
            Athlete blueAthlete = blueOvrId.isEmpty() ? null : Athlete.findById(blueOvrId);
            Athlete redAthlete = redOvrId.isEmpty() ? null : Athlete.findById(redOvrId);

            Match match = new Match(matchNumber);
            match.mat = Integer.parseInt(f[1].trim());
            match.phase = f[2].trim();
            match.category = category;
            match.blueAthlete = blueAthlete;
            match.redAthlete = redAthlete;
            match.videoQuota = Integer.parseInt(f[8].trim());
            match.matchVictoryCriteria = f[9].trim();
            match.nextMatchNumber = f[10].trim().isEmpty() ? null : f[10].trim();
            match.nextMatchColor = f[11].trim().isEmpty() ? null : f[11].trim();
            match.wtCompetitionDataProtocol = true;
            match.persist();
            count++;
        }
        log.info("Combates cargados: {}", count);
    }

    private Iterable<String[]> readCsv(String resourceName) {
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
            if (is == null) {
                log.error("Archivo CSV no encontrado: {}", resourceName);
                return rows;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                boolean first = true;
                while ((line = br.readLine()) != null) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    if (line.trim().isEmpty()) continue;
                    rows.add(line.split(",", -1));
                }
            }
        } catch (Exception e) {
            log.error("Error leyendo {}: {}", resourceName, e.getMessage());
        }
        return rows;
    }
}