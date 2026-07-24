package com.kaio.cobra3d.contract;

import com.kaio.cobra3d.model.entity.Tip;

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
