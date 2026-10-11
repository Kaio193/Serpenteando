package com.kaio.cobra3d.view.renderer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.filament.Engine;
import com.google.android.filament.Renderer;
import com.google.android.filament.SwapChain;
import com.google.android.filament.SwapChainFlags;

/**
 * SurfaceView que hospeda o render 3D do Filament e trata o toque.
 *
 * Duas responsabilidades:
 * 1. Ciclo de vida — cria o SwapChain quando a surface existe e garante que o
 *    render loop comece/termine junto com a tela (senão vaza GPU).
 * 2. Toque — um dedo orbita o modelo, dois dedos dão zoom.
 *
 * A versão antiga também fazia ray-picking das bolinhas de hotspot e iniciava
 * um "tour" automático; tudo isso saiu junto com a versão antiga da tela.
 */
// [ALÉM DO TEI] Choreographer — sincroniza o render loop com o VSync do dispositivo para animação fluida
public class AnatomySurfaceView extends SurfaceView
        implements SurfaceHolder.Callback, Choreographer.FrameCallback {

    private static final String TAG = "AnatomySurfaceView";

    private Engine engine;
    private SwapChain swapChain;
    private Renderer renderer;
    private Anatomy3DRenderer scene3d;
    private Choreographer choreographer;
    private boolean ready;
    private boolean running;

    // Controle do gesto: guarda a posição anterior pra calcular o delta do
    // arrasto (órbita) e a distância anterior entre os dedos (pinça/zoom).
    private float prevX, prevY;
    private float prevDist = -1f;

    public AnatomySurfaceView(Context context) {
        super(context);
        init();
    }

    public AnatomySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // O fundo BRANCO do 3D não é pintado pelo GL: ele vem da própria View do
        // Android que fica atrás (o FrameLayout do fragment_anatomy_3d.xml, que é
        // @android:color/white).
        //
        // Por que não pintar no GL? Porque o Filament aplica color grading (que é
        // obrigatório, é ele quem faz a codificação de gamma) através de um LUT de
        // 32 níveis — e o LUT nunca devolve 1,0: o branco puro satura em 247
        // (#F7F7F7), então o "branco" do 3D ficava 8 tons mais escuro que o branco
        // do resto do app. Deixando o GL transparente, o branco vem do Android e é
        // 255 exato, igual ao resto do app.
        //
        // ZOrderOnTop: por padrão o SurfaceView é uma camada ATRÁS da janela (a
        // janela abre um buraco pra ele aparecer). Só com ele por cima é que o
        // transparente do GL deixa vazar a View branca de baixo.
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    /** Deve ser chamado antes do attach da surface (o Fragment já garante a ordem). */
    public void createFilament(Engine engine, Renderer renderer) {
        this.engine = engine;
        this.renderer = renderer;
        this.scene3d = new Anatomy3DRenderer(getContext(), engine, renderer);
    }

    public Anatomy3DRenderer getScene3d() {
        return scene3d;
    }

    // ----- SurfaceHolder.Callback -----

    // [ALÉM DO TEI] SurfaceHolder.Callback — ciclo de vida da superfície nativa; o SwapChain só pode ser criado quando a surface já existe
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (engine == null) {
            Log.w(TAG, "surfaceCreated antes de createFilament()");
            return;
        }
        // CONFIG_TRANSPARENT = SwapChain com canal alfa (o que permite o fundo
        // vazar pra View branca de trás).
        swapChain = engine.createSwapChain(holder.getSurface(), SwapChainFlags.CONFIG_TRANSPARENT);
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
            // Sincroniza a referência interna do renderer, senão destroy()
            // tentaria liberar o mesmo SwapChain de novo (crash).
            if (scene3d != null) {
                scene3d.invalidateSwapChain();
            }
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

    // ----- Toque: orbitar e zoom -----

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            prevX = event.getX();
            prevY = event.getY();
            prevDist = -1f;
            return true;
        }

        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            // Entrou o segundo dedo: guarda a distância entre eles pra começar a pinça
            if (event.getPointerCount() >= 2) {
                float dx = event.getX(0) - event.getX(1);
                float dy = event.getY(0) - event.getY(1);
                prevDist = (float) Math.sqrt(dx * dx + dy * dy);
            }
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (event.getPointerCount() >= 2 && prevDist > 0) {
                float dx = event.getX(0) - event.getX(1);
                float dy = event.getY(0) - event.getY(1);
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > 0) {
                    if (scene3d != null) scene3d.zoom(prevDist / dist);
                    prevDist = dist;
                }
            } else if (event.getPointerCount() == 1) {
                float deltaX = event.getX() - prevX;
                float deltaY = event.getY() - prevY;
                if (scene3d != null) scene3d.orbit(deltaX, deltaY);
                prevX = event.getX();
                prevY = event.getY();
            }
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            prevDist = -1f;
            return true;
        }

        return super.onTouchEvent(event);
    }
}
