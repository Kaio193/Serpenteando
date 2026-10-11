package com.kaio.cobra3d;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.filament.utils.Utils;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.util.AlarmScheduler;
import com.kaio.cobra3d.util.NotificationHelper;
import com.kaio.cobra3d.util.PreferenceManager;

/**
 * Application entry point.
 *
 * Inicializa o Utils do Filament aqui (carrega a native lib filament-jni uma ǧnica vez
 * no boot do processo). Tentar inicializar em outro lugar �?" p.ex. em uma Activity
 * qualquer �?" pode falhar com UnsatisfiedLinkError em cold start, porque a lib s�� Ǹ
 * resolvida quando alguǸm chama Utils.init() antes de qualquer uso de Engine.
 */
public class SerpenteandoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init();

        // Se o conteúdo (JSONs) mudou desde a última abertura, repopula o banco.
        // Assim a missão do 3D e as dicas novas aparecem em quem já tinha o app.
        DatabaseHelper.ensureContentVersion(this);

        // Restaura o tema salvo nas Ajustes — sem isso o toggle de Modo Escuro
        // funciona só até fechar o app e ele volta a seguir o tema do sistema
        AppCompatDelegate.setDefaultNightMode(
                new PreferenceManager(this).isDarkModeEnabled()
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);

        // Inicializa canais de notificacao
        new NotificationHelper(this);
        // Agenda alarme diario (com try-catch por seguranca)
        try {
            PreferenceManager prefs = new PreferenceManager(this);
            if (prefs.isDailyReminderEnabled()) {
                AlarmScheduler.scheduleDailyReminder(this);
            }
        } catch (Exception e) {
            android.util.Log.w("SerpenteandoApp", "Falha ao agendar alarme", e);
        }
    }
}
