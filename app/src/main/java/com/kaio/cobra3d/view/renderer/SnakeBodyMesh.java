package com.kaio.cobra3d.view.renderer;

import com.google.android.filament.Engine;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.VertexBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SnakeBodyMesh {

    private static final int RING_SEGMENTS = 32;
    private static final int BODY_RINGS = 80;
    private static final float BODY_LENGTH = 4.0f;
    private static final float BODY_RADIUS = 0.18f;
    private static final float WAVE_AMPLITUDE = 0.35f;
    private static final float WAVE_FREQUENCY = 2.0f;

    private static final int RATTLE_SEGMENTS = 4;
    private static final int RATTLE_RINGS_PER_SEGMENT = 3;
    private static final int RATTLE_TOTAL_RINGS = RATTLE_SEGMENTS * RATTLE_RINGS_PER_SEGMENT + 1;
    private static final int RATTLE_QUAD_STRIPS = RATTLE_TOTAL_RINGS - 1;

    private static final float[] RATTLE_RADII  = { 0.12f, 0.10f, 0.08f, 0.06f };
    private static final float[] RATTLE_LENGTHS = { 0.15f, 0.12f, 0.10f, 0.08f };
    private static final float RATTLE_GAP = 0.02f;
    private static final float RATTLE_NECK_RADIUS = 0.025f;

    private static final int FLOATS_PER_VERTEX = 8;

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

    public static RenderedMesh build(Engine engine) {
        int bodyRingCount = BODY_RINGS + 1;
        int rattleRingCount = RATTLE_TOTAL_RINGS;
        int totalRings = bodyRingCount + rattleRingCount;
        int vertexCount = totalRings * (RING_SEGMENTS + 1);

        float[] vertices = new float[vertexCount * FLOATS_PER_VERTEX];
        int v = 0;

        for (int ring = 0; ring <= BODY_RINGS; ring++) {
            float t = (float) ring / BODY_RINGS;
            float x = -BODY_LENGTH / 2.0f + BODY_LENGTH * t;
            float y = (float) Math.sin(t * Math.PI * WAVE_FREQUENCY) * WAVE_AMPLITUDE;
            float z = 0.0f;
            float radiusFactor = 1.0f - 0.5f * (float) Math.abs(t - 0.5f) * 2.0f;
            float radius = BODY_RADIUS * radiusFactor;
            float u = t;
            for (int seg = 0; seg <= RING_SEGMENTS; seg++) {
                float angle = (float) (seg * 2.0 * Math.PI / RING_SEGMENTS);
                float cosA = (float) Math.cos(angle);
                float sinA = (float) Math.sin(angle);
                vertices[v++] = x;
                vertices[v++] = y + cosA * radius;
                vertices[v++] = z + sinA * radius;
                vertices[v++] = 1.0f;
                vertices[v++] = 0.0f;
                vertices[v++] = 0.0f;
                vertices[v++] = u;
                vertices[v++] = (float) seg / RING_SEGMENTS;
            }
        }

        float tailX = -BODY_LENGTH / 2.0f;
        float curX = tailX;
        float rattleU = 0.0f;

        for (int seg = 0; seg < RATTLE_SEGMENTS; seg++) {
            float segLen = RATTLE_LENGTHS[seg];
            float bulgeR = RATTLE_RADII[seg];
            if (seg > 0) curX -= RATTLE_GAP;

            float xNeckIn  = curX;
            float xBulge   = curX - segLen * 0.5f;
            float xNeckOut = curX - segLen;
            curX = xNeckOut;

            float[] ringX = { xNeckIn, xBulge, xNeckOut };
            float[] ringR = { RATTLE_NECK_RADIUS, bulgeR, RATTLE_NECK_RADIUS };

            for (int r = 0; r < RATTLE_RINGS_PER_SEGMENT; r++) {
                float rx = ringX[r];
                float rr = ringR[r];
                for (int s = 0; s <= RING_SEGMENTS; s++) {
                    float angle = (float) (s * 2.0 * Math.PI / RING_SEGMENTS);
                    float cosA = (float) Math.cos(angle);
                    float sinA = (float) Math.sin(angle);
                    vertices[v++] = rx;
                    vertices[v++] = cosA * rr;
                    vertices[v++] = sinA * rr;
                    if (rr > 0.001f) {
                        vertices[v++] = 1.0f;
                        vertices[v++] = 0.0f;
                        vertices[v++] = 0.0f;
                    } else {
                        vertices[v++] = -1.0f;
                        vertices[v++] = 0.0f;
                        vertices[v++] = 0.0f;
                    }
                    vertices[v++] = rattleU;
                    vertices[v++] = (float) s / RING_SEGMENTS;
                }
            }
        }

        curX -= RATTLE_GAP * 0.5f;
        for (int s = 0; s <= RING_SEGMENTS; s++) {
            vertices[v++] = curX;
            vertices[v++] = 0.0f;
            vertices[v++] = 0.0f;
            vertices[v++] = -1.0f;
            vertices[v++] = 0.0f;
            vertices[v++] = 0.0f;
            vertices[v++] = rattleU;
            vertices[v++] = (float) s / RING_SEGMENTS;
        }

        int bodyQuads   = BODY_RINGS * RING_SEGMENTS;
        int rattleQuads = RATTLE_QUAD_STRIPS * RING_SEGMENTS;
        int totalQuads  = bodyQuads + rattleQuads;
        short[] indices = new short[totalQuads * 6];
        int i = 0;

        for (int ring = 0; ring < BODY_RINGS; ring++) {
            for (int seg = 0; seg < RING_SEGMENTS; seg++) {
                int row0 = ring * (RING_SEGMENTS + 1);
                int row1 = (ring + 1) * (RING_SEGMENTS + 1);
                short a = (short) (row0 + seg);
                short b = (short) (row0 + seg + 1);
                short c = (short) (row1 + seg);
                short d = (short) (row1 + seg + 1);
                indices[i++] = a; indices[i++] = c; indices[i++] = b;
                indices[i++] = b; indices[i++] = c; indices[i++] = d;
            }
        }

        for (int rattleRing = 0; rattleRing < RATTLE_QUAD_STRIPS; rattleRing++) {
            int globalRing0 = BODY_RINGS + rattleRing;
            int globalRing1 = globalRing0 + 1;
            for (int seg = 0; seg < RING_SEGMENTS; seg++) {
                int row0 = globalRing0 * (RING_SEGMENTS + 1);
                int row1 = globalRing1 * (RING_SEGMENTS + 1);
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
}
