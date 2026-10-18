package com.serpenteando.cobra3d.view.renderer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.filament.Box;
import com.google.android.filament.Camera;
import com.google.android.filament.ColorGrading;
import com.google.android.filament.Engine;
import com.google.android.filament.EntityManager;
import com.google.android.filament.IndirectLight;
import com.google.android.filament.LightManager;
import com.google.android.filament.Renderer;
import com.google.android.filament.Scene;
import com.google.android.filament.SwapChain;
import com.google.android.filament.Texture;
import com.google.android.filament.ToneMapper;
import com.google.android.filament.TransformManager;
import com.google.android.filament.View;
import com.google.android.filament.Viewport;
import com.google.android.filament.gltfio.FilamentAsset;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

// Quem desenha o modelo 3D na tela
// [Não existe nos slides do prof. Alexandre] — é a parte inovadora do TCC
// Mostra o Serpente_3D.glb com fundo branco: arrastar gira e dois dedos dão zoom.
// Aqui ficou só o essencial: ler o arquivo em thread de fundo (com tela de
// carregamento), enquadrar a câmera, a luz e o loop de desenho.
public final class Anatomy3DRenderer {

    private static final String TAG = "Anatomy3DRenderer";

    // O modelo que o Kaio mandou. É a serpente COMPLETA (malha + material +
    // texturas próprios), então nada é sobrescrito nele.
    private static final String MODELO = "Serpente_3D.glb";

    private static final float FOV_GRAUS = 50.0f;

    private final Engine engine;
    private final Scene scene;
    private final View view;
    private final Camera camera;
    private final Renderer renderer;
    private final Context appContext;

    private SwapChain swapChain;
    private Viewport viewport;

    // Dono do carregamento atual: asset + loader + provider + texturas.
    // Guardar isso é o que permite liberar tudo na saída da tela (era o vazamento).
    private GlbLoader.Carga carga;
    private final List<Integer> modelEntities = new ArrayList<>();
    private boolean modelBuilt;

    // ------------------------------------------------------------------
    // Estado do carregamento
    // ------------------------------------------------------------------

    // Quem mostra e esconde a tela de carregamento é o Fragment, que só olha
    // este número a cada 100 ms.
    public static final int MODELO_CARREGANDO = 0;
    public static final int MODELO_PRONTO = 1;
    public static final int MODELO_FALHOU = 2;

    // Se as texturas travarem, mostra o modelo assim mesmo em vez de deixar o
    // usuário olhando pra tela de carregamento pra sempre.
    private static final long PRAZO_CARGA_MS = 15_000L;

    private volatile int estadoModelo = MODELO_CARREGANDO;
    private volatile boolean destruido;

    // Trava entre a thread que lê o arquivo e o destroy(): garante que o engine
    // não seja destruído no meio de um create() (seria crash nativo).
    private final Object cadeadoCarga = new Object();

    private Thread threadCarga;
    private long prazoCarga;

    // Quantos quadros desenhar sem ninguém pedir
    // (o loop roda junto com o VSync e, parado, ficava redesenhando a mesma imagem)
    private static final int QUADROS_AQUECIMENTO = 180;
    private int quadrosAquecimento = QUADROS_AQUECIMENTO;

    // Quem acorda o loop quando o usuário mexe na câmera. É a SurfaceView.
    public interface FrameRequester {
        void requestFrame();
    }

    private FrameRequester frameRequester;

    // Só fica ligada quando a leitura terminou. Sem ela, um quadro no meio do
    // carregamento dava o modelo por pronto antes da hora (e o app caía)
    private boolean cargaAplicada;

    private IndirectLight indirectLight;
    private Texture environmentTexture;

    // Color grading do frame. Não é pra "deixar bonito": é onde mora o tone
    // mapping do Filament. Ver setupViewDefaults().
    private ColorGrading colorGrading;

    private int lightEntity;
    private int fillLightEntity;
    private int rimLightEntity;

    // Órbita: a câmera fica numa esfera ao redor do alvo (a origem).
    private float orbitTheta = (float) (Math.PI / 2.0);
    private float orbitPhi = 0.22f;
    private float orbitDistance = 12.0f;
    private float orbitTargetX, orbitTargetY, orbitTargetZ;

    // Meia-largura do modelo em cada eixo, já normalizada (o maior lado vira 6
    // unidades). É o que permite calcular a distância exata pra cobra caber na
    // tela de QUALQUER celular, sem cortar as pontas.
    private float halfX = 3.0f, halfY = 1.0f, halfZ = 1.0f;

    // Distância calculada pra enquadrar o modelo inteiro (base do zoom).
    private float frameDistance = 12.0f;

    public Anatomy3DRenderer(Context context, Engine engine, Renderer renderer) {
        this.appContext = context.getApplicationContext();
        this.engine = engine;
        this.renderer = renderer;
        this.scene = engine.createScene();
        this.view = engine.createView();
        this.view.setScene(scene);
        this.camera = engine.createCamera(EntityManager.get().create());

        setupViewDefaults();
        setupLighting();
    }

    public View getView() { return view; }
    public Scene getScene() { return scene; }
    public Camera getCamera() { return camera; }
    public Viewport getViewport() { return viewport; }

    public void attachSwapChain(SwapChain sc, int width, int height) {
        this.swapChain = sc;
        this.viewport = new Viewport(0, 0, width, height);
        this.view.setViewport(viewport);
        updateCameraProjection(width, height);
        frameCamera(false);
        pedirQuadros();
    }

    public void resize(int width, int height) {
        if (swapChain == null || width <= 0 || height <= 0) return;
        viewport = new Viewport(0, 0, width, height);
        view.setViewport(viewport);
        updateCameraProjection(width, height);
        pedirQuadros();
        frameCamera(false);
    }

    // ------------------------------------------------------------------
    // Modelo
    // ------------------------------------------------------------------

    // Começa a carregar o modelo (chamado pelo fragment quando a tela abre)
    // A leitura dos 66 MB roda em thread de fundo pra não travar a interface
    public void iniciarCarregamento() {
        if (modelBuilt || threadCarga != null) return;
        estadoModelo = MODELO_CARREGANDO;
        threadCarga = new Thread(this::carregarEmFundo, "serpenteando-glb");
        threadCarga.start();
    }

    // O fragment usa isto pra saber quando tirar a tela de carregamento
    public int getEstadoModelo() {
        return estadoModelo;
    }

    // Lê e monta o modelo fora da thread principal (a interface não trava)
    // O Filament aceita criar objetos em outra thread; o que mexe na cena
    // continua rodando na main.
    private void carregarEmFundo() {
        try {
            ByteBuffer dados = GlbLoader.readAsset(appContext.getAssets(), MODELO);
            if (dados == null) {
                new Handler(Looper.getMainLooper()).post(this::aoTerminarLeitura);
                return;
            }
            synchronized (cadeadoCarga) {
                // Se a tela já foi fechada, não cria nada: o engine pode estar
                // sendo destruído neste instante.
                if (!destruido) {
                    carga = GlbLoader.create(engine, dados, MODELO);
                }
            }
            // O gltfio copia o blob pra dentro do asset, então o nosso buffer de
            // 66 MB morre aqui (antes ele ficava vivo junto com o modelo).
        } catch (Throwable t) {
            Log.e(TAG, "Nao foi possivel carregar " + MODELO, t);
        }
        // Volta pra thread principal: daqui pra frente é cena, e cena é da UI.
        new Handler(Looper.getMainLooper()).post(this::aoTerminarLeitura);
    }

    /** Na thread principal: registra a carga e começa a decodificar as texturas. */
    private void aoTerminarLeitura() {
        if (destruido) return; // destroy() já liberou tudo
        if (carga == null) {
            Log.e(TAG, MODELO + " nao pode ser carregado");
            estadoModelo = MODELO_FALHOU;
            return;
        }

        prazoCarga = System.currentTimeMillis() + PRAZO_CARGA_MS;
        cargaAplicada = true;

        // As texturas entram em pedaços, um pouco por frame. O modelo só entra na
        // cena quando estiver pronto — senão apareceria sem cor por cima da tela
        // de carregamento.
        if (GlbLoader.beginAsyncLoad(engine, carga)) return;

        // Sem texturas pendentes: já está pronto — e a cópia interna do arquivo
        // (dezenas de MB) já pode ser liberada.
        mostrarModelo();
        GlbLoader.liberarFonte(carga);
    }

    /** Chamado a cada frame enquanto as texturas carregam. */
    private void atualizarCarga() {
        if (estadoModelo != MODELO_CARREGANDO || !cargaAplicada || carga == null || modelBuilt) return;
        boolean terminou = GlbLoader.atualizarTexturas(carga);
        if (terminou) {
            mostrarModelo();
            // As texturas já subiram pra GPU: a cópia do .glb no asset não serve
            // pra mais nada e é devolvida agora.
            GlbLoader.liberarFonte(carga);
        } else if (System.currentTimeMillis() > prazoCarga) {
            // Prazo estourado: mostra o modelo mesmo assim (melhor do que deixar
            // o usuário olhando pra tela de carregamento pra sempre). A cópia do
            // .glb fica, porque a carga ainda pode estar usando.
            Log.w(TAG, "Texturas demoraram demais, mostrando o modelo assim mesmo");
            mostrarModelo();
        }
    }

    // Coloca o modelo na cena e enquadra a câmera nele
    // O arquivo vem numa escala qualquer, então escala pro maior lado virar 6
    // e centra na origem — assim a câmera e o zoom têm uma referência fixa.
    private void mostrarModelo() {
        if (carga == null || carga.asset == null) {
            estadoModelo = MODELO_FALHOU;
            return;
        }

        FilamentAsset asset = carga.asset;
        int[] entities = asset.getEntities();
        if (entities == null || entities.length == 0) {
            Log.e(TAG, MODELO + " nao tem entidades");
            estadoModelo = MODELO_FALHOU;
            return;
        }

        for (int e : entities) {
            if (e == 0) continue;
            scene.addEntity(e);
            modelEntities.add(e);
        }

        Box bb = asset.getBoundingBox();
        if (bb != null) {
            float[] centro = bb.getCenter();
            float[] meiaExtensao = bb.getHalfExtent();
            float maior = Math.max(Math.abs(meiaExtensao[0]), Math.max(Math.abs(meiaExtensao[1]), Math.abs(meiaExtensao[2])));
            Log.d(TAG, MODELO + " bbox center=(" + centro[0] + "," + centro[1] + "," + centro[2]
                + ") half=(" + meiaExtensao[0] + "," + meiaExtensao[1] + "," + meiaExtensao[2] + ")");

            if (maior > 0.0001f) {
                float escala = 6.0f / (2.0f * maior);
                halfX = Math.abs(meiaExtensao[0]) * escala;
                halfY = Math.abs(meiaExtensao[1]) * escala;
                halfZ = Math.abs(meiaExtensao[2]) * escala;

                TransformManager tm = engine.getTransformManager();
                int root = asset.getRoot();
                if (root != 0) {
                    int instancia = tm.getInstance(root);
                    if (instancia != 0) {
                        // Matriz coluna-dominante (padrão Filament): escala na
                        // diagonal e translação nas posições 12/13/14.
                        tm.setTransform(instancia, new float[]{
                            escala, 0, 0, 0,
                            0, escala, 0, 0,
                            0, 0, escala, 0,
                            -centro[0] * escala, -centro[1] * escala, -centro[2] * escala, 1
                        });
                    }
                }
            }
        } else {
            Log.w(TAG, MODELO + " sem bounding box");
        }

        modelBuilt = true;
        frameCamera(true);
        // O loop só desenha enquanto está carregando ou há quadros de aquecimento.
        // Sem reservar quadros aqui, um carregamento mais longo que o aquecimento
        // inicial terminava com a cena pronta e nenhum desenho — o modelo só
        // aparecia depois do primeiro toque. Isto garante o quadro automático.
        aquecerQuadros();
        estadoModelo = MODELO_PRONTO;
        Log.d(TAG, MODELO + " na cena com " + entities.length + " entidades");
    }

    // ------------------------------------------------------------------
    // Câmera
    // ------------------------------------------------------------------

    /** Arrasta com um dedo: gira a cobra em volta do próprio eixo. */
    public void orbit(float deltaX, float deltaY) {
        orbitTheta -= deltaX * 0.008f;
        orbitPhi = Math.max(-1.3f, Math.min(1.3f, orbitPhi - deltaY * 0.008f));
        updateOrbitCamera();
        pedirQuadros();
    }

    // Pinça com dois dedos: aproxima ou afasta
    public void zoom(float factor) {
        // Limites do zoom: perto o modelo estourava a tela e longe virava um
        // pontinho. Esta faixa deixa chegar bem perto das escamas sem perder a cobra.
        float min = frameDistance * 0.7f;
        float max = frameDistance * 1.7f;
        orbitDistance = Math.max(min, Math.min(max, orbitDistance * factor));
        updateOrbitCamera();
        pedirQuadros();
    }

    // Volta pro enquadramento inicial (vista de lado, modelo inteiro)
    public void resetCamera() {
        frameCamera(true);
    }

    // Calcula a distância da câmera pra o modelo caber inteiro na tela
    // A tela é retrato, então o campo de visão horizontal é menor que o vertical:
    // a gente converte usando o aspecto e usa o pior caso, pra caber em pé ou deitado.
    private void frameCamera(boolean resetOrbit) {
        if (viewport != null && viewport.height > 0) {
            double aspect = (double) viewport.width / viewport.height;
            double vHalf = Math.toRadians(FOV_GRAUS / 2.0);
            double hHalf = Math.atan(Math.tan(vHalf) * aspect);

            // Gira o modelo: o pior caso horizontal é o maior lado da "planta".
            double raioH = Math.max(halfX, halfZ);
            double distH = raioH / Math.tan(hHalf);
            double distV = halfY / Math.tan(vHalf);

            // 1.18 = ~18% de respiro pra ponta da cobra não encostar na borda.
            frameDistance = (float) (Math.max(distH, distV) * 1.18);
        }

        orbitTargetX = 0.0f;
        orbitTargetY = 0.0f;
        orbitTargetZ = 0.0f;
        if (resetOrbit) {
            orbitTheta = (float) (Math.PI / 2.0);
            orbitPhi = 0.22f;
        }
        orbitDistance = frameDistance;
        updateOrbitCamera();
    }

    private void updateOrbitCamera() {
        float cosT = (float) Math.cos(orbitTheta);
        float sinT = (float) Math.sin(orbitTheta);
        float cosP = (float) Math.cos(orbitPhi);
        float sinP = (float) Math.sin(orbitPhi);
        camera.lookAt(
            orbitTargetX + orbitDistance * cosT * cosP,
            orbitTargetY + orbitDistance * sinP,
            orbitTargetZ + orbitDistance * sinT * cosP,
            orbitTargetX, orbitTargetY, orbitTargetZ,
            0.0, 1.0, 0.0);
    }

    private void updateCameraProjection(int width, int height) {
        double aspect = (double) width / Math.max(1, height);
        camera.setProjection(FOV_GRAUS, aspect, 0.1, 400.0, Camera.Fov.VERTICAL);
    }

    // ------------------------------------------------------------------
    // Render
    // ------------------------------------------------------------------

    /** Chamado a cada VSync pela AnatomySurfaceView. */
    public void render(long frameTimeNanos) {
        // A carga das texturas anda mesmo antes da surface existir.
        atualizarCarga();
        if (swapChain == null) return;
        if (!precisaRenderizar()) return;
        if (renderer.beginFrame(swapChain, frameTimeNanos)) {
            renderer.render(view);
            renderer.endFrame();
            if (quadrosAquecimento > 0) quadrosAquecimento--;
        }
    }

    /**
     * Enquanto o modelo está chegando, continua desenhando (é assim que as
     * texturas que carregam em paralelo aparecem). Depois disso, só desenha
     * quando alguém pediu quadro novo — ou seja, quando o usuário girou ou deu
     * zoom. Com a cena parada, o último quadro desenhado continua na tela.
     */
    public boolean precisaRenderizar() {
        return estadoModelo == MODELO_CARREGANDO || quadrosAquecimento > 0;
    }

    public void setFrameRequester(FrameRequester requester) {
        this.frameRequester = requester;
    }

    /**
     * Pede quadro novo depois de mexer na câmera. Além do quadro do toque,
     * deixa alguns de aquecimento: se o usuário girar rápido, o loop continua
     * vivo entre um toque e outro em vez de parar e voltar a cada movimento.
     */
    private void pedirQuadros() {
        aquecerQuadros();
        if (frameRequester != null) frameRequester.requestFrame();
    }

    /**
     * Reserva os quadros de aquecimento sem acordar o loop. Separado do
     * pedirQuadros() de propósito: a SurfaceView chama este pelo caminho do
     * requestFrame, e se ele avisasse de volta daria recursão infinita.
     */
    public void aquecerQuadros() {
        quadrosAquecimento = Math.max(quadrosAquecimento, QUADROS_AQUECIMENTO);
    }

    /**
     * Chamado quando a surface foi destruída — o SwapChain já foi liberado por
     * lá. Só zera a referência pra o destroy() não tentar liberar duas vezes
     * (era o crash "Calling method on destroyed SwapChain").
     */
    public void invalidateSwapChain() {
        this.swapChain = null;
    }

    // ------------------------------------------------------------------
    // Setup da cena
    // ------------------------------------------------------------------

    private void setupViewDefaults() {
        view.setCamera(camera);

        // Fundo do 3D: transparente (alfa 0). Quem pinta o branco na tela é a View do
        // Android atrás do SurfaceView — o LUT do color grading não devolve branco 255
        // (satura em 247) e o fundo ficava mais escuro que o branco do resto do app.
        // Ver o comentário em AnatomySurfaceView.init(). Sem skydome — nada de gradiente.
        scene.setSkybox(null);
        Renderer.ClearOptions clearOpts = new Renderer.ClearOptions();
        clearOpts.clearColor = new double[]{ 0.0, 0.0, 0.0, 0.0 };
        clearOpts.clear = true;
        renderer.setClearOptions(clearOpts);

        // O tone mapping do Filament roda no frame INTEIRO, inclusive no fundo, e o
        // ACES_LEGACY (padrão) transforma branco 1,0 em ~(230,225,219) — era por isso
        // que o fundo do 3D "não era branco" mesmo com clear color 1,1,1. Trocando pro
        // LINEAR a cor de entrada volta: 1,0 = 255.
        // ⚠️ No Filament 1.72 o View.setToneMapping() está @Deprecated e o corpo dele é
        // VAZIO (no-op) — quem manda no tone mapping agora é o ColorGrading.
        colorGrading = new ColorGrading.Builder()
            .toneMapper(new ToneMapper.Linear())
            .quality(ColorGrading.QualityLevel.HIGH)
            .build(engine);
        view.setColorGrading(colorGrading);

        view.setShadowingEnabled(true);
        view.setAntiAliasing(View.AntiAliasing.FXAA);
        view.setDithering(View.Dithering.TEMPORAL);

        View.VsmShadowOptions vsm = new View.VsmShadowOptions();
        vsm.mipmapping = true;
        vsm.msaaSamples = 2;
        vsm.highPrecision = true;
        vsm.minVarianceScale = 0.75f;
        vsm.lightBleedReduction = 0.08f;
        view.setVsmShadowOptions(vsm);

        setupEnvironmentLight();
    }

    /**
     * Luz de ambiente neutra (IBL).
     *
     * Um modelo PBR precisa de luz ambiente pra as partes escuras não virarem
     * preto chapado e pra o brilho das escamas refletir alguma coisa. Sem um
     * HDRI de verdade, geramos um cubemap CINZA CLARO liso em memória (16x16,
     * 6 faces) e usamos ele como reflexo e como luz difusa.
     *
     * A versão antiga usava aqui um gradiente esmeralda (o "fundo verde") —
     * como o Kaio reprovou aquele visual, agora é um cinza neutro de estúdio,
     * que é o que dá o look de "visualizador 3D do Google".
     */
    private void setupEnvironmentLight() {
        try {
            environmentTexture = buildNeutralCubemap();
            IndirectLight ibl = new IndirectLight.Builder()
                .reflections(environmentTexture)
                .irradiance(environmentTexture)
                .intensity(25_000.0f)
                .build(engine);
            scene.setIndirectLight(ibl);
            indirectLight = ibl;
        } catch (Throwable t) {
            // Se o IBL falhar, o modelo continua iluminado pelas 3 luzes
            // direcionais — só perde um pouco do brilho de ambiente.
            Log.w(TAG, "Luz de ambiente indisponivel, seguindo so com as direcionais", t);
        }
    }

    private Texture buildNeutralCubemap() {
        int faceSize = 16;
        int pixelsPorFace = faceSize * faceSize;
        byte[] face = new byte[pixelsPorFace * 4];
        // Cinza claro (215) em todos os pixels — luz de estúdio, sem cor.
        for (int i = 0; i < pixelsPorFace; i++) {
            face[i * 4]     = (byte) 215;
            face[i * 4 + 1] = (byte) 215;
            face[i * 4 + 2] = (byte) 215;
            face[i * 4 + 3] = (byte) 255;
        }
        // Cubemap = 6 faces (+X, -X, +Y, -Y, +Z, -Z): o mesmo cinza nas seis.
        ByteBuffer allFaces = ByteBuffer
            .allocateDirect(pixelsPorFace * 4 * 6)
            .order(ByteOrder.nativeOrder());
        for (int indiceFace = 0; indiceFace < 6; indiceFace++) {
            allFaces.put(face);
        }
        allFaces.flip();

        Texture texture = new Texture.Builder()
            .width(faceSize)
            .height(faceSize)
            .levels(1)
            .format(Texture.InternalFormat.RGBA8)
            .sampler(Texture.Sampler.SAMPLER_CUBEMAP)
            .usage(Texture.Usage.UPLOADABLE | Texture.Usage.SAMPLEABLE)
            .build(engine);

        texture.setImage(engine, 0, 0, 0, 0, faceSize, faceSize, 6,
            new Texture.PixelBufferDescriptor(
                allFaces,
                Texture.Format.RGBA,
                Texture.Type.UBYTE,
                1));

        return texture;
    }

    /**
     * Três luzes direcionais: uma principal quente (faz a sombra) + um
     * preenchimento azulado + um contraluz. É o esquema clássico de estúdio —
     * dá volume no corpo da cobra e separa ela do fundo branco.
     */
    private void setupLighting() {
        lightEntity = EntityManager.get().create();
        LightManager.ShadowOptions shadowOpts = new LightManager.ShadowOptions();
        shadowOpts.mapSize = 2048;
        shadowOpts.shadowCascades = 4;
        shadowOpts.cascadeSplitPositions = new float[]{ 0.05f, 0.15f, 0.4f, 1.0f };
        shadowOpts.constantBias = 0.001f;
        shadowOpts.normalBias = 0.002f;

        LightManager.Builder sun = new LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.05f, 0.96f, 0.88f)
            .intensity(28_000.0f)
            .direction(-0.35f, -0.85f, -0.25f)
            .castShadows(true)
            .shadowOptions(shadowOpts);
        sun.build(engine, lightEntity);
        scene.addEntity(lightEntity);

        fillLightEntity = EntityManager.get().create();
        LightManager.Builder fill = new LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.40f, 0.50f, 0.60f)
            .intensity(12_000.0f)
            .direction(0.75f, 0.25f, 0.65f)
            .castShadows(false);
        fill.build(engine, fillLightEntity);
        scene.addEntity(fillLightEntity);

        rimLightEntity = EntityManager.get().create();
        LightManager.Builder rim = new LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.55f, 0.85f, 0.75f)
            .intensity(18_000.0f)
            .direction(0.0f, 0.55f, -1.0f)
            .castShadows(false);
        rim.build(engine, rimLightEntity);
        scene.addEntity(rimLightEntity);
    }

    // ------------------------------------------------------------------
    // Limpeza
    // ------------------------------------------------------------------

    public void destroy() {
        // Marca antes de tudo: se a thread de fundo ainda estiver lendo o arquivo
        // ela para aí e não cria nada num engine prestes a morrer.
        synchronized (cadeadoCarga) {
            destruido = true;
        }
        cargaAplicada = false;

        // O antigo vazamento morava aqui: asset, AssetLoader, UbershaderProvider e
        // ResourceLoader nunca eram liberados (~12 MB de memória nativa por entrada na
        // tela). Tem que ser antes do engine.destroy(), porque os materiais e as texturas
        // vivem dentro do engine.
        if (carga != null) {
            GlbLoader.destroy(carga);
            carga = null;
        }
        modelEntities.clear();

        destroyLight(lightEntity);
        lightEntity = 0;
        destroyLight(fillLightEntity);
        fillLightEntity = 0;
        destroyLight(rimLightEntity);
        rimLightEntity = 0;

        scene.setIndirectLight(null);
        if (indirectLight != null) { engine.destroyIndirectLight(indirectLight); indirectLight = null; }
        if (environmentTexture != null) { engine.destroyTexture(environmentTexture); environmentTexture = null; }
        if (colorGrading != null) { engine.destroyColorGrading(colorGrading); colorGrading = null; }

        if (swapChain != null) {
            engine.destroySwapChain(swapChain);
            swapChain = null;
        }

        engine.destroyView(view);
        engine.destroyScene(scene);
        engine.destroyCameraComponent(camera.getEntity());
        EntityManager.get().destroy(camera.getEntity());
    }

    private void destroyLight(int entity) {
        if (entity == 0) return;
        scene.removeEntity(entity);
        if (engine.getLightManager().hasComponent(entity)) {
            engine.getLightManager().destroy(entity);
        }
        EntityManager.get().destroy(entity);
    }
}
