package com.serpenteando.cobra3d.view.renderer;

import android.content.res.AssetManager;
import android.util.Log;

import com.google.android.filament.Engine;
import com.google.android.filament.EntityManager;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.MaterialProvider;
import com.google.android.filament.gltfio.ResourceLoader;
import com.google.android.filament.gltfio.UbershaderProvider;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Leitor do Serpente_3D.glb (66 MB).
 *
 * O carregamento é dividido em 3 passos, e cada um fica no lugar certo:
 *
 *  1. readAsset()  -> só leitura de arquivo, nenhum objeto do Filament envolvido,
 *                     então roda numa thread de fundo. Era AQUI que a interface
 *                     congelava ~1,8s (o "Skipped 170 frames" do Choreographer).
 *  2. create()     -> o parse do .glb. Devolve uma {@link Carga}, que é o dono de
 *                     TUDO que foi criado neste carregamento.
 *  3. beginAsyncLoad() + atualizarTexturas() -> as texturas são decodificadas um
 *                     pouco por frame (API async do próprio gltfio), então a tela
 *                     de carregamento continua respondendo enquanto isso.
 *
 * ⚠️ LIÇÃO APRENDIDA (QA no device, 30/09/2026): antes o AssetLoader, o
 * UbershaderProvider e o ResourceLoader NUNCA eram destruídos. Cada ida e volta
 * na aba 3D vazava ~12 MB de memória nativa (medido: 283 MB -> 331 MB de Native
 * Heap em 4 entradas, com swap subindo junto) — depois de vários usos o sistema
 * mata o processo = o "crash do 3D". Agora quem manda liberar é o destroy(Carga).
 */
public class GlbLoader {

    private static final String TAG = "GlbLoader";

    // Lê de 256 KB em 256 KB em vez de alocar um byte[] do arquivo inteiro: antes
    // eram 66 MB no heap Java + 66 MB de buffer direto ao mesmo tempo (~132 MB de
    // pico, com o heap do aparelho em 256 MB). Agora só existe o buffer direto.
    private static final int TAMANHO_BLOCO = 256 * 1024;

    /**
     * Um carregamento do modelo: o asset e tudo que foi criado junto dele.
     *
     * É a peça que faltava antes. Como cada carregamento tem o seu dono, a
     * limpeza pode ser feita sem estado estático compartilhado (o antigo
     * "cachedLoader" estático podia apontar pro engine de uma tela já destruída).
     */
    public static final class Carga {

        final FilamentAsset asset;
        private final AssetLoader loader;
        private final MaterialProvider provider;
        private ResourceLoader resources;

        // Ligado enquanto a decodificação assíncrona está no meio do caminho.
        // Se a tela fechar nesse estado, o ResourceLoader precisa ser CANCELADO
        // antes de ser destruído (ver destroy()).
        private boolean texturasEmAndamento;

        Carga(FilamentAsset asset, AssetLoader loader, MaterialProvider provider) {
            this.asset = asset;
            this.loader = loader;
            this.provider = provider;
        }
    }

    /** Impede instanciar esta classe (ela é só um punhado de funções). */
    private GlbLoader() {}

    /**
     * Passo 1 — lê o .glb inteiro para um buffer direto.
     *
     * Feito pra rodar em thread de fundo: aqui não existe nenhum objeto do
     * Filament, é só I/O.
     *
     * @return o buffer pronto pra leitura (position 0), ou null se falhar.
     */
    public static ByteBuffer readAsset(AssetManager androidAssets, String path) {
        try {
            InputStream is = androidAssets.open(path);
            int tamanho = is.available();
            if (tamanho <= 0) {
                Log.e(TAG, "Asset " + path + " veio vazio");
                is.close();
                return null;
            }

            ByteBuffer buffer = ByteBuffer.allocateDirect(tamanho).order(ByteOrder.nativeOrder());
            byte[] bloco = new byte[TAMANHO_BLOCO];
            int lido = 0;
            int bytesLidos;
            while (lido < tamanho
                    && (bytesLidos = is.read(bloco, 0, Math.min(TAMANHO_BLOCO, tamanho - lido))) > 0) {
                buffer.put(bloco, 0, bytesLidos);
                lido += bytesLidos;
            }
            is.close();

            if (lido != tamanho) {
                // Leu menos do que o arquivo declara ter: melhor falhar aqui do
                // que entregar um .glb cortado pro parser nativo.
                Log.e(TAG, path + " incompleto: " + lido + " de " + tamanho + " bytes");
                return null;
            }

            buffer.flip();
            Log.d(TAG, path + " lido (" + tamanho + " bytes)");
            return buffer;
        } catch (Throwable t) {
            // Throwable (e não Exception) porque são 66 MB: se estourar o heap
            // queremos cair aqui e avisar, não derrubar o app.
            Log.e(TAG, "Falha lendo " + path, t);
            return null;
        }
    }

    /**
     * Passo 2 — transforma os bytes no asset do Filament.
     *
     * @return a carga (com asset + loader + provider), ou null se falhar.
     */
    public static Carga create(Engine engine, ByteBuffer dados, String path) {
        if (dados == null) return null;

        UbershaderProvider provider = null;
        AssetLoader loader = null;
        try {
            provider = new UbershaderProvider(engine);
            loader = new AssetLoader(engine, provider, EntityManager.get());
            FilamentAsset asset = loader.createAsset(dados);
            if (asset == null) {
                Log.e(TAG, "createAsset retornou null para " + path);
                loader.destroy();
                provider.destroy();
                return null;
            }
            Log.d(TAG, "Loaded " + path + " with " + asset.getEntities().length + " entities");
            // Observação importante (evita segurar 66 MB à toa): o gltfio COPIA o
            // blob pra dentro do asset ("we create a copy of the source blob and
            // stash it inside the asset"), então o buffer que veio do
            // readAsset() pode ser liberado — quem chama é que solta a referência.
            return new Carga(asset, loader, provider);
        } catch (Throwable t) {
            Log.e(TAG, "AssetLoader falhou para " + path, t);
            // Se falhou no meio, o que já foi criado não pode ficar pendurado.
            if (loader != null) {
                try { loader.destroy(); } catch (Throwable ignorado) { /* já era */ }
            }
            if (provider != null) {
                try { provider.destroy(); } catch (Throwable ignorado) { /* já era */ }
            }
            return null;
        }
    }

    /**
     * Passo 3 — começa a decodificar as texturas em pedaços, sem travar a UI.
     *
     * @return true se a carga assíncrona começou (nesse caso quem avança é o
     *         {@link #atualizarTexturas(Carga)}); false se não havia nada pra
     *         carregar (ou o caminho async não estivesse disponível) — nesse
     *         caso os recursos já foram carregados aqui mesmo, de uma vez.
     */
    public static boolean beginAsyncLoad(Engine engine, Carga carga) {
        if (carga == null || carga.asset == null) return false;

        ResourceLoader resources = null;
        try {
            resources = new ResourceLoader(engine, true);
            if (resources.asyncBeginLoad(carga.asset)) {
                carga.resources = resources;
                carga.texturasEmAndamento = true;
                return true;
            }
        } catch (Throwable t) {
            Log.w(TAG, "Carga assincrona indisponivel, tentando de uma vez", t);
        }

        // Plano B: sem texturas pendentes (ou async fora do ar), carrega tudo
        // agora — foi assim que a versão antiga sempre fez.
        try {
            resources.loadResources(carga.asset);
        } catch (Throwable t) {
            Log.w(TAG, "ResourceLoader falhou, asset pode estar incompleto", t);
        }
        try { resources.destroy(); } catch (Throwable ignorado) { /* já era */ }
        return false;
    }

    /** Progresso 0..1 das texturas; -1 quando não há carga em andamento. */
    public static float progresso(Carga carga) {
        if (carga == null || carga.resources == null) return -1f;
        try {
            return carga.resources.asyncGetLoadProgress();
        } catch (Throwable t) {
            return -1f;
        }
    }

    /**
     * Avança a decodificação das texturas. Precisa ser chamado na thread
     * principal, uma vez por frame (é o render loop que chama).
     *
     * @return true quando terminou (ou quando não há mais o que carregar).
     */
    public static boolean atualizarTexturas(Carga carga) {
        if (carga == null || carga.resources == null) return true;
        try {
            carga.resources.asyncUpdateLoad();
            if (carga.resources.asyncGetLoadProgress() >= 1.0f) {
                carga.resources.destroy();
                carga.resources = null;
                carga.texturasEmAndamento = false;
                Log.d(TAG, "Texturas prontas");
                return true;
            }
            return false;
        } catch (Throwable t) {
            Log.w(TAG, "Falha na carga das texturas, seguindo com o que deu", t);
            try { carga.resources.destroy(); } catch (Throwable ignorado) { /* já era */ }
            carga.resources = null;
            carga.texturasEmAndamento = false;
            return true;
        }
    }

    /**
     * Solta a cópia interna do .glb que fica dentro do asset (o "source data").
     *
     * São dezenas de MB de memória nativa que só servem durante o carregamento —
     * a documentação do Filament manda chamar isto logo depois que os recursos
     * foram carregados (loadResources/asyncBeginLoad).
     */
    public static void liberarFonte(Carga carga) {
        if (carga == null || carga.texturasEmAndamento) return;
        try {
            carga.asset.releaseSourceData();
            Log.d(TAG, "Memoria do arquivo liberada (releaseSourceData)");
        } catch (Throwable t) {
            Log.w(TAG, "releaseSourceData falhou", t);
        }
    }

    /**
     * Libera TUDO o que foi criado para este carregamento.
     *
     * A ordem importa: o material provider só pode soltar os materiais DEPOIS
     * que o asset (que usa esses materiais) foi destruído, e o engine ainda
     * precisa estar vivo — por isso isto é chamado antes do engine.destroy().
     */
    public static void destroy(Carga carga) {
        if (carga == null) return;

        if (carga.resources != null) {
            // ⚠️ ACHADO NO DEVICE (30/09/2026): sair da aba 3D com a carga
            // assíncrona em andamento fazia o app abortar dentro do
            // libfilament-jni ("Pointer tag ... was truncated" = free em memória
            // já liberada). A causa é documentada: "asyncCancelLoad: calling this
            // is only necessary if the asyncBeginLoad API was used and
            // cancellation is required before progress reaches 100%". Sem
            // cancelar, as threads de decodificação de textura continuam
            // trabalhando em cima do que acaba de ser destruído.
            if (carga.texturasEmAndamento) {
                try { carga.resources.asyncCancelLoad(); }
                catch (Throwable t) { Log.w(TAG, "asyncCancelLoad", t); }
                carga.texturasEmAndamento = false;
            }
            try { carga.resources.destroy(); } catch (Throwable t) { Log.w(TAG, "ResourceLoader", t); }
            carga.resources = null;
        }
        if (carga.asset != null) {
            try { carga.loader.destroyAsset(carga.asset); } catch (Throwable t) { Log.w(TAG, "destroyAsset", t); }
        }
        if (carga.provider != null) {
            try { carga.provider.destroyMaterials(); } catch (Throwable t) { Log.w(TAG, "destroyMaterials", t); }
            try { carga.provider.destroy(); } catch (Throwable t) { Log.w(TAG, "provider", t); }
        }
        if (carga.loader != null) {
            try { carga.loader.destroy(); } catch (Throwable t) { Log.w(TAG, "AssetLoader", t); }
        }
    }
}
