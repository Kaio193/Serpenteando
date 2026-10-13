package com.serpenteando.cobra3d.view.renderer;

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

// É a view onde o modelo 3D aparece (Filament), e também quem lê o toque:
// um dedo gira o modelo, dois dedos dão zoom.
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
        // O fundo branco do 3D vem da View que fica atrás, não do GL: desenhando
        // transparente, o branco é o 255 exato do resto do app (pintado pelo
        // Filament ele ficava um pouco mais escuro)
        // ZOrderOnTop deixa a superfície por cima, e é isso que faz o
        // transparente do GL mostrar a View de baixo
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    // Chamado antes da surface existir (o fragment garante a ordem)
    public void createFilament(Engine engine, Renderer renderer) {
        this.engine = engine;
        this.renderer = renderer;
        this.scene3d = new Anatomy3DRenderer(getContext(), engine, renderer);
        // O renderer avisa quando a câmera muda, aí a tela redesenha
        this.scene3d.setFrameRequester(this::requestFrame);
    }

    // Acorda o desenho quando o usuário mexe no modelo
    public void requestFrame() {
        onResumeFrame();
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
        // CONFIG_TRANSPARENT deixa o fundo de trás aparecer através do desenho
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
            // Avisa o renderer, senão ele tentaria liberar o mesmo SwapChain de novo
            if (scene3d != null) {
                scene3d.invalidateSwapChain();
            }
        }
    }

    public void onResumeFrame() {
        if (choreographer == null) choreographer = Choreographer.getInstance();
        // Garante alguns quadros antes do loop poder parar de novo
        if (scene3d != null) scene3d.aquecerQuadros();
        if (ready && !running) {
            running = true;
            choreographer.postFrameCallback(this);
        }
    }

    public void onPauseFrame() {
        running = false;
        if (choreographer != null) choreographer.removeFrameCallback(this);
    }

    // Chamado quando a tela fecha, pra liberar tudo
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

        // Com o modelo parado, para de pedir quadro: o último desenho continua na
        // tela e o toque acorda o loop (antes ficava redesenhando a mesma imagem)
        if (running && choreographer != null && (scene3d == null || scene3d.precisaRenderizar())) {
            choreographer.postFrameCallback(this);
        } else {
            running = false;
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
            // Chegou o segundo dedo: guarda a distância pra começar o zoom
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
