package com.kaio.cobra3d.view.renderer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.kaio.cobra3d.R;

public final class TourOverlayManager {

    private static final long FADE_DURATION_MS = 400;
    private static final long DISPLAY_DURATION_MS = 3000;
    private static final long HIDE_DURATION_MS = 300;

    private final View overlayPanel;
    private final TextView nameView;
    private final TextView functionView;
    private final TextView howItWorksView;
    private final TextView curiosityView;
    private final Context context;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable hideRunnable = this::animateOut;

    private boolean visible;

    /**
     * Cada estrutura guarda os IDs dos resources (R.string.tour_X_name, etc.)
     * O texto é resolvido via context.getString() respeitando o idioma atual.
     */
    public static final class StructureInfo {
        public final int nameRes;
        public final int funcRes;
        public final int howRes;
        public final int curioRes;

        public StructureInfo(int nameRes, int funcRes, int howRes, int curioRes) {
            this.nameRes = nameRes;
            this.funcRes = funcRes;
            this.howRes = howRes;
            this.curioRes = curioRes;
        }
    }

    public static final StructureInfo[] STRUCTURES = {
        new StructureInfo(R.string.tour_0_name,  R.string.tour_0_func,  R.string.tour_0_how,  R.string.tour_0_curio),
        new StructureInfo(R.string.tour_1_name,  R.string.tour_1_func,  R.string.tour_1_how,  R.string.tour_1_curio),
        new StructureInfo(R.string.tour_2_name,  R.string.tour_2_func,  R.string.tour_2_how,  R.string.tour_2_curio),
        new StructureInfo(R.string.tour_3_name,  R.string.tour_3_func,  R.string.tour_3_how,  R.string.tour_3_curio),
        new StructureInfo(R.string.tour_4_name,  R.string.tour_4_func,  R.string.tour_4_how,  R.string.tour_4_curio),
        new StructureInfo(R.string.tour_5_name,  R.string.tour_5_func,  R.string.tour_5_how,  R.string.tour_5_curio),
        new StructureInfo(R.string.tour_6_name,  R.string.tour_6_func,  R.string.tour_6_how,  R.string.tour_6_curio),
        new StructureInfo(R.string.tour_7_name,  R.string.tour_7_func,  R.string.tour_7_how,  R.string.tour_7_curio),
        new StructureInfo(R.string.tour_8_name,  R.string.tour_8_func,  R.string.tour_8_how,  R.string.tour_8_curio),
        new StructureInfo(R.string.tour_9_name,  R.string.tour_9_func,  R.string.tour_9_how,  R.string.tour_9_curio),
        new StructureInfo(R.string.tour_10_name, R.string.tour_10_func, R.string.tour_10_how, R.string.tour_10_curio),
        new StructureInfo(R.string.tour_11_name, R.string.tour_11_func, R.string.tour_11_how, R.string.tour_11_curio),
        new StructureInfo(R.string.tour_12_name, R.string.tour_12_func, R.string.tour_12_how, R.string.tour_12_curio),
        new StructureInfo(R.string.tour_13_name, R.string.tour_13_func, R.string.tour_13_how, R.string.tour_13_curio),
        new StructureInfo(R.string.tour_14_name, R.string.tour_14_func, R.string.tour_14_how, R.string.tour_14_curio),
        new StructureInfo(R.string.tour_15_name, R.string.tour_15_func, R.string.tour_15_how, R.string.tour_15_curio),
        new StructureInfo(R.string.tour_16_name, R.string.tour_16_func, R.string.tour_16_how, R.string.tour_16_curio),
        new StructureInfo(R.string.tour_17_name, R.string.tour_17_func, R.string.tour_17_how, R.string.tour_17_curio),
        new StructureInfo(R.string.tour_18_name, R.string.tour_18_func, R.string.tour_18_how, R.string.tour_18_curio),
        new StructureInfo(R.string.tour_19_name, R.string.tour_19_func, R.string.tour_19_how, R.string.tour_19_curio),
        new StructureInfo(R.string.tour_20_name, R.string.tour_20_func, R.string.tour_20_how, R.string.tour_20_curio),
        new StructureInfo(R.string.tour_21_name, R.string.tour_21_func, R.string.tour_21_how, R.string.tour_21_curio),
        new StructureInfo(R.string.tour_22_name, R.string.tour_22_func, R.string.tour_22_how, R.string.tour_22_curio),
    };

    public TourOverlayManager(Context context, View panel, TextView name, TextView function, TextView howItWorks, TextView curiosity) {
        this.context = context;
        this.overlayPanel = panel;
        this.nameView = name;
        this.functionView = function;
        this.howItWorksView = howItWorks;
        this.curiosityView = curiosity;
        this.visible = false;
    }

    public void showStructure(int index) {
        if (index < 0 || index >= STRUCTURES.length) return;
        handler.removeCallbacks(hideRunnable);
        StructureInfo s = STRUCTURES[index];
        nameView.setText(context.getString(s.nameRes));
        functionView.setText(context.getString(s.funcRes));
        howItWorksView.setText(context.getString(s.howRes));
        curiosityView.setText(context.getString(s.curioRes));
        animateIn();
        handler.postDelayed(hideRunnable, DISPLAY_DURATION_MS);
    }

    public void hide() {
        handler.removeCallbacks(hideRunnable);
        animateOut();
    }

    private void animateIn() {
        if (visible) return;
        visible = true;
        overlayPanel.setAlpha(0f);
        overlayPanel.setTranslationY(40f);
        overlayPanel.setVisibility(View.VISIBLE);
        overlayPanel.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(FADE_DURATION_MS)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private void animateOut() {
        if (!visible) return;
        visible = false;
        overlayPanel.animate()
            .alpha(0f)
            .translationY(20f)
            .setDuration(HIDE_DURATION_MS)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> overlayPanel.setVisibility(View.GONE))
            .start();
    }

    public boolean isVisible() {
        return visible;
    }
}
