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
        db.insert(Contract.UserProgressTable.TABLE_NAME, null, cv);
    }

    public void updateQuizScore(int score) {
        UserProgress progress = getProgress();
        ContentValues cv = new ContentValues();
        if (score > progress.getQuizHighScore()) {
            cv.put(Contract.UserProgressTable.COL_QUIZ_HIGH_SCORE, score);
        }
        cv.put(Contract.UserProgressTable.COL_QUIZZES_COMPLETED, progress.getQuizzesCompleted() + 1);
        cv.put(Contract.UserProgressTable.COL_TOTAL_POINTS, progress.getTotalPoints() + score);
        cv.put(Contract.UserProgressTable.COL_LAST_QUIZ_DATE,
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        cv.put(Contract.UserProgressTable.COL_UPDATED_AT,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(Contract.UserProgressTable.TABLE_NAME, cv,
                Contract.UserProgressTable.COL_ID + " = 1", null);
    }

    public void updateMissionCompleted(int points) {
        UserProgress progress = getProgress();
        ContentValues cv = new ContentValues();
        cv.put(Contract.UserProgressTable.COL_MISSIONS_COMPLETED, progress.getMissionsCompleted() + 1);
        cv.put(Contract.UserProgressTable.COL_TOTAL_POINTS, progress.getTotalPoints() + points);
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
        return progress;
    }
}
