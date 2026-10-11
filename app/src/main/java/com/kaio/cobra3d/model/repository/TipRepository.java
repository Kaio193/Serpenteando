package com.kaio.cobra3d.model.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kaio.cobra3d.model.db.Contract;
import com.kaio.cobra3d.model.entity.Tip;

import java.util.ArrayList;
import java.util.List;

public class TipRepository {

    private final SQLiteDatabase db;

    public TipRepository(SQLiteDatabase db) {
        this.db = db;
    }

    public List<Tip> getAll() {
        List<Tip> list = new ArrayList<>();
        Cursor c = db.query(Contract.TipsTable.TABLE_NAME, null, null, null,
                null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToTip(c));
            }
            c.close();
        }
        return list;
    }

    // Dica do dia: mesma dica o dia inteiro, muda automaticamente à meia-noite.
    // Usa dias desde epoch para escolher um índice estável entre as dicas ativas.
    public Tip getDailyTip() {
        Cursor count = db.rawQuery(
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

        Cursor c = db.rawQuery(
                "SELECT * FROM " + Contract.TipsTable.TABLE_NAME +
                        " WHERE " + Contract.TipsTable.COL_IS_ACTIVE + " = 1" +
                        " ORDER BY " + Contract.TipsTable.COL_ID +
                        " LIMIT 1 OFFSET ?", new String[]{String.valueOf(offset)});
        if (c != null && c.moveToFirst()) {
            Tip tip = cursorToTip(c);
            c.close();
            return tip;
        }
        return null;
    }

    public Tip getRandom() {
        Cursor c = db.rawQuery(
                "SELECT * FROM " + Contract.TipsTable.TABLE_NAME +
                        " WHERE " + Contract.TipsTable.COL_IS_ACTIVE + " = 1" +
                        " ORDER BY RANDOM() LIMIT 1", null);
        if (c != null && c.moveToFirst()) {
            Tip tip = cursorToTip(c);
            c.close();
            return tip;
        }
        return null;
    }

    public Tip getById(long id) {
        Cursor c = db.query(Contract.TipsTable.TABLE_NAME, null,
                Contract.TipsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (c != null && c.moveToFirst()) {
            Tip tip = cursorToTip(c);
            c.close();
            return tip;
        }
        return null;
    }

    public long insert(Tip tip) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.TipsTable.COL_TIP_TEXT, tip.getTipText());
        cv.put(Contract.TipsTable.COL_CATEGORY, tip.getCategory());
        cv.put(Contract.TipsTable.COL_IS_ACTIVE, tip.isActive() ? 1 : 0);
        return db.insert(Contract.TipsTable.TABLE_NAME, null, cv);
    }

    public int update(Tip tip) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.TipsTable.COL_TIP_TEXT, tip.getTipText());
        cv.put(Contract.TipsTable.COL_CATEGORY, tip.getCategory());
        cv.put(Contract.TipsTable.COL_IS_ACTIVE, tip.isActive() ? 1 : 0);
        return db.update(Contract.TipsTable.TABLE_NAME, cv,
                Contract.TipsTable.COL_ID + " = ?",
                new String[]{String.valueOf(tip.getId())});
    }

    public int delete(long id) {
        return db.delete(Contract.TipsTable.TABLE_NAME,
                Contract.TipsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private Tip cursorToTip(Cursor c) {
        Tip tip = new Tip();
        tip.setId(c.getLong(c.getColumnIndexOrThrow(Contract.TipsTable.COL_ID)));
        tip.setTipText(c.getString(c.getColumnIndexOrThrow(Contract.TipsTable.COL_TIP_TEXT)));
        tip.setCategory(c.getString(c.getColumnIndexOrThrow(Contract.TipsTable.COL_CATEGORY)));
        tip.setActive(c.getInt(c.getColumnIndexOrThrow(Contract.TipsTable.COL_IS_ACTIVE)) == 1);
        return tip;
    }
}