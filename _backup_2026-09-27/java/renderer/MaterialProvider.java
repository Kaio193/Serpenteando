package com.kaio.cobra3d.view.renderer;

import android.content.Context;
import android.util.Log;

import com.google.android.filament.Engine;
import com.google.android.filament.Material;
import com.google.android.filament.filamat.MaterialBuilder;
import com.google.android.filament.filamat.MaterialBuilder.BlendingMode;
import com.google.android.filament.filamat.MaterialBuilder.Shading;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class MaterialProvider {

    private static final String TAG = "MaterialProvider";

    private static final String MAT_BODY_NAME = "serpenteando_body_v16.filamat";
    private static final String MAT_HOTSPOT_NAME = "serpenteando_hotspot.filamat";
    private static final String MAT_PARTICLE_NAME = "serpenteando_particle.filamat";
    private static final String MAT_ORGAN_NAME = "serpenteando_organ.filamat";
    private static final String MAT_EYE_NAME = "serpenteando_eye_v2.filamat";
    private static final String MAT_TONGUE_NAME = "serpenteando_tongue_v2.filamat";

    private final Context appContext;
    private final Engine engine;

    private Material bodyMaterial;
    private Material hotspotMaterial;
    private Material particleMaterial;
    private Material organMaterial;
    private Material eyeMaterial;
    private Material tongueMaterial;

    public MaterialProvider(Context context, Engine engine) {
        this.appContext = context.getApplicationContext();
        this.engine = engine;
    }

    public Material getBodyMaterial() {
        if (bodyMaterial == null) {
            bodyMaterial = loadOrBuild(MAT_BODY_NAME, this::buildBodyPackage);
        }
        return bodyMaterial;
    }

    public Material getHotspotMaterial() {
        if (hotspotMaterial == null) {
            hotspotMaterial = loadOrBuild(MAT_HOTSPOT_NAME, this::buildHotspotPackage);
        }
        return hotspotMaterial;
    }

    public Material getParticleMaterial() {
        if (particleMaterial == null) {
            particleMaterial = loadOrBuild(MAT_PARTICLE_NAME, this::buildParticlePackage);
        }
        return particleMaterial;
    }

    public Material getOrganMaterial() {
        if (organMaterial == null) {
            organMaterial = loadOrBuild(MAT_ORGAN_NAME, this::buildOrganPackage);
        }
        return organMaterial;
    }

    public Material getEyeMaterial() {
        if (eyeMaterial == null) {
            eyeMaterial = loadOrBuild(MAT_EYE_NAME, this::buildEyePackage);
        }
        return eyeMaterial;
    }

    public Material getTongueMaterial() {
        if (tongueMaterial == null) {
            tongueMaterial = loadOrBuild(MAT_TONGUE_NAME, this::buildTonguePackage);
        }
        return tongueMaterial;
    }

    public void destroy() {
        if (bodyMaterial != null) { engine.destroyMaterial(bodyMaterial); bodyMaterial = null; }
        if (hotspotMaterial != null) { engine.destroyMaterial(hotspotMaterial); hotspotMaterial = null; }
        if (particleMaterial != null) { engine.destroyMaterial(particleMaterial); particleMaterial = null; }
        if (organMaterial != null) { engine.destroyMaterial(organMaterial); organMaterial = null; }
        if (eyeMaterial != null) { engine.destroyMaterial(eyeMaterial); eyeMaterial = null; }
        if (tongueMaterial != null) { engine.destroyMaterial(tongueMaterial); tongueMaterial = null; }
    }

    private interface PackageSupplier {
        byte[] build() throws Exception;
    }

    private Material loadOrBuild(String filename, PackageSupplier supplier) {
        File cached = new File(appContext.getFilesDir(), filename);
        ByteBuffer buffer = null;
        if (cached.exists() && cached.length() > 0) {
            try {
                buffer = readFile(cached);
            } catch (IOException e) {
                Log.w(TAG, "Failed to read cache " + filename + ", recompiling.", e);
            }
        }
        if (buffer == null) {
            try {
                byte[] pkg = supplier.build();
                writeFile(cached, pkg);
                buffer = ByteBuffer.wrap(pkg);
            } catch (Exception e) {
                Log.e(TAG, "Failed to compile material " + filename, e);
                return null;
            }
        }

        // Lightweight sanity: must be at least 32 bytes
        if (buffer == null || buffer.remaining() < 32) {
            Log.e(TAG, "Material package " + filename + " is too small or empty, skipping.");
            if (cached.exists()) cached.delete();
            return null;
        }

        // Try to build material; if it fails, attempt to rebuild from supplier and fallback safely.
        try {
            return new Material.Builder()
                    .payload(buffer, buffer.remaining())
                    .build(engine);
        } catch (Throwable t) {
            // Native or runtime failure while parsing material package. Avoid abort by deleting cache and retrying once.
            Log.e(TAG, "Material build failed for " + filename + ": " + t.getMessage(), t);
            try {
                if (cached.exists()) cached.delete();
                byte[] pkg = supplier.build();
                writeFile(cached, pkg);
                ByteBuffer retryBuf = ByteBuffer.wrap(pkg);
                try {
                    return new Material.Builder().payload(retryBuf, retryBuf.remaining()).build(engine);
                } catch (Throwable t2) {
                    Log.e(TAG, "Retry material build also failed for " + filename + ": " + t2.getMessage(), t2);
                    // Give up and return null: callers must handle null and avoid creating MaterialInstances for this material.
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to rebuild material package for " + filename, e);
                return null;
            }
        }
    }

    private static ByteBuffer readFile(File f) throws IOException {
        byte[] data = new byte[(int) f.length()];
        try (java.io.FileInputStream in = new java.io.FileInputStream(f)) {
            int read = 0;
            while (read < data.length) {
                int n = in.read(data, read, data.length - read);
                if (n < 0) break;
                read += n;
            }
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length);
        bb.put(data);
        bb.flip();
        return bb;
    }

    private static void writeFile(File f, byte[] data) throws IOException {
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(data);
        }
    }

    private byte[] buildBodyPackage() throws Exception {
        MaterialBuilder.init();
        MaterialBuilder builder = new MaterialBuilder()
                .name("SerpenteandoBody")
                .shading(Shading.LIT)
                .blending(BlendingMode.OPAQUE)
                .platform(MaterialBuilder.Platform.MOBILE)
                .require(MaterialBuilder.VertexAttribute.UV0)
                .uniformParameter(MaterialBuilder.UniformType.FLOAT3, "baseColor")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "roughness")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "metallic")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "useScaleNormal")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "useTexture")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "clearCoat")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "clearCoatRoughness")
                .samplerParameter(MaterialBuilder.SamplerType.SAMPLER_2D,
                        MaterialBuilder.SamplerFormat.FLOAT,
                        MaterialBuilder.ParameterPrecision.DEFAULT, "scaleTexture")
                .samplerParameter(MaterialBuilder.SamplerType.SAMPLER_2D,
                        MaterialBuilder.SamplerFormat.FLOAT,
                        MaterialBuilder.ParameterPrecision.DEFAULT, "scaleNormalTexture")
                .material("void material(inout MaterialInputs material) {\n" +
                        "    prepareMaterial(material);\n" +
                        "    vec4 tex = texture(materialParams_scaleTexture, getUV0());\n" +
                        // difusa jade viva: a textura so modula uma variacao suave de
                        // luminosidade (0.60~1.1) — antes o tex.rgb multiplicava direto
                        // e as escamas douradas escondiam o jade (Gemini: corpo ficava
                        // "cinza/esbranquiçado" mesmo com baseColor verde)
                        "    float shade = 0.60 + 0.50 * dot(tex.rgb, vec3(0.299, 0.587, 0.114));\n" +
                        "    material.baseColor.rgb = materialParams.baseColor * shade;\n" +
                        "    vec3 n = texture(materialParams_scaleNormalTexture, getUV0()).rgb * 2.0 - 1.0;\n" +
                        // [Gemini v6] relevo mais forte: n.xy*5.0 em vez de 3.0 — o
                        // corpo estava liso ("tubo emborrachado"), faltava profundidade
                        // nas cavidades das escamas
                        "    material.normal = normalize(vec3(n.xy * 5.0, n.z));\n" +
                        // [Gemini v6] roughness modulado pelas cristas: a mesma textura
                        // de escamas define onde dar brilho — nas cristas (lum alta) o
                        // roughness cai, gerando os pontos reluzentes de pele de serpente
                        "    float ridge = smoothstep(0.55, 0.9, dot(tex.rgb, vec3(0.299, 0.587, 0.114)));\n" +
                        "    material.roughness = mix(materialParams.roughness, 0.10, ridge * 0.55);\n" +
                        "    material.metallic = materialParams.metallic;\n" +
                        "    material.ambientOcclusion = 1.0;\n" +
                        "    // [3D] clearcoat = verniz acetinado por cima das escamas (fosco+brilho), igual o Snake do three.js\n" +
                        "    material.clearCoat = materialParams.clearCoat;\n" +
                        "    material.clearCoatRoughness = materialParams.clearCoatRoughness;\n" +
                        "    // Fresnel rim (u_fresnelPower 3.5 / u_fresnelIntensity 0.3): as bordas\n" +
                        "    // do corpo pegam um brilho quente no ângulo rasante — dá o \"brilho\n" +
                        "    // da escama\" que o professor vai perguntar na apresentação\n" +
                        "    vec3 vNormal = normalize(getWorldNormalVector());\n" +
                        "    vec3 vView = normalize(getWorldViewVector());\n" +
                        "    float rim = pow(1.0 - max(dot(vNormal, vView), 0.0), 3.5);\n" +
                        // emissivo jade forte: garante que o corpo leia VERDE mesmo sob
                        // luzes fortes / highlights que antes lavavam pra tons claros
                        "    material.emissive = vec4(materialParams.baseColor.rgb * 1.0 + vec3(1.0) * rim * 0.2, 1.0);\n" +
                        "}\n");
        return builder.build(null).getBuffer().array();
    }

    private byte[] buildHotspotPackage() throws Exception {
        MaterialBuilder.init();
        MaterialBuilder builder = new MaterialBuilder()
                  .name("SerpenteandoHotspot")
                  .shading(Shading.LIT)
                  .blending(BlendingMode.TRANSPARENT)
                  .platform(MaterialBuilder.Platform.MOBILE)
                  .uniformParameter(MaterialBuilder.UniformType.FLOAT3, "baseColor")
                  .uniformParameter(MaterialBuilder.UniformType.FLOAT, "emissive")
                  .material("void material(inout MaterialInputs material) {\n" +
                          "    prepareMaterial(material);\n" +
                          "    material.baseColor.rgb = materialParams.baseColor;\n" +
                          "    material.baseColor.a = 0.85;\n" +
                          "    material.emissive = vec4(materialParams.baseColor * materialParams.emissive, 1.0);\n" +
                          "    material.roughness = 0.35;\n" +
                          "    material.metallic = 0.0;\n" +
                          "}\n");
        return builder.build(null).getBuffer().array();
    }

    // Partículas neon: UNLIT + emissive forte, blending para dar brilho
    // sempre visível (não depende de iluminação da cena)
    private byte[] buildParticlePackage() throws Exception {
        MaterialBuilder.init();
        MaterialBuilder builder = new MaterialBuilder()
                  .name("SerpenteandoParticle")
                  .shading(Shading.UNLIT)
                  .blending(BlendingMode.TRANSPARENT)
                  .doubleSided(true)
                  .platform(MaterialBuilder.Platform.MOBILE)
                  .uniformParameter(MaterialBuilder.UniformType.FLOAT3, "baseColor")
                  .uniformParameter(MaterialBuilder.UniformType.FLOAT, "emissive")
                  .material("void material(inout MaterialInputs material) {\n" +
                          "    prepareMaterial(material);\n" +
                          "    material.baseColor.rgb = materialParams.baseColor;\n" +
                          "    material.baseColor.a = 0.55;\n" +
                          "    material.emissive = vec4(materialParams.baseColor * materialParams.emissive, 1.0);\n" +
                          "}\n");
        return builder.build(null).getBuffer().array();
    }

    private byte[] buildOrganPackage() throws Exception {
        MaterialBuilder.init();
        MaterialBuilder builder = new MaterialBuilder()
                .name("SerpenteandoOrgan")
                .shading(Shading.LIT)
                .blending(BlendingMode.OPAQUE)
                .platform(MaterialBuilder.Platform.MOBILE)
                .uniformParameter(MaterialBuilder.UniformType.FLOAT3, "baseColor")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "roughness")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "metallic")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "opacity")
                .material("void material(inout MaterialInputs material) {\n" +
                        "    prepareMaterial(material);\n" +
                        "    material.baseColor.rgb = materialParams.baseColor;\n" +
                        "    material.baseColor.a = materialParams.opacity;\n" +
                        "    material.roughness = materialParams.roughness;\n" +
                        "    material.metallic = materialParams.metallic;\n" +
                        "}\n");
        return builder.build(null).getBuffer().array();
    }

    private byte[] buildEyePackage() throws Exception {
        MaterialBuilder.init();
        MaterialBuilder builder = new MaterialBuilder()
                .name("SerpenteandoEye")
                .shading(Shading.LIT)
                .blending(BlendingMode.OPAQUE)
                .platform(MaterialBuilder.Platform.MOBILE)
                .uniformParameter(MaterialBuilder.UniformType.FLOAT3, "baseColor")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "emissive")
                .material("void material(inout MaterialInputs material) {\n" +
                        "    prepareMaterial(material);\n" +
                        // ballFrag.glsl portado: olho = bolt dourado emissivo com fresnel
                        // (acabou o "ponto preto" no olho, agora ele brilha de leve
                        // e vira destaque quando o bloom pega o emissivo)
                        "    vec3 vNormal = normalize(getWorldNormalVector());\n" +
                        "    vec3 vView = normalize(getWorldViewVector());\n" +
                        "    float fresnel = pow(1.0 - max(dot(vNormal, vView), 0.0), 2.5);\n" +
                        "    material.baseColor.rgb = materialParams.baseColor * 0.4;\n" +
                        "    material.roughness = 0.2;\n" +
                        "    material.metallic = 0.0;\n" +
                        "    material.ambientOcclusion = 0.8;\n" +
                        "    material.emissive = vec4(materialParams.baseColor * (materialParams.emissive + fresnel * 0.6), 1.0);\n" +
                        "}\n");
        return builder.build(null).getBuffer().array();
    }

    private byte[] buildTonguePackage() throws Exception {
        MaterialBuilder.init();
        MaterialBuilder builder = new MaterialBuilder()
                .name("SerpenteandoTongue")
                .shading(Shading.LIT)
                .blending(BlendingMode.OPAQUE)
                .platform(MaterialBuilder.Platform.MOBILE)
                .uniformParameter(MaterialBuilder.UniformType.FLOAT3, "baseColor")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "emissive")
                .material("void material(inout MaterialInputs material) {\n" +
                        "    prepareMaterial(material);\n" +
                        // língua bífida avermelhada com leve emissivo: sem isso ela
                        // ficava "invisível" na ponta do focinho (Gemini v9)
                        "    material.baseColor.rgb = materialParams.baseColor;\n" +
                        "    material.roughness = 0.5;\n" +
                        "    material.metallic = 0.0;\n" +
                        "    material.emissive = vec4(materialParams.baseColor * materialParams.emissive, 1.0);\n" +
                        "}\n");
        return builder.build(null).getBuffer().array();
    }
}
