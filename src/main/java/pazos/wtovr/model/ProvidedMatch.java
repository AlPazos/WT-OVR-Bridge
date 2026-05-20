package pazos.wtovr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class ProvidedMatch {
    private String matchNumber;
    private String BlueID;
    private String RedID;
    private int CategoryID;
    private int mat;
    private String phase;
    @JsonIgnoreProperties(ignoreUnknown = true)
    private String nextCombat;
    @JsonIgnoreProperties(ignoreUnknown = true)
    private String nextMatchColor;

    public String getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(String matchNumber) {
        this.matchNumber = matchNumber;
    }

    public ProvidedMatch() {

    }

    public String getBlueID() {
        return BlueID;
    }

    public void setBlueID(String blueID) {
        BlueID = blueID;
    }

    public String getRedID() {
        return RedID;
    }

    public void setRedID(String redID) {
        RedID = redID;
    }

    public int getCategoryID() {
        return CategoryID;
    }

    public void setCategoryID(int categoryID) {
        CategoryID = categoryID;
    }

    public int getMat() {
        return mat;
    }

    public void setMat(int mat) {
        this.mat = mat;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getNextCombat() {
        return nextCombat;
    }

    public void setNextCombat(String nextCombat) {
        this.nextCombat = nextCombat;
    }

    public String getNextMatchColor() {
        return nextMatchColor;
    }

    public void setNextMatchColor(String nextMatchColor) {
        this.nextMatchColor = nextMatchColor;
    }
}
