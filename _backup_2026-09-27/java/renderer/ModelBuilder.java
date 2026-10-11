package com.kaio.cobra3d.view.renderer;

import com.google.android.filament.Engine;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.VertexBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ModelBuilder {

    private static final int FLOATS_PER_VERTEX = 9;
    private static final int BODY_RINGS = 120;
    private static final int BODY_SEGMENTS = 28;
    private static final int HEAD_RINGS = 14;
    private static final int HEAD_SEGMENTS = 28;
    private static final int RATTLE_SEGMENTS = 6;
    private static final int RATTLE_RINGS_PER_SEG = 4;
    private static final int EYE_SEGMENTS = 12;
    private static final int EYE_RINGS = 8;

    private static float[] tangentQuat(float tx, float ty, float tz,
                                       float bx, float by, float bz,
                                       float nx, float ny, float nz) {
        float m00 = tx, m01 = bx, m02 = nx;
        float m10 = ty, m11 = by, m12 = ny;
        float m20 = tz, m21 = bz, m22 = nz;
        float trace = m00 + m11 + m22;
        float[] q = new float[4];
        if (trace > 0f) {
            float s = 0.5f / (float) Math.sqrt(trace + 1f);
            q[3] = 0.25f / s;
            q[0] = (m21 - m12) * s;
            q[1] = (m02 - m20) * s;
            q[2] = (m10 - m01) * s;
        } else if (m00 > m11 && m00 > m22) {
            float s = 2f * (float) Math.sqrt(1f + m00 - m11 - m22);
            q[3] = (m21 - m12) / s;
            q[0] = 0.25f * s;
            q[1] = (m01 + m10) / s;
            q[2] = (m02 + m20) / s;
        } else if (m11 > m22) {
            float s = 2f * (float) Math.sqrt(1f + m11 - m00 - m22);
            q[3] = (m02 - m20) / s;
            q[0] = (m01 + m10) / s;
            q[1] = 0.25f * s;
            q[2] = (m12 + m21) / s;
        } else {
            float s = 2f * (float) Math.sqrt(1f + m22 - m00 - m11);
            q[3] = (m10 - m01) / s;
            q[0] = (m02 + m20) / s;
            q[1] = (m12 + m21) / s;
            q[2] = 0.25f * s;
        }
        return q;
    }

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

    // Port do perfil de espessura do procedural-snake (three.js, sujenphea):
    // cascata de smoothsteps — tailRamp -> neckPinch -> headBulge -> tipClosure.
    // Isso dá o contorno orgânico da cascavel (afinando na cauda, pescoço
    // estreito, cabeça triangular alargada, focinho fechado) em vez de um tubo.
    private static final float TAIL_RAMP_END = 0.10f;
    private static final float NECK_START    = 0.72f;
    private static final float NECK_END      = 0.94f;
    private static final float NECK_DEPTH    = 0.25f;
    private static final float HEAD_START    = 0.80f;
    private static final float HEAD_END      = 1.0f;
    private static final float HEAD_RADIUS   = 1.15f;
    private static final float HEAD_BULGE    = 0.55f;
    private static final float SCALE_MIN     = 0.16f;
    private static final float SCALE_MAX     = 0.28f;
    // Tubo elíptico: raio vertical (normal/cima) menor que o horizontal,
    // deixa o corpo achatado como o de uma cascavel real.
    private static final float RADIUS_N = 0.82f;
    private static final float RADIUS_B = 1.16f;
    private static final float BELLY_OFFSET = 0.06f;
    private static final float TWIST = 1.2f;

    private static float smoothstepE(float a, float b, float x) {
        float t = Math.max(0.0f, Math.min(1.0f, (x - a) / (b - a)));
        return t * t * (3.0f - 2.0f * t);
    }

    private static float mixE(float a, float b, float t) {
        return a + (b - a) * t;
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
            // spineU: 0 = cauda (perto do guizo), 1 = focinho
            float t = (float) ring / (BODY_RINGS + HEAD_RINGS);

            // 1. tail ramp: o corpo engorda rápido depois do guizo
            float tailRamp = smoothstepE(0.0f, TAIL_RAMP_END, t);

            // 2. neck pinch: afina antes da cabeça
            float neckMid = (NECK_START + NECK_END) * 0.5f;
            float neckDown = smoothstepE(NECK_START, neckMid, t);
            float neckUp = smoothstepE(neckMid, NECK_END, t);
            float neckPinch = 1.0f - NECK_DEPTH * neckDown * (1.0f - neckUp);

            // 3. head bulge: alarga a região do crânio (triangular)
            float headMid = (HEAD_START + HEAD_END) * 0.5f;
            float headRampUp = smoothstepE(HEAD_START, headMid, t);
            float headRampDown = smoothstepE(headMid, HEAD_END, t);
            float headBulge = HEAD_BULGE * headRampUp * (1.0f - headRampDown);
            float headBase = neckPinch * mixE(1.0f, HEAD_RADIUS, headRampUp);

            // 4. tip closure: fecha o focinho
            float tipClosure = 1.0f - smoothstepE(0.96f, 1.0f, t);

            // combina tudo e mapeia para o raio base (sem clamp, a cabeça
            // extrapola o SCALE_MAX de propósito — é o que cria o volume)
            float combined = tailRamp * (headBase + headBulge) * tipClosure;
            float scale = Math.max(mixE(SCALE_MIN, SCALE_MAX, combined), 0.01f);

            float x = -2.5f + 5.0f * t;
            float y = (float) (0.55 * Math.sin(1.8 * Math.PI * t) + 0.25 * Math.sin(3.2 * Math.PI * t + 0.3));
            float z = (float) (0.35 * Math.sin(2.1 * Math.PI * t + 0.7));

            float rx = scale * RADIUS_N; // raio vertical (cima-baixo)
            float ry = scale * RADIUS_B; // raio horizontal (achatado)

            ringCenters[ring * 4] = x;
            ringCenters[ring * 4 + 1] = y;
            ringCenters[ring * 4 + 2] = z;
            ringCenters[ring * 4 + 3] = scale;

            for (int seg = 0; seg <= BODY_SEGMENTS; seg++) {
                float angle = (float) (seg * 2.0 * Math.PI / BODY_SEGMENTS);
                // torção suave acumulando ao longo do corpo, como no shader de referência
                float twisted = angle + t * TWIST;
                float cosA = (float) Math.cos(twisted);
                float sinA = (float) Math.sin(twisted);

                // anel elíptico (Y vertical, Z lateral) + ventre empurrado levemente
                float posY = cosA * rx + BELLY_OFFSET * scale;
                float posZ = sinA * ry;

                // normal do tubo elíptico: raios invertidos, curvatura correta
                float ny = cosA * (1.0f / rx);
                float nz = sinA * (1.0f / ry);
                float nlen = (float) Math.sqrt(ny * ny + nz * nz);
                ny /= nlen;
                nz /= nlen;

                // frame ortonormal: tangente ao longo do corpo (X), normal (0,ny,nz)
                float[] q = tangentQuat(1.0f, 0.0f, 0.0f, 0.0f, nz, -ny, 0.0f, ny, nz);

                vertices[v++] = x;
                vertices[v++] = y + posY;
                vertices[v++] = z + posZ;
                vertices[v++] = q[0];
                vertices[v++] = q[1];
                vertices[v++] = q[2];
                vertices[v++] = q[3];
                vertices[v++] = t;
                vertices[v++] = (float) seg / BODY_SEGMENTS;
            }
        }

        float curX = -2.5f;

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

                    vertices[v++] = rx;
                    vertices[v++] = cosA * rr;
                    vertices[v++] = sinA * rr;
                    vertices[v++] = (float) Math.sin(angle * 0.5f);
                    vertices[v++] = 0.0f;
                    vertices[v++] = 0.0f;
                    vertices[v++] = (float) Math.cos(angle * 0.5f);
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
        // olho antes tinha 0.045 e sumia no render: a cabeça tem raio ~0.39 no
        // mundo, então o olho precisa ser proporcional (0.09) pra aparecer
        float eyeRadius = 0.09f;
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
                float x = eyeRadius * sinT * cosP;
                float y = eyeRadius * cosT;
                float z = eyeRadius * sinT * sinP;
                float tx = -sinP, ty = 0.0f, tz = cosP;
                float bx = cosT * cosP, by = -sinT, bz = cosT * sinP;
                float nx = x / eyeRadius, ny = y / eyeRadius, nz = z / eyeRadius;
                float[] q = tangentQuat(tx, ty, tz, bx, by, bz, nx, ny, nz);
                vertices[v++] = x;
                vertices[v++] = y;
                vertices[v++] = z;
                vertices[v++] = q[0];
                vertices[v++] = q[1];
                vertices[v++] = q[2];
                vertices[v++] = q[3];
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
        float tongueLen = 0.34f;
        float tongueRadius = 0.05f;
        float forkSplit = 0.10f;

        for (int fork = 0; fork < 2; fork++) {
            float forkOffset = (fork == 0) ? forkSplit : -forkSplit;
            for (int ring = 0; ring <= LEN_SEGMENTS; ring++) {
                float t = (float) ring / LEN_SEGMENTS;
                float tx = tongueLen * t;
                // fork no eixo Y (cima/baixo) — a câmera vê a cobra de lado (+Z),
                // então um fork em Z (lados) projetaria as duas pontas sobrepostas
                // numa linha única = "traço fino isolado" do Gemini v9.8.
                // Em Y vira um V visível de perfil, como língua de serpente real.
                float ty = forkOffset * t * t;
                float tz = 0.0f;
                float r = tongueRadius * (1.0f - t * 0.6f);
                for (int seg = 0; seg <= RAD_SEGMENTS; seg++) {
                    float angle = (float) (seg * 2.0 * Math.PI / RAD_SEGMENTS);
                    float cosA = (float) Math.cos(angle);
                    float sinA = (float) Math.sin(angle);
                    // Cliindro com seção redonda: sem achatar, senão some de perfil.
                    vertices[v++] = tx + r * cosA;
                    vertices[v++] = ty + r * sinA;
                    vertices[v++] = tz + r * cosA * 0.5f;
                    vertices[v++] = (float) Math.sin(angle * 0.5f);
                    vertices[v++] = 0.0f;
                    vertices[v++] = 0.0f;
                    vertices[v++] = (float) Math.cos(angle * 0.5f);
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
