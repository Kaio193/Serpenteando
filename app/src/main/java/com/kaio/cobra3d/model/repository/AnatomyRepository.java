package com.kaio.cobra3d.model.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kaio.cobra3d.model.db.Contract;
import com.kaio.cobra3d.model.entity.AnatomyLayer;
import com.kaio.cobra3d.model.entity.AnatomyHotspot;

import java.util.ArrayList;
import java.util.List;

public class AnatomyRepository {

    private final SQLiteDatabase db;

    public AnatomyRepository(SQLiteDatabase db) {
        this.db = db;
    }

    public List<AnatomyLayer> getAllLayers() {
        List<AnatomyLayer> list = new ArrayList<>();
        Cursor c = db.query(Contract.AnatomyLayersTable.TABLE_NAME, null, null, null,
                null, null, Contract.AnatomyLayersTable.COL_DISPLAY_ORDER + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToLayer(c));
            }
            c.close();
        }
        return list;
    }

    public AnatomyLayer getLayerById(long id) {
        Cursor c = db.query(Contract.AnatomyLayersTable.TABLE_NAME, null,
                Contract.AnatomyLayersTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (c != null && c.moveToFirst()) {
            AnatomyLayer layer = cursorToLayer(c);
            c.close();
            return layer;
        }
        return null;
    }

    public AnatomyLayer getLayerByName(String name) {
        Cursor c = db.query(Contract.AnatomyLayersTable.TABLE_NAME, null,
                Contract.AnatomyLayersTable.COL_LAYER_NAME + " = ?",
                new String[]{name}, null, null, null);
        if (c != null && c.moveToFirst()) {
            AnatomyLayer layer = cursorToLayer(c);
            c.close();
            return layer;
        }
        return null;
    }

    public List<AnatomyHotspot> getHotspotsByLayerId(long layerId) {
        List<AnatomyHotspot> list = new ArrayList<>();
        Cursor c = db.query(Contract.AnatomyHotspotsTable.TABLE_NAME, null,
                Contract.AnatomyHotspotsTable.COL_LAYER_ID + " = ?",
                new String[]{String.valueOf(layerId)}, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToHotspot(c));
            }
            c.close();
        }
        return list;
    }

    public AnatomyHotspot getHotspotById(long id) {
        Cursor c = db.query(Contract.AnatomyHotspotsTable.TABLE_NAME, null,
                Contract.AnatomyHotspotsTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (c != null && c.moveToFirst()) {
            AnatomyHotspot hotspot = cursorToHotspot(c);
            c.close();
            return hotspot;
        }
        return null;
    }

    private AnatomyLayer cursorToLayer(Cursor c) {
        AnatomyLayer layer = new AnatomyLayer();
        layer.setId(c.getLong(c.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_ID)));
        layer.setLayerName(c.getString(c.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_LAYER_NAME)));
        layer.setDisplayName(c.getString(c.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_DISPLAY_NAME)));
        layer.setDescription(c.getString(c.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_DESCRIPTION)));
        layer.setModelFileName(c.getString(c.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_MODEL_FILE_NAME)));
        layer.setDisplayOrder(c.getInt(c.getColumnIndexOrThrow(Contract.AnatomyLayersTable.COL_DISPLAY_ORDER)));
        return layer;
    }

    private AnatomyHotspot cursorToHotspot(Cursor c) {
        AnatomyHotspot hotspot = new AnatomyHotspot();
        hotspot.setId(c.getLong(c.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_ID)));
        hotspot.setLayerId(c.getLong(c.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_LAYER_ID)));
        hotspot.setHotspotName(c.getString(c.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_HOTSPOT_NAME)));
        hotspot.setDescription(c.getString(c.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_DESCRIPTION)));
        hotspot.setPositionX(c.getFloat(c.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_POSITION_X)));
        hotspot.setPositionY(c.getFloat(c.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_POSITION_Y)));
        hotspot.setPositionZ(c.getFloat(c.getColumnIndexOrThrow(Contract.AnatomyHotspotsTable.COL_POSITION_Z)));
        return hotspot;
    }
}
