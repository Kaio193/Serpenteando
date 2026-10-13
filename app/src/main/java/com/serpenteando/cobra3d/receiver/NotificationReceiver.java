package com.serpenteando.cobra3d.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.model.repository.MissionRepository;
import com.serpenteando.cobra3d.util.AlarmScheduler;
import com.serpenteando.cobra3d.util.NotificationHelper;
import com.serpenteando.cobra3d.util.PreferenceManager;

// Recebe o alarme das 00:00 e mostra a notificação certa
// No fim de cada disparo ele reagenda pro dia seguinte, porque o AlarmManager
// só toca uma vez por agendamento. O texto sai no idioma escolhido no app.
public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_DAILY_REMINDER =
            "com.serpenteando.cobra3d.ACTION_DAILY_REMINDER";
    public static final String ACTION_MISSIONS_REMINDER =
            "com.serpenteando.cobra3d.ACTION_MISSIONS_REMINDER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        PreferenceManager prefs = new PreferenceManager(context);
        NotificationHelper notificacoes = new NotificationHelper(context);

        switch (intent.getAction()) {
            case ACTION_DAILY_REMINDER:
                if (prefs.isDailyReminderEnabled()) {
                    notificacoes.sendDailyReminder();
                }
                // Emenda o dia seguinte: é o que torna o lembrete diário
                AlarmScheduler.scheduleDailyReminder(context);
                break;

            case ACTION_MISSIONS_REMINDER:
                if (prefs.isMissionNotifEnabled()) {
                    // Sem missão pendente o helper não notifica nada
                    notificacoes.sendPendingMissionsReminder(contarMissoesPendentes(context));
                }
                AlarmScheduler.scheduleMissionsReminder(context);
                break;
        }
    }

    // Quantas missões ainda estão pendentes (0 se não conseguir ler)
    private int contarMissoesPendentes(Context context) {
        try {
            MissionRepository repositorio = new MissionRepository(
                    DatabaseHelper.getInstance(context).getReadableDatabase());
            return repositorio.getIncomplete().size();
        } catch (Exception e) {
            return 0;
        }
    }
}
