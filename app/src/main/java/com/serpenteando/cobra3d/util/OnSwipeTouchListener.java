package com.serpenteando.cobra3d.util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Detector de gesto de swipe horizontal.
 * Usado pra permitir arrastar entre as seções principais do app
 * (Home ↔ Quiz ↔ 3D ↔ Missões ↔ Config).
 *
 * Funcionamento:
 * 1. Detecta fling horizontal (arrasto rápido)
 * 2. Se swipe pra direita → seção anterior
 * 3. Se swipe pra esquerda → próxima seção
 * 4. Só ativa em telas principais (não em sub-telas)
 *
 * [ALÉM DO TEI] Não existe nos slides do prof. Alexandre.
 * É uma melhoria de UX que o professor pode perguntar.
 */
public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private final SwipeCallback callback;

    public interface SwipeCallback {
        void onSwipeLeft();   // Próxima seção (direita)
        void onSwipeRight();  // Seção anterior (esquerda)
    }

    public OnSwipeTouchListener(Context context, SwipeCallback callback) {
        this.callback = callback;
        this.gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        // Distância mínima pra considerar swipe (em pixels)
        private static final int SWIPE_THRESHOLD = 100;
        // Velocidade mínima do fling
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            // Só processa se o swipe horizontal for maior que o vertical
            // (evita confundir com scroll vertical)
            if (Math.abs(diffX) > Math.abs(diffY)
                    && Math.abs(diffX) > SWIPE_THRESHOLD
                    && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                if (diffX > 0) {
                    // Swipe pra direita → seção anterior
                    callback.onSwipeRight();
                } else {
                    // Swipe pra esquerda → próxima seção
                    callback.onSwipeLeft();
                }
                return true;
            }
            return false;
        }
    }
}
