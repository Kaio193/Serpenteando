package com.serpenteando.cobra3d.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * FrameLayout que detecta swipes horizontais pra trocar entre seções.
 *
 * [ALÉM DO TEI] Não existe nos slides do prof. Alexandre.
 * Permite arrastar pra navegar: Home ↔ Quiz ↔ 3D ↔ Missões ↔ Config.
 *
 * Funcionamento:
 * 1. Detecta fling horizontal (arrasto rápido)
 * 2. Ignora scrolls verticais (ScrollView, RecyclerView)
 * 3. Só processa se o swipe horizontal > vertical
 * 4. Callback chama navigateToAdjacentTab() na Activity
 */
public class SwipeFrameLayout extends FrameLayout {

    private GestureDetector gestureDetector;
    private SwipeCallback callback;
    private boolean enabled = true;

    private static final int SWIPE_THRESHOLD = 80;
    private static final int SWIPE_VELOCITY = 200;

    public interface SwipeCallback {
        void onSwipeLeft();
        void onSwipeRight();
    }

    public SwipeFrameLayout(Context context) {
        super(context);
        init(context);
    }

    public SwipeFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null || !enabled) return false;

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                // Só processa se horizontal > vertical e acima do threshold
                if (Math.abs(diffX) > Math.abs(diffY)
                        && Math.abs(diffX) > SWIPE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY) {

                    if (diffX > 0) {
                        // Swipe direita → seção anterior
                        if (callback != null) callback.onSwipeRight();
                    } else {
                        // Swipe esquerda → próxima seção
                        if (callback != null) callback.onSwipeLeft();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true; // Necessário pra habilitar onFling
            }
        });
    }

    public void setSwipeCallback(SwipeCallback callback) {
        this.callback = callback;
    }

    public void setSwipeEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Deixa o GestureDetector analisar o evento
        // Se detectar swipe, intercepta; senão, deixa os filhos tratarem
        if (enabled && gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        return false; // Nunca intercepta — apenas detecta
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enabled && gestureDetector != null) {
            return gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }
}
