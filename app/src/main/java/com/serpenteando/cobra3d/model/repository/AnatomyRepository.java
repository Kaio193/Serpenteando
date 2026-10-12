package com.serpenteando.cobra3d.model.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.serpenteando.cobra3d.model.db.Contract;
import com.serpenteando.cobra3d.model.entity.AnatomyLayer;
import com.serpenteando.cobra3d.model.entity.AnatomyHotspot;

import java.util.ArrayList;
import java.util.List;

public class AnatomyRepository {

    private final SQLiteDatabase bancoDados;

    public AnatomyRepository(SQLiteDatabase bancoDados) {
        this.bancoDados = bancoDados;
    }

    public List<AnatomyLayer> getAllLayers() {
        List<AnatomyLayer> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.AnatomyLayersTable.TABLE_NAME, null, null, null,
                null, null, Contract.AnatomyLayersTable.COL_DISPLAY_ORDER + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToLayer(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public AnatomyLayer getLayerById(long id) {
        Cursor cursor = bancoDados.query(Contract.AnatomyLayersTable.TABLE_NAME, null,
                Contract.AnatomyLayersTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            AnatomyLayer layer = cursorToLayer(cursor);
            cursor.close();
            return layer;
        }
        return null;
    }

    public AnatomyLayer getLayerByName(String name) {
        Cursor cursor = bancoDados.query(Contract.AnatomyLayersTable.TABLE_NAME, null,
                Contract.AnatomyLayersTable.COL_LAYER_NAME + " = ?",
                new String[]{name}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            AnatomyLayer layer = cursorToLayer(cursor);
            cursor.close();
            return layer;
        }
        return null;
    }

    public List<AnatomyHotspot> getHotspotsByLayerId(long layerId) {
        List<AnatomyHotspot> list = new ArrayList<>();
        Cursor cursor = bancoDados.query(Contract.AnatomyHotspotsTable.TABLE_NAME, null,
                Contract.AnatomyHotspotsTable.COL_LAYER_ID + " = ?",
                new String[]{String.valueOf(layerId)}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToHotspot(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public AnatomyHotspot getHotspotById(long id) {
        Cursor cursor = bancoDados.query(Contract.AnatomyHotspotsTable.TABLE_NAME, null,
                Contract.AnatomyHotspotsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            AnatomyHotspot hotspot = cursorToHotspot(cursor);
            cursor.close();
            return hotspot;
        }
        return null;
    }

    private AnatomyLayer cursorToLayer(Cursor cursor) {
        AnatomyLayer layer = new AnatomyLayer();
        layer.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_ID)));
        layer.setLayerName(cursor.getString(cursor.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_LAYER_NAME)));
        layer.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_DISPLAY_NAME)));
        layer.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_DESCRIPTION)));
        layer.setModelFileName(cursor.getString(cursor.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_MODEL_FILE_NAME)));
        layer.setDisplayOrder(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_DISPLAY_ORDER)));
        return layer;
    }

    private AnatomyHotspot cursorToHotspot(Cursor cursor) {
        AnatomyHotspot hotspot = new AnatomyHotspot();
        hotspot.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_ID)));
        hotspot.setLayerId(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_LAYER_ID)));
        hotspot.setHotspotName(cursor.getString(cursor.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_HOTSPOT_NAME)));
        hotspot.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_DESCRIPTION)));
        hotspot.setPositionX(cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_POSITION_X)));
        hotspot.setPositionY(cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_POSITION_Y)));
        hotspot.setPositionZ(cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_POSITION_Z)));
        return hotspot;
    }
}
