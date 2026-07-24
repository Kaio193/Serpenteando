package com.kaio.cobra3d.view.renderer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.filament.Engine;
import com.google.android.filament.Renderer;
import com.google.android.filament.SwapChain;

import com.kaio.cobra3d.model.entity.AnatomyHotspot;

import java.util.List;

/**
 * SurfaceView que hospeda o ciclo de render Filament e despacha toques para o
 * Anatomy3DRenderer. Usa Utils como ponte (carrega a native lib filament-jni).
 *
 * Importante: instanciar Engine/Utils é caro — fazemos em init() e liberamos em
 * release(). O Choreographer só é registrado após o surface estar ready.
 */
// [ALÉM DO TEI] Choreographer — sincroniza o render loop com o VSync do dispositivo para animação fluida
public class AnatomySurfaceView extends SurfaceView
        implements SurfaceHolder.Callback, Choreographer.FrameCallback {

    private static final String TAG = "AnatomySurfaceView";

    public interface OnHotspotPickedListener {
        void onHotspotPicked(int hotspotIndex);
    }

    private Engine engine;
    private SwapChain swapChain;
    private Renderer renderer;
    private Anatomy3DRenderer scene3d;
    private Choreographer choreographer;
    private boolean ready;
    private boolean running;
    private float lastTouchX, lastTouchY;
    private float prevX, prevY;
    private float prevDist = -1f;
    private boolean touchMoved = false;
    private OnHotspotPickedListener pickListener;
    private List<AnatomyHotspot> hotspots;

    public AnatomySurfaceView(Context context) {
        super(context);
        init();
    }

    public AnatomySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        setFocusable(true);
    }

    /** Deve ser chamado antes de attach do surface (já em ordem pelo Fragment). */
    public void createFilament(Engine engine, Renderer renderer) {
        this.engine = engine;
        this.renderer = renderer;
        this.scene3d = new Anatomy3DRenderer(getContext(), engine, renderer);
    }

    public Anatomy3DRenderer getScene3d() {
        return scene3d;
    }

    public void setHotspots(List<AnatomyHotspot> hotspots) {
        this.hotspots = hotspots;
    }

    public void setOnHotspotPickedListener(OnHotspotPickedListener l) {
        this.pickListener = l;
    }

    // ----- SurfaceHolder.Callback -----

    // [ALÉM DO TEI] SurfaceHolder.Callback — ciclo de vida da superfície nativa; SwapChain só é criado quando a surface existe
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (engine == null) {
            // Caso a engine ainda não tenha sido inicializada pelo fragment, falha silenciosa
            Log.w(TAG, "surfaceCreated antes de createFilament()");
            return;
        }
        swapChain = engine.createSwapChain(holder.getSurface());
        ready = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!ready) return;
        scene3d.attachSwapChain(swapChain, width, height);
        onResumeFrame();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        ready = false;
        if (engine != null && swapChain != null) {
            engine.destroySwapChain(swapChain);
            swapChain = null;
        }
    }

    public void onResumeFrame() {
        if (choreographer == null) choreographer = Choreographer.getInstance();
        if (ready && !running) {
            running = true;
            choreographer.postFrameCallback(this);
        }
    }

    public void onPauseFrame() {
        running = false;
        if (choreographer != null) choreographer.removeFrameCallback(this);
    }

    /** Chamado pelo Fragment.onDestroyView para liberar tudo. */
    public void release() {
        onPauseFrame();
        if (scene3d != null) {
            scene3d.destroy();
            scene3d = null;
        }
        engine = null;
        renderer = null;
        swapChain = null;
    }

    // ----- Choreographer.FrameCallback -----

    // [ALÉM DO TEI] doFrame — disparado em cada VSync (≈16ms); aciona render() do Filament mantendo 60 fps sem busy-wait
    @Override
    public void doFrame(long frameTimeNanos) {
        if (!ready) {
            running = false;
            return;
        }
        if (scene3d != null) scene3d.render(frameTimeNanos);
        if (running && choreographer != null) {
            choreographer.postFrameCallback(this);
        }
    }

    // ----- Toque: orbitar, zoom e ray-picking -----

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            prevX = lastTouchX = event.getX();
            prevY = lastTouchY = event.getY();
            prevDist = -1f;
            touchMoved = false;
            if (scene3d != null && scene3d.isTourRunning()) {
                scene3d.stopTour();
            }
            return true;
        }

        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            if (event.getPointerCount() >= 2) {
                float dx = event.getX(0) - event.getX(1);
                float dy = event.getY(0) - event.getY(1);
                prevDist = (float) Math.sqrt(dx * dx + dy * dy);
            }
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            touchMoved = true;
            if (event.getPointerCount() >= 2 && prevDist > 0) {
                float dx = event.getX(0) - event.getX(1);
                float dy = event.getY(0) - event.getY(1);
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float factor = prevDist / dist;
                if (scene3d != null) scene3d.zoom(factor);
                prevDist = dist;
            } else if (event.getPointerCount() == 1) {
                float deltaX = event.getX() - prevX;
                float deltaY = event.getY() - prevY;
                if (scene3d != null) scene3d.orbit(deltaX, deltaY);
                prevX = event.getX();
                prevY = event.getY();
            }
            return true;
        }

        if (action == MotionEvent.ACTION_UP) {
            if (!touchMoved && scene3d != null && hotspots != null) {
                int idx = scene3d.pickHotspotIndex(
                    (int) event.getX(), (int) event.getY(), getWidth(), getHeight(), hotspots);
                if (idx >= 0 && pickListener != null) {
                    pickListener.onHotspotPicked(idx);
                } else if (!scene3d.isTourRunning()) {
                    scene3d.startTour();
                }
            }
            prevDist = -1f;
            return true;
        }

        return super.onTouchEvent(event);
    }
}
