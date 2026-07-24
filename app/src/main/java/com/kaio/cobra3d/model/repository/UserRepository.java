package com.kaio.cobra3d.model.repository;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kaio.cobra3d.model.db.Contract;

// Repository para dados de usuário - preferências e configurações
public class UserRepository {

    private final SharedPreferences prefs;
    private final SQLiteDatabase db;

    public UserRepository(SharedPreferences prefs, SQLiteDatabase db) {
        this.prefs = prefs;
        this.db = db;
    }

    // Preferências de tema
    public boolean isDarkModeEnabled() {
        return prefs.getBoolean("dark_mode", false);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean("dark_mode", enabled).apply();
    }

    // Preferências de voz
    public boolean isVoiceModeEnabled() {
        return prefs.getBoolean("voice_mode", false);
    }

    public void setVoiceMode(boolean enabled) {
        prefs.edit().putBoolean("voice_mode", enabled).apply();
    }

    // Dados de usuário
    public String getUsername() {
        return prefs.getString("username", "Jogador");
    }

    public void setUsername(String username) {
        prefs.edit().putString("username", username).apply();
    }

    public String getUserId() {
        return prefs.getString("user_id", "default");
    }

    public void setUserId(String userId) {
        prefs.edit().putString("user_id", userId).apply();
    }

    // Logout - limpa dados
    public void logout() {
        prefs.edit().clear().apply();
    }

    // Versão do app
    public String getAppVersion() {
        return "0.1.0";
    }

    // Contato/informações
    public String getContactEmail() {
        return "contato@serpenteando.com.br";
    }

    public String getAppWebsite() {
        return "https://serpenteando.com.br";
    }
}
