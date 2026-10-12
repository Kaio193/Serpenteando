package com.serpenteando.cobra3d.model.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serpenteando.cobra3d.model.db.Contract;
import com.serpenteando.cobra3d.model.entity.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionRepository {

    private final SQLiteDatabase bancoDados;

    public QuestionRepository(SQLiteDatabase bancoDados) {
        this.bancoDados = bancoDados;
    }

    public List<Question> getAll() {
        List<Question> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.QuestionsTable.TABLE_NAME, null, null, null,
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToQuestion(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<Question> getByCategory(String category) {
        List<Question> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.QuestionsTable.TABLE_NAME, null,
                Contract.QuestionsTable.COL_CATEGORY + " = ?",
                new String[]{category}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToQuestion(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<Question> getByDifficulty(int difficulty) {
        List<Question> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.QuestionsTable.TABLE_NAME, null,
                Contract.QuestionsTable.COL_DIFFICULTY + " = ?",
                new String[]{String.valueOf(difficulty)}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToQuestion(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<Question> getByCategoryAndDifficulty(String category, int difficulty) {
        List<Question> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.QuestionsTable.TABLE_NAME, null,
                Contract.QuestionsTable.COL_CATEGORY + " = ? AND " +
                        Contract.QuestionsTable.COL_DIFFICULTY + " = ?",
                new String[]{category, String.valueOf(difficulty)}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToQuestion(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public Question getById(long id) {
        Cursor cursor = bancoDados.query(Contract.QuestionsTable.TABLE_NAME, null,
                Contract.QuestionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Question q = cursorToQuestion(cursor);
            cursor.close();
            return q;
        }
        return null;
    }

    public int getCount() {
        Cursor cursor = bancoDados.rawQuery("SELECT COUNT(*) FROM " + Contract.QuestionsTable.TABLE_NAME, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public long insert(Question q) {
        ContentValues valores = new ContentValues();
        valores.put(Contract.QuestionsTable.COL_QUESTION_TEXT, q.getQuestionText());
        valores.put(Contract.QuestionsTable.COL_OPTION_A, q.getOptionA());
        valores.put(Contract.QuestionsTable.COL_OPTION_B, q.getOptionB());
        valores.put(Contract.QuestionsTable.COL_OPTION_C, q.getOptionC());
        valores.put(Contract.QuestionsTable.COL_OPTION_D, q.getOptionD());
        valores.put(Contract.QuestionsTable.COL_CORRECT_ANSWER, q.getCorrectAnswer());
        valores.put(Contract.QuestionsTable.COL_DIFFICULTY, q.getDifficulty());
        valores.put(Contract.QuestionsTable.COL_CATEGORY, q.getCategory());
        valores.put(Contract.QuestionsTable.COL_EXPLANATION, q.getExplanation());
        return bancoDados.insert(Contract.QuestionsTable.TABLE_NAME, null, valores);
    }

    public int update(Question q) {
        ContentValues valores = new ContentValues();
        valores.put(Contract.QuestionsTable.COL_QUESTION_TEXT, q.getQuestionText());
        valores.put(Contract.QuestionsTable.COL_OPTION_A, q.getOptionA());
        valores.put(Contract.QuestionsTable.COL_OPTION_B, q.getOptionB());
        valores.put(Contract.QuestionsTable.COL_OPTION_C, q.getOptionC());
        valores.put(Contract.QuestionsTable.COL_OPTION_D, q.getOptionD());
        valores.put(Contract.QuestionsTable.COL_CORRECT_ANSWER, q.getCorrectAnswer());
        valores.put(Contract.QuestionsTable.COL_DIFFICULTY, q.getDifficulty());
        valores.put(Contract.QuestionsTable.COL_CATEGORY, q.getCategory());
        valores.put(Contract.QuestionsTable.COL_EXPLANATION, q.getExplanation());
        return bancoDados.update(Contract.QuestionsTable.TABLE_NAME, valores,
                Contract.QuestionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(q.getId())});
    }

    public int delete(long id) {
        return bancoDados.delete(Contract.QuestionsTable.TABLE_NAME,
                Contract.QuestionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private Question cursorToQuestion(Cursor cursor) {
        Question q = new Question();
        q.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.QuestionsTable.COL_ID)));
        q.setQuestionText(cursor.getString(cursor.getColumnIndexOrThrow(Contract.QuestionsTable.COL_QUESTION_TEXT)));
        q.setOptionA(cursor.getString(cursor.getColumnIndexOrThrow(Contract.QuestionsTable.COL_OPTION_A)));
        q.setOptionB(cursor.getString(cursor.getColumnIndexOrThrow(Contract.QuestionsTable.COL_OPTION_B)));
        q.setOptionC(cursor.getString(cursor.getColumnIndexOrThrow(Contract.QuestionsTable.COL_OPTION_C)));
        q.setOptionD(cursor.getString(cursor.getColumnIndexOrThrow(Contract.QuestionsTable.COL_OPTION_D)));
        q.setCorrectAnswer(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.QuestionsTable.COL_CORRECT_ANSWER)));
        q.setDifficulty(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.QuestionsTable.COL_DIFFICULTY)));
        q.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(Contract.QuestionsTable.COL_CATEGORY)));
        q.setExplanation(cursor.getString(cursor.getColumnIndexOrThrow(Contract.QuestionsTable.COL_EXPLANATION)));
        return q;
    }
}