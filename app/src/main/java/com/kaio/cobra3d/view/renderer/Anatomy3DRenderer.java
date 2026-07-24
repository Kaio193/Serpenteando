package com.kaio.cobra3d.view.renderer;

import android.content.Context;
import android.util.Log;

import com.google.android.filament.Box;
import com.google.android.filament.Camera;
import com.google.android.filament.Renderer;
import com.google.android.filament.Engine;
import com.google.android.filament.EntityManager;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.LightManager;
import com.google.android.filament.Material;
import com.google.android.filament.MaterialInstance;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.Scene;
import com.google.android.filament.Skybox;
import com.google.android.filament.SwapChain;
import com.google.android.filament.Texture;
import com.google.android.filament.TransformManager;
import com.google.android.filament.VertexBuffer;
import com.google.android.filament.View;
import com.google.android.filament.Viewport;
import com.google.android.filament.gltfio.FilamentAsset;
import com.kaio.cobra3d.model.entity.AnatomyHotspot;

import java.util.ArrayList;
import java.util.List;

public final class Anatomy3DRenderer {

    private static final String TAG = "Anatomy3DRenderer";

    private static final String UNIFORM_BASE_COLOR = "baseColor";
    private static final String UNIFORM_ROUGHNESS  = "roughness";
    private static final String UNIFORM_METALLIC   = "metallic";
    private static final String UNIFORM_EMISSIVE   = "emissive";
    private static final String UNIFORM_USE_SCALE_NORMAL = "useScaleNormal";

    private static final float[] COLOR_BODY_GOLDEN  = { 0.545f, 0.451f, 0.333f };
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
    private final List<Integer> modelEntities = new ArrayList<>();

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

    private ModelBuilder.RenderedMesh bodyMesh;
    private ModelBuilder.RenderedMesh leftEyeMesh;
    private ModelBuilder.RenderedMesh rightEyeMesh;
    private ModelBuilder.RenderedMesh tongueMesh;
    private UVSphereMesh.RenderedMesh sphereMesh;
    private UVSphereMesh.RenderedMesh hotspotMesh;

    private boolean bodyBuilt;
    private boolean hotspotsBuilt;
    private int currentLayerIndex;
    private boolean useGlb;

    private float modelScale = 1.0f;
    private float orbitTheta = (float) Math.PI / 2.0f;
    private float orbitPhi = 0.266f;
    private float orbitDistance = 25.0f;
    private float orbitTargetX, orbitTargetY, orbitTargetZ;

    private static final float[][] TOUR_KEYFRAMES = {
        {  0.0f,  3.0f, 15.0f,  0.0f,  0.0f,  0.0f },
        {  5.0f,  1.0f, 10.0f,  0.0f,  0.0f,  0.0f },
        { -3.0f,  2.0f, 12.0f,  0.0f,  0.5f,  0.0f },
        {  0.0f,  6.0f, 12.0f,  0.0f,  0.0f,  0.0f },
        {  2.5f,  0.5f,  5.0f,  2.5f,  0.3f,  0.0f },
        {  2.3f, -0.5f,  4.0f,  2.5f,  0.0f,  0.0f },
        {  2.0f,  1.0f,  4.0f,  2.5f,  0.5f,  0.0f },
        {  2.5f,  0.0f,  3.5f,  2.5f,  0.0f,  0.0f },
        {  3.0f,  0.2f,  3.0f,  2.8f,  0.1f,  0.0f },
        {  2.5f,  0.8f,  3.0f,  2.5f,  0.5f,  0.0f },
        {  2.3f,  0.8f,  2.0f,  2.5f,  0.5f,  0.0f },
        {  0.0f,  1.5f,  8.0f,  0.0f,  0.3f,  0.0f },
        {  0.0f,  1.0f,  7.0f,  0.0f,  0.3f,  0.0f },
        { -0.5f,  0.5f,  5.0f, -0.5f,  0.3f,  0.0f },
        { -0.8f,  1.0f,  6.0f, -0.8f,  0.5f,  0.0f },
        { -0.3f,  0.2f,  6.0f, -0.3f,  0.0f,  0.0f },
        { -0.6f,  0.5f,  5.0f, -0.6f,  0.3f,  0.0f },
        { -1.5f,  0.5f,  6.0f, -1.5f,  0.3f,  0.0f },
        { -1.0f,  0.2f,  5.5f, -1.0f,  0.0f,  0.0f },
        { -1.8f,  0.3f,  6.0f, -1.8f,  0.2f,  0.0f },
        {  0.0f,  2.0f,  9.0f,  0.0f,  0.5f,  0.0f },
        { -0.5f,  0.5f,  7.0f, -0.5f,  0.0f,  0.0f },
        { -2.8f,  0.3f,  4.0f, -2.5f,  0.0f,  0.0f },
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
        bodyInst.setParameter(UNIFORM_BASE_COLOR,
            COLOR_BODY_GOLDEN[0], COLOR_BODY_GOLDEN[1], COLOR_BODY_GOLDEN[2]);
        bodyInst.setParameter(UNIFORM_ROUGHNESS, 0.5f);
        bodyInst.setParameter(UNIFORM_METALLIC, 0.0f);
        bodyInst.setParameter(UNIFORM_USE_SCALE_NORMAL, 1.0f);

        FilamentAsset glbAsset = null;
        try {
            glbAsset = GlbLoader.load(engine, appContext.getAssets(), "models/cascavel.glb");
        } catch (Exception e) {
            Log.w(TAG, "GLB load failed, using procedural", e);
        }

        if (glbAsset != null) {
            useGlb = true;
            int[] entities = glbAsset.getEntities();
            for (int e : entities) {
                if (e == 0) continue;
                scene.addEntity(e);
                modelEntities.add(e);
                if (bodyEntity == 0) bodyEntity = e;
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
            bodyBuilt = true;
            return;
        }

        useGlb = false;
        MaterialInstance headInst = bodyMat.createInstance();
        headInst.setParameter(UNIFORM_BASE_COLOR,
            COLOR_HEAD[0], COLOR_HEAD[1], COLOR_HEAD[2]);
        headInst.setParameter(UNIFORM_ROUGHNESS, 0.4f);
        headInst.setParameter(UNIFORM_METALLIC, 0.0f);
        headInst.setParameter(UNIFORM_USE_SCALE_NORMAL, 1.0f);

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

        Material eyeMat = materialProvider.getEyeMaterial();
        if (eyeMat != null) {
            MaterialInstance eyeInst = eyeMat.createInstance();
            leftEyeMesh = ModelBuilder.buildEye(engine);
            if (leftEyeMesh != null) {
                leftEyeEntity = makeRenderable(
                    leftEyeMesh.getVertexBuffer(), leftEyeMesh.getIndexBuffer(),
                    leftEyeMesh.getIndexCount(), eyeInst, false, true, false);
                if (leftEyeEntity != 0) {
                    TransformManager tm = engine.getTransformManager();
                    int t = tm.create(leftEyeEntity);
                    tm.setTransform(t, matrixTranslation(
                        2.6f * modelScale, 0.25f * modelScale, -0.18f * modelScale));
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
                        2.6f * modelScale, 0.25f * modelScale, 0.18f * modelScale));
                    scene.addEntity(rightEyeEntity);
                }
            }
        }

        Material tongueMat = materialProvider.getTongueMaterial();
        if (tongueMat != null) {
            MaterialInstance tongueInst = tongueMat.createInstance();
            tongueMesh = ModelBuilder.buildTongue(engine);
            if (tongueMesh != null) {
                tongueEntity = makeRenderable(
                    tongueMesh.getVertexBuffer(), tongueMesh.getIndexBuffer(),
                    tongueMesh.getIndexCount(), tongueInst, false, false, true);
                if (tongueEntity != 0) {
                    TransformManager tm = engine.getTransformManager();
                    int t = tm.create(tongueEntity);
                    tm.setTransform(t, matrixTranslation(
                        2.8f * modelScale, 0.30f * modelScale, 0.0f));
                    scene.addEntity(tongueEntity);
                }
            }
        }

        bodyBuilt = true;
    }

    public void resetLayer(int layerIndex) {
        if (layerIndex == currentLayerIndex) return;
        currentLayerIndex = layerIndex;
        bodyBuilt = false;
        hotspotsBuilt = false;
        if (useGlb) {
            for (int e : modelEntities) {
                if (e != 0) scene.removeEntity(e);
            }
            modelEntities.clear();
            useGlb = false;
        }
        if (bodyEntity != 0) { scene.removeEntity(bodyEntity); bodyEntity = 0; }
        if (headEntity != 0) { scene.removeEntity(headEntity); headEntity = 0; }
        if (leftEyeEntity != 0) { scene.removeEntity(leftEyeEntity); leftEyeEntity = 0; }
        if (rightEyeEntity != 0) { scene.removeEntity(rightEyeEntity); rightEyeEntity = 0; }
        if (tongueEntity != 0) { scene.removeEntity(tongueEntity); tongueEntity = 0; }
        for (int e : hotspotEntities) {
            if (e != 0) scene.removeEntity(e);
        }
        hotspotEntities.clear();
        hotspotWorldPositions.clear();
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
        MaterialInstance hotInst = hotMat.createInstance();
        hotInst.setParameter(UNIFORM_BASE_COLOR,
            COLOR_HOTSPOT[0], COLOR_HOTSPOT[1], COLOR_HOTSPOT[2]);
        hotInst.setParameter(UNIFORM_EMISSIVE, 1.0f);

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

        for (int i = 0; i < hotspots.size(); i++) {
            AnatomyHotspot h = hotspots.get(i);
            int e = makeRenderable(
                hotspotMesh.getVertexBuffer(), hotspotMesh.getIndexBuffer(),
                hotspotMesh.getIndexCount(), hotInst, false, false, false);
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
        if (renderer.beginFrame(swapChain, frameTimeNanos)) {
            renderer.render(view);
            renderer.endFrame();
        }
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
            float oscX = 0.5f * (float) Math.sin(elapsed * 5.0f);
            tm.setTransform(t, matrixTranslation(
                (2.8f + oscX) * modelScale, 0.30f * modelScale, 0.0f));
        }
    }

    public void destroy() {
        RenderableManager rm = engine.getRenderableManager();
        TransformManager tm = engine.getTransformManager();

        for (int e : hotspotEntities) {
            if (e == 0) continue;
            scene.removeEntity(e);
            if (rm.hasComponent(e)) rm.destroy(e);
            tm.destroy(e);
            EntityManager.get().destroy(e);
        }
        hotspotEntities.clear();
        hotspotWorldPositions.clear();

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

        skybox = new Skybox.Builder()
            .color(new float[]{ 0.015f, 0.025f, 0.02f, 1.0f })
            .build(engine);
        scene.setSkybox(skybox);

        Renderer.ClearOptions clearOpts = new Renderer.ClearOptions();
        clearOpts.clearColor = new double[]{ 0.015, 0.025, 0.02, 1.0 };
        clearOpts.clear = true;
        renderer.setClearOptions(clearOpts);
        view.setShadowingEnabled(true);
        view.setAntiAliasing(View.AntiAliasing.FXAA);
        view.setDithering(View.Dithering.TEMPORAL);

        View.VsmShadowOptions vsm = new View.VsmShadowOptions();
        vsm.mipmapping = true;
        vsm.msaaSamples = 2;
        vsm.highPrecision = true;
        vsm.minVarianceScale = 0.5f;
        vsm.lightBleedReduction = 0.15f;
        view.setVsmShadowOptions(vsm);

        View.BloomOptions bloom = new View.BloomOptions();
        bloom.enabled = true;
        bloom.dirt = null;
        view.setBloomOptions(bloom);
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
            .color(1.0f, 0.92f, 0.75f)
            .intensity(120_000.0f)
            .direction(-0.4f, -1.0f, -0.2f)
            .castShadows(true)
            .shadowOptions(shadowOpts);
        sun.build(engine, lightEntity);
        scene.addEntity(lightEntity);

        fillLightEntity = EntityManager.get().create();
        LightManager.Builder fill = new LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.45f, 0.55f, 0.85f)
            .intensity(35_000.0f)
            .direction(0.7f, 0.3f, 0.6f)
            .castShadows(false);
        fill.build(engine, fillLightEntity);
        scene.addEntity(fillLightEntity);

        rimLightEntity = EntityManager.get().create();
        LightManager.Builder rim = new LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.8f, 0.85f, 1.0f)
            .intensity(25_000.0f)
            .direction(0.0f, 0.8f, -1.0f)
            .castShadows(false);
        rim.build(engine, rimLightEntity);
        scene.addEntity(rimLightEntity);
    }

    private void updateCameraProjection(int w, int h) {
        double aspect = (double) w / Math.max(1, h);
        camera.setProjection(50.0, aspect, 0.1, 200.0, Camera.Fov.VERTICAL);
        orbitTargetX = 0.0f;
        orbitTargetY = 0.0f;
        orbitTargetZ = 0.0f;
        orbitDistance = 25.0f;
        orbitTheta = (float) Math.PI / 2.0f;
        orbitPhi = 0.266f;
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
        engine.getRenderableManager().setMaterialInstanceAt(entity, 0, mat);
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

    private static float[] scaleMatrix(float s) {
        return new float[]{
            s, 0, 0, 0,
            0, s, 0, 0,
            0, 0, s, 0,
            0, 0, 0, 1
        };
    }
}
