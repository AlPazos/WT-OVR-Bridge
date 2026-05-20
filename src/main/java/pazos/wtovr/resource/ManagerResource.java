package pazos.wtovr.resource;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pazos.wtovr.entity.Athlete;
import pazos.wtovr.entity.Category;
import pazos.wtovr.entity.Match;
import pazos.wtovr.model.MatchConfigurationDto;
import pazos.wtovr.service.MatchStateService;
import pazos.wtovr.service.TournamentService;

import java.util.List;

@Path("/manager")
public class ManagerResource {
    @Inject
    MatchStateService matchStateService;

    @Inject
    TournamentService tournamentService;

    @GET
    @Path("/matches")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMatches() {
        return Response.ok(matchStateService.getIndistinctMatches()).build();
    }

    @GET
    @Path("/matches/{ring}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMatchesForRing(@PathParam("ring") String ring) {
        return Response.ok(matchStateService.getAllMatches(ring, "available")).build();
    }


    @GET
    @Path("/athletes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAthletes() {
        List<Athlete> list = Athlete.findAll().list();
        return Response.ok(list).build();
    }

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories() {
        List<Category> list = Category.findAll().list();
        return Response.ok(list).build();
    }


    @POST
    @Path("/categories")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createCategory(Category input) {
        if (input == null || input.name == null || input.gender == null || input.subCategory == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("name, gender and subCategory are required").build();
        }

        Category existing = Category.findByNameGenderSubcategory(input.name, input.gender, input.subCategory);
        if (existing != null) {
            return Response.status(Response.Status.CONFLICT).entity(existing).build();
        }

        Category cat = new Category(input.name, input.gender, input.subCategory);
        cat.bodyLevel = input.bodyLevel;
        cat.headLevel = input.headLevel;
        cat.rounds = input.rounds;
        cat.roundTimeMinutes = input.roundTimeMinutes;
        cat.roundTimeSeconds = input.roundTimeSeconds;
        cat.kyeShiTimeMinutes = input.kyeShiTimeMinutes;
        cat.kyeShiTimeSeconds = input.kyeShiTimeSeconds;
        cat.goldenPointEnabled = input.goldenPointEnabled;
        cat.goldenPointTimeMinutes = input.goldenPointTimeMinutes;
        cat.goldenPointTimeSeconds = input.goldenPointTimeSeconds;
        cat.differentialScore = input.differentialScore;
        cat.maxAllowedGamJeoms = input.maxAllowedGamJeoms;
        cat.persist();
        return Response.status(Response.Status.CREATED).entity(cat).build();
    }

    @POST
    @Path("/matches")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createMatch(MatchConfigurationDto dto) {
        if (dto == null || dto.getMatchNumber() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("matchNumber is required").build();
        }

        if (Match.findById(dto.getMatchNumber()) != null) {
            return Response.status(Response.Status.CONFLICT).entity("match already exists").build();
        }

        Match m = new Match(dto.getMatchNumber());
        if (dto.getMat() != null) m.mat = dto.getMat();
        m.phase = dto.getPhase();

        // category by name/gender/subCategory if provided
        MatchConfigurationDto.CategoryDto c = dto.getCategory();
        if (c != null && c.getName() != null && c.getGender() != null && c.getSubCategory() != null) {
            Category cat = Category.findByNameGenderSubcategory(c.getName(), c.getGender(), c.getSubCategory());
            m.category = cat;
        }

        // athletes by ovrInternalId if provided
        MatchConfigurationDto.AthleteDto blue = dto.getBlueAthlete();
        if (blue != null && blue.getOvrInternalId() != null) {
            m.blueAthlete = Athlete.findById(blue.getOvrInternalId());
        }
        MatchConfigurationDto.AthleteDto red = dto.getRedAthlete();
        if (red != null && red.getOvrInternalId() != null) {
            m.redAthlete = Athlete.findById(red.getOvrInternalId());
        }

        Integer videoQuota = dto.getBlueAthleteVideoQuota() != null ? dto.getBlueAthleteVideoQuota() : dto.getRedAthleteVideoQuota();
        m.videoQuota = videoQuota != null ? videoQuota : 0;
        m.matchVictoryCriteria = dto.getMatchVictoryCriteria();
        m.wtCompetitionDataProtocol = dto.getWtCompetitionDataProtocol();

        m.persist();
        return Response.status(Response.Status.CREATED).entity(matchStateService.convertMatchToDto(m)).build();
    }

    @POST
    @Path("/matches/{matchNumber}/winner")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response declareWinner(@PathParam("matchNumber") String matchNumber, java.util.Map<String, String> body) {
        if (matchNumber == null || matchNumber.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("matchNumber required").build();
        }
        String winnerSide = null;
        if (body != null) winnerSide = body.get("winnerSide");
        if (winnerSide == null || winnerSide.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("winnerSide required (e.g. BLUE or RED or home/away)").build();
        }

        tournamentService.advance(matchNumber, winnerSide);
        return Response.ok().build();
    }

}
