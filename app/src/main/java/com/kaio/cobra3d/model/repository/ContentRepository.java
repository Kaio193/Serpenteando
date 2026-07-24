package com.kaio.cobra3d.model.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kaio.cobra3d.model.db.Contract;
import com.kaio.cobra3d.model.entity.Content;

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
        content.setBody(c.getString(c.getColumnIndexOrThrow(Contract.ContentTable.COL_BODY)));
        content.setImageResName(c.getString(c.getColumnIndexOrThrow(Contract.ContentTable.COL_IMAGE_RES_NAME)));
        content.setDisplayOrder(c.getInt(c.getColumnIndexOrThrow(Contract.ContentTable.COL_DISPLAY_ORDER)));
        return content;
    }
}
