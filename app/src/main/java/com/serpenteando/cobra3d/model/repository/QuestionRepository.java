package com.serpenteando.cobra3d.model.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serpenteando.cobra3d.model.db.Contract;
import com.serpenteando.cobra3d.model.entity.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionRepository {

    private final SQLiteDatabase db;

    public QuestionRepository(SQLiteDatabase db) {
        this.db = db;
    }

    public List<Question> getAll() {
        List<Question> list = new ArrayList<>();
        Cursor c = db.query(Contract.QuestionsTable.TABLE_NAME, null, null, null,
                null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToQuestion(c));
            }
            c.close();
        }
        return list;
    }

    public List<Question> getByCategory(String category) {
        List<Question> list = new ArrayList<>();
        Cursor c = db.query(Contract.QuestionsTable.TABLE_NAME, null,
                Contract.QuestionsTable.COL_CATEGORY + " = ?",
                new String[]{category}, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToQuestion(c));
            }
            c.close();
        }
        return list;
    }

    public List<Question> getByDifficulty(int difficulty) {
        List<Question> list = new ArrayList<>();
        Cursor c = db.query(Contract.QuestionsTable.TABLE_NAME, null,
                Contract.QuestionsTable.COL_DIFFICULTY + " = ?",
                new String[]{String.valueOf(difficulty)}, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToQuestion(c));
            }
            c.close();
        }
        return list;
    }

    public List<Question> getByCategoryAndDifficulty(String category, int difficulty) {
        List<Question> list = new ArrayList<>();
        Cursor c = db.query(Contract.QuestionsTable.TABLE_NAME, null,
                Contract.QuestionsTable.COL_CATEGORY + " = ? AND " +
                        Contract.QuestionsTable.COL_DIFFICULTY + " = ?",
                new String[]{category, String.valueOf(difficulty)}, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToQuestion(c));
            }
            c.close();
        }
        return list;
    }

    public Question getById(long id) {
        Cursor c = db.query(Contract.QuestionsTable.TABLE_NAME, null,
                Contract.QuestionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (c != null && c.moveToFirst()) {
            Question q = cursorToQuestion(c);
            c.close();
            return q;
        }
        return null;
    }

    public int getCount() {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + Contract.QuestionsTable.TABLE_NAME, null);
        int count = 0;
        if (c != null && c.moveToFirst()) {
            count = c.getInt(0);
            c.close();
        }
        return count;
    }

    public long insert(Question q) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.QuestionsTable.COL_QUESTION_TEXT, q.getQuestionText());
        cv.put(Contract.QuestionsTable.COL_OPTION_A, q.getOptionA());
        cv.put(Contract.QuestionsTable.COL_OPTION_B, q.getOptionB());
        cv.put(Contract.QuestionsTable.COL_OPTION_C, q.getOptionC());
        cv.put(Contract.QuestionsTable.COL_OPTION_D, q.getOptionD());
        cv.put(Contract.QuestionsTable.COL_CORRECT_ANSWER, q.getCorrectAnswer());
        cv.put(Contract.QuestionsTable.COL_DIFFICULTY, q.getDifficulty());
        cv.put(Contract.QuestionsTable.COL_CATEGORY, q.getCategory());
        cv.put(Contract.QuestionsTable.COL_EXPLANATION, q.getExplanation());
        return db.insert(Contract.QuestionsTable.TABLE_NAME, null, cv);
    }

    public int update(Question q) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.QuestionsTable.COL_QUESTION_TEXT, q.getQuestionText());
        cv.put(Contract.QuestionsTable.COL_OPTION_A, q.getOptionA());
        cv.put(Contract.QuestionsTable.COL_OPTION_B, q.getOptionB());
        cv.put(Contract.QuestionsTable.COL_OPTION_C, q.getOptionC());
        cv.put(Contract.QuestionsTable.COL_OPTION_D, q.getOptionD());
        cv.put(Contract.QuestionsTable.COL_CORRECT_ANSWER, q.getCorrectAnswer());
        cv.put(Contract.QuestionsTable.COL_DIFFICULTY, q.getDifficulty());
        cv.put(Contract.QuestionsTable.COL_CATEGORY, q.getCategory());
        cv.put(Contract.QuestionsTable.COL_EXPLANATION, q.getExplanation());
        return db.update(Contract.QuestionsTable.TABLE_NAME, cv,
                Contract.QuestionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(q.getId())});
    }

    public int delete(long id) {
        return db.delete(Contract.QuestionsTable.TABLE_NAME,
                Contract.QuestionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private Question cursorToQuestion(Cursor c) {
        Question q = new Question();
        q.setId(c.getLong(c.getColumnIndexOrThrow(Contract.QuestionsTable.COL_ID)));
        q.setQuestionText(c.getString(c.getColumnIndexOrThrow(Contract.QuestionsTable.COL_QUESTION_TEXT)));
        q.setOptionA(c.getString(c.getColumnIndexOrThrow(Contract.QuestionsTable.COL_OPTION_A)));
        q.setOptionB(c.getString(c.getColumnIndexOrThrow(Contract.QuestionsTable.COL_OPTION_B)));
        q.setOptionC(c.getString(c.getColumnIndexOrThrow(Contract.QuestionsTable.COL_OPTION_C)));
        q.setOptionD(c.getString(c.getColumnIndexOrThrow(Contract.QuestionsTable.COL_OPTION_D)));
        q.setCorrectAnswer(c.getInt(c.getColumnIndexOrThrow(Contract.QuestionsTable.COL_CORRECT_ANSWER)));
        q.setDifficulty(c.getInt(c.getColumnIndexOrThrow(Contract.QuestionsTable.COL_DIFFICULTY)));
        q.setCategory(c.getString(c.getColumnIndexOrThrow(Contract.QuestionsTable.COL_CATEGORY)));
        q.setExplanation(c.getString(c.getColumnIndexOrThrow(Contract.QuestionsTable.COL_EXPLANATION)));
        return q;
    }
}