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

    private static final String MAT_BODY_NAME = "serpenteando_body.filamat";
    private static final String MAT_HOTSPOT_NAME = "serpenteando_hotspot.filamat";
    private static final String MAT_ORGAN_NAME = "serpenteando_organ.filamat";
    private static final String MAT_EYE_NAME = "serpenteando_eye.filamat";
    private static final String MAT_TONGUE_NAME = "serpenteando_tongue.filamat";

    private final Context appContext;
    private final Engine engine;

    private Material bodyMaterial;
    private Material hotspotMaterial;
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
        return new Material.Builder()
                .payload(buffer, buffer.remaining())
                .build(engine);
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
                .uniformParameter(MaterialBuilder.UniformType.FLOAT3, "baseColor")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "roughness")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "metallic")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "useScaleNormal")
                .material("void material(inout MaterialInputs material) {\n" +
                        "    prepareMaterial(material);\n" +
                        "    material.baseColor.rgb = materialParams.baseColor;\n" +
                        "    material.roughness = materialParams.roughness;\n" +
                        "    material.metallic = materialParams.metallic;\n" +
                        "    material.ambientOcclusion = 1.0;\n" +
                        "    if (materialParams.useScaleNormal > 0.5) {\n" +
                        "        float du = 0.3 * sin(getUV0().x * 200.0 + getUV0().y * 100.0);\n" +
                        "        float dv = 0.3 * cos(getUV0().x * 150.0 + getUV0().y * 80.0);\n" +
                        "        material.normal = normalize(vec3(du, dv, 1.0));\n" +
                        "    }\n" +
                        "}\n");
        return builder.build(null).getBuffer().array();
    }

    private byte[] buildHotspotPackage() throws Exception {
        MaterialBuilder builder = new MaterialBuilder()
                .name("SerpenteandoHotspot")
                .shading(Shading.LIT)
                .blending(BlendingMode.OPAQUE)
                .uniformParameter(MaterialBuilder.UniformType.FLOAT3, "baseColor")
                .uniformParameter(MaterialBuilder.UniformType.FLOAT, "emissive")
                .material("void material(inout MaterialInputs material) {\n" +
                        "    prepareMaterial(material);\n" +
                        "    material.baseColor.rgb = materialParams.baseColor;\n" +
                        "    material.emissive = vec4(materialParams.baseColor * materialParams.emissive, 1.0);\n" +
                        "    material.roughness = 0.2;\n" +
                        "    material.metallic = 0.8;\n" +
                        "}\n");
        return builder.build(null).getBuffer().array();
    }

    private byte[] buildOrganPackage() throws Exception {
        MaterialBuilder builder = new MaterialBuilder()
                .name("SerpenteandoOrgan")
                .shading(Shading.LIT)
                .blending(BlendingMode.OPAQUE)
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
        MaterialBuilder builder = new MaterialBuilder()
                .name("SerpenteandoEye")
                .shading(Shading.LIT)
                .blending(BlendingMode.OPAQUE)
                .material("void material(inout MaterialInputs material) {\n" +
                        "    prepareMaterial(material);\n" +
                        "    material.baseColor.rgb = vec3(0.05, 0.05, 0.05);\n" +
                        "    material.roughness = 0.1;\n" +
                        "    material.metallic = 0.9;\n" +
                        "    material.ambientOcclusion = 0.8;\n" +
                        "}\n");
        return builder.build(null).getBuffer().array();
    }

    private byte[] buildTonguePackage() throws Exception {
        MaterialBuilder builder = new MaterialBuilder()
                .name("SerpenteandoTongue")
                .shading(Shading.LIT)
                .blending(BlendingMode.OPAQUE)
                .material("void material(inout MaterialInputs material) {\n" +
                        "    prepareMaterial(material);\n" +
                        "    material.baseColor.rgb = vec3(0.85, 0.15, 0.15);\n" +
                        "    material.roughness = 0.6;\n" +
                        "    material.metallic = 0.0;\n" +
                        "}\n");
        return builder.build(null).getBuffer().array();
    }
}
