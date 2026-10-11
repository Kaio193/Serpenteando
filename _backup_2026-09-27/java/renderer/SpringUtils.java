package com.kaio.cobra3d.view.renderer;

import android.view.View;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public final class SpringUtils {

    private static final float DAMPING = SpringForce.DAMPING_RATIO_HIGH_BOUNCY;
    private static final float STIFFNESS = SpringForce.STIFFNESS_LOW;

    public static void press(View v) {
        SpringAnimation scaleX = new SpringAnimation(v, DynamicAnimation.SCALE_X, 0.92f);
        SpringAnimation scaleY = new SpringAnimation(v, DynamicAnimation.SCALE_Y, 0.92f);
        scaleX.setSpring(springForce()).setStartVelocity(4f).start();
        scaleY.setSpring(springForce()).setStartVelocity(4f).start();
    }

    public static void release(View v) {
        SpringAnimation scaleX = new SpringAnimation(v, DynamicAnimation.SCALE_X, 1f);
        SpringAnimation scaleY = new SpringAnimation(v, DynamicAnimation.SCALE_Y, 1f);
        scaleX.setSpring(springForce()).setStartVelocity(2f).start();
        scaleY.setSpring(springForce()).setStartVelocity(2f).start();
    }

    private static SpringForce springForce() {
        return new SpringForce()
                .setDampingRatio(DAMPING)
                .setStiffness(STIFFNESS);
    }
}
