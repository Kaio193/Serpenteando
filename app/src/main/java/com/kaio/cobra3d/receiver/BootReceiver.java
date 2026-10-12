package com.kaio.cobra3d.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kaio.cobra3d.util.AlarmScheduler;

/**
 * Reagenda o alarme de notificação diária após o celular reiniciar.
 *
 * Registrado no AndroidManifest.xml com filtro BOOT_COMPLETED.
 * Quando o celular liga, o AlarmManager perde os alarmes agendados —
 * este receiver garante que o lembrete das 00:00 seja reagendado.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AlarmScheduler.rescheduleAll(context);
        }
    }
}
