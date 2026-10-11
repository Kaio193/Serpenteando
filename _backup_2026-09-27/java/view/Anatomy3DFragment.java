package com.kaio.cobra3d.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.filament.Engine;
import com.google.android.filament.Renderer;
import com.kaio.cobra3d.R;
import com.kaio.cobra3d.view.renderer.Anatomy3DRenderer;
import com.kaio.cobra3d.view.renderer.AnatomySurfaceView;

/**
 * Tela de Anatomia 3D — recurso INOVADOR do TCC.
 *
 * ⚠️ NÃO EXISTE NOS SLIDES DO PROF. ALEXANDRE — é inovação total.
 * O professor vai perguntar como funciona, então tudo está comentado!
 *
 * Como está hoje (pedido do Kaio em 23/09/2026): a tela mostra SÓ o modelo,
 * sem nenhum botão. Nada de camadas, hotspots ou tour — a interação é feita
 * direto no modelo: arrastar gira, pinça dá zoom (o AnatomySurfaceView cuida
 * disso sozinho no onTouchEvent).
 *
 * Tecnologias:
 * - Google Filament: engine 3D open source (mesma do Google Maps 3D) — é ela
 *   que lê e desenha o arquivo assets/Serpente_3D.glb.
 * - SurfaceView: renderiza a cena 3D fora da UI thread (performático), com o
 *   render loop sincronizado no VSync pelo Choreographer.
 */
public class Anatomy3DFragment extends Fragment {

    // Superfície 3D (Filament). É a única view da tela.
    private AnatomySurfaceView surfaceView;

    private Engine filamentEngine;
    private Renderer filamentRenderer;

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
        if (surfaceView == null) return;

        // Cria a engine 3D do Filament
        filamentEngine = Engine.create();
        if (filamentEngine == null) return;

        filamentRenderer = filamentEngine.createRenderer();
        surfaceView.createFilament(filamentEngine, filamentRenderer);

        // Monta o corpo — é aqui que assets/Serpente_3D.glb é carregado — e
        // enquadra a câmera mostrando a serpente inteira.
        surfaceView.postDelayed(() -> {
            if (!isAdded() || surfaceView == null) return;
            Anatomy3DRenderer scene = surfaceView.getScene3d();
            if (scene == null) return;
            scene.buildSnakeBody();
            scene.resetCamera();
        }, 300);
    }

    @Override
    public void onDestroyView() {
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
        super.onDestroyView();
    }
}
