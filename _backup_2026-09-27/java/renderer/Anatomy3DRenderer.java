package com.kaio.cobra3d.view.renderer;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.android.filament.Box;
import com.google.android.filament.Camera;
import com.google.android.filament.ColorGrading;
import com.google.android.filament.Renderer;
import com.google.android.filament.Engine;
import com.google.android.filament.EntityManager;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.IndirectLight;
import com.google.android.filament.LightManager;
import com.google.android.filament.Material;
import com.google.android.filament.MaterialInstance;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.Scene;
import com.google.android.filament.Skybox;
import com.google.android.filament.SwapChain;
import com.google.android.filament.Texture;
import com.google.android.filament.TextureSampler;
import com.google.android.filament.TransformManager;
import com.google.android.filament.VertexBuffer;
import com.google.android.filament.View;
import com.google.android.filament.Viewport;
import com.google.android.filament.gltfio.FilamentAsset;
import com.kaio.cobra3d.model.entity.AnatomyHotspot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Anatomy3DRenderer {

    private static final String TAG = "Anatomy3DRenderer";

    private static final String UNIFORM_BASE_COLOR = "baseColor";
    private static final String UNIFORM_ROUGHNESS  = "roughness";
    private static final String UNIFORM_METALLIC   = "metallic";
    private static final String UNIFORM_EMISSIVE   = "emissive";
    private static final String UNIFORM_USE_SCALE_NORMAL = "useScaleNormal";
    private static final String UNIFORM_USE_TEXTURE     = "useTexture";
    private static final String UNIFORM_SCALE_TEXTURE   = "scaleTexture";
    private static final String UNIFORM_SCALE_NORMAL_TEXTURE = "scaleNormalTexture";

    // Teal esmeralda do mockup (baseColor 0x2a9d8f do Snake.ts). O Gemini
    // reclamou que o tom marrom-dourado antigo ficava "acinzentado chapado".
    private static final float[] COLOR_BODY_GOLDEN  = { 0.05f, 0.85f, 0.52f };
    private static final float[] COLOR_HEAD         = { 0.580f, 0.470f, 0.310f };
    private static final float[] COLOR_HOTSPOT      = { 0.95f, 0.78f, 0.10f };

    private final Engine engine;
    private final Scene scene;
    private final View view;
    private final Camera camera;
    private final Renderer renderer;
    private final Context appContext;
    private final MaterialProvider materialProvider;

    private final List<Integer> hotspotEntities = new ArrayList<>();
    private final List<float[]> hotspotWorldPositions = new ArrayList<>();
    // Per-hotspot material instances so individual hotspots can be pulsed independently
    private final List<MaterialInstance> hotspotInstances = new ArrayList<>();
    private final List<Integer> modelEntities = new ArrayList<>();
    private final List<Integer> particleEntities = new ArrayList<>();
    private final List<float[]> particleSeeds = new ArrayList<>();
    private UVSphereMesh.RenderedMesh particleMesh;

    private int bodyEntity;
    private int headEntity;
    private int leftEyeEntity;
    private int rightEyeEntity;
    private int tongueEntity;
    private int lightEntity;
    private int fillLightEntity;
    private int rimLightEntity;
    private SwapChain swapChain;
    private Viewport viewport;
    private Skybox skybox;
    private Texture backgroundTexture;
    private Texture scaleTexture;
    private Texture scaleNormalTexture;
    private ColorGrading colorGrading;
    private IndirectLight indirectLight;

    private ModelBuilder.RenderedMesh bodyMesh;
    private ModelBuilder.RenderedMesh leftEyeMesh;
    private ModelBuilder.RenderedMesh rightEyeMesh;
    private ModelBuilder.RenderedMesh tongueMesh;
    private UVSphereMesh.RenderedMesh sphereMesh;
    private UVSphereMesh.RenderedMesh hotspotMesh;

    private MaterialInstance bodyInstance;
    private MaterialInstance headInstance;
    private MaterialInstance eyeInstance;
    private MaterialInstance tongueInstance;
    private final List<MaterialInstance> particleInstances = new ArrayList<>();
    // NOTE: hotspotInstance (single) kept for backward compat but not used for per-hotspot control
    private MaterialInstance hotspotInstance;
    private final java.util.ArrayList<MaterialInstance> orphanInstances = new java.util.ArrayList<>();
    private final java.util.ArrayList<MaterialInstance> tempToDestroy = new java.util.ArrayList<>();
    private FilamentAsset glbAsset;

    // Hotspot animation state
    private int animHotspotIndex = -1;
    private long animHotspotStartNanos = -1L;
    private boolean animHotspotActive = false;
    private static final float HOTSPOT_ANIM_DURATION = 0.40f; // seconds


    private boolean bodyBuilt;
    private boolean hotspotsBuilt;
    private int currentLayerIndex;
    private boolean useGlb;

    private float modelScale = 1.2f;
    private float orbitTheta = (float) Math.PI / 2.0f;
    private float orbitPhi = 0.266f;
    private float orbitDistance = 15.0f;
    private float orbitTargetX, orbitTargetY, orbitTargetZ;

    // Enquadramento da câmera (item 6). Com o corpo procedural são os valores
    // históricos; quando o modelo COMPLETO (assets/Serpente_3D.glb) entra na
    // cena, são recalculados a partir do bounding box do próprio arquivo.
    private float frameTargetX = 1.0f;
    private float frameTargetY = 0.12f;
    private float frameTargetZ = 0.0f;
    private float frameDistance = 9.3f;

    private static final float[][] TOUR_KEYFRAMES = {
        {  0.0f,  1.8f,  9.0f,  0.0f,  0.0f,  0.0f },
        {  3.0f,  0.6f,  6.0f,  0.0f,  0.0f,  0.0f },
        { -1.8f,  1.5f,  7.2f,  0.0f,  0.6f,  0.0f },
        {  0.0f,  3.6f,  7.2f,  0.0f,  0.0f,  0.0f },
        {  3.0f,  0.5f,  3.0f,  3.0f,  0.4f,  0.0f },
        {  2.9f, -0.3f,  2.4f,  3.0f,  0.0f,  0.0f },
        {  2.7f,  0.9f,  2.4f,  3.0f,  0.6f,  0.0f },
        {  3.0f,  0.0f,  2.1f,  3.0f,  0.0f,  0.0f },
        {  3.5f,  0.2f,  1.8f,  3.4f,  0.1f,  0.0f },
        {  3.0f,  0.8f,  1.8f,  3.0f,  0.6f,  0.0f },
        {  2.9f,  0.8f,  1.2f,  3.0f,  0.6f,  0.0f },
        {  0.0f,  1.1f,  4.8f,  0.0f,  0.4f,  0.0f },
        {  0.0f,  0.8f,  4.2f,  0.0f,  0.4f,  0.0f },
        { -0.6f,  0.5f,  3.0f, -0.6f,  0.4f,  0.0f },
        { -1.0f,  0.9f,  3.6f, -1.0f,  0.6f,  0.0f },
        { -0.4f,  0.1f,  3.6f, -0.4f,  0.0f,  0.0f },
        { -0.7f,  0.5f,  3.0f, -0.7f,  0.4f,  0.0f },
        { -1.8f,  0.5f,  3.6f, -1.8f,  0.4f,  0.0f },
        { -1.2f,  0.1f,  3.3f, -1.2f,  0.0f,  0.0f },
        { -2.2f,  0.3f,  3.6f, -2.2f,  0.2f,  0.0f },
        {  0.0f,  1.5f,  5.4f,  0.0f,  0.6f,  0.0f },
        { -0.6f,  0.3f,  4.2f, -0.6f,  0.0f,  0.0f },
        { -3.2f,  0.2f,  2.4f, -3.0f,  0.0f,  0.0f },
    };

    private static final float[] KEYFRAME_DURATIONS = {
        4.0f, 3.5f, 3.5f, 3.0f,
        2.8f, 2.5f, 2.5f, 2.5f, 2.8f, 2.5f, 2.8f,
        3.0f, 2.5f, 2.8f, 2.5f, 2.5f, 2.5f, 2.8f,
        2.5f, 2.5f, 3.0f, 2.5f, 3.0f,
    };

    private boolean tourRunning = false;
    private int     tourKeyframe = 0;
    private float   tourProgress = 0.0f;
    private long    lastFrameTimeNanos = 0L;
    private long    animStartNanos = -1L;

    private final float[] breathMatrix = new float[16];

    public interface OnTourStructureChangedListener {
        void onTourStructureChanged(int structureIndex);
    }

    private OnTourStructureChangedListener structureListener;

    public void setOnTourStructureChangedListener(OnTourStructureChangedListener l) {
        this.structureListener = l;
    }

    public Anatomy3DRenderer(Context context, Engine engine, Renderer renderer) {
        this.appContext = context.getApplicationContext();
        this.engine = engine;
        this.renderer = renderer;
        this.scene = engine.createScene();
        this.view = engine.createView();
        this.view.setScene(scene);
        this.camera = engine.createCamera(EntityManager.get().create());
        this.materialProvider = new MaterialProvider(appContext, engine);

        this.bodyEntity = 0;
        this.headEntity = 0;
        this.lightEntity = 0;
        this.fillLightEntity = 0;
        this.rimLightEntity = 0;
        this.leftEyeEntity = 0;
        this.rightEyeEntity = 0;
        this.tongueEntity = 0;
        this.useGlb = false;

        setupViewDefaults();
        setupLighting();
    }

    public View getView() { return view; }
    public Scene getScene() { return scene; }
    public Camera getCamera() { return camera; }
    public Viewport getViewport() { return viewport; }

    public void setModelScale(float scale) {
        this.modelScale = scale;
    }

    public void attachSwapChain(SwapChain sc, int width, int height) {
        this.swapChain = sc;
        this.viewport = new Viewport(0, 0, width, height);
        this.view.setViewport(viewport);
        updateCameraProjection(width, height);
    }

    public void resize(int width, int height) {
        if (swapChain == null || width <= 0 || height <= 0) return;
        viewport = new Viewport(0, 0, width, height);
        view.setViewport(viewport);
        updateCameraProjection(width, height);
    }

    public void buildSnakeBody() {
        if (bodyBuilt) return;

        Material bodyMat = materialProvider.getBodyMaterial();
        if (bodyMat == null) {
            Log.e(TAG, "Material do corpo nao disponivel");
            return;
        }
        MaterialInstance bodyInst = bodyMat.createInstance();
        if (bodyInstance != null) { engine.destroyMaterialInstance(bodyInstance); }
        bodyInstance = bodyInst;
        bodyInst.setParameter(UNIFORM_BASE_COLOR,
            COLOR_BODY_GOLDEN[0], COLOR_BODY_GOLDEN[1], COLOR_BODY_GOLDEN[2]);
        // Slightly less rough and a touch of metallic for a more polished look
        // Acetinado: brilho suave sem cara de plástico (roughness médio, sem metal)
        bodyInst.setParameter(UNIFORM_ROUGHNESS, 0.32f);
        bodyInst.setParameter(UNIFORM_METALLIC, 0.0f);
        bodyInst.setParameter(UNIFORM_USE_SCALE_NORMAL, 1.0f);
        // Verniz acetinado por cima das escamas (Reference 3 clearcoat): base
        // meio fosca + camada de brilho suave. Mesmo BlendingMode do Snake.frag.
        bodyInst.setParameter("clearCoat", 0.10f);
        bodyInst.setParameter("clearCoatRoughness", 0.30f);
        applyScaleTextures(bodyInst);

        FilamentAsset glbAsset = null;
        // Guard de DEV: no emulador x86 o override do GLB (setMaterialInstanceAt)
        // crasha no libfilament-jni (SIGSEGV). Força o caminho procedural lá —
        // a textura de escamas é a MESMA (material compartilhado). Dispositivo
        // real (arm) segue usando o GLB normalmente.
        boolean isDevEmulator = Build.PRODUCT != null && Build.PRODUCT.startsWith("sdk");

        // === MODELO COMPLETO: assets/Serpente_3D.glb (item 6) ===
        // Esse arquivo ja traz malha + material + textura proprios, entao e usado
        // do jeito que esta: NAO passa pelo setMaterialInstanceAt (que era o que
        // derrubava o Filament no Android 16) e NAO recebe olhos/lingua
        // procedurais, porque ele ja e a serpente inteira.
        if (!isDevEmulator && tryBuildFullModel()) {
            bodyBuilt = true;
            return;
        }
        // A36 (Android 16 + Adreno 710) tambem crasha no override do GLB —
        // mesmo SIGSEGV do emulador, confirmado no device em 09/09/2026.
        // Nestes aparelhos o corpo procedural evita a API que derruba o
        // Filament, mantendo a mesma textura de escamas.
        boolean glbOverrideUnsafe = Build.VERSION.SDK_INT >= 36;
        if (!isDevEmulator && !glbOverrideUnsafe) {
            try {
                glbAsset = GlbLoader.load(engine, appContext.getAssets(), "models/cascavel.glb");
            } catch (Exception e) {
                Log.w(TAG, "GLB load failed, using procedural", e);
            }
        } else {
            Log.w(TAG, "Override do GLB inseguro (PRODUCT=" + Build.PRODUCT
                + " SDK=" + Build.VERSION.SDK_INT + "), usando corpo procedural");
        }
        this.glbAsset = glbAsset;

        if (glbAsset != null) {
            useGlb = true;
            Box bb = glbAsset.getBoundingBox();
            if (bb != null) {
                float[] bc = bb.getCenter();
                float[] bh = bb.getHalfExtent();
                Log.d(TAG, "GLB bbox center=(" + bc[0] + "," + bc[1] + "," + bc[2]
                    + ") halfExtent=(" + bh[0] + "," + bh[1] + "," + bh[2] + ")");
            } else {
                Log.d(TAG, "GLB bbox null");
            }
            int[] entities = glbAsset.getEntities();
            RenderableManager rm = engine.getRenderableManager();
            StringBuilder sb = new StringBuilder();
            for (int e : entities) sb.append(e).append(",");
            Log.d(TAG, "GLB entities=[" + sb + "] hasComponent=" + rm.hasComponent(entities[0]));
            for (int e : entities) {
                if (e == 0) continue;
                scene.addEntity(e);
                modelEntities.add(e);
                if (bodyEntity == 0) bodyEntity = e;
                if (rm.hasComponent(e)) {
                    int inst = rm.getInstance(e);
                    int primCount = rm.getPrimitiveCount(inst);
                    Log.d(TAG, "GLB override: e=" + e + " inst=" + inst + " primCount=" + primCount);
                    for (int p = 0; p < primCount; p++) {
                        MaterialInstance orig = rm.getMaterialInstanceAt(inst, p);
                        if (orig != null && !orphanInstances.contains(orig)) orphanInstances.add(orig);
                        rm.setMaterialInstanceAt(e, p, bodyInst);
                    }
                }
            }

            int root = glbAsset.getRoot();
            if (root != 0) {
                TransformManager tm = engine.getTransformManager();
                int t = tm.getInstance(root);
                if (t != 0) {
                    float[] s = new float[16];
                    tm.getTransform(t, s);
                    if (s != null) {
                        float[] scaled = new float[]{
                            s[0] * modelScale, s[1], s[2], s[3],
                            s[4], s[5] * modelScale, s[6], s[7],
                            s[8], s[9], s[10] * modelScale, s[11],
                            s[12], s[13], s[14], s[15]
                        };
                        tm.setTransform(t, scaled);
                    }
                }
            }

            Log.d(TAG, "GLB loaded with " + entities.length + " entities");

            // GLB e so o corpo (sem cabeca/olhos/lingua). Constroi as partes
            // procedurais na ponta do corpo (x ~ 2.8) pra cobrir o que falta.
            buildEyesAndTongue();
            buildParticles();

            bodyBuilt = true;
            return;
        }

        useGlb = false;
        MaterialInstance headInst = bodyMat.createInstance();
        if (headInstance != null) { engine.destroyMaterialInstance(headInstance); }
        headInstance = headInst;
        headInst.setParameter(UNIFORM_BASE_COLOR,
            COLOR_HEAD[0], COLOR_HEAD[1], COLOR_HEAD[2]);
        headInst.setParameter(UNIFORM_ROUGHNESS, 0.3f);
        headInst.setParameter(UNIFORM_METALLIC, 0.0f);
        headInst.setParameter(UNIFORM_USE_SCALE_NORMAL, 1.0f);
        headInst.setParameter("clearCoat", 0.35f);
        headInst.setParameter("clearCoatRoughness", 0.15f);
        applyScaleTextures(headInst);

        bodyMesh = ModelBuilder.buildRattlesnake(engine);
        if (bodyMesh == null) {
            Log.w(TAG, "ModelBuilder.buildRattlesnake() returned null, fallback to old mesh");
            SnakeBodyMesh.RenderedMesh oldMesh = SnakeBodyMesh.build(engine);
            if (oldMesh != null) {
                bodyEntity = makeRenderable(
                    oldMesh.getVertexBuffer(), oldMesh.getIndexBuffer(),
                    oldMesh.getIndexCount(), bodyInst, true, true, true);
                if (bodyEntity != 0) scene.addEntity(bodyEntity);

                sphereMesh = UVSphereMesh.build(engine);
                if (sphereMesh != null) {
                    headEntity = makeRenderable(
                        sphereMesh.getVertexBuffer(), sphereMesh.getIndexBuffer(),
                        sphereMesh.getIndexCount(), headInst, true, true, true);
                    if (headEntity != 0) {
                        TransformManager tm = engine.getTransformManager();
                        int t = tm.create(headEntity);
                        tm.setTransform(t, matrixTranslation(2.4f * modelScale, 0.35f * modelScale, 0.0f));
                        scene.addEntity(headEntity);
                    }
                }
            }
            bodyBuilt = true;
            return;
        }

        bodyEntity = makeRenderable(
            bodyMesh.getVertexBuffer(), bodyMesh.getIndexBuffer(),
            bodyMesh.getIndexCount(), bodyInst, true, true, true);
        if (bodyEntity != 0) {
            TransformManager tm = engine.getTransformManager();
            int t = tm.create(bodyEntity);
            tm.setTransform(t, scaleMatrix(modelScale));
            scene.addEntity(bodyEntity);
        }

        buildEyesAndTongue();

        bodyBuilt = true;
        buildParticles();
    }

    /**
     * Carrega o modelo COMPLETO de assets/Serpente_3D.glb e coloca na cena.
     *
     * Diferenca pro cascavel.glb: aquele e so o corpo e precisava de override de
     * material + olhos/lingua procedurais. Este aqui ja e a serpente inteira com
     * material proprio, entao nada e sobrescrito.
     *
     * @return true se o modelo entrou na cena (ai o corpo procedural nao e montado).
     */
    private boolean tryBuildFullModel() {
        FilamentAsset asset;
        try {
            asset = GlbLoader.load(engine, appContext.getAssets(), "Serpente_3D.glb");
        } catch (Throwable t) { // inclui OutOfMemoryError (o arquivo tem ~64 MB)
            Log.w(TAG, "Serpente_3D.glb nao carregou, usando o corpo procedural", t);
            return false;
        }
        if (asset == null) return false;

        int[] entities = asset.getEntities();
        if (entities == null || entities.length == 0) {
            Log.w(TAG, "Serpente_3D.glb sem entidades");
            GlbLoader.destroy(asset);
            return false;
        }

        RenderableManager rm = engine.getRenderableManager();
        for (int e : entities) {
            if (e == 0) continue;
            scene.addEntity(e);
            modelEntities.add(e);
            if (bodyEntity == 0 && rm.hasComponent(e)) bodyEntity = e;
        }

        // Normaliza escala e centro pelo bounding box do proprio arquivo: o corpo
        // procedural ocupa ~6 unidades (-3..+3) e a camera/zoom/tour do app foi
        // toda calibrada nessa escala. Aqui a gente escala o modelo pra caber no
        // mesmo espaco e o centra na origem — assim ele entra no enquadramento
        // historico sem ter que mexer em camera nenhuma.
        Box bb = asset.getBoundingBox();
        if (bb != null) {
            float[] c = bb.getCenter();
            float[] h = bb.getHalfExtent();
            float maior = Math.max(Math.abs(h[0]), Math.max(Math.abs(h[1]), Math.abs(h[2])));
            Log.d(TAG, "Serpente_3D bbox center=(" + c[0] + "," + c[1] + "," + c[2]
                + ") half=(" + h[0] + "," + h[1] + "," + h[2] + ")");
            if (maior > 0.0001f) {
                float k = 6.0f / (2.0f * maior);
                TransformManager tm = engine.getTransformManager();
                int root = asset.getRoot();
                if (root != 0) {
                    int t = tm.getInstance(root);
                    if (t != 0) {
                        // Coluna-dominante (igual Filament): escala em 0/5/10,
                        // translacao em 12/13/14 -> p * k - centro * k.
                        tm.setTransform(t, new float[]{
                            k, 0, 0, 0,
                            0, k, 0, 0,
                            0, 0, k, 0,
                            -c[0] * k, -c[1] * k, -c[2] * k, 1
                        });
                        Log.d(TAG, "Serpente_3D normalizado com escala k=" + k);
                    }
                }
                // Modelo centrado na origem: a camera historica enquadra ele.
                frameTargetX = 0.0f;
                frameTargetY = 0.0f;
                frameTargetZ = 0.0f;
                frameDistance = 9.3f;
            }
        } else {
            Log.w(TAG, "Serpente_3D.glb sem bounding box");
        }

        glbAsset = asset;
        useGlb = true;
        int w = viewport != null ? viewport.width : 1;
        int h2 = viewport != null ? viewport.height : 1;
        updateCameraProjection(w, h2);
        Log.d(TAG, "Serpente_3D.glb na cena com " + entities.length + " entidades");
        return true;
    }

    private void buildEyesAndTongue() {
        Material eyeMat = materialProvider.getEyeMaterial();
        if (eyeMat != null) {
            MaterialInstance eyeInst = eyeMat.createInstance();
            if (eyeInstance != null) { engine.destroyMaterialInstance(eyeInstance); }
            eyeInstance = eyeInst;
            // Pupila dourada com micro-emissivo: em vez do "ponto preto", o olho
            // ganha um reflexo luminoso que o bloom pega (ballFrag portado).
            eyeInst.setParameter("baseColor", 1.0f, 0.72f, 0.12f);
            eyeInst.setParameter("emissive", 0.9f);
            leftEyeMesh = ModelBuilder.buildEye(engine);
            if (leftEyeMesh != null) {
                leftEyeEntity = makeRenderable(
                    leftEyeMesh.getVertexBuffer(), leftEyeMesh.getIndexBuffer(),
                    leftEyeMesh.getIndexCount(), eyeInst, false, true, false);
                if (leftEyeEntity != 0) {
                    TransformManager tm = engine.getTransformManager();
                    int t = tm.create(leftEyeEntity);
                    // Olho na lateral do crânio, alinhado com a espinha (não no
                    // topo — antes ficava parecendo "chifre/sobrancelha"). A
                    // espinha em t~0.96 fica em (2.30,-0.54,0.24); o olho nasce
                    // na lateral e avança um pouco para o lado.
                    tm.setTransform(t, matrixTranslation(
                        2.28f * modelScale, -0.50f * modelScale, -0.60f * modelScale));
                    scene.addEntity(leftEyeEntity);
                }
            }
            rightEyeMesh = ModelBuilder.buildEye(engine);
            if (rightEyeMesh != null) {
                rightEyeEntity = makeRenderable(
                    rightEyeMesh.getVertexBuffer(), rightEyeMesh.getIndexBuffer(),
                    rightEyeMesh.getIndexCount(), eyeInst, false, true, false);
                if (rightEyeEntity != 0) {
                    TransformManager tm = engine.getTransformManager();
                    int t = tm.create(rightEyeEntity);
                    tm.setTransform(t, matrixTranslation(
                        2.28f * modelScale, -0.50f * modelScale, 0.60f * modelScale));
                    scene.addEntity(rightEyeEntity);
                }
            }
        }

        Material tongueMat = materialProvider.getTongueMaterial();
        if (tongueMat != null) {
            MaterialInstance tongueInst = tongueMat.createInstance();
            if (tongueInstance != null) { engine.destroyMaterialInstance(tongueInstance); }
            tongueInstance = tongueInst;
            tongueMesh = ModelBuilder.buildTongue(engine);
            if (tongueMesh != null) {
                tongueInstance = tongueInst;
                tongueInst.setParameter("baseColor", 0.92f, 0.12f, 0.12f);
                tongueInst.setParameter("emissive", 0.30f);
                tongueEntity = makeRenderable(
                    tongueMesh.getVertexBuffer(), tongueMesh.getIndexBuffer(),
                    tongueMesh.getIndexCount(), tongueInst, false, false, true);
                if (tongueEntity != 0) {
                    TransformManager tm = engine.getTransformManager();
                    int t = tm.create(tongueEntity);
                    // Língua ancorada no focinho REAL (t=1.0). A espinha dobra pra CIMA na
                    // ponta: focinho y ≈ -0.628, com a língua reto em +X ela "saía por
                    // baixo" do nariz, solta (Gemini v9.4..v9.8). Correção: translada
                    // base pra trás (dentro da cabeça, boca) E rotaciona +0.20 rad em Z
                    // pra acompanhar a subida da ponta (dy/dx≈0.2 → ~11.6°).
                    float[] foc = spinePos(1.0f);
                    tm.setTransform(t, matrixRotZTrans(
                        0.20f,
                        foc[0] - 0.10f * modelScale,
                        foc[1] - 0.08f * modelScale,
                        foc[2] - 0.02f * modelScale));
                    scene.addEntity(tongueEntity);
                }
            }
        }
    }

    private void applyScaleTextures(MaterialInstance inst) {
        if (scaleTexture == null) {
            scaleTexture = TextureGenerator.generateScaleDiffuse(engine);
        }
        if (scaleNormalTexture == null) {
            scaleNormalTexture = TextureGenerator.generateScaleNormal(engine);
        }
        TextureSampler sampler = new TextureSampler(
            TextureSampler.MinFilter.LINEAR,
            TextureSampler.MagFilter.LINEAR,
            TextureSampler.WrapMode.REPEAT,
            TextureSampler.WrapMode.REPEAT,
            TextureSampler.WrapMode.REPEAT);
        inst.setParameter(UNIFORM_SCALE_TEXTURE, scaleTexture, sampler);
        inst.setParameter(UNIFORM_SCALE_NORMAL_TEXTURE, scaleNormalTexture, sampler);
        inst.setParameter(UNIFORM_USE_TEXTURE, 1.0f);
    }

    private void buildParticles() {
        if (!particleEntities.isEmpty()) return;
        Material pMat = materialProvider.getParticleMaterial();
        if (pMat == null) {
            Log.w(TAG, "Material de partícula indisponível");
            return;
        }
        UVSphereMesh.RenderedMesh particleSphere = UVSphereMesh.build(engine);
        if (particleSphere == null) {
            Log.w(TAG, "UVSphereMesh nulo para partículas");
            return;
        }
        particleMesh = particleSphere;
        TransformManager tm = engine.getTransformManager();
        Random rnd = new Random(42);
        // Partículas em anel ao redor do corpo inteiro (Gemini pediu: antes elas
        // amontoavam no dorso e escondiam a cobra). Cada uma fica presa a um ponto
        // da espinha e orbita ao redor dela com raio fixo — a mesma equação de
        // espinha do ModelBuilder, então o anel acompanha as curvas da cobra.
        for (int i = 0; i < 36; i++) {
            Log.d(TAG, "Particle " + i + " building...");
            MaterialInstance pInst = pMat.createInstance();
            particleInstances.add(pInst);
            pInst.setParameter(UNIFORM_BASE_COLOR, 0.30f, 1.0f, 0.70f);
            pInst.setParameter(UNIFORM_EMISSIVE, 2.6f);
            int e = makeRenderable(
                particleSphere.getVertexBuffer(), particleSphere.getIndexBuffer(),
                particleSphere.getIndexCount(), pInst, false, false, false);
            if (e == 0) continue;

            // t ao longo do corpo de ponta a ponta (Gemini v9: antes só cobria a
            // metade esquerda). Distribuição uniforme no anel + jitter no raio.
            float tSpine = 0.03f + 0.94f * ((float) i / 36f + rnd.nextFloat() * 0.02f);
            if (tSpine > 0.99f) tSpine = 0.99f;

            // espinha do corpo na posição t (mesma fórmula do ModelBuilder)
            float spineX = (-2.5f + 5.0f * tSpine) * modelScale;
            float spineY = (float) (0.55 * Math.sin(1.8 * Math.PI * tSpine)
                    + 0.25 * Math.sin(3.2 * Math.PI * tSpine + 0.3)) * modelScale;
            float spineZ = (float) (0.35 * Math.sin(2.1 * Math.PI * tSpine + 0.7)) * modelScale;

            // ângulo estratificado (Golden ratio) pra espalhar uniforme no anel —
            // antes aleatório aglomerava partículas na mesma direção (Gemini v9.2)
            float angle = (float) (i * 2.39996323) + rnd.nextFloat() * 0.4f;
            // raio de órbita: um pouco além da superfície (corpo ~0.28*1.16*1.2 ≈ 0.39),
            // maior variabilidade pra envolver por todos os lados sem empilhar
            float orbitR = 0.58f + rnd.nextFloat() * 0.28f;

            float seedX = spineX;
            float y0 = spineY + (float) Math.cos(angle) * orbitR;
            float seedZ = spineZ + (float) Math.sin(angle) * orbitR;

            int t = tm.create(e);
            tm.setTransform(t, scaleTranslate(0.12f, seedX, y0, seedZ));
            scene.addEntity(e);
            particleEntities.add(e);
            // seed: [espinhaX, espinhaY, espinhaZ, ângulo inicial, raio de órbita]
            particleSeeds.add(new float[]{ spineX, spineY, spineZ, angle, orbitR });
        }
        Log.d(TAG, "Partículas: " + particleEntities.size());
    }

    // Posição da espinha do corpo (mesma fórmula do ModelBuilder) já em escala
    // de mundo. Usada pra ancorar a língua na boca sem hardcode.
    private float[] spinePos(float tSpine) {
        float[] p = new float[3];
        p[0] = (-2.5f + 5.0f * tSpine) * modelScale;
        p[1] = (float) (0.55 * Math.sin(1.8 * Math.PI * tSpine)
                + 0.25 * Math.sin(3.2 * Math.PI * tSpine + 0.3)) * modelScale;
        p[2] = (float) (0.35 * Math.sin(2.1 * Math.PI * tSpine + 0.7)) * modelScale;
        return p;
    }

    private void animateParticles(long frameTimeNanos) {
        if (particleEntities.isEmpty() || particleSeeds.size() != particleEntities.size()) return;
        float elapsed = (frameTimeNanos - animStartNanos) / 1_000_000_000.0f;
        TransformManager tm = engine.getTransformManager();
        for (int i = 0; i < particleEntities.size(); i++) {
            int p = particleEntities.get(i);
            float[] s = particleSeeds.get(i);
            int t = tm.getInstance(p);
            if (t == 0) continue;
            // órbita lenta em volta da espinha: o ângulo anda devagar e o raio
            // oscila um pouquinho, mantendo sempre o anel ao redor do corpo
            float angle = s[3] + elapsed * 0.25f;
            float orbitR = s[4] * (1.0f + 0.10f * (float) Math.sin(elapsed * 0.6f + s[3]));
            float y = s[1] + (float) Math.cos(angle) * orbitR;
            float z = s[2] + (float) Math.sin(angle) * orbitR;
            tm.setTransform(t, scaleTranslate(0.12f, s[0], y, z));
        }
    }

    public void resetLayer(int layerIndex) {
        if (layerIndex == currentLayerIndex) return;
        currentLayerIndex = layerIndex;
        bodyBuilt = false;
        hotspotsBuilt = false;
        RenderableManager rm = engine.getRenderableManager();
        if (useGlb) {
            // As entidades de um GLB pertencem ao ASSET: quem libera elas e o
            // proprio asset. Destruir entidade por entidade aqui (como era antes)
            // deixava o asset vivo na memoria — com o modelo completo de ~64 MB
            // isso estourava a memoria depois de duas trocas de camada.
            if (glbAsset != null) {
                GlbLoader.destroy(glbAsset);
                glbAsset = null;
            }
            modelEntities.clear();
            useGlb = false;
        }
        if (bodyEntity != 0) {
            scene.removeEntity(bodyEntity);
            if (rm.hasComponent(bodyEntity)) rm.destroy(bodyEntity);
            bodyEntity = 0;
        }
        if (headEntity != 0) {
            scene.removeEntity(headEntity);
            if (rm.hasComponent(headEntity)) rm.destroy(headEntity);
            headEntity = 0;
        }
        if (leftEyeEntity != 0) {
            scene.removeEntity(leftEyeEntity);
            if (rm.hasComponent(leftEyeEntity)) rm.destroy(leftEyeEntity);
            leftEyeEntity = 0;
        }
        if (rightEyeEntity != 0) {
            scene.removeEntity(rightEyeEntity);
            if (rm.hasComponent(rightEyeEntity)) rm.destroy(rightEyeEntity);
            rightEyeEntity = 0;
        }
        if (tongueEntity != 0) {
            scene.removeEntity(tongueEntity);
            if (rm.hasComponent(tongueEntity)) rm.destroy(tongueEntity);
            tongueEntity = 0;
        }
        for (int e : hotspotEntities) {
            if (e != 0) {
                scene.removeEntity(e);
                if (rm.hasComponent(e)) rm.destroy(e);
            }
        }
        hotspotEntities.clear();
        hotspotWorldPositions.clear();
        // destroy any per-hotspot material instances
        for (MaterialInstance mi : hotspotInstances) {
            try { engine.destroyMaterialInstance(mi); } catch (Exception ignored) {}
        }
        hotspotInstances.clear();
        removeParticles();
    }

    private void removeParticles() {
        RenderableManager rm = engine.getRenderableManager();
        TransformManager tm = engine.getTransformManager();
        for (int e : particleEntities) {
            if (e == 0) continue;
            scene.removeEntity(e);
            if (rm.hasComponent(e)) rm.destroy(e);
            if (tm.getInstance(e) != 0) tm.destroy(e);
            EntityManager.get().destroy(e);
        }
        particleEntities.clear();
        particleSeeds.clear();
        for (MaterialInstance mi : particleInstances) {
            try { engine.destroyMaterialInstance(mi); } catch (Exception ignored) {}
        }
        particleInstances.clear();
        if (particleMesh != null) { particleMesh.destroy(engine); particleMesh = null; }
    }

    public void buildHotspots(List<AnatomyHotspot> hotspots) {
        if (hotspotsBuilt) return;
        if (hotspots == null || hotspots.isEmpty()) {
            hotspotsBuilt = true;
            return;
        }

        Material hotMat = materialProvider.getHotspotMaterial();
        if (hotMat == null) {
            Log.e(TAG, "Material de hotspot nao disponivel");
            return;
        }

        // Destroy any existing per-hotspot material instances we created previously
        if (!hotspotInstances.isEmpty()) {
            for (MaterialInstance mi : hotspotInstances) {
                try { engine.destroyMaterialInstance(mi); } catch (Exception ignored) {}
            }
            hotspotInstances.clear();
        }

        hotspotMesh = UVSphereMesh.build(engine);
        if (hotspotMesh == null) {
            Log.w(TAG, "UVSphereMesh.build() retornou null para hotspots");
            hotspotsBuilt = true;
            return;
        }

        RenderableManager rm = engine.getRenderableManager();
        TransformManager tm = engine.getTransformManager();
        hotspotEntities.clear();
        hotspotWorldPositions.clear();
        hotspotInstances.clear();

        for (int i = 0; i < hotspots.size(); i++) {
            AnatomyHotspot h = hotspots.get(i);
            // Create per-hotspot material instance so we can pulse emissive for a single hotspot
            MaterialInstance mi = hotMat.createInstance();
            mi.setParameter(UNIFORM_BASE_COLOR,
                COLOR_HOTSPOT[0], COLOR_HOTSPOT[1], COLOR_HOTSPOT[2]);
            // start with zero emissive so hotspots are not constantly glowing
            mi.setParameter(UNIFORM_EMISSIVE, 0.0f);
            hotspotInstances.add(mi);

            int e = makeRenderable(
                hotspotMesh.getVertexBuffer(), hotspotMesh.getIndexBuffer(),
                hotspotMesh.getIndexCount(), mi, false, false, false);
            if (e == 0) continue;
            float x = h.getPositionX() * modelScale;
            float y = h.getPositionY() * modelScale;
            float z = h.getPositionZ() * modelScale;
            int t = tm.create(e);
            tm.setTransform(t, matrixTranslation(x, y, z));
            scene.addEntity(e);
            hotspotEntities.add(e);
            hotspotWorldPositions.add(new float[]{ x, y, z });
        }
        hotspotsBuilt = true;
    }

    public boolean isHotspotEntity(int entity) {
        for (int e : hotspotEntities) if (e == entity) return true;
        return false;
    }

    public void startTour() {
        tourKeyframe  = 0;
        tourProgress  = 0.0f;
        lastFrameTimeNanos = 0L;
        animStartNanos = -1L;
        tourRunning   = true;
        if (structureListener != null) {
            structureListener.onTourStructureChanged(0);
        }
    }

    public void stopTour() {
        tourRunning = false;
    }

    public boolean isTourRunning() {
        return tourRunning;
    }

    public void orbit(float deltaX, float deltaY) {
        orbitTheta -= deltaX * 0.008f;
        orbitPhi = Math.max(-1.3f, Math.min(1.3f, orbitPhi - deltaY * 0.008f));
        updateOrbitCamera();
    }

    public void zoom(float factor) {
        orbitDistance = Math.max(3.0f, Math.min(50.0f, orbitDistance * factor));
        updateOrbitCamera();
    }

    // Reset camera to a sensible default view (used by UI reset button)
    // Igual ao default do updateCameraProjection: theta=PI/2 + phi=0.36 da
    // uma vista 3/4 lateral da cobra. theta=0 olhava a cobra "de ponta",
    // sumindo com a cabeca/olhos/língua e deixando o corpo parecendo um tubo.
    // Target X=2.0 (pertinho do focinho ~2.9): enquadra cabeca, olhos e a
    // lingua — o Gemini pediu pra cabeca ficar visível.
    public void resetCamera() {
        orbitTheta = (float) Math.PI / 2.0f;
        orbitPhi = 0.22f;
        // dist 9.3 + target 1.0: corpo inteiro (-3..+3) folgado na margem,
        // focinho/língua deixam de tocar a borda direita (Gemini v9.3/v9.4)
        orbitDistance = frameDistance;
        orbitTargetX = frameTargetX;
        orbitTargetY = frameTargetY;
        orbitTargetZ = frameTargetZ;
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

    private void updateTour(long frameTimeNanos) {
        if (!tourRunning) return;

        float deltaSec = 0.0f;
        if (lastFrameTimeNanos != 0L) {
            long deltaNs = frameTimeNanos - lastFrameTimeNanos;
            if (deltaNs > 0 && deltaNs < 500_000_000L) {
                deltaSec = deltaNs / 1_000_000_000.0f;
            }
        }
        lastFrameTimeNanos = frameTimeNanos;

        float currentDuration = KEYFRAME_DURATIONS[tourKeyframe];
        int lastKnownKeyframe = tourKeyframe;
        tourProgress += deltaSec / currentDuration;

        if (tourProgress >= 1.0f) {
            tourProgress = 0.0f;
            tourKeyframe++;
            if (tourKeyframe >= TOUR_KEYFRAMES.length - 1) {
                float[] last = TOUR_KEYFRAMES[TOUR_KEYFRAMES.length - 1];
                camera.lookAt(last[0], last[1], last[2], last[3], last[4], last[5], 0.0, 1.0, 0.0);
                tourRunning = false;
                if (structureListener != null) {
                    structureListener.onTourStructureChanged(TOUR_KEYFRAMES.length - 1);
                }
                return;
            }
            if (structureListener != null) {
                structureListener.onTourStructureChanged(tourKeyframe);
            }
        }

        int nextKeyframe = Math.min(tourKeyframe + 1, TOUR_KEYFRAMES.length - 1);
        float[] from = TOUR_KEYFRAMES[tourKeyframe];
        float[] to   = TOUR_KEYFRAMES[nextKeyframe];
        float   s    = smoothstep(tourProgress);

        double eyeX    = lerp(from[0], to[0], s);
        double eyeY    = lerp(from[1], to[1], s);
        double eyeZ    = lerp(from[2], to[2], s);
        double targetX = lerp(from[3], to[3], s);
        double targetY = lerp(from[4], to[4], s);
        double targetZ = lerp(from[5], to[5], s);

        camera.lookAt(eyeX, eyeY, eyeZ, targetX, targetY, targetZ, 0.0, 1.0, 0.0);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float smoothstep(float t) {
        return t * t * (3f - 2f * t);
    }

    public void render(long frameTimeNanos) {
        if (swapChain == null) return;
        if (tourRunning) updateTour(frameTimeNanos);
        animateBreathing(frameTimeNanos);
        animateTongue(frameTimeNanos);
        // Animate any hotspot pulse before particles so it is visible
        animateHotspotFrame(frameTimeNanos);
        animateParticles(frameTimeNanos);
        if (renderer.beginFrame(swapChain, frameTimeNanos)) {
            renderer.render(view);
            renderer.endFrame();
        }
    }

    // Public API to trigger a short pulse animation on a hotspot by index
    public void animateHotspot(int index) {
        if (index < 0 || index >= hotspotEntities.size()) return;
        animHotspotIndex = index;
        animHotspotStartNanos = -1L;
        animHotspotActive = true;
    }

    private void animateHotspotFrame(long frameTimeNanos) {
        if (!animHotspotActive) return;
        if (animHotspotIndex < 0 || animHotspotIndex >= hotspotInstances.size()) {
            animHotspotActive = false; return;
        }
        if (animHotspotStartNanos < 0) animHotspotStartNanos = frameTimeNanos;
        float elapsed = (frameTimeNanos - animHotspotStartNanos) / 1_000_000_000.0f;
        float t = elapsed / HOTSPOT_ANIM_DURATION;
        MaterialInstance mi = hotspotInstances.get(animHotspotIndex);
        if (mi == null) { animHotspotActive = false; return; }
        if (t >= 1.0f) {
            try { mi.setParameter(UNIFORM_EMISSIVE, 0.0f); } catch (Exception ignored) {}
            animHotspotActive = false;
            animHotspotIndex = -1;
            animHotspotStartNanos = -1L;
            return;
        }
        // pulse using sine (0 -> peak -> 0). amplitude=3.0 for a noticeable but short glow
        float pulse = 3.0f * Math.max(0.0f, (float) Math.sin(Math.PI * t));
        try { mi.setParameter(UNIFORM_EMISSIVE, pulse); } catch (Exception ignored) {}
    }

    private void animateBreathing(long frameTimeNanos) {
        if (useGlb) return;
        if (animStartNanos < 0) animStartNanos = frameTimeNanos;
        float elapsed = (frameTimeNanos - animStartNanos) / 1_000_000_000.0f;

        if (bodyEntity != 0) {
            TransformManager tm = engine.getTransformManager();
            int t = tm.getInstance(bodyEntity);
            if (t != 0) {
                float breath = 1.0f + 0.003f * (float) Math.sin(elapsed * 2.094f);
                float s = modelScale;
                breathMatrix[0] = s;
                breathMatrix[1] = 0;
                breathMatrix[2] = 0;
                breathMatrix[3] = 0;
                breathMatrix[4] = 0;
                breathMatrix[5] = s * breath;
                breathMatrix[6] = 0;
                breathMatrix[7] = 0;
                breathMatrix[8] = 0;
                breathMatrix[9] = 0;
                breathMatrix[10] = s;
                breathMatrix[11] = 0;
                breathMatrix[12] = 0;
                breathMatrix[13] = 0;
                breathMatrix[14] = 0;
                breathMatrix[15] = 1;
                tm.setTransform(t, breathMatrix);
            }
        }
    }

    private void animateTongue(long frameTimeNanos) {
        if (tongueEntity == 0) return;
        if (animStartNanos < 0) animStartNanos = frameTimeNanos;
        float elapsed = (frameTimeNanos - animStartNanos) / 1_000_000_000.0f;

        TransformManager tm = engine.getTransformManager();
        int t = tm.getInstance(tongueEntity);
        if (t != 0) {
            // BUG RAIZ (Gemini v9.4..v9.8): aqui o transform era sobrescrito a cada
            // frame com (2.42, 0.42, 0)*1.2 — y=+0.5, BEM ACIMA da espinha do focinho
            // (y≈-0.63). A língua ancorada no nariz era substituída e aparecia
            // "flutuando à direita da cabeça, perto do botão ?". Agora reusa a mesma
            // âncora+rotação do build e só adiciona o "linguado" (oscilação no X).
            float[] foc = spinePos(1.0f);
            float oscX = 0.12f * (float) Math.sin(elapsed * 5.0f);
            tm.setTransform(t, matrixRotZTrans(
                0.20f,
                (foc[0] - 0.10f * modelScale) + oscX,
                foc[1] - 0.08f * modelScale,
                foc[2] - 0.02f * modelScale));
        }
    }

    /**
     * Chamado pela AnatomySurfaceView quando a surface foi destruída — o SwapChain
     * já foi liberado por lá. Só invalidamos a referência para o destroy() não
     * tentar liberar duas vezes (crash "Calling method on destroyed SwapChain").
     */
    public void invalidateSwapChain() {
        this.swapChain = null;
    }

    public void destroy() {
        RenderableManager rm = engine.getRenderableManager();
        TransformManager tm = engine.getTransformManager();

        if (glbAsset != null) {
            // O AssetLoader do gltfio é quem criou o FilamentAsset e suas MaterialInstances.
            // destroyAsset() libera tudo de uma vez — entidades, renderables e materiais —
            // evitando o "Object doesn't exist (double free?)" da destruição manual.
            GlbLoader.destroy(glbAsset);
            glbAsset = null;
            orphanInstances.clear();
        }

        removeParticles();

        // destroy hotspots and their material instances
        for (int e : hotspotEntities) {
            if (e == 0) continue;
            scene.removeEntity(e);
            if (rm.hasComponent(e)) rm.destroy(e);
            if (tm.getInstance(e) != 0) tm.destroy(e);
            EntityManager.get().destroy(e);
        }
        hotspotEntities.clear();
        hotspotWorldPositions.clear();
        for (MaterialInstance mi : hotspotInstances) {
            try { engine.destroyMaterialInstance(mi); } catch (Exception ignored) {}
        }
        hotspotInstances.clear();

        if (bodyEntity != 0) {
            scene.removeEntity(bodyEntity);
            if (rm.hasComponent(bodyEntity)) rm.destroy(bodyEntity);
            tm.destroy(bodyEntity);
            EntityManager.get().destroy(bodyEntity);
            bodyEntity = 0;
        }
        if (headEntity != 0) {
            scene.removeEntity(headEntity);
            if (rm.hasComponent(headEntity)) rm.destroy(headEntity);
            tm.destroy(headEntity);
            EntityManager.get().destroy(headEntity);
            headEntity = 0;
        }
        if (leftEyeEntity != 0) {
            scene.removeEntity(leftEyeEntity);
            if (rm.hasComponent(leftEyeEntity)) rm.destroy(leftEyeEntity);
            tm.destroy(leftEyeEntity);
            EntityManager.get().destroy(leftEyeEntity);
            leftEyeEntity = 0;
        }
        if (rightEyeEntity != 0) {
            scene.removeEntity(rightEyeEntity);
            if (rm.hasComponent(rightEyeEntity)) rm.destroy(rightEyeEntity);
            tm.destroy(rightEyeEntity);
            EntityManager.get().destroy(rightEyeEntity);
            rightEyeEntity = 0;
        }
        if (tongueEntity != 0) {
            scene.removeEntity(tongueEntity);
            if (rm.hasComponent(tongueEntity)) rm.destroy(tongueEntity);
            tm.destroy(tongueEntity);
            EntityManager.get().destroy(tongueEntity);
            tongueEntity = 0;
        }

        for (int e : modelEntities) {
            if (e == 0) continue;
            scene.removeEntity(e);
            if (rm.hasComponent(e)) rm.destroy(e);
            EntityManager.get().destroy(e);
        }
        modelEntities.clear();

        if (lightEntity != 0) {
            scene.removeEntity(lightEntity);
            if (engine.getLightManager().hasComponent(lightEntity)) {
                engine.getLightManager().destroy(lightEntity);
            }
            EntityManager.get().destroy(lightEntity);
            lightEntity = 0;
        }
        if (fillLightEntity != 0) {
            scene.removeEntity(fillLightEntity);
            if (engine.getLightManager().hasComponent(fillLightEntity)) {
                engine.getLightManager().destroy(fillLightEntity);
            }
            EntityManager.get().destroy(fillLightEntity);
            fillLightEntity = 0;
        }
        if (rimLightEntity != 0) {
            scene.removeEntity(rimLightEntity);
            if (engine.getLightManager().hasComponent(rimLightEntity)) {
                engine.getLightManager().destroy(rimLightEntity);
            }
            EntityManager.get().destroy(rimLightEntity);
            rimLightEntity = 0;
        }
        if (swapChain != null) {
            engine.destroySwapChain(swapChain);
            swapChain = null;
        }
        if (skybox != null) { engine.destroySkybox(skybox); skybox = null; }
        if (indirectLight != null) { engine.destroyIndirectLight(indirectLight); indirectLight = null; }
        if (colorGrading != null) { engine.destroyColorGrading(colorGrading); colorGrading = null; }
        if (backgroundTexture != null) { engine.destroyTexture(backgroundTexture); backgroundTexture = null; }
        if (scaleTexture != null) { engine.destroyTexture(scaleTexture); scaleTexture = null; }
        if (scaleNormalTexture != null) { engine.destroyTexture(scaleNormalTexture); scaleNormalTexture = null; }
        if (bodyMesh != null) { bodyMesh.destroy(engine); bodyMesh = null; }
        if (leftEyeMesh != null) { leftEyeMesh.destroy(engine); leftEyeMesh = null; }
        if (rightEyeMesh != null) { rightEyeMesh.destroy(engine); rightEyeMesh = null; }
        if (tongueMesh != null) { tongueMesh.destroy(engine); tongueMesh = null; }
        if (sphereMesh != null) { sphereMesh.destroy(engine); sphereMesh = null; }
        if (hotspotMesh != null) { hotspotMesh.destroy(engine); hotspotMesh = null; }

        engine.destroyView(view);
        engine.destroyScene(scene);
        engine.destroyCameraComponent(camera.getEntity());
        EntityManager.get().destroy(camera.getEntity());
        if (bodyInstance != null) { engine.destroyMaterialInstance(bodyInstance); bodyInstance = null; }
        if (headInstance != null) { engine.destroyMaterialInstance(headInstance); headInstance = null; }
        if (eyeInstance != null) { engine.destroyMaterialInstance(eyeInstance); eyeInstance = null; }
        if (tongueInstance != null) { engine.destroyMaterialInstance(tongueInstance); tongueInstance = null; }
        for (MaterialInstance mi : particleInstances) { engine.destroyMaterialInstance(mi); }
        particleInstances.clear();
        if (hotspotInstance != null) { engine.destroyMaterialInstance(hotspotInstance); hotspotInstance = null; }
        materialProvider.destroy();
    }

    public int pickHotspotIndex(int screenX, int screenY, int viewWidth, int viewHeight,
                                List<AnatomyHotspot> hotspots) {
        if (hotspots == null || hotspots.isEmpty() || hotspotWorldPositions.isEmpty()) {
            return -1;
        }
        if (viewWidth <= 0 || viewHeight <= 0) return -1;

        float ndcX = (2.0f * screenX) / viewWidth - 1.0f;
        float ndcY = 1.0f - (2.0f * screenY) / viewHeight;

        float[] eyeF = new float[3];
        camera.getPosition(eyeF);
        float[] dirFF = new float[3];
        camera.getForwardVector(dirFF);

        double[] eye = new double[]{ eyeF[0], eyeF[1], eyeF[2] };
        double[] dirF = new double[]{ dirFF[0], dirFF[1], dirFF[2] };

        double[] up = { 0.0, 1.0, 0.0 };
        double[] dirR = cross(dirF, up);
        normalize(dirR);
        double[] dirU = cross(dirR, dirF);
        normalize(dirU);

        double aspect = (double) viewWidth / viewHeight;
        double fovRad = Math.toRadians(50.0);
        double halfH = Math.tan(fovRad / 2.0);
        double halfW = halfH * aspect;

        double[] rayDir = new double[3];
        rayDir[0] = dirF[0] + dirR[0] * ndcX * halfW + dirU[0] * ndcY * halfH;
        rayDir[1] = dirF[1] + dirR[1] * ndcX * halfW + dirU[1] * ndcY * halfH;
        rayDir[2] = dirF[2] + dirR[2] * ndcX * halfW + dirU[2] * ndcY * halfH;
        normalize(rayDir);

        double bestT = Double.MAX_VALUE;
        int bestIdx = -1;
        for (int i = 0; i < hotspotWorldPositions.size(); i++) {
            float[] p = hotspotWorldPositions.get(i);
            double vx = p[0] - eye[0];
            double vy = p[1] - eye[1];
            double vz = p[2] - eye[2];
            double t = vx * rayDir[0] + vy * rayDir[1] + vz * rayDir[2];
            if (t < 0) continue;
            double px = rayDir[0] * t;
            double py = rayDir[1] * t;
            double pz = rayDir[2] * t;
            double dx = vx - px;
            double dy = vy - py;
            double dz = vz - pz;
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d < 0.6 && t < bestT) {
                bestT = t;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private static double[] cross(double[] a, double[] b) {
        return new double[]{
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0]
        };
    }

    private static void normalize(double[] v) {
        double len = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (len < 1e-9) return;
        v[0] /= len; v[1] /= len; v[2] /= len;
    }

    private void setupViewDefaults() {
        view.setCamera(camera);

        if (skybox != null) {
            engine.destroySkybox(skybox);
            skybox = null;
        }
        scene.setSkybox(null);

        Renderer.ClearOptions clearOpts = new Renderer.ClearOptions();
        clearOpts.clearColor = new double[]{ 1.0, 1.0, 1.0, 1.0 };
        clearOpts.clear = true;
        renderer.setClearOptions(clearOpts);
        view.setShadowingEnabled(true);
        view.setAntiAliasing(View.AntiAliasing.FXAA);
        view.setDithering(View.Dithering.TEMPORAL);

        View.VsmShadowOptions vsm = new View.VsmShadowOptions();
        vsm.mipmapping = true;
        vsm.msaaSamples = 2;
        vsm.highPrecision = true;
        // Reduce light bleed slightly and increase min variance for softer shadows
        vsm.minVarianceScale = 0.75f;
        vsm.lightBleedReduction = 0.08f;
        view.setVsmShadowOptions(vsm);

        // Bloom seletivo (Reference 3: webgl_postprocessing_unreal_bloom_selective):
        // threshold afastado pra luzes comuns não estourar — só o emissivo das
        // partículas/olhos passa do limite e ganha glow. Nada de lens dirt.
        View.BloomOptions bloom = new View.BloomOptions();
        bloom.enabled = true;
        // nesta versão do Filament o "threshold" vira duas coisas:
        // threshold = habilita o modo seletivo e highlight = o brilho mínimo.
        bloom.threshold = true;
        bloom.highlight = 1.4f;
        bloom.strength = 0.6f;
        bloom.dirt = null;
        view.setBloomOptions(bloom);

        // ColorGrading com tone mapping cinematográfico (Reference 3: NeutralToneMapping
        // + exposure). Aplica um contraste suave estilo "trailer" no modelo inteiro.
        try {
            ColorGrading cg = new ColorGrading.Builder()
                    .toneMapping(ColorGrading.ToneMapping.FILMIC)
                    // saturei 1.3: o tom jade do corpo tendia a acinzentar no
                    // tonemapping FILMIC — subir a saturação devolve o verde vivo
                    .saturation(1.3f)
                    .quality(ColorGrading.QualityLevel.ULTRA)
                    .build(engine);
            colorGrading = cg;
            view.setColorGrading(cg);
        } catch (Throwable t) {
            Log.w(TAG, "ColorGrading indisponivel, usando default", t);
        }

        // IBL ambiente: sem HDRI de verdade, geramos um IndirectLight a partir do
        // mesmo cubemap do fundo — o clearcoat e o specular do corpo refletem algo
        // em vez de depender só das luzes direcionais. Intensidade baixa de proposito.
        try {
            IndirectLight ibl = new IndirectLight.Builder()
                    .reflections(backgroundTexture)
                    .irradiance(backgroundTexture)
                    // 30k em vez de 40k: o IBL alto lavava os highlights do
                    // clearcoat pra tons claros/dourados, sumindo o jade
                    .intensity(30_000.0f)
                    .build(engine);
            scene.setIndirectLight(ibl);
            indirectLight = ibl;
        } catch (Throwable t) {
            Log.w(TAG, "IndirectLight indisponivel", t);
        }
    }

    private void setupLighting() {
        lightEntity = EntityManager.get().create();
        LightManager lm = engine.getLightManager();
        LightManager.ShadowOptions shadowOpts = new LightManager.ShadowOptions();
        shadowOpts.mapSize = 2048;
        shadowOpts.shadowCascades = 4;
        shadowOpts.cascadeSplitPositions = new float[]{ 0.05f, 0.15f, 0.4f, 1.0f };
        shadowOpts.constantBias = 0.001f;
        shadowOpts.normalBias = 0.002f;

        LightManager.Builder sun = new LightManager.Builder(LightManager.Type.DIRECTIONAL)
        // Slightly warmer key light for pleasing skin tones
        .color(1.05f, 0.96f, 0.88f)
        // Balanced intensity: high values blow out the scale texture to white
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
        // softer, cooler rim for subtle separation
        .color(0.55f, 0.85f, 0.75f)
        .intensity(18_000.0f)
        .direction(0.0f, 0.55f, -1.0f)
        .castShadows(false);
        rim.build(engine, rimLightEntity);
        scene.addEntity(rimLightEntity);
    }

    private void updateCameraProjection(int w, int h) {
        double aspect = (double) w / Math.max(1, h);
        camera.setProjection(50.0, aspect, 0.1, 200.0, Camera.Fov.VERTICAL);
        orbitTargetX = frameTargetX;
        orbitTargetY = frameTargetY;
        orbitTargetZ = frameTargetZ;
        orbitDistance = frameDistance;
        orbitTheta = (float) Math.PI / 2.0f;
        orbitPhi = 0.22f;
        updateOrbitCamera();
    }

    private int makeRenderable(VertexBuffer vb, IndexBuffer ib, int indexCount,
                               MaterialInstance mat, boolean culling,
                               boolean castShadows, boolean receiveShadows) {
        int entity = EntityManager.get().create();
        RenderableManager.Builder b = new RenderableManager.Builder(1)
            .boundingBox(new Box(-3.0f, -0.8f, -0.8f, 3.0f, 0.8f, 0.8f))
            .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vb, ib)
            .material(0, mat)
            .culling(culling)
            .castShadows(castShadows)
            .receiveShadows(receiveShadows);
        b.build(engine, entity);
        boolean hasComp = engine.getRenderableManager().hasComponent(entity);
        int inst = engine.getRenderableManager().getInstance(entity);
        Log.d(TAG, "makeRenderable: entity=" + entity
            + " vbNull=" + (vb == null) + " ibNull=" + (ib == null)
            + " matNull=" + (mat == null) + " hasComp=" + hasComp
            + " inst=" + inst
            + " primCount=" + (hasComp ? engine.getRenderableManager().getPrimitiveCount(inst) : -1));
        return entity;
    }

    private static float[] matrixTranslation(float x, float y, float z) {
        return new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            x, y, z, 1
        };
    }

    // Translação + rotação no eixo Z (coluna-dominante, igual Filament).
    // Usada pra língua: o focinho sobe (dy/dx≈+0.2 na ponta) enquanto a língua
    // apontava reto +X e "saía por baixo" do nariz parecendo solta (Gemini v9.7).
    private static float[] matrixRotZTrans(float angleRad, float x, float y, float z) {
        float c = (float) Math.cos(angleRad);
        float s = (float) Math.sin(angleRad);
        return new float[]{
            c,  s, 0, 0,
           -s,  c, 0, 0,
            0,  0, 1, 0,
            x,  y, z, 1
        };
    }

    private static float[] scaleTranslate(float s, float x, float y, float z) {
        return new float[]{
            s, 0, 0, 0,
            0, s, 0, 0,
            0, 0, s, 0,
            x, y, z, 1
        };
    }

    private static float[] scaleMatrix(float s) {
        return new float[]{
            s, 0, 0, 0,
            0, s, 0, 0,
            0, 0, s, 0,
            0, 0, 0, 1
        };
    }
}
