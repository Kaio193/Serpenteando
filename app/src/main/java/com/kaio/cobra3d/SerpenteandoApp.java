package com.kaio.cobra3d;

import android.app.Application;

import com.google.android.filament.utils.Utils;

/**
 * Application entry point.
 *
 * Inicializa o Utils do Filament aqui (carrega a native lib filament-jni uma única vez
 * no boot do processo). Tentar inicializar em outro lugar — p.ex. em uma Activity
 * qualquer — pode falhar com UnsatisfiedLinkError em cold start, porque a lib só é
 * resolvida quando alguém chama Utils.init() antes de qualquer uso de Engine.
 */
public class SerpenteandoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init();
    }
}
