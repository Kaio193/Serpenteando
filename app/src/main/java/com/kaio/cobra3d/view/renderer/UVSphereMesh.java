package com.kaio.cobra3d.view.renderer;

import com.google.android.filament.Engine;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.VertexBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class UVSphereMesh {

    private static final int LAT_SEGMENTS = 20;
    private static final int LON_SEGMENTS = 32;

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
        int vertexCount = (LAT_SEGMENTS + 1) * (LON_SEGMENTS + 1);
        float[] vertices = new float[vertexCount * FLOATS_PER_VERTEX];

        int v = 0;
        for (int lat = 0; lat <= LAT_SEGMENTS; lat++) {
            float theta = (float) (lat * Math.PI / LAT_SEGMENTS);
            float sinT = (float) Math.sin(theta);
            float cosT = (float) Math.cos(theta);
            float vCoord = (float) lat / LAT_SEGMENTS;
            for (int lon = 0; lon <= LON_SEGMENTS; lon++) {
                float phi = (float) (lon * 2.0 * Math.PI / LON_SEGMENTS);
                float sinP = (float) Math.sin(phi);
                float cosP = (float) Math.cos(phi);
                float x = sinT * cosP;
                float y = cosT;
                float z = sinT * sinP;
                vertices[v++] = x;
                vertices[v++] = y;
                vertices[v++] = z;
                vertices[v++] = -sinP;
                vertices[v++] = 0.0f;
                vertices[v++] = cosP;
                vertices[v++] = (float) lon / LON_SEGMENTS;
                vertices[v++] = vCoord;
            }
        }

        int quadCount = LAT_SEGMENTS * LON_SEGMENTS;
        short[] indices = new short[quadCount * 6];
        int i = 0;
        for (int lat = 0; lat < LAT_SEGMENTS; lat++) {
            for (int lon = 0; lon < LON_SEGMENTS; lon++) {
                int row0 = lat * (LON_SEGMENTS + 1);
                int row1 = (lat + 1) * (LON_SEGMENTS + 1);
                short a = (short) (row0 + lon);
                short b = (short) (row0 + lon + 1);
                short c = (short) (row1 + lon);
                short d = (short) (row1 + lon + 1);
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
