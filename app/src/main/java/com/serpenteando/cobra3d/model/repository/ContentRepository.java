package com.serpenteando.cobra3d.model.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serpenteando.cobra3d.model.db.Contract;
import com.serpenteando.cobra3d.model.entity.Content;

import java.util.ArrayList;
import java.util.List;

public class ContentRepository {

    private final SQLiteDatabase bancoDados;

    public ContentRepository(SQLiteDatabase bancoDados) {
        this.bancoDados = bancoDados;
    }

    public List<Content> getAll() {
        List<Content> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.ContentTable.TABLE_NAME, null, null, null,
                null, null, Contract.ContentTable.COL_DISPLAY_ORDER + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToContent(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<Content> getByCategory(String category) {
        List<Content> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.ContentTable.TABLE_NAME, null,
                Contract.ContentTable.COL_CATEGORY + " = ?",
                new String[]{category}, null, null,
                Contract.ContentTable.COL_DISPLAY_ORDER + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToContent(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public Content getById(long id) {
        Cursor cursor = bancoDados.query(Contract.ContentTable.TABLE_NAME, null,
                Contract.ContentTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Content content = cursorToContent(cursor);
            cursor.close();
            return content;
        }
        return null;
    }

    private Content cursorToContent(Cursor cursor) {
        Content content = new Content();
        content.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.ContentTable.COL_ID)));
        content.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(Contract.ContentTable.COL_CATEGORY)));
        content.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(Contract.ContentTable.COL_TITLE)));
        content.setScientificName(getString(cursor, Contract.ContentTable.COL_SCIENTIFIC_NAME));
        content.setFamily(getString(cursor, Contract.ContentTable.COL_FAMILY));
        content.setVenomous(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ContentTable.COL_IS_VENOMOUS)) == 1);
        content.setBody(cursor.getString(cursor.getColumnIndexOrThrow(Contract.ContentTable.COL_BODY)));
        content.setHabitat(getString(cursor, Contract.ContentTable.COL_HABITAT));
        content.setDistribution(getString(cursor, Contract.ContentTable.COL_DISTRIBUTION));
        content.setDiet(getString(cursor, Contract.ContentTable.COL_DIET));
        content.setReproduction(getString(cursor, Contract.ContentTable.COL_REPRODUCTION));
        content.setMedicalImportance(getString(cursor, Contract.ContentTable.COL_MEDICAL_IMPORTANCE));
        content.setCuriosity(getString(cursor, Contract.ContentTable.COL_CURIOSITY));
        content.setImageResName(getString(cursor, Contract.ContentTable.COL_IMAGE_RES_NAME));
        content.setDisplayOrder(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.ContentTable.COL_DISPLAY_ORDER)));
        return content;
    }

    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndex(column);
        return idx >= 0 ? cursor.getString(idx) : null;
    }
}
