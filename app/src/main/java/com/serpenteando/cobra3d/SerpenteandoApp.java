package com.serpenteando.cobra3d;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.filament.utils.Utils;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.util.AlarmScheduler;
import com.serpenteando.cobra3d.util.NotificationHelper;
import com.serpenteando.cobra3d.util.PreferenceManager;

// Primeiro código que roda quando o app abre
// Aqui a gente sobe o Filament, confere o conteúdo do banco, aplica o tema
// salvo e reagenda os alarmes das notificações
public class SerpenteandoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init();

        // Se os JSONs de conteúdo mudaram, atualiza o banco de quem já tem o app
        DatabaseHelper.ensureContentVersion(this);

        // Volta com o tema que o usuário escolheu nas Ajustes
        AppCompatDelegate.setDefaultNightMode(
                new PreferenceManager(this).isDarkModeEnabled()
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);

        // Cria os canais das notificações
        new NotificationHelper(this);
        // Reagenda os alarmes (cancela antes de agendar, então não duplica)
        try {
            AlarmScheduler.rescheduleAll(this);
        } catch (Exception e) {
            android.util.Log.w("SerpenteandoApp", "Falha ao agendar alarme", e);
        }
    }
}
