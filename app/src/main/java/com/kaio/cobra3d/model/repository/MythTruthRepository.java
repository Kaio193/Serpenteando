package com.kaio.cobra3d.model.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kaio.cobra3d.model.db.Contract;
import com.kaio.cobra3d.model.entity.MythTruth;

import java.util.ArrayList;
import java.util.List;

public class MythTruthRepository {

    private final SQLiteDatabase db;

    public MythTruthRepository(SQLiteDatabase db) {
        this.db = db;
    }

    public List<MythTruth> getAll() {
        List<MythTruth> list = new ArrayList<>();
        Cursor c = db.query(Contract.MythsTruthsTable.TABLE_NAME, null, null, null,
                null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToMythTruth(c));
            }
            c.close();
        }
        return list;
    }

    public List<MythTruth> getMyths() {
        List<MythTruth> list = new ArrayList<>();
        Cursor c = db.query(Contract.MythsTruthsTable.TABLE_NAME, null,
                Contract.MythsTruthsTable.COL_IS_MYTH + " = 1",
                null, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToMythTruth(c));
            }
            c.close();
        }
        return list;
    }

    public List<MythTruth> getTruths() {
        List<MythTruth> list = new ArrayList<>();
        Cursor c = db.query(Contract.MythsTruthsTable.TABLE_NAME, null,
                Contract.MythsTruthsTable.COL_IS_MYTH + " = 0",
                null, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToMythTruth(c));
            }
            c.close();
        }
        return list;
    }

    private MythTruth cursorToMythTruth(Cursor c) {
        MythTruth mt = new MythTruth();
        mt.setId(c.getLong(c.getColumnIndexOrThrow(Contract.MythsTruthsTable.COL_ID)));
        mt.setStatement(c.getString(c.getColumnIndexOrThrow(Contract.MythsTruthsTable.COL_STATEMENT)));
        mt.setMyth(c.getInt(c.getColumnIndexOrThrow(Contract.MythsTruthsTable.COL_IS_MYTH)) == 1);
        mt.setExplanation(c.getString(c.getColumnIndexOrThrow(Contract.MythsTruthsTable.COL_EXPLANATION)));
        mt.setCategory(c.getString(c.getColumnIndexOrThrow(Contract.MythsTruthsTable.COL_CATEGORY)));
        return mt;
    }
}
