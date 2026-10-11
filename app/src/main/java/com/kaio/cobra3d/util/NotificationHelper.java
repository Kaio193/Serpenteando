package com.kaio.cobra3d.util;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.view.MainActivity;

/**
 * Helper para criar canais de notificação e disparar notificações do app.
 *
 * No Android 8+ (API 26), todo app precisa de um NotificationChannel.
 * Canais:
 *   - "serpenteando_daily"   → lembretes diários (dica do dia)
 *   - "serpenteando_missions" → novas missões disponíveis
 *   - "serpenteando_achievements" → conquistas e completos
 */
public class NotificationHelper {

    private static final String CHANNEL_DAILY = "serpenteando_daily";
    private static final String CHANNEL_MISSIONS = "serpenteando_missions";
    private static final String CHANNEL_ACHIEVEMENTS = "serpenteando_achievements";

    private final Context context;
    private final NotificationManager manager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.manager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannels();
    }

    /** Cria os canais de notificação (idempotente — só cria se não existir) */
    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel daily = new NotificationChannel(
                    CHANNEL_DAILY,
                    context.getString(R.string.notif_channel_daily),
                    NotificationManager.IMPORTANCE_HIGH);
            daily.setDescription(context.getString(R.string.notif_channel_daily_desc));
            daily.enableVibration(true);
            daily.setVibrationPattern(new long[]{0, 300, 200, 300});
            daily.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    new android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());

            NotificationChannel missions = new NotificationChannel(
                    CHANNEL_MISSIONS,
                    context.getString(R.string.notif_channel_missions),
                    NotificationManager.IMPORTANCE_HIGH);
            missions.setDescription(context.getString(R.string.notif_channel_missions_desc));
            missions.enableVibration(true);
            missions.setVibrationPattern(new long[]{0, 300, 200, 300});
            missions.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    new android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());

            NotificationChannel achievements = new NotificationChannel(
                    CHANNEL_ACHIEVEMENTS,
                    context.getString(R.string.notif_channel_achievements),
                    NotificationManager.IMPORTANCE_HIGH);
            achievements.setDescription(context.getString(R.string.notif_channel_achievements_desc));
            achievements.enableVibration(true);
            achievements.setVibrationPattern(new long[]{0, 500, 200, 500});
            achievements.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    new android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());

            manager.createNotificationChannels(java.util.Arrays.asList(daily, missions, achievements));
        }
    }

    /** PendingIntent que abre o MainActivity na tela Home */
    private PendingIntent getMainActivityPendingIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    // ========== NOTIFICAÇÕES ESPECÍFICAS ==========

    /** Lembrete diário: "Hora de aprender sobre cobras!" */
    @SuppressLint("MissingPermission")
    public void sendDailyReminder() {
        // Envia notificação independentemente do som

        String title = context.getString(R.string.notif_daily_title);
        String text = context.getString(R.string.notif_daily_text);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DAILY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(getMainActivityPendingIntent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 300, 200, 300})
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS);

        if (hasPermission(context)) {
            NotificationManagerCompat.from(context).notify(1001, builder.build());
        }
    }

    /** Nova missão disponível */
    @SuppressLint("MissingPermission")
    public void sendMissionAvailable() {
        String title = context.getString(R.string.notif_mission_title);
        String text = context.getString(R.string.notif_mission_text);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MISSIONS)
                .setSmallIcon(R.drawable.ic_rocket)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(getMainActivityPendingIntent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 300, 200, 300})
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS);

        if (hasPermission(context)) {
            NotificationManagerCompat.from(context).notify(1002, builder.build());
        }
    }

    /** Conquista desbloqueada */
    @SuppressLint("MissingPermission")
    public void sendAchievement(String achievementName) {
        String title = context.getString(R.string.notif_achievement_title);
        String text = context.getString(R.string.notif_achievement_text, achievementName);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENTS)
                .setSmallIcon(R.drawable.ic_rocket)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(getMainActivityPendingIntent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS);

        if (hasPermission(context)) {
            NotificationManagerCompat.from(context).notify(1003, builder.build());
        }
    }

    /** Cancela todas as notificações pendentes */
    public void cancelAll() {
        manager.cancelAll();
    }

    /** Verifica se permissão de notificação foi concedida (Android 13+) */
    public static boolean hasPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android < 13 não precisa de permissão
    }
}
