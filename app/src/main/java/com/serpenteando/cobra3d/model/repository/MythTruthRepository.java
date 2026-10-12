package com.serpenteando.cobra3d.model.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serpenteando.cobra3d.model.db.Contract;
import com.serpenteando.cobra3d.model.entity.MythTruth;

import java.util.ArrayList;
import java.util.List;

public class MythTruthRepository {

    private final SQLiteDatabase bancoDados;

    public MythTruthRepository(SQLiteDatabase bancoDados) {
        this.bancoDados = bancoDados;
    }

    public List<MythTruth> getAll() {
        List<MythTruth> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.MythsTruthsTable.TABLE_NAME, null, null, null,
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToMythTruth(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<MythTruth> getMyths() {
        List<MythTruth> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.MythsTruthsTable.TABLE_NAME, null,
                Contract.MythsTruthsTable.COL_IS_MYTH + " = 1",
                null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToMythTruth(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<MythTruth> getTruths() {
        List<MythTruth> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.MythsTruthsTable.TABLE_NAME, null,
                Contract.MythsTruthsTable.COL_IS_MYTH + " = 0",
                null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToMythTruth(cursor));
            }
            cursor.close();
        }
        return list;
    }

    private MythTruth cursorToMythTruth(Cursor cursor) {
        MythTruth mt = new MythTruth();
        mt.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.MythsTruthsTable.COL_ID)));
        mt.setStatement(cursor.getString(cursor.getColumnIndexOrThrow(Contract.MythsTruthsTable.COL_STATEMENT)));
        mt.setMyth(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.MythsTruthsTable.COL_IS_MYTH)) == 1);
        mt.setExplanation(cursor.getString(cursor.getColumnIndexOrThrow(Contract.MythsTruthsTable.COL_EXPLANATION)));
        mt.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(Contract.MythsTruthsTable.COL_CATEGORY)));
        return mt;
    }
}
