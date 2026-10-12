package com.serpenteando.cobra3d.util;

import android.view.MotionEvent;
import android.view.View;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public final class PressSpring {

    private static final float PRESS_SCALE = 0.96f;

    private static final FloatPropertyCompat<View> SCALE_X =
            new FloatPropertyCompat<View>("scaleX") {
                @Override
                public float getValue(View v) {
                    return v.getScaleX();
                }

                @Override
                public void setValue(View v, float value) {
                    v.setScaleX(value);
                }
            };

    private static final FloatPropertyCompat<View> SCALE_Y =
            new FloatPropertyCompat<View>("scaleY") {
                @Override
                public float getValue(View v) {
                    return v.getScaleY();
                }

                @Override
                public void setValue(View v, float value) {
                    v.setScaleY(value);
                }
            };

    private PressSpring() {}

    public static void attach(View view) {
        if (view == null) return;
        final SpringAnimation[] pressX = {null};
        final SpringAnimation[] pressY = {null};
        view.setOnTouchListener((v, e) -> {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    cancel(pressX[0]);
                    cancel(pressY[0]);
                    pressX[0] = springTo(v, SCALE_X, PRESS_SCALE,
                            SpringForce.STIFFNESS_MEDIUM, SpringForce.DAMPING_RATIO_NO_BOUNCY);
                    pressY[0] = springTo(v, SCALE_Y, PRESS_SCALE,
                            SpringForce.STIFFNESS_MEDIUM, SpringForce.DAMPING_RATIO_NO_BOUNCY);
                    return false;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    cancel(pressX[0]);
                    cancel(pressY[0]);
                    pressX[0] = springTo(v, SCALE_X, 1f,
                            SpringForce.STIFFNESS_LOW, SpringForce.DAMPING_RATIO_LOW_BOUNCY);
                    pressY[0] = springTo(v, SCALE_Y, 1f,
                            SpringForce.STIFFNESS_LOW, SpringForce.DAMPING_RATIO_LOW_BOUNCY);
                    return false;
                default:
                    return false;
            }
        });
    }

    private static void cancel(SpringAnimation sa) {
        if (sa != null && sa.isRunning()) sa.cancel();
    }

    private static SpringAnimation springTo(View view, FloatPropertyCompat<View> property, float target,
                                            float stiffness, float damping) {
        SpringAnimation sa = new SpringAnimation(view, property, target);
        sa.setSpring(new SpringForce(target)
                .setStiffness(stiffness)
                .setDampingRatio(damping));
        sa.start();
        return sa;
    }
}