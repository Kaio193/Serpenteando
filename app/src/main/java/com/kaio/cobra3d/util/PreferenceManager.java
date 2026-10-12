package com.kaio.cobra3d.util;

import android.content.Context;
import android.content.SharedPreferences;

// [TEI Persistência] SharedPreferences: armazena preferências simples do usuário (chave-valor)
public class PreferenceManager {

    private static final String PREFS_NAME = "serpenteando_prefs";
    private static final String PREF_VOICE_ENABLED = "voice_enabled";
    private static final String PREF_DARK_MODE = "dark_mode";
    private static final String PREF_LAST_MISSION = "last_mission_id";
    private static final String PREF_NOTIF_DAILY = "notif_daily";
    private static final String PREF_NOTIF_MISSIONS = "notif_missions";
    private static final String PREF_NOTIF_ACHIEVEMENTS = "notif_achievements";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        // [TEI Persistência] getSharedPreferences: obtém arquivo de preferências do app
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isVoiceEnabled() {
        return prefs.getBoolean(PREF_VOICE_ENABLED, false);
    }

    public void setVoiceEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_VOICE_ENABLED, enabled).apply();
    }

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(PREF_DARK_MODE, true);
    }

    public void setDarkModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_DARK_MODE, enabled).apply();
    }

    public long getLastMissionId() {
        return prefs.getLong(PREF_LAST_MISSION, -1);
    }

    public void setLastMissionId(long missionId) {
        prefs.edit().putLong(PREF_LAST_MISSION, missionId).apply();
    }

    // ========== Notificações ==========
    public boolean isDailyReminderEnabled() {
        return prefs.getBoolean(PREF_NOTIF_DAILY, true);
    }

    public void setDailyReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_NOTIF_DAILY, enabled).apply();
    }

    public boolean isMissionNotifEnabled() {
        return prefs.getBoolean(PREF_NOTIF_MISSIONS, true);
    }

    public void setMissionNotifEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_NOTIF_MISSIONS, enabled).apply();
    }

    public boolean isAchievementNotifEnabled() {
        return prefs.getBoolean(PREF_NOTIF_ACHIEVEMENTS, true);
    }

    public void setAchievementNotifEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_NOTIF_ACHIEVEMENTS, enabled).apply();
    }
}
