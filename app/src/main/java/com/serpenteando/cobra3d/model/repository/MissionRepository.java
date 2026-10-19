package com.serpenteando.cobra3d.model.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serpenteando.cobra3d.model.db.Contract;
import com.serpenteando.cobra3d.model.entity.Mission;
import com.serpenteando.cobra3d.model.entity.MissionItem;

import java.util.ArrayList;
import java.util.List;

public class MissionRepository {

    private final SQLiteDatabase bancoDados;

    public MissionRepository(SQLiteDatabase bancoDados) {
        this.bancoDados = bancoDados;
    }

    public List<Mission> getAll() {
        List<Mission> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.MissionsTable.TABLE_NAME, null, null, null,
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToMission(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<Mission> getIncomplete() {
        List<Mission> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.MissionsTable.TABLE_NAME, null,
                Contract.MissionsTable.COL_IS_COMPLETED + " = 0",
                null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToMission(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public Mission getById(long id) {
        Cursor cursor = bancoDados.query(Contract.MissionsTable.TABLE_NAME, null,
                Contract.MissionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Mission m = cursorToMission(cursor);
            cursor.close();
            return m;
        }
        return null;
    }

    public List<MissionItem> getItemsByMissionId(long missionId) {
        List<MissionItem> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.MissionItemsTable.TABLE_NAME, null,
                Contract.MissionItemsTable.COL_MISSION_ID + " = ?",
                new String[]{String.valueOf(missionId)}, null, null,
                Contract.MissionItemsTable.COL_DISPLAY_ORDER + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToMissionItem(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public void markCompleted(long missionId) {
        ContentValues valores = new ContentValues();
        valores.put(Contract.MissionsTable.COL_IS_COMPLETED, 1);
        bancoDados.update(Contract.MissionsTable.TABLE_NAME, valores,
                Contract.MissionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(missionId)});
    }

    // Marca um item da missão como cumprido (a missão avança sozinha conforme as ações reais)
    public void updateItemCorrect(long itemId, boolean correct) {
        ContentValues valores = new ContentValues();
        valores.put(Contract.MissionItemsTable.COL_IS_CORRECT, correct ? 1 : 0);
        bancoDados.update(Contract.MissionItemsTable.TABLE_NAME, valores,
                Contract.MissionItemsTable.COL_ID + " = ?",
                new String[]{String.valueOf(itemId)});
    }

    /**
     * Soma os pontos das missões registradas em awarded_missions (cada
     * conclusão, inclusive repetições). É usado só para exibir o total
     * acumulado na tela de Missões; não altera a pontuação salva.
     */
    public int getAwardedPoints(String awardedIds) {
        if (awardedIds == null || awardedIds.trim().isEmpty()) return 0;
        int total = 0;
        for (String parte : awardedIds.split(",")) {
            String id = parte.trim();
            if (id.isEmpty()) continue;
            Cursor cursor = null;
            try {
                cursor = bancoDados.rawQuery(
                        "SELECT " + Contract.MissionsTable.COL_POINTS +
                                " FROM " + Contract.MissionsTable.TABLE_NAME +
                                " WHERE " + Contract.MissionsTable.COL_ID + " = ?",
                        new String[]{id});
                if (cursor != null && cursor.moveToFirst()) {
                    total += cursor.getInt(0);
                }
            } catch (Exception ignored) {
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        return total;
    }

    public int getCompletedCount() {
        Cursor cursor = bancoDados.rawQuery(
                "SELECT COUNT(*) FROM " + Contract.MissionsTable.TABLE_NAME +
                        " WHERE " + Contract.MissionsTable.COL_IS_COMPLETED + " = 1", null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    private Mission cursorToMission(Cursor cursor) {
        Mission m = new Mission();
        m.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.MissionsTable.COL_ID)));
        m.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(Contract.MissionsTable.COL_TITLE)));
        m.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(Contract.MissionsTable.COL_DESCRIPTION)));
        m.setDifficulty(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.MissionsTable.COL_DIFFICULTY)));
        m.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.MissionsTable.COL_POINTS)));
        m.setTotalDays(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.MissionsTable.COL_TOTAL_DAYS)));
        m.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.MissionsTable.COL_IS_COMPLETED)) == 1);
        return m;
    }

    private MissionItem cursorToMissionItem(Cursor cursor) {
        MissionItem item = new MissionItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_ID)));
        item.setMissionId(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_MISSION_ID)));
        item.setItemText(cursor.getString(cursor.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_ITEM_TEXT)));
        item.setTrigger(cursor.getString(cursor.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_TRIGGER)));
        item.setTarget(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_TARGET)));
        item.setCorrect(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_IS_CORRECT)) == 1);
        item.setDisplayOrder(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.MissionItemsTable.COL_DISPLAY_ORDER)));
        return item;
    }

    public long insert(Mission mission) {
        ContentValues valores = new ContentValues();
        valores.put(Contract.MissionsTable.COL_TITLE, mission.getTitle());
        valores.put(Contract.MissionsTable.COL_DESCRIPTION, mission.getDescription());
        valores.put(Contract.MissionsTable.COL_DIFFICULTY, mission.getDifficulty());
        valores.put(Contract.MissionsTable.COL_POINTS, mission.getPoints());
        valores.put(Contract.MissionsTable.COL_IS_COMPLETED, mission.isCompleted() ? 1 : 0);
        return bancoDados.insert(Contract.MissionsTable.TABLE_NAME, null, valores);
    }

    public int update(Mission mission) {
        ContentValues valores = new ContentValues();
        valores.put(Contract.MissionsTable.COL_TITLE, mission.getTitle());
        valores.put(Contract.MissionsTable.COL_DESCRIPTION, mission.getDescription());
        valores.put(Contract.MissionsTable.COL_DIFFICULTY, mission.getDifficulty());
        valores.put(Contract.MissionsTable.COL_POINTS, mission.getPoints());
        valores.put(Contract.MissionsTable.COL_IS_COMPLETED, mission.isCompleted() ? 1 : 0);
        return bancoDados.update(Contract.MissionsTable.TABLE_NAME, valores,
                Contract.MissionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(mission.getId())});
    }

    public int delete(long id) {
        return bancoDados.delete(Contract.MissionsTable.TABLE_NAME,
                Contract.MissionsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }
}
