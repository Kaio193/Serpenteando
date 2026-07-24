package com.kaio.cobra3d.presenter;

import com.kaio.cobra3d.contract.TipContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Tip;
import com.kaio.cobra3d.model.repository.TipRepository;

public class TipPresenter implements TipContract.Presenter {

    private final TipContract.View view;
    private final TipRepository tipRepository;
    private boolean voiceEnabled;

    public TipPresenter(TipContract.View view, DatabaseHelper dbHelper) {
        this.view = view;
        this.tipRepository = new TipRepository(dbHelper.getWritableDatabase());
        this.voiceEnabled = false;
    }

    @Override
    public void loadRandomTip() {
        Tip tip = tipRepository.getRandom();
        if (tip != null) {
            view.showTip(tip);
            if (voiceEnabled) {
                view.showTTS(tip.getTipText());
            }
        } else {
            view.showError("Nenhuma dica disponível");
        }
    }

    @Override
    public void onVoiceToggle(boolean enabled) {
        this.voiceEnabled = enabled;
        if (!enabled) {
            view.stopTTS();
        }
    }

    @Override
    public void onDestroy() {
        view.stopTTS();
    }
}
