package com.serpenteando.cobra3d.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.serpenteando.cobra3d.receiver.NotificationReceiver;

import java.util.Calendar;

// Agenda e cancela os alarmes das notificações (AlarmManager)
// [Não existe nos slides do prof. Alexandre] — parte extra das notificações
// Lembrete do dia, às 00:00 (o das missões foi removido em 13/10). O alarme é exato
// pro lembrete não atrasar e quem reagenda pro dia seguinte é o receiver. No
// reboot, o BootReceiver põe tudo de volta.
public class AlarmScheduler {

    private static final int REQUEST_DAILY = 2001;

    // ========== AGENDAR ==========

    /** Agenda o lembrete diário (dica do dia) para 00:00 todo dia */
    public static void scheduleDailyReminder(Context context) {
        agendarTodoDia(context, NotificationReceiver.ACTION_DAILY_REMINDER, REQUEST_DAILY);
    }

    /** Cancela o lembrete diário */
    public static void cancelDailyReminder(Context context) {
        cancelar(context, NotificationReceiver.ACTION_DAILY_REMINDER, REQUEST_DAILY);
    }

    // Limpa o alarme do lembrete de missões: o recurso saiu do app em 13/10,
    // mas quem já tinha o alarme agendado ainda receberia a notificação.
    private static void limparAlarmeDeMissoes(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.serpenteando.cobra3d.ACTION_MISSIONS_REMINDER");
        alarmManager.cancel(PendingIntent.getBroadcast(context, 2002, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
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

        // O lembrete de missões saiu do app (13/10): cancela o alarme que ficou
        // agendado nas versões antigas, senão ele ainda dispararia.
        limparAlarmeDeMissoes(context);
    }

    /**
     * Agenda um alarme para a PRÓXIMA meia-noite local.
     *
     * O cancelar antes de agendar é de propósito: garante UM alarme por ação
     * (reabrir o app ou ligar o celular não deixa alarme duplicado pendurado).
     */
    private static void agendarTodoDia(Context context, String action, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = createPendingIntent(context, action, requestCode);

        // Próxima ocorrência às 00:00 (meia-noite) no fuso do aparelho
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Se já passou das 00:00 hoje, agenda pra amanhã
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.cancel(pendingIntent);
        agendar(alarmManager, calendar.getTimeInMillis(), pendingIntent);
    }

    /** Escolhe o melhor tipo de alarme que o aparelho permite (exato quando dá). */
    private static void agendar(AlarmManager alarmManager, long tempo, PendingIntent pendingIntent) {
        boolean podeExato = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || alarmManager.canScheduleExactAlarms();

        if (podeExato && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, tempo, pendingIntent);
        } else if (podeExato) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, tempo, pendingIntent);
        } else {
            // Plano B: inexato, mas ainda acorda o aparelho no Doze
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, tempo, pendingIntent);
        }
    }

    private static void cancelar(Context context, String action, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        alarmManager.cancel(createPendingIntent(context, action, requestCode));
    }

    /** Cria PendingIntent com action e request code únicos */
    private static PendingIntent createPendingIntent(Context context, String action, int requestCode) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
