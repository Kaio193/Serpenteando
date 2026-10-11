package com.kaio.cobra3d.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.filament.Engine;
import com.google.android.filament.Renderer;
import com.kaio.cobra3d.R;
import com.kaio.cobra3d.view.renderer.Anatomy3DRenderer;
import com.kaio.cobra3d.view.renderer.AnatomySurfaceView;

/**
 * Tela 3D — o recurso INOVADOR do TCC.
 *
 * ⚠️ NÃO EXISTE NOS SLIDES DO PROF. ALEXANDRE — é inovação total.
 * O professor vai perguntar como funciona, então está tudo comentado!
 *
 * Como está agora (pedido do Kaio em 27/09/2026): fundo BRANCO e SÓ o modelo,
 * no estilo dos visualizadores 3D do Google — nada de botão, camada, hotspot
 * ou tour. A interação é direto no modelo: arrastar gira, pinça dá zoom (quem
 * faz isso é o AnatomySurfaceView.onTouchEvent).
 *
 * Tela de carregamento (30/09/2026): o arquivo tem 66 MB, então abrir o 3D
 * passou a ter 3 tempos — toque na aba → tela "CARREGANDO MODELO 3D..." (que
 * aparece no mesmo instante) → o modelo, quando estiver pronto. Quem faz o
 * trabalho pesado fora da thread principal é o Anatomy3DRenderer
 * (iniciarCarregamento), e aqui só se acompanha o estado dele pra saber a hora
 * de tirar o carregamento da frente.
 */
public class Anatomy3DFragment extends Fragment {

    // De quanto em quanto tempo olhar se o modelo já ficou pronto. 50 ms = o
    // carregamento sai da frente quase no mesmo frame em que o modelo entra.
    private static final long INTERVALO_ACOMPANHAMENTO_MS = 50L;

    // Superfície 3D (Filament). É a única view da tela.
    private AnatomySurfaceView surfaceView;

    // Camada de carregamento (fica na frente da superfície enquanto o modelo
    // não está pronto). É a View de baixo que aparece através do GL transparente.
    private View loadingOverlay;
    private View loadingSpinner;
    private TextView loadingText;

    private Engine filamentEngine;
    private Renderer filamentRenderer;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    /**
     * Fica perguntando pro renderer em que pé está o carregamento. Simples de
     * propósito: assim nada precisa atravessar thread pra avisar a UI.
     */
    private final Runnable acompanharCarga = new Runnable() {
        @Override
        public void run() {
            Anatomy3DRenderer scene = (surfaceView == null) ? null : surfaceView.getScene3d();
            if (scene == null) {
                esconderCarregamento();
                return;
            }

            int estado = scene.getEstadoModelo();
            if (estado == Anatomy3DRenderer.MODELO_PRONTO) {
                esconderCarregamento();
            } else if (estado == Anatomy3DRenderer.MODELO_FALHOU) {
                mostrarFalha();
            } else {
                uiHandler.postDelayed(this, INTERVALO_ACOMPANHAMENTO_MS);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_anatomy_3d, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        surfaceView = view.findViewById(R.id.anatomySurfaceView);
        loadingOverlay = view.findViewById(R.id.loadingModelOverlay);
        loadingSpinner = view.findViewById(R.id.loadingModelSpinner);
        loadingText = view.findViewById(R.id.loadingModelText);
        if (surfaceView == null) return;

        // A tela de carregamento já nasce visível: o usuário tocou na aba, então
        // a resposta tem que ser imediata, antes de qualquer trabalho pesado.
        mostrarCarregamento();

        // Cria a engine 3D do Filament
        filamentEngine = Engine.create();
        if (filamentEngine == null) {
            mostrarFalha();
            return;
        }

        filamentRenderer = filamentEngine.createRenderer();
        surfaceView.createFilament(filamentEngine, filamentRenderer);

        // Dispara a leitura dos 66 MB numa thread de fundo (não trava a UI) e
        // começa a acompanhar: quando o modelo entrar na cena, o carregamento sai.
        Anatomy3DRenderer scene = surfaceView.getScene3d();
        if (scene != null) {
            scene.iniciarCarregamento();
        }
        iniciarAcompanhamento();
    }

    @Override
    public void onResume() {
        super.onResume();
        // O render loop parou no onPause: volta a desenhar.
        if (surfaceView != null) {
            surfaceView.onResumeFrame();
        }
        iniciarAcompanhamento();
    }

    @Override
    public void onPause() {
        // Sem isso o render loop continuava rodando com o app em segundo plano
        // (gastando GPU e bateria à toa).
        uiHandler.removeCallbacks(acompanharCarga);
        if (surfaceView != null) {
            surfaceView.onPauseFrame();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        uiHandler.removeCallbacks(acompanharCarga);

        // Libera a superfície 3D e a engine, senão vaza memória/GPU a cada vez
        // que o usuário sai e volta pra aba 3D.
        if (surfaceView != null) {
            surfaceView.release();
            surfaceView = null;
        }
        if (filamentRenderer != null && filamentEngine != null) {
            filamentEngine.destroyRenderer(filamentRenderer);
        }
        filamentRenderer = null;
        if (filamentEngine != null) {
            filamentEngine.destroy();
            filamentEngine = null;
        }
        loadingOverlay = null;
        loadingSpinner = null;
        loadingText = null;
        super.onDestroyView();
    }

    // ------------------------------------------------------------------
    // Tela de carregamento
    // ------------------------------------------------------------------

    private void iniciarAcompanhamento() {
        uiHandler.removeCallbacks(acompanharCarga);
        uiHandler.post(acompanharCarga);
    }

    private void mostrarCarregamento() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        if (loadingSpinner != null) loadingSpinner.setVisibility(View.VISIBLE);
        if (loadingText != null) loadingText.setText(R.string.loading_3d_model);
    }

    private void esconderCarregamento() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
    }

    /**
     * Se o modelo não puder ser carregado, a tela NÃO pode ficar em branco sem
     * explicação: mantém a camada mas troca o texto e tira o indicador.
     */
    private void mostrarFalha() {
        if (loadingSpinner != null) loadingSpinner.setVisibility(View.GONE);
        if (loadingText != null) loadingText.setText(R.string.error_3d_load);
        if (loadingText != null) loadingText.setVisibility(View.VISIBLE);
    }
}
