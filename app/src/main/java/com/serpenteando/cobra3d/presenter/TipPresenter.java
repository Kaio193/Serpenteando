package com.serpenteando.cobra3d.presenter;

import android.content.Context;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.contract.TipContract;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.model.entity.Tip;
import com.serpenteando.cobra3d.model.repository.TipRepository;

public class TipPresenter implements TipContract.Presenter {

    private final TipContract.View view;
    private final Context context;
    private final TipRepository tipRepository;
    private boolean voiceEnabled;

    public TipPresenter(TipContract.View view, DatabaseHelper dbHelper, Context context) {
        this.view = view;
        this.context = context.getApplicationContext();
        this.tipRepository = new TipRepository(dbHelper.getWritableDatabase());
        this.voiceEnabled = false;
    }

    @Override
    public void loadRandomTip() {
        Tip tip = tipRepository.getDailyTip();
        if (tip != null) {
            view.showTip(tip);
            if (voiceEnabled) {
                view.showTTS(tip.getTipText());
            }
        } else {
            view.showError(context.getString(R.string.error_no_tip));
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
