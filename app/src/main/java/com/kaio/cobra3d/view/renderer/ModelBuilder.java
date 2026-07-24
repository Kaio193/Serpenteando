package com.kaio.cobra3d.view.renderer;

import com.google.android.filament.Engine;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.VertexBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ModelBuilder {

    private static final int FLOATS_PER_VERTEX = 8;
    private static final int BODY_RINGS = 120;
    private static final int BODY_SEGMENTS = 28;
    private static final int HEAD_RINGS = 14;
    private static final int HEAD_SEGMENTS = 28;
    private static final int RATTLE_SEGMENTS = 6;
    private static final int RATTLE_RINGS_PER_SEG = 4;
    private static final int EYE_SEGMENTS = 12;
    private static final int EYE_RINGS = 8;

    public static final class RenderedMesh {
        private final VertexBuffer vb;
        private final IndexBuffer ib;
        private final int indexCount;

        public RenderedMesh(VertexBuffer vb, IndexBuffer ib, int indexCount) {
            this.vb = vb;
            this.ib = ib;
            this.indexCount = indexCount;
        }

        public VertexBuffer getVertexBuffer() { return vb; }
        public IndexBuffer getIndexBuffer() { return ib; }
        public int getIndexCount() { return indexCount; }

        public void destroy(Engine engine) {
            engine.destroyIndexBuffer(ib);
            engine.destroyVertexBuffer(vb);
        }
    }

    public static RenderedMesh buildRattlesnake(Engine engine) {
        int bodyRingCount = BODY_RINGS + HEAD_RINGS + 1;
        int rattleRingCount = RATTLE_SEGMENTS * RATTLE_RINGS_PER_SEG;
        int totalRings = bodyRingCount + rattleRingCount;
        int vertexCount = totalRings * (BODY_SEGMENTS + 1);

        float[] vertices = new float[vertexCount * FLOATS_PER_VERTEX];
        float[] ringCenters = new float[(totalRings) * 4];
        int v = 0;

        for (int ring = 0; ring <= BODY_RINGS + HEAD_RINGS; ring++) {
            float t = (float) ring / (BODY_RINGS + HEAD_RINGS);
            float bodyT = Math.min(t * (BODY_RINGS + HEAD_RINGS) / BODY_RINGS, 1.0f);

            float headFade = Math.max(0.0f, (t - 0.85f) / 0.15f);
            float headFactor = headFade * headFade * (3.0f - 2.0f * headFade);
            float headScale = 1.0f + headFactor * 0.6f;
            float headWidth = 1.0f + headFactor * 0.8f;

            float x = -2.5f + 5.0f * t;
            float y = (float) (0.55 * Math.sin(1.8 * Math.PI * bodyT) + 0.25 * Math.sin(3.2 * Math.PI * bodyT + 0.3));
            float z = (float) (0.35 * Math.sin(2.1 * Math.PI * bodyT + 0.7));

            float midT = (float) (1.0 - Math.abs(t - 0.5) * 2.0);
            float radius = 0.20f * (1.0f - 0.55f * (1.0f - midT * midT));

            float tipT = Math.min(t * 3.0f, 1.0f);
            float snoutFade = (float) Math.pow(t, 4.0f);
            radius *= (1.0f - snoutFade * 0.4f);

            ringCenters[ring * 4] = x;
            ringCenters[ring * 4 + 1] = y;
            ringCenters[ring * 4 + 2] = z;
            ringCenters[ring * 4 + 3] = radius;

            for (int seg = 0; seg <= BODY_SEGMENTS; seg++) {
                float angle = (float) (seg * 2.0 * Math.PI / BODY_SEGMENTS);
                float cosA = (float) Math.cos(angle);
                float sinA = (float) Math.sin(angle);

                float r = radius * (1.0f - headFactor * 0.2f);
                float w = radius * headWidth;
                float hw = (float) (w * (0.7 + 0.3 * Math.abs(cosA)));

                vertices[v++] = x + hw * cosA;
                vertices[v++] = y + r * sinA * 0.8f;
                vertices[v++] = z + r * sinA;
                vertices[v++] = 0.0f;
                vertices[v++] = 0.0f;
                vertices[v++] = 1.0f;
                vertices[v++] = t;
                vertices[v++] = (float) seg / BODY_SEGMENTS;
            }
        }

        float tailX = -2.5f;
        float curX = tailX;

        for (int seg = 0; seg < RATTLE_SEGMENTS; seg++) {
            float segFrac = (float) seg / RATTLE_SEGMENTS;
            float segLen = 0.14f - segFrac * 0.04f;
            float bulgeR = 0.10f - segFrac * 0.04f;
            float neckR = 0.025f - segFrac * 0.005f;
            if (bulgeR < 0.015f) bulgeR = 0.015f;
            if (neckR < 0.005f) neckR = 0.005f;

            float xIn = curX;
            float xMid = curX - segLen * 0.5f;
            float xOut = curX - segLen;
            curX = xOut;

            float gap = 0.015f;
            float[] ringX = { xIn, xIn - gap, xMid, xOut, xOut };
            float[] ringR = { neckR, neckR * 1.5f, bulgeR, neckR * 1.5f, neckR };

            for (int r = 0; r < RATTLE_RINGS_PER_SEG; r++) {
                float rx = ringX[r];
                float rr = ringR[r];
                for (int s = 0; s <= BODY_SEGMENTS; s++) {
                    float angle = (float) (s * 2.0 * Math.PI / BODY_SEGMENTS);
                    float cosA = (float) Math.cos(angle);
                    float sinA = (float) Math.sin(angle);

                    float prevX = tailX;
                    float dirX = prevX - rx;
                    float dirLen = (float) Math.sqrt(dirX * dirX + 0.01f);

                    vertices[v++] = rx;
                    vertices[v++] = cosA * rr;
                    vertices[v++] = sinA * rr;
                    vertices[v++] = dirX / dirLen;
                    vertices[v++] = 0.0f;
                    vertices[v++] = 1.0f;
                    vertices[v++] = 1.0f + (float) (seg * RATTLE_RINGS_PER_SEG + r) / (RATTLE_SEGMENTS * RATTLE_RINGS_PER_SEG);
                    vertices[v++] = (float) s / BODY_SEGMENTS;
                }
            }
        }

        int bodyQuads = (BODY_RINGS + HEAD_RINGS) * BODY_SEGMENTS;
        int rattleQuads = (RATTLE_SEGMENTS * RATTLE_RINGS_PER_SEG - 1) * BODY_SEGMENTS;
        int totalQuads = bodyQuads + rattleQuads;
        short[] indices = new short[totalQuads * 6];
        int i = 0;

        for (int ring = 0; ring < BODY_RINGS + HEAD_RINGS; ring++) {
            for (int seg = 0; seg < BODY_SEGMENTS; seg++) {
                int row0 = ring * (BODY_SEGMENTS + 1);
                int row1 = (ring + 1) * (BODY_SEGMENTS + 1);
                short a = (short) (row0 + seg);
                short b = (short) (row0 + seg + 1);
                short c = (short) (row1 + seg);
                short d = (short) (row1 + seg + 1);
                indices[i++] = a; indices[i++] = c; indices[i++] = b;
                indices[i++] = b; indices[i++] = c; indices[i++] = d;
            }
        }

        for (int rattleRing = 0; rattleRing < RATTLE_SEGMENTS * RATTLE_RINGS_PER_SEG - 1; rattleRing++) {
            int globalRing0 = bodyRingCount + rattleRing;
            int globalRing1 = globalRing0 + 1;
            for (int seg = 0; seg < BODY_SEGMENTS; seg++) {
                int row0 = globalRing0 * (BODY_SEGMENTS + 1);
                int row1 = globalRing1 * (BODY_SEGMENTS + 1);
                short a = (short) (row0 + seg);
                short b = (short) (row0 + seg + 1);
                short c = (short) (row1 + seg);
                short d = (short) (row1 + seg + 1);
                indices[i++] = a; indices[i++] = c; indices[i++] = b;
                indices[i++] = b; indices[i++] = c; indices[i++] = d;
            }
        }

        ByteBuffer vbBuf = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder());
        vbBuf.asFloatBuffer().put(vertices).position(0);

        ByteBuffer ibBuf = ByteBuffer.allocateDirect(indices.length * 2)
                .order(ByteOrder.nativeOrder());
        ibBuf.asShortBuffer().put(indices).position(0);

        int stride = FLOATS_PER_VERTEX * 4;
        VertexBuffer vb = new VertexBuffer.Builder()
                .bufferCount(1)
                .vertexCount(vertexCount)
                .attribute(VertexBuffer.VertexAttribute.POSITION, 0,
                        VertexBuffer.AttributeType.FLOAT3, 0, stride)
                .attribute(VertexBuffer.VertexAttribute.TANGENTS, 0,
                        VertexBuffer.AttributeType.FLOAT4, 3 * 4, stride)
                .attribute(VertexBuffer.VertexAttribute.UV0, 0,
                        VertexBuffer.AttributeType.FLOAT2, 7 * 4, stride)
                .build(engine);

        vbBuf.position(0);
        vb.setBufferAt(engine, 0, vbBuf);

        IndexBuffer ib = new IndexBuffer.Builder()
                .indexCount(indices.length)
                .bufferType(IndexBuffer.Builder.IndexType.USHORT)
                .build(engine);

        ibBuf.position(0);
        ib.setBuffer(engine, ibBuf);

        return new RenderedMesh(vb, ib, indices.length);
    }

    public static RenderedMesh buildEye(Engine engine) {
        int vertexCount = (EYE_RINGS + 1) * (EYE_SEGMENTS + 1);
        float[] vertices = new float[vertexCount * FLOATS_PER_VERTEX];
        float eyeRadius = 0.045f;
        int v = 0;

        for (int lat = 0; lat <= EYE_RINGS; lat++) {
            float theta = (float) (lat * Math.PI / EYE_RINGS);
            float sinT = (float) Math.sin(theta);
            float cosT = (float) Math.cos(theta);
            float vCoord = (float) lat / EYE_RINGS;
            for (int lon = 0; lon <= EYE_SEGMENTS; lon++) {
                float phi = (float) (lon * 2.0 * Math.PI / EYE_SEGMENTS);
                float sinP = (float) Math.sin(phi);
                float cosP = (float) Math.cos(phi);
                vertices[v++] = eyeRadius * sinT * cosP;
                vertices[v++] = eyeRadius * cosT;
                vertices[v++] = eyeRadius * sinT * sinP;
                vertices[v++] = -sinP;
                vertices[v++] = 0.0f;
                vertices[v++] = cosP;
                vertices[v++] = (float) lon / EYE_SEGMENTS;
                vertices[v++] = vCoord;
            }
        }

        int quadCount = EYE_RINGS * EYE_SEGMENTS;
        short[] indices = new short[quadCount * 6];
        int i = 0;
        for (int lat = 0; lat < EYE_RINGS; lat++) {
            for (int lon = 0; lon < EYE_SEGMENTS; lon++) {
                int row0 = lat * (EYE_SEGMENTS + 1);
                int row1 = (lat + 1) * (EYE_SEGMENTS + 1);
                short a = (short) (row0 + lon);
                short b = (short) (row0 + lon + 1);
                short c = (short) (row1 + lon);
                short d = (short) (row1 + lon + 1);
                indices[i++] = a; indices[i++] = c; indices[i++] = b;
                indices[i++] = b; indices[i++] = c; indices[i++] = d;
            }
        }

        return buildRenderedMesh(engine, vertices, indices);
    }

    public static RenderedMesh buildTongue(Engine engine) {
        int LEN_SEGMENTS = 10;
        int RAD_SEGMENTS = 6;
        int lenTotal = LEN_SEGMENTS + 1;
        int vertexCount = lenTotal * (RAD_SEGMENTS + 1) * 2;
        float[] vertices = new float[vertexCount * FLOATS_PER_VERTEX];
        int v = 0;
        float tongueLen = 0.5f;
        float tongueRadius = 0.008f;
        float forkSplit = 0.08f;

        for (int fork = 0; fork < 2; fork++) {
            float forkOffset = (fork == 0) ? forkSplit : -forkSplit;
            for (int ring = 0; ring <= LEN_SEGMENTS; ring++) {
                float t = (float) ring / LEN_SEGMENTS;
                float tx = tongueLen * t;
                float ty = forkOffset * t * t;
                float tz = 0.015f * (float) Math.sin(t * Math.PI * 2.0f);
                float r = tongueRadius * (1.0f - t * 0.7f);
                for (int seg = 0; seg <= RAD_SEGMENTS; seg++) {
                    float angle = (float) (seg * 2.0 * Math.PI / RAD_SEGMENTS);
                    float cosA = (float) Math.cos(angle);
                    float sinA = (float) Math.sin(angle);
                    vertices[v++] = tx + r * cosA;
                    vertices[v++] = ty + r * sinA * 0.3f;
                    vertices[v++] = tz + r * sinA;
                    vertices[v++] = 1.0f;
                    vertices[v++] = 0.0f;
                    vertices[v++] = 0.0f;
                    vertices[v++] = t;
                    vertices[v++] = (float) seg / RAD_SEGMENTS;
                }
            }
        }

        int totalQuads = 2 * LEN_SEGMENTS * RAD_SEGMENTS;
        short[] indices = new short[totalQuads * 6];
        int i = 0;
        for (int fork = 0; fork < 2; fork++) {
            int base = fork * lenTotal * (RAD_SEGMENTS + 1);
            for (int ring = 0; ring < LEN_SEGMENTS; ring++) {
                for (int seg = 0; seg < RAD_SEGMENTS; seg++) {
                    int row0 = base + ring * (RAD_SEGMENTS + 1);
                    int row1 = base + (ring + 1) * (RAD_SEGMENTS + 1);
                    short a = (short) (row0 + seg);
                    short b = (short) (row0 + seg + 1);
                    short c = (short) (row1 + seg);
                    short d = (short) (row1 + seg + 1);
                    indices[i++] = a; indices[i++] = c; indices[i++] = b;
                    indices[i++] = b; indices[i++] = c; indices[i++] = d;
                }
            }
        }

        return buildRenderedMesh(engine, vertices, indices);
    }

    private static RenderedMesh buildRenderedMesh(Engine engine, float[] vertices, short[] indices) {
        int vertexCount = vertices.length / FLOATS_PER_VERTEX;
        ByteBuffer vbBuf = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder());
        vbBuf.asFloatBuffer().put(vertices).position(0);

        ByteBuffer ibBuf = ByteBuffer.allocateDirect(indices.length * 2)
                .order(ByteOrder.nativeOrder());
        ibBuf.asShortBuffer().put(indices).position(0);

        int stride = FLOATS_PER_VERTEX * 4;
        VertexBuffer vb = new VertexBuffer.Builder()
                .bufferCount(1)
                .vertexCount(vertexCount)
                .attribute(VertexBuffer.VertexAttribute.POSITION, 0,
                        VertexBuffer.AttributeType.FLOAT3, 0, stride)
                .attribute(VertexBuffer.VertexAttribute.TANGENTS, 0,
                        VertexBuffer.AttributeType.FLOAT4, 3 * 4, stride)
                .attribute(VertexBuffer.VertexAttribute.UV0, 0,
                        VertexBuffer.AttributeType.FLOAT2, 7 * 4, stride)
                .build(engine);

        vbBuf.position(0);
        vb.setBufferAt(engine, 0, vbBuf);

        IndexBuffer ib = new IndexBuffer.Builder()
                .indexCount(indices.length)
                .bufferType(IndexBuffer.Builder.IndexType.USHORT)
                .build(engine);

        ibBuf.position(0);
        ib.setBuffer(engine, ibBuf);

        return new RenderedMesh(vb, ib, indices.length);
    }
}
