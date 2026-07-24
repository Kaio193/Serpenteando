package com.kaio.cobra3d.view.renderer;

/**
 * Helpers numéricos para construção procedural de geometria.
 * Mantém o renderer sem dependências externas além do Filament.
 */
public final class RenderMath {

    private RenderMath() {}

    /** Escreve 3 floats (x, y, z) no array de saída a partir do índice. */
    public static void writeVec3(float[] out, int index, float x, float y, float z) {
        out[index] = x;
        out[index + 1] = y;
        out[index + 2] = z;
    }

    /** Escreve 2 floats (u, v) no array de saída a partir do índice. */
    public static void writeVec2(float[] out, int index, float u, float v) {
        out[index] = u;
        out[index + 1] = v;
    }
}
