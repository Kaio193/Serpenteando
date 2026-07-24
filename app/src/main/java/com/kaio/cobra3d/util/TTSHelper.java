package com.kaio.cobra3d.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

// [ALÉM DO TEI] TTS (Text-to-Speech) — acessibilidade: lê conteúdo em voz alta para o aluno
public class TTSHelper implements TextToSpeech.OnInitListener {

    private final Context context;
    private TextToSpeech tts;
    private boolean initialized;

    public TTSHelper(Context context) {
        this.context = context;
        this.initialized = false;
        // [ALÉM DO TEI] TextToSpeech inicializado com callback onInit para configurar idioma
        this.tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("pt", "BR"));
            if (result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED) {
                initialized = true;
            }
        }
    }

    public void speak(String text) {
        if (initialized && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_id");
        }
    }

    public void stop() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
