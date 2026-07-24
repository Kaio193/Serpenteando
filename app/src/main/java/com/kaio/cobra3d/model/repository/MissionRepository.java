package com.kaio.cobra3d.model.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kaio.cobra3d.model.db.Contract;
import com.kaio.cobra3d.model.entity.Mission;
import com.kaio.cobra3d.model.entity.MissionItem;

import java.util.ArrayList;
import java.util.List;

public class MissionRepository {

    private final SQLiteDatabase db;

    public MissionRepository(SQLiteDatabase db) {
        this.db = db;
    }

    public List<Mission> getAll() {
        List<Mission> list = new ArrayList<>();
        Cursor c = db.query(Contract.MissionsTable.TABLE_NAME, null, null, null,
                null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToMission(c));
            }
            c.close();
        }
        return list;
    }

    public List<Mission> getIncomplete() {
        List<Mission> list = new ArrayList<>();
        Cursor c = db.query(Contract.MissionsTable.TABLE_NAME, null,
                Contract.MissionsTable.COL_IS_COMPLETED + " = 0",
                null, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToMission(c));
            }
            c.close();
        }
        return list;
    }

    public Mission getById(long id) {
        Cursor c = db.query(Contract.MissionsTable.TABLE_NAME, null,
                Contract.MissionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (c != null && c.moveToFirst()) {
            Mission m = cursorToMission(c);
            c.close();
            return m;
        }
        return null;
    }

    public List<MissionItem> getItemsByMissionId(long missionId) {
        List<MissionItem> list = new ArrayList<>();
        Cursor c = db.query(Contract.MissionItemsTable.TABLE_NAME, null,
                Contract.MissionItemsTable.COL_MISSION_ID + " = ?",
                new String[]{String.valueOf(missionId)}, null, null,
                Contract.MissionItemsTable.COL_DISPLAY_ORDER + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToMissionItem(c));
            }
            c.close();
        }
        return list;
    }

    public void markCompleted(long missionId) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.MissionsTable.COL_IS_COMPLETED, 1);
        db.update(Contract.MissionsTable.TABLE_NAME, cv,
                Contract.MissionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(missionId)});
    }

    public int getCompletedCount() {
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + Contract.MissionsTable.TABLE_NAME +
                        " WHERE " + Contract.MissionsTable.COL_IS_COMPLETED + " = 1", null);
        int count = 0;
        if (c != null && c.moveToFirst()) {
            count = c.getInt(0);
            c.close();
        }
        return count;
    }

    private Mission cursorToMission(Cursor c) {
        Mission m = new Mission();
        m.setId(c.getLong(c.getColumnIndexOrThrow(Contract.MissionsTable.COL_ID)));
        m.setTitle(c.getString(c.getColumnIndexOrThrow(Contract.MissionsTable.COL_TITLE)));
        m.setDescription(c.getString(c.getColumnIndexOrThrow(Contract.MissionsTable.COL_DESCRIPTION)));
        m.setDifficulty(c.getInt(c.getColumnIndexOrThrow(Contract.MissionsTable.COL_DIFFICULTY)));
        m.setPoints(c.getInt(c.getColumnIndexOrThrow(Contract.MissionsTable.COL_POINTS)));
        m.setCompleted(c.getInt(c.getColumnIndexOrThrow(Contract.MissionsTable.COL_IS_COMPLETED)) == 1);
        return m;
    }

    private MissionItem cursorToMissionItem(Cursor c) {
        MissionItem item = new MissionItem();
        item.setId(c.getLong(c.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_ID)));
        item.setMissionId(c.getLong(c.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_MISSION_ID)));
        item.setItemText(c.getString(c.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_ITEM_TEXT)));
        item.setCorrect(c.getInt(c.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_IS_CORRECT)) == 1);
        item.setDisplayOrder(c.getInt(c.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_DISPLAY_ORDER)));
        return item;
    }

    public long insert(Mission mission) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.MissionsTable.COL_TITLE, mission.getTitle());
        cv.put(Contract.MissionsTable.COL_DESCRIPTION, mission.getDescription());
        cv.put(Contract.MissionsTable.COL_DIFFICULTY, mission.getDifficulty());
        cv.put(Contract.MissionsTable.COL_POINTS, mission.getPoints());
        cv.put(Contract.MissionsTable.COL_IS_COMPLETED, mission.isCompleted() ? 1 : 0);
        return db.insert(Contract.MissionsTable.TABLE_NAME, null, cv);
    }

    public int update(Mission mission) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.MissionsTable.COL_TITLE, mission.getTitle());
        cv.put(Contract.MissionsTable.COL_DESCRIPTION, mission.getDescription());
        cv.put(Contract.MissionsTable.COL_DIFFICULTY, mission.getDifficulty());
        cv.put(Contract.MissionsTable.COL_POINTS, mission.getPoints());
        cv.put(Contract.MissionsTable.COL_IS_COMPLETED, mission.isCompleted() ? 1 : 0);
        return db.update(Contract.MissionsTable.TABLE_NAME, cv,
                Contract.MissionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(mission.getId())});
    }

    public int delete(long id) {
        return db.delete(Contract.MissionsTable.TABLE_NAME,
                Contract.MissionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }
}
