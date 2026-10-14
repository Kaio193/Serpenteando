package com.serpenteando.cobra3d.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.serpenteando.cobra3d.util.AlarmScheduler;
import com.serpenteando.cobra3d.util.NotificationHelper;
import com.serpenteando.cobra3d.util.PreferenceManager;

// Recebe o alarme das 00:00 e mostra a notificação certa
// No fim de cada disparo ele reagenda pro dia seguinte, porque o AlarmManager
// só toca uma vez por agendamento. O texto sai no idioma escolhido no app.
public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_DAILY_REMINDER =
            "com.serpenteando.cobra3d.ACTION_DAILY_REMINDER";

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
        }
    }
}
