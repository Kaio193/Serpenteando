package com.serpenteando.cobra3d.model.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serpenteando.cobra3d.model.db.Contract;
import com.serpenteando.cobra3d.model.entity.Tip;

import java.util.ArrayList;
import java.util.List;

public class TipRepository {

    private final SQLiteDatabase bancoDados;

    public TipRepository(SQLiteDatabase bancoDados) {
        this.bancoDados = bancoDados;
    }

    public List<Tip> getAll() {
        List<Tip> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.TipsTable.TABLE_NAME, null, null, null,
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToTip(cursor));
            }
            cursor.close();
        }
        return list;
    }

    // Dica do dia: mesma dica o dia inteiro, muda automaticamente à meia-noite.
    // Usa dias desde epoch para escolher um índice estável entre as dicas ativas.
    public Tip getDailyTip() {
        Cursor count = bancoDados.rawQuery(
                "SELECT COUNT(*) FROM " + Contract.TipsTable.TABLE_NAME +
                        " WHERE " + Contract.TipsTable.COL_IS_ACTIVE + " = 1", null);
        int total = 0;
        if (count != null && count.moveToFirst()) {
            total = count.getInt(0);
            count.close();
        }
        if (total == 0) return null;

        long dayIndex = System.currentTimeMillis() / (24L * 60L * 60L * 1000L);
        int offset = (int) (dayIndex % total);

        Cursor cursor = bancoDados.rawQuery(
                "SELECT * FROM " + Contract.TipsTable.TABLE_NAME +
                        " WHERE " + Contract.TipsTable.COL_IS_ACTIVE + " = 1" +
                        " ORDER BY " + Contract.TipsTable.COL_ID +
                        " LIMIT 1 OFFSET ?", new String[]{String.valueOf(offset)});
        if (cursor != null && cursor.moveToFirst()) {
            Tip tip = cursorToTip(cursor);
            cursor.close();
            return tip;
        }
        return null;
    }

    public Tip getById(long id) {
        Cursor cursor = bancoDados.query(Contract.TipsTable.TABLE_NAME, null,
                Contract.TipsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Tip tip = cursorToTip(cursor);
            cursor.close();
            return tip;
        }
        return null;
    }

    public long insert(Tip tip) {
        ContentValues valores = new ContentValues();
        valores.put(Contract.TipsTable.COL_TIP_TEXT, tip.getTipText());
        valores.put(Contract.TipsTable.COL_CATEGORY, tip.getCategory());
        valores.put(Contract.TipsTable.COL_IS_ACTIVE, tip.isActive() ? 1 : 0);
        return bancoDados.insert(Contract.TipsTable.TABLE_NAME, null, valores);
    }

    public int update(Tip tip) {
        ContentValues valores = new ContentValues();
        valores.put(Contract.TipsTable.COL_TIP_TEXT, tip.getTipText());
        valores.put(Contract.TipsTable.COL_CATEGORY, tip.getCategory());
        valores.put(Contract.TipsTable.COL_IS_ACTIVE, tip.isActive() ? 1 : 0);
        return bancoDados.update(Contract.TipsTable.TABLE_NAME, valores,
                Contract.TipsTable.COL_ID + " = ?",
                new String[]{String.valueOf(tip.getId())});
    }

    public int delete(long id) {
        return bancoDados.delete(Contract.TipsTable.TABLE_NAME,
                Contract.TipsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private Tip cursorToTip(Cursor cursor) {
        Tip tip = new Tip();
        tip.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.TipsTable.COL_ID)));
        tip.setTipText(cursor.getString(cursor.getColumnIndexOrThrow(Contract.TipsTable.COL_TIP_TEXT)));
        tip.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(Contract.TipsTable.COL_CATEGORY)));
        tip.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.TipsTable.COL_IS_ACTIVE)) == 1);
        return tip;
    }
}