package com.kaio.cobra3d.model.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kaio.cobra3d.model.db.Contract;
import com.kaio.cobra3d.model.entity.UserProgress;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// [ALÉM DO TEI] Repository Pattern — isola as queries SQL; a lógica de negócio não conhece o banco
public class ProgressRepository {

    private final SQLiteDatabase db;

    public ProgressRepository(SQLiteDatabase db) {
        this.db = db;
    }

    public UserProgress getProgress() {
        // [TEI Persistência] db.query: lê registro de progresso do SQLite
        Cursor c = db.query(Contract.UserProgressTable.TABLE_NAME, null,
                Contract.UserProgressTable.COL_ID + " = 1", null, null, null, null);
        if (c != null && c.moveToFirst()) {
            UserProgress progress = cursorToProgress(c);
            c.close();
            return progress;
        }
        UserProgress defaultProgress = new UserProgress();
        insertProgress(defaultProgress);
        return defaultProgress;
    }

    public void insertProgress(UserProgress progress) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.UserProgressTable.COL_QUIZ_HIGH_SCORE, progress.getQuizHighScore());
        cv.put(Contract.UserProgressTable.COL_QUIZZES_COMPLETED, progress.getQuizzesCompleted());
        cv.put(Contract.UserProgressTable.COL_MISSIONS_COMPLETED, progress.getMissionsCompleted());
        cv.put(Contract.UserProgressTable.COL_TOTAL_POINTS, progress.getTotalPoints());
        cv.put(Contract.UserProgressTable.COL_CURRENT_STREAK, progress.getCurrentStreak());
        cv.put(Contract.UserProgressTable.COL_QUIZ_FACIL_COUNT, progress.getQuizFacilCount());
        cv.put(Contract.UserProgressTable.COL_QUIZ_MEDIO_COUNT, progress.getQuizMedioCount());
        cv.put(Contract.UserProgressTable.COL_QUIZ_DIFICIL_COUNT, progress.getQuizDificilCount());
        cv.put(Contract.UserProgressTable.COL_ANATOMY_HOTSPOT_COUNT, progress.getAnatomyHotspotCount());
        cv.put(Contract.UserProgressTable.COL_ANATOMY_LAYER_COUNT, progress.getAnatomyLayerCount());
        cv.put(Contract.UserProgressTable.COL_CONTENT_DETAIL_COUNT, progress.getContentDetailCount());
        cv.put(Contract.UserProgressTable.COL_AWARDED_MISSIONS,
                progress.getAwardedMissions() != null ? progress.getAwardedMissions() : "");
        db.insert(Contract.UserProgressTable.TABLE_NAME, null, cv);
    }

    // Incrementa o contador de uma ação real (quiz por dificuldade, anatomia, conteúdo)
    public void incrementEvent(String trigger) {
        if (trigger == null || trigger.isEmpty()) return;
        String column = null;
        switch (trigger) {
            case "QUIZ_FACIL": column = Contract.UserProgressTable.COL_QUIZ_FACIL_COUNT; break;
            case "QUIZ_MEDIO": column = Contract.UserProgressTable.COL_QUIZ_MEDIO_COUNT; break;
            case "QUIZ_DIFICIL": column = Contract.UserProgressTable.COL_QUIZ_DIFICIL_COUNT; break;
            case "ANATOMY_HOTSPOT": column = Contract.UserProgressTable.COL_ANATOMY_HOTSPOT_COUNT; break;
            case "ANATOMY_LAYER": column = Contract.UserProgressTable.COL_ANATOMY_LAYER_COUNT; break;
            case "CONTENT_DETAIL": column = Contract.UserProgressTable.COL_CONTENT_DETAIL_COUNT; break;
            default: return;
        }
        db.execSQL("UPDATE " + Contract.UserProgressTable.TABLE_NAME +
                " SET " + column + " = " + column + " + 1" +
                " WHERE " + Contract.UserProgressTable.COL_ID + " = 1");
    }

    // Quantas vezes o usuário já realizou a ação X (usado pelas missões)
    public int getEventCount(String trigger) {
        if (trigger == null) return 0;
        String column = null;
        switch (trigger) {
            case "QUIZ_FACIL": column = Contract.UserProgressTable.COL_QUIZ_FACIL_COUNT; break;
            case "QUIZ_MEDIO": column = Contract.UserProgressTable.COL_QUIZ_MEDIO_COUNT; break;
            case "QUIZ_DIFICIL": column = Contract.UserProgressTable.COL_QUIZ_DIFICIL_COUNT; break;
            case "ANATOMY_HOTSPOT": column = Contract.UserProgressTable.COL_ANATOMY_HOTSPOT_COUNT; break;
            case "ANATOMY_LAYER": column = Contract.UserProgressTable.COL_ANATOMY_LAYER_COUNT; break;
            case "CONTENT_DETAIL": column = Contract.UserProgressTable.COL_CONTENT_DETAIL_COUNT; break;
            default: return 0;
        }
        Cursor c = db.rawQuery("SELECT " + column +
                " FROM " + Contract.UserProgressTable.TABLE_NAME +
                " WHERE " + Contract.UserProgressTable.COL_ID + " = 1", null);
        int value = 0;
        if (c != null && c.moveToFirst()) {
            value = c.getInt(0);
            c.close();
        }
        return value;
    }

    public void updateQuizScore(int score) {
        UserProgress progress = getProgress();
        ContentValues cv = new ContentValues();
        String hoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (score > progress.getQuizHighScore()) {
            cv.put(Contract.UserProgressTable.COL_QUIZ_HIGH_SCORE, score);
        }
        cv.put(Contract.UserProgressTable.COL_QUIZZES_COMPLETED, progress.getQuizzesCompleted() + 1);
        cv.put(Contract.UserProgressTable.COL_TOTAL_POINTS, progress.getTotalPoints() + score);

        // Sequência de dias seguidos jogando (card "sequência" da Home):
        // antes essa coluna nunca era escrita e o card ficava sempre em 0.
        cv.put(Contract.UserProgressTable.COL_CURRENT_STREAK,
                calcularSequencia(progress.getLastQuizDate(), progress.getCurrentStreak(), hoje));
        cv.put(Contract.UserProgressTable.COL_LAST_QUIZ_DATE, hoje);
        cv.put(Contract.UserProgressTable.COL_UPDATED_AT,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(Contract.UserProgressTable.TABLE_NAME, cv,
                Contract.UserProgressTable.COL_ID + " = 1", null);
    }

    // Regra da sequência: primeiro quiz = 1 · jogar de novo no mesmo dia mantém
    // · jogar no dia seguinte soma 1 · pular um dia reinicia em 1.
    private int calcularSequencia(String ultimoDia, int sequenciaAtual, String hoje) {
        if (ultimoDia == null || ultimoDia.trim().isEmpty()) return 1;
        if (hoje.equals(ultimoDia.trim())) return Math.max(sequenciaAtual, 1);
        if (ehDiaAnterior(ultimoDia.trim(), hoje)) return Math.max(sequenciaAtual, 0) + 1;
        return 1;
    }

    private boolean ehDiaAnterior(String dia, String hoje) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            fmt.setLenient(false);
            Date referencia = fmt.parse(hoje);
            if (referencia == null) return false;
            Date ontem = new Date(referencia.getTime() - 24L * 60 * 60 * 1000);
            return fmt.format(ontem).equals(dia);
        } catch (Exception e) {
            return false;
        }
    }

    public void updateMissionCompleted(long missionId, int points) {
        UserProgress progress = getProgress();
        ContentValues cv = new ContentValues();
        cv.put(Contract.UserProgressTable.COL_MISSIONS_COMPLETED, progress.getMissionsCompleted() + 1);
        cv.put(Contract.UserProgressTable.COL_TOTAL_POINTS, progress.getTotalPoints() + points);
        // Registra o id da missão premiada (ex.: "1,3") para não premiar a mesma missão 2x
        String current = progress.getAwardedMissions();
        String sep = (current == null || current.trim().isEmpty()) ? "" : ",";
        cv.put(Contract.UserProgressTable.COL_AWARDED_MISSIONS,
                (current == null ? "" : current.trim()) + sep + missionId);
        cv.put(Contract.UserProgressTable.COL_UPDATED_AT,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(Contract.UserProgressTable.TABLE_NAME, cv,
                Contract.UserProgressTable.COL_ID + " = 1", null);
    }

    private UserProgress cursorToProgress(Cursor c) {
        UserProgress progress = new UserProgress();
        progress.setId(c.getLong(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_ID)));
        progress.setQuizHighScore(c.getInt(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZ_HIGH_SCORE)));
        progress.setQuizzesCompleted(c.getInt(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZZES_COMPLETED)));
        progress.setMissionsCompleted(c.getInt(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_MISSIONS_COMPLETED)));
        progress.setTotalPoints(c.getInt(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_TOTAL_POINTS)));
        progress.setCurrentStreak(c.getInt(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_CURRENT_STREAK)));
        progress.setLastQuizDate(c.getString(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_LAST_QUIZ_DATE)));
        progress.setLastTipDate(c.getString(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_LAST_TIP_DATE)));
        progress.setQuizFacilCount(c.getInt(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZ_FACIL_COUNT)));
        progress.setQuizMedioCount(c.getInt(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZ_MEDIO_COUNT)));
        progress.setQuizDificilCount(c.getInt(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_QUIZ_DIFICIL_COUNT)));
        progress.setAnatomyHotspotCount(c.getInt(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_ANATOMY_HOTSPOT_COUNT)));
        progress.setAnatomyLayerCount(c.getInt(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_ANATOMY_LAYER_COUNT)));
        progress.setContentDetailCount(c.getInt(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_CONTENT_DETAIL_COUNT)));
        progress.setAwardedMissions(c.getString(c.getColumnIndexOrThrow(Contract.UserProgressTable.COL_AWARDED_MISSIONS)));
        return progress;
    }
}
