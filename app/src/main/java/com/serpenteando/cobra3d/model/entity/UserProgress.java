package com.serpenteando.cobra3d.model.entity;

// [ALÉM DO TEI] POJO entity — objeto simples que representa o estado do progresso no banco de dados
public class UserProgress {

    private long id;
    private int quizHighScore;
    private int quizzesCompleted;
    private int missionsCompleted;
    private int totalPoints;
    private int currentStreak;
    private String lastQuizDate;
    private String lastTipDate;
    private String lastMissionDate;
    private int quizFacilCount;
    private int quizMedioCount;
    private int quizDificilCount;
    private int anatomyHotspotCount;
    private int anatomyLayerCount;
    private int contentDetailCount;
    private String awardedMissions;

    // [ALÉM DO TEI] Campos mapeados para colunas SQLite — lidos via Cursor pelo ProgressRepository e gravados com ContentValues
    public UserProgress() {
        this.quizHighScore = 0;
        this.quizzesCompleted = 0;
        this.missionsCompleted = 0;
        this.totalPoints = 0;
        this.currentStreak = 0;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getQuizHighScore() { return quizHighScore; }
    public void setQuizHighScore(int quizHighScore) { this.quizHighScore = quizHighScore; }

    public int getQuizzesCompleted() { return quizzesCompleted; }
    public void setQuizzesCompleted(int quizzesCompleted) { this.quizzesCompleted = quizzesCompleted; }

    public int getMissionsCompleted() { return missionsCompleted; }
    public void setMissionsCompleted(int missionsCompleted) { this.missionsCompleted = missionsCompleted; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public String getLastQuizDate() { return lastQuizDate; }
    public void setLastQuizDate(String lastQuizDate) { this.lastQuizDate = lastQuizDate; }

    public String getLastTipDate() { return lastTipDate; }
    public void setLastTipDate(String lastTipDate) { this.lastTipDate = lastTipDate; }

    /** Dia (yyyy-MM-dd) da ultima missao concluida — base da sequencia de dias. */
    public String getLastMissionDate() { return lastMissionDate; }
    public void setLastMissionDate(String lastMissionDate) { this.lastMissionDate = lastMissionDate; }

    public int getQuizFacilCount() { return quizFacilCount; }
    public void setQuizFacilCount(int quizFacilCount) { this.quizFacilCount = quizFacilCount; }

    public int getQuizMedioCount() { return quizMedioCount; }
    public void setQuizMedioCount(int quizMedioCount) { this.quizMedioCount = quizMedioCount; }

    public int getQuizDificilCount() { return quizDificilCount; }
    public void setQuizDificilCount(int quizDificilCount) { this.quizDificilCount = quizDificilCount; }

    public int getAnatomyHotspotCount() { return anatomyHotspotCount; }
    public void setAnatomyHotspotCount(int anatomyHotspotCount) { this.anatomyHotspotCount = anatomyHotspotCount; }

    public int getAnatomyLayerCount() { return anatomyLayerCount; }
    public void setAnatomyLayerCount(int anatomyLayerCount) { this.anatomyLayerCount = anatomyLayerCount; }

    public int getContentDetailCount() { return contentDetailCount; }
    public void setContentDetailCount(int contentDetailCount) { this.contentDetailCount = contentDetailCount; }

    public String getAwardedMissions() { return awardedMissions; }
    public void setAwardedMissions(String ids) { this.awardedMissions = ids; }
}
