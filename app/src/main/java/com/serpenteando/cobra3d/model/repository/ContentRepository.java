package com.serpenteando.cobra3d.model.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serpenteando.cobra3d.model.db.Contract;
import com.serpenteando.cobra3d.model.entity.Content;

import java.util.ArrayList;
import java.util.List;

public class ContentRepository {

    private final SQLiteDatabase db;

    public ContentRepository(SQLiteDatabase db) {
        this.db = db;
    }

    public List<Content> getAll() {
        List<Content> list = new ArrayList<>();
        Cursor c = db.query(Contract.ContentTable.TABLE_NAME, null, null, null,
                null, null, Contract.ContentTable.COL_DISPLAY_ORDER + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToContent(c));
            }
            c.close();
        }
        return list;
    }

    public List<Content> getByCategory(String category) {
        List<Content> list = new ArrayList<>();
        Cursor c = db.query(Contract.ContentTable.TABLE_NAME, null,
                Contract.ContentTable.COL_CATEGORY + " = ?",
                new String[]{category}, null, null,
                Contract.ContentTable.COL_DISPLAY_ORDER + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToContent(c));
            }
            c.close();
        }
        return list;
    }

    public Content getById(long id) {
        Cursor c = db.query(Contract.ContentTable.TABLE_NAME, null,
                Contract.ContentTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (c != null && c.moveToFirst()) {
            Content content = cursorToContent(c);
            c.close();
            return content;
        }
        return null;
    }

    private Content cursorToContent(Cursor c) {
        Content content = new Content();
        content.setId(c.getLong(c.getColumnIndexOrThrow(Contract.ContentTable.COL_ID)));
        content.setCategory(c.getString(c.getColumnIndexOrThrow(Contract.ContentTable.COL_CATEGORY)));
        content.setTitle(c.getString(c.getColumnIndexOrThrow(Contract.ContentTable.COL_TITLE)));
        content.setScientificName(getString(c, Contract.ContentTable.COL_SCIENTIFIC_NAME));
        content.setFamily(getString(c, Contract.ContentTable.COL_FAMILY));
        content.setVenomous(c.getInt(c.getColumnIndexOrThrow(Contract.ContentTable.COL_IS_VENOMOUS)) == 1);
        content.setBody(c.getString(c.getColumnIndexOrThrow(Contract.ContentTable.COL_BODY)));
        content.setHabitat(getString(c, Contract.ContentTable.COL_HABITAT));
        content.setDistribution(getString(c, Contract.ContentTable.COL_DISTRIBUTION));
        content.setDiet(getString(c, Contract.ContentTable.COL_DIET));
        content.setReproduction(getString(c, Contract.ContentTable.COL_REPRODUCTION));
        content.setMedicalImportance(getString(c, Contract.ContentTable.COL_MEDICAL_IMPORTANCE));
        content.setCuriosity(getString(c, Contract.ContentTable.COL_CURIOSITY));
        content.setImageResName(getString(c, Contract.ContentTable.COL_IMAGE_RES_NAME));
        content.setDisplayOrder(c.getInt(c.getColumnIndexOrThrow(Contract.ContentTable.COL_DISPLAY_ORDER)));
        return content;
    }

    private String getString(Cursor c, String column) {
        int idx = c.getColumnIndex(column);
        return idx >= 0 ? c.getString(idx) : null;
    }
}
