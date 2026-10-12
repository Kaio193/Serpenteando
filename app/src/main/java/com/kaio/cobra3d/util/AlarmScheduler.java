package com.kaio.cobra3d.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.kaio.cobra3d.receiver.NotificationReceiver;

import java.util.Calendar;

/**
 * Agenda e cancela alarmes de notificação via AlarmManager.
 *
 * [Não existe nos slides do prof. Alexandre] — implementação extra
 * pra notificações reais. O professor pode perguntar como funciona.
 *
 * Funcionamento:
 * - Lembrete diário: todos os dias às 00:00 (meia-noite)
 * - Usa setExactAndAllowWhileIdle pra funcionar em Doze Mode (Android 6+)
 * - BootReceiver reagenda após reiniciar o celular
 *
 * [TEI Persistência] AlarmManager é o sistema de agendamento do Android
 * — diferente do slide 06 (que usa SharedPreferences/SQLite), mas
 * é a forma correta de fazer notificações agendadas.
 *
 * Uso:
 *   AlarmScheduler.scheduleDailyReminder(context);  // agenda
 *   AlarmScheduler.cancelDailyReminder(context);    // cancela
 *   AlarmScheduler.rescheduleAll(context);          // reagenda tudo (aps boot)
 */
public class AlarmScheduler {

    private static final int REQUEST_DAILY = 2001;
    private static final int REQUEST_MISSION = 2002;

    // ========== AGENDAR ==========

    /** Agenda o lembrete diário para 00:00 (meia-noite) todo dia */
    public static void scheduleDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = createPendingIntent(context,
                NotificationReceiver.ACTION_DAILY_REMINDER, REQUEST_DAILY);

        // Próxima ocorrência às 00:00 (meia-noite)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Se já passou das 00:00 hoje, agenda pra amanhã
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Android 12+ requer permissao SCHEDULE_EXACT_ALARM (grant via settings)
        // Se nao tiver, usa set() que funciona sem permissao exata
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Fallback: alarm inexacto (funciona sem permissao)
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        }
    }

    /** Cancela o lembrete diário */
    public static void cancelDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = createPendingIntent(context,
                NotificationReceiver.ACTION_DAILY_REMINDER, REQUEST_DAILY);
        alarmManager.cancel(pendingIntent);
    }

    // ========== UTILITÁRIOS ==========

    /** Reagenda todos os alarmes conforme as preferências atuais */
    public static void rescheduleAll(Context context) {
        PreferenceManager prefs = new PreferenceManager(context);

        if (prefs.isDailyReminderEnabled()) {
            scheduleDailyReminder(context);
        } else {
            cancelDailyReminder(context);
        }
    }

    /** Cria PendingIntent com action e request code únicos */
    private static PendingIntent createPendingIntent(Context context, String action, int requestCode) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
