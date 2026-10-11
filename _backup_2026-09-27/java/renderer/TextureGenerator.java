package com.kaio.cobra3d.view.renderer;

import com.google.android.filament.Engine;
import com.google.android.filament.Texture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class TextureGenerator {

    private static final int SIZE = 1024;

    public static Texture generateScaleDiffuse(Engine engine) {
        int pixelCount = SIZE * SIZE;
        byte[] pixels = new byte[pixelCount * 4];

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int i = (y * SIZE + x) * 4;
                float u = (float) x / SIZE;
                float v = (float) y / SIZE;

                float scale = scalePattern(u, v);

                // vale entre escamas escurece = profundidade sem linha de luz dura
                // agora sobre o novo tom teal-esmeralda do corpo (0x2a9d8f) com
                // contraste alto: vale escuro, escama clara (bem marcada), como o
                // Gemini pediu (antes o corpo ficava "chapado / acinzentado").
                float r = 0.06f + 0.40f * scale;
                float g = 0.28f + 0.48f * scale;
                float b = 0.20f + 0.44f * scale;

                // manchas dorsais: seguem o comprimento do corpo (U)
                // e esmaecem nas laterais (longe de V≈0.25)
                float darkPatch = diamondPattern(u * 4.0f, v);
                // contorno bege-claro bem definido ao redor da mancha escura
                float ringPatch = diamondRing(u * 4.0f, v);
                r += ringPatch * 0.30f;
                g += ringPatch * 0.27f;
                b += ringPatch * 0.20f;
                r -= darkPatch * 0.30f;
                g -= darkPatch * 0.26f;
                b -= darkPatch * 0.20f;

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
                .usage(Texture.Usage.UPLOADABLE | Texture.Usage.SAMPLEABLE)
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

                // fator 0.9: relevo de escama bem legível (0.42 liso → 0.7 ok, mas Gemini
                // v9.3 ainda viu corpo "liso/acetinado" — subir pra 0.9)
                float nx = -du * 0.9f;
                float ny = -dv * 0.9f;
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
                .usage(Texture.Usage.UPLOADABLE | Texture.Usage.SAMPLEABLE)
                .build(engine);

        texture.setImage(engine, 0,
                new Texture.PixelBufferDescriptor(buf,
                        Texture.Format.RGBA,
                        Texture.Type.UBYTE,
                        1));

        return texture;
    }

    /**
     * Cubemap de fundo com gradiente radial: halo suave no centro (onde a cobra
     * fica) iluminado por um tom esmeralda, escurecendo em direção às bordas.
     * Claro no centro da tela, sem cara de "buraco preto".
     */
    public static Texture generateBackgroundGradient(Engine engine) {
        int faceSize = 128;
        int facePixels = faceSize * faceSize;
        byte[] faceData = new byte[facePixels * 4];

        ByteBuffer allFaces = ByteBuffer.allocateDirect(facePixels * 4 * 6)
                .order(ByteOrder.nativeOrder());

        // Direções padrão das faces de um cubemap (OpenGL):
        // 0:+X, 1:-X, 2:+Y, 3:-Y, 4:+Z, 5:-Z
        for (int face = 0; face < 6; face++) {
            for (int y = 0; y < faceSize; y++) {
                for (int x = 0; x < faceSize; x++) {
                    float u = ((float) x + 0.5f) / faceSize * 2.0f - 1.0f;
                    float v = ((float) y + 0.5f) / faceSize * 2.0f - 1.0f;

                    float dx, dy, dz;
                    switch (face) {
                        case 0: dx = 1.0f;  dy = -v;    dz = -u;    break;
                        case 1: dx = -1.0f; dy = -v;    dz = u;     break;
                        case 2: dx = u;     dy = 1.0f;  dz = -v;    break;
                        case 3: dx = u;     dy = -1.0f; dz = v;     break;
                        case 4: dx = u;     dy = -v;    dz = 1.0f;  break;
                        default: dx = -u;   dy = -v;    dz = -1.0f; break;
                    }

                    float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    dx /= len; dy /= len; dz /= len;

                    // Halo central dirigido para a câmera (-Z na cena)
                    float viewDot = (-dx * 0.80f + dy * 0.35f + dz * (-0.80f));
                    if (viewDot < 0.0f) viewDot = Math.max(0.0f, viewDot * 2.2f);
                    float halo = viewDot * viewDot;

                    // Fundo premium: esmeralda profunda com halo jade no centro onde
                    // a cobra fica, vinheta escura nas bordas (contraste "stage").
                    float baseR = 0.006f;
                    float baseG = 0.014f;
                    float baseB = 0.010f;

                    // glow jade (verde) + toque dourado sutil perto do centro
                    float r = baseR + halo * 0.028f + halo * halo * 0.012f;
                    float g = baseG + halo * 0.070f;
                    float b = baseB + halo * 0.045f;

                    // vinheta: escurece conforme a face se afasta do centro visual
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    float vig = 1.0f - 0.55f * Math.min(1.0f, dist * dist);
                    r *= vig; g *= vig; b *= vig;

                    if (r > 1.0f) r = 1.0f;
                    if (g > 1.0f) g = 1.0f;
                    if (b > 1.0f) b = 1.0f;

                    int i = (y * faceSize + x) * 4;
                    faceData[i]     = (byte) (r * 255);
                    faceData[i + 1] = (byte) (g * 255);
                    faceData[i + 2] = (byte) (b * 255);
                    faceData[i + 3] = (byte) 255;
                }
            }
            allFaces.put(faceData);
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

// Escala hexagonal (brick) em densidade balanceada: grande o bastante
    // para as escamas aparecerem níticas num tubo fino (densidade 120x60
    // virava um "conduíte cinza"). 24x12 deixa cada escama em tamanho
    // legível — o padrão hexagonal/losango fica visível sem virar vinco.
    private static final int HEX_COLS = 16;
    private static final int HEX_ROWS = 9;

    private static float scalePattern(float u, float v) {
        float cu = u * HEX_COLS;
        float cv = v * HEX_ROWS;
        int row = (int) Math.floor(cv);
        float rowOffset = (row % 2) == 0 ? 0.0f : 0.5f;
        int col = (int) Math.floor(cu + rowOffset);
        float cx = cu + rowOffset - col;
        float cy = cv - row;
        // deslocamento do centro mais forte pra quebrar a uniformidade (look orgânico)
        float jx = (hash(row, col) - 0.5f) * 0.32f;
        float jy = (hash(row + 1000, col + 777) - 0.5f) * 0.32f;
        float dx = cx - 0.5f - jx;
        float dy = cy - 0.5f - jy;
        // métrica hexagonal imbricada: escamas se sobrepõem (fator baixo) e
        // preenchem a célula sem deixar "cruzes" de vale entre elas.
        // *2.9: o vale (borda da célula) cai bem abaixo de 0.5, criando a
        // separação escura que faz cada escama ler como uma unidade (Gemini
        // v9.3/v9.4: padrão virava "ruído" porque a célula nunca escurecia)
        float shape = Math.max(0.0f, 1.0f - (Math.abs(dx) * 0.55f + Math.abs(dy)) * 2.9f);
        // Endurecer as bordas (Gemini v9.9): shape linear tinha falloff suave,
        // então a célula nunca chegava num "sulco" escuro — corpo ficava
        // esfumado/ondulado. smoothstep + threshold corta o vale entre células
        // pra zero, transformando relevo em unidades hex losangulares legíveis.
        float soft = shape * shape * (3.0f - 2.0f * shape);
        float crisped = Math.max(0.0f, (soft - 0.55f) * 2.8f);
        // quilha central sutil (realce vertical da escama)
        float keel = Math.max(0.0f, 1.0f - Math.abs(dy) * 3.5f)
                * Math.max(0.0f, 1.0f - Math.abs(dx) * 0.5f);
        // variação de brilho por célula mais forte (cada escama com seu próprio tom)
        float shade = 0.68f + 0.32f * hash(row, col + 42);
        return Math.max(0.0f, crisped * crisped * shade + keel * 0.22f);
    }

    private static float hash(int a, int b) {
        float s = (float) Math.sin(a * 12.9898 + b * 78.233) * 43758.5453f;
        return s - (float) Math.floor(s);
    }

    private static float diamondPattern(float u, float v) {
        // u = comprimento do corpo, v = volta da circunferência.
        // O dorso da cascavel fica em V≈0.25 no UV do cilindro, por isso a
        // faixa é horizontal: manchas modulares descem pelo corpo e
        // esmaecem nas laterais/barriga.
        float cu = u - (float) Math.floor(u) - 0.5f;
        float cv = v - 0.25f;
        float diamondDist = Math.abs(cu) + Math.abs(cv) * 2.0f;
        // borda mais nítida: smoothstep no lugar do linear, contraste maior
        float t = Math.max(0.0f, 1.0f - diamondDist * 3.6f);
        float d = t * t * (3.0f - 2.0f * t);
        float flank = Math.max(0.0f, 1.0f - Math.abs(cv) * 3.5f);
        return d * flank;
    }

    // Anel bege-claro que contorna o losango escuro (borda de alto contraste
    // que o Gemini pediu). Anel mais estreito = contorno mais nítido.
    private static float diamondRing(float u, float v) {
        float cu = u - (float) Math.floor(u) - 0.5f;
        float cv = v - 0.25f;
        float diamondDist = Math.abs(cu) + Math.abs(cv) * 2.0f;
        // losango mais largo (fator menor) = região do contorno
        float tOuter = Math.max(0.0f, 1.0f - diamondDist * 3.1f);
        float outer = tOuter * tOuter * (3.0f - 2.0f * tOuter);
        float tInner = Math.max(0.0f, 1.0f - diamondDist * 3.6f);
        float inner = tInner * tInner * (3.0f - 2.0f * tInner);
        float flank = Math.max(0.0f, 1.0f - Math.abs(cv) * 3.5f);
        return (outer - inner) * flank;
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
