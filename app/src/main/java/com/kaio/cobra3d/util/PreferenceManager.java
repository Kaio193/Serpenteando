package com.kaio.cobra3d.util;

import android.content.Context;
import android.content.SharedPreferences;

// [TEI Persistência] SharedPreferences: armazena preferências simples do usuário (chave-valor)
public class PreferenceManager {

    private static final String PREFS_NAME = "serpenteando_prefs";
    private static final String PREF_VOICE_ENABLED = "voice_enabled";
    private static final String PREF_DARK_MODE = "dark_mode";

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
}
