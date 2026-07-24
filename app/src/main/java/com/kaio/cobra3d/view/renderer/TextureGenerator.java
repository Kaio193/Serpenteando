package com.kaio.cobra3d.view.renderer;

import com.google.android.filament.Engine;
import com.google.android.filament.Texture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class TextureGenerator {

    private static final int SIZE = 512;

    public static Texture generateScaleDiffuse(Engine engine) {
        int pixelCount = SIZE * SIZE;
        byte[] pixels = new byte[pixelCount * 4];

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int i = (y * SIZE + x) * 4;
                float u = (float) x / SIZE;
                float v = (float) y / SIZE;

                float scalePattern = scalePattern(u, v);

                float r = 0.545f + 0.12f * scalePattern;
                float g = 0.451f + 0.10f * scalePattern;
                float b = 0.333f + 0.08f * scalePattern;

                float darkPatch = diamondPattern(u * 4.0f, v * 2.0f);
                r -= darkPatch * 0.15f;
                g -= darkPatch * 0.12f;
                b -= darkPatch * 0.08f;

                float edge = edgePattern(u * SIZE, v * SIZE);
                r += edge * 0.08f;
                g += edge * 0.06f;
                b += edge * 0.04f;

                if (r > 1.0f) r = 1.0f;
                if (g > 1.0f) g = 1.0f;
                if (b > 1.0f) b = 1.0f;
                if (r < 0.0f) r = 0.0f;
                if (g < 0.0f) g = 0.0f;
                if (b < 0.0f) b = 0.0f;

                pixels[i]     = (byte) (r * 255);
                pixels[i + 1] = (byte) (g * 255);
                pixels[i + 2] = (byte) (b * 255);
                pixels[i + 3] = (byte) 255;
            }
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(pixels.length)
                .order(ByteOrder.nativeOrder());
        buf.put(pixels);
        buf.flip();

        Texture texture = new Texture.Builder()
                .width(SIZE)
                .height(SIZE)
                .levels(1)
                .format(Texture.InternalFormat.RGBA8)
                .sampler(Texture.Sampler.SAMPLER_2D)
                .usage(Texture.Usage.SAMPLEABLE)
                .build(engine);

        texture.setImage(engine, 0,
                new Texture.PixelBufferDescriptor(buf,
                        Texture.Format.RGBA,
                        Texture.Type.UBYTE,
                        1));

        return texture;
    }

    public static Texture generateScaleNormal(Engine engine) {
        int pixelCount = SIZE * SIZE;
        byte[] pixels = new byte[pixelCount * 4];

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int i = (y * SIZE + x) * 4;
                float u = (float) x / SIZE;
                float v = (float) y / SIZE;

                float du = (scalePattern(u + 0.01f, v) - scalePattern(u - 0.01f, v)) * 2.0f;
                float dv = (scalePattern(u, v + 0.01f) - scalePattern(u, v - 0.01f)) * 2.0f;

                float nx = -du * 0.5f;
                float ny = -dv * 0.5f;
                float nz = (float) Math.sqrt(Math.max(0.0f, 1.0f - nx * nx - ny * ny));

                pixels[i]     = (byte) ((nx * 0.5f + 0.5f) * 255);
                pixels[i + 1] = (byte) ((ny * 0.5f + 0.5f) * 255);
                pixels[i + 2] = (byte) ((nz * 0.5f + 0.5f) * 255);
                pixels[i + 3] = (byte) 255;
            }
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(pixels.length)
                .order(ByteOrder.nativeOrder());
        buf.put(pixels);
        buf.flip();

        Texture texture = new Texture.Builder()
                .width(SIZE)
                .height(SIZE)
                .levels(1)
                .format(Texture.InternalFormat.RGBA8)
                .sampler(Texture.Sampler.SAMPLER_2D)
                .usage(Texture.Usage.SAMPLEABLE)
                .build(engine);

        texture.setImage(engine, 0,
                new Texture.PixelBufferDescriptor(buf,
                        Texture.Format.RGBA,
                        Texture.Type.UBYTE,
                        1));

        return texture;
    }

    private static float scalePattern(float u, float v) {
        float scaleU = u * 32.0f;
        float scaleV = v * 16.0f;
        float rowOffset = ((int) Math.floor(scaleV)) % 2 == 0 ? 0.0f : 0.5f;
        float cellU = scaleU + rowOffset - (float) Math.floor(scaleU + rowOffset);
        float cellV = scaleV - (float) Math.floor(scaleV);
        float cx = cellU - 0.5f;
        float cy = cellV - 0.5f;
        float dist = (float) Math.sqrt(cx * cx + cy * cy);
        return 1.0f - Math.min(1.0f, dist * 2.5f);
    }

    private static float diamondPattern(float u, float v) {
        float cx = u - (float) Math.floor(u) - 0.5f;
        float cy = v - (float) Math.floor(v) - 0.5f;
        float diamondDist = Math.abs(cx) + Math.abs(cy);
        return Math.max(0.0f, 1.0f - diamondDist * 3.0f);
    }

    private static float edgePattern(float x, float y) {
        float cx = x % 32.0f;
        float cy = y % 16.0f;
        float rowOff = (((int) (y / 16.0f)) % 2 == 0) ? 0.0f : 16.0f;
        cx = (cx + rowOff) % 32.0f;
        float dx = Math.abs(cx - 16.0f);
        float dy = Math.abs(cy - 8.0f);
        float edge = Math.max(0.0f, 1.0f - (dx * dx + dy * dy) / 100.0f);
        return edge > 0.5f ? 1.0f : 0.0f;
    }
}
