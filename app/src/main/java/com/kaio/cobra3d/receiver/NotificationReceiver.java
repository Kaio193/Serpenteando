package com.kaio.cobra3d.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kaio.cobra3d.util.NotificationHelper;
import com.kaio.cobra3d.util.PreferenceManager;

/**
 * Recebe o alarme diário e dispara a notificação de lembrete.
 *
 * Registrado via AlarmScheduler — o alarme dispara todo dia às 19:00.
 * O receiver verifica se a preferência ainda está ativa antes de notificar.
 */
public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_DAILY_REMINDER =
            "com.kaio.cobra3d.ACTION_DAILY_REMINDER";
    public static final String ACTION_MISSION_AVAILABLE =
            "com.kaio.cobra3d.ACTION_MISSION_AVAILABLE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        PreferenceManager prefs = new PreferenceManager(context);
        NotificationHelper notifHelper = new NotificationHelper(context);

        switch (intent.getAction()) {
            case ACTION_DAILY_REMINDER:
                if (prefs.isDailyReminderEnabled()) {
                    notifHelper.sendDailyReminder();
                }
                break;

            case ACTION_MISSION_AVAILABLE:
                if (prefs.isMissionNotifEnabled()) {
                    notifHelper.sendMissionAvailable();
                }
                break;
        }
    }
}
