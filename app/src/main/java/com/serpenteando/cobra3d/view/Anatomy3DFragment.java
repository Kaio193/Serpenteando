package com.serpenteando.cobra3d.view;

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
import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.view.renderer.Anatomy3DRenderer;
import com.serpenteando.cobra3d.view.renderer.AnatomySurfaceView;

// Tela 3D — a parte INOVADORA do TCC
// [Não existe nos slides do prof. Alexandre] — o professor vai pedir explicação
// Só o modelo na tela: arrastar gira e a pinça dá zoom (quem trata o toque é o
// AnatomySurfaceView). Enquanto o arquivo de 66 MB carrega, fica na frente a
// tela de "CARREGANDO MODELO 3D...", que some quando o renderer avisa.
public class Anatomy3DFragment extends Fragment {

    // De quanto em quanto tempo conferir se o modelo já ficou pronto
    private static final long INTERVALO_ACOMPANHAMENTO_MS = 50L;

    // Onde o modelo é desenhado (Filament) — é a única view da tela
    private AnatomySurfaceView surfaceView;

    // Tela de carregamento que fica na frente enquanto o modelo não chega
    private View loadingOverlay;
    private View loadingSpinner;
    private TextView loadingText;

    private Engine filamentEngine;
    private Renderer filamentRenderer;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    // Fica perguntando pro renderer em que pé está o carregamento
    // (mais simples que ficar avisando a tela de outra thread)
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

        // Já mostra o carregamento: o usuário tocou na aba e a resposta tem que ser imediata
        mostrarCarregamento();

        // Cria a engine 3D do Filament
        filamentEngine = Engine.create();
        if (filamentEngine == null) {
            mostrarFalha();
            return;
        }

        filamentRenderer = filamentEngine.createRenderer();
        surfaceView.createFilament(filamentEngine, filamentRenderer);

        // Lê os 66 MB numa thread de fundo e começa a acompanhar
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
        // Para o desenho quando o app sai da frente (senão gasta GPU e bateria)
        uiHandler.removeCallbacks(acompanharCarga);
        if (surfaceView != null) {
            surfaceView.onPauseFrame();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        uiHandler.removeCallbacks(acompanharCarga);

        // Libera a superfície e a engine, senão vaza memória a cada ida à aba 3D
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

    // ---------- Tela de carregamento ----------

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

    // Se o modelo não carregar, avisa na tela em vez de deixar tudo branco
    private void mostrarFalha() {
        if (loadingSpinner != null) loadingSpinner.setVisibility(View.GONE);
        if (loadingText != null) loadingText.setText(R.string.error_3d_load);
        if (loadingText != null) loadingText.setVisibility(View.VISIBLE);
    }
}
