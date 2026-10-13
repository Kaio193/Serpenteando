package com.serpenteando.cobra3d.util;

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

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.view.MainActivity;

// Cria os canais das notificações e dispara cada uma delas
// (no Android 8+ todo app precisa de canal)
//
// Três canais: lembrete do dia, lembrete das missões pendentes e conquistas.
// O texto sai no idioma escolhido no app (LocaleHelper.wrap) e usa o
// BigTextStyle pra aparecer inteiro, sem cortar com "...".
public class NotificationHelper {

    private static final String CHANNEL_DAILY = "serpenteando_daily";
    private static final String CHANNEL_MISSIONS = "serpenteando_missions";
    private static final String CHANNEL_ACHIEVEMENTS = "serpenteando_achievements";

    private final Context context;
    private final NotificationManager manager;

    public NotificationHelper(Context context) {
        // wrap(): a partir daqui todo getString() sai no idioma do app
        this.context = LocaleHelper.wrap(context.getApplicationContext());
        this.manager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannels();
    }

    // Cria os canais (recriar só atualiza nome e descrição, não duplica)
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

    // PendingIntent que abre o app quando o usuário toca na notificação
    private PendingIntent getMainActivityPendingIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    // Monta a notificação com ícone, som e vibração (BigTextStyle mostra o texto inteiro)
    private NotificationCompat.Builder criarBuilder(String canal, int icone, String titulo,
                                                     String texto, long[] vibracao) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        return new NotificationCompat.Builder(context, canal)
                .setSmallIcon(icone)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setContentIntent(getMainActivityPendingIntent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setVibrate(vibracao)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS);
    }

    // ========== NOTIFICAÇÕES ESPECÍFICAS ==========

    // Lembrete diário da dica — todo dia à meia-noite
    @SuppressLint("MissingPermission")
    public void sendDailyReminder() {
        String title = context.getString(R.string.notif_daily_title);
        String text = context.getString(R.string.notif_daily_text);

        NotificationCompat.Builder builder = criarBuilder(
                CHANNEL_DAILY, R.drawable.ic_notification, title, text,
                new long[]{0, 300, 200, 300});

        if (hasPermission(context)) {
            NotificationManagerCompat.from(context).notify(1001, builder.build());
        }
    }

    // Lembrete das missões pendentes (substituiu a antiga "nova missão")
    // Só dispara se ainda existir missão pra concluir
    @SuppressLint("MissingPermission")
    public void sendPendingMissionsReminder(int pendentes) {
        if (pendentes <= 0) return;

        String title = context.getString(R.string.notif_missions_pending_title);
        String text = context.getResources().getQuantityString(
                R.plurals.notif_missions_pending_text, pendentes, pendentes);

        NotificationCompat.Builder builder = criarBuilder(
                CHANNEL_MISSIONS, R.drawable.ic_rocket, title, text,
                new long[]{0, 300, 200, 300});

        if (hasPermission(context)) {
            NotificationManagerCompat.from(context).notify(1002, builder.build());
        }
    }

    // Avisa que o usuário desbloqueou uma conquista
    @SuppressLint("MissingPermission")
    public void sendAchievement(String achievementName) {
        String title = context.getString(R.string.notif_achievement_title);
        String text = context.getString(R.string.notif_achievement_text, achievementName);

        NotificationCompat.Builder builder = criarBuilder(
                CHANNEL_ACHIEVEMENTS, R.drawable.ic_rocket, title, text,
                new long[]{0, 500, 200, 500});

        if (hasPermission(context)) {
            NotificationManagerCompat.from(context).notify(1003, builder.build());
        }
    }

    // Tira todas as notificações da barra
    public void cancelAll() {
        manager.cancelAll();
    }

    // No Android 13+ o usuário precisa autorizar as notificações
    public static boolean hasPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android < 13 não precisa de permissão
    }
}
