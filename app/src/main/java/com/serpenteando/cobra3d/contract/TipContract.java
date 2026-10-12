package com.serpenteando.cobra3d.contract;

import com.serpenteando.cobra3d.model.entity.Tip;

/**
 * Contrato MVP pra Dicas.
 * [ALÉM DO TEI] Inclui TTS (Text-to-Speech) pra ler dica em voz alta.
 */
public interface TipContract {

    interface View {
        void showTip(Tip tip);
        void showTTS(String text);
        void stopTTS();
        void showError(String message);
    }

    interface Presenter {
        void loadRandomTip();
        void onVoiceToggle(boolean enabled);
        void onDestroy();
    }
}
