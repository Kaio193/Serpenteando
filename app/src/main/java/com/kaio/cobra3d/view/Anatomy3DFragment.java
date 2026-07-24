package com.kaio.cobra3d.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.filament.Engine;
import com.google.android.filament.Renderer;
import com.kaio.cobra3d.R;
import com.kaio.cobra3d.contract.AnatomyContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.AnatomyHotspot;
import com.kaio.cobra3d.model.entity.AnatomyLayer;
import com.kaio.cobra3d.presenter.AnatomyPresenter;
import com.kaio.cobra3d.view.adapter.HotspotAdapter;
import com.kaio.cobra3d.view.renderer.Anatomy3DRenderer;
import com.kaio.cobra3d.view.renderer.AnatomySurfaceView;
import com.kaio.cobra3d.view.renderer.TourOverlayManager;

import java.util.List;

public class Anatomy3DFragment extends Fragment implements AnatomyContract.View {

    private AnatomyContract.Presenter presenter;

    private AnatomySurfaceView surfaceView;
    private RecyclerView hotspotsRecyclerView;
    private TextView hotspotInfoTitle;
    private TextView hotspotInfoDescription;
    private View hotspotInfoPanel;
    private TextView modelPlaceholder;
    private TextView layerNameText;
    private TextView layerDescriptionText;

    private View infoOverlayPanel;
    private TextView structureNameText;
    private TextView structureFunctionText;
    private TextView structureHowItWorksText;
    private TextView structureCuriosityText;

    private TextView hintTouchExplore;

    private TourOverlayManager overlayManager;

    private HotspotAdapter hotspotAdapter;

    private Engine filamentEngine;
    private Renderer filamentRenderer;

    @Nullable
    private List<AnatomyHotspot> currentHotspots;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_anatomy_3d, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new AnatomyPresenter(this, DatabaseHelper.getInstance(requireContext()));

        bindViews(view);
        setupSurface(view);
        setupRecyclerView(view);
        setupLayerButtons(view);
        setupTourOverlay();

        presenter.loadLayers();

        surfaceView.postDelayed(() -> {
            if (!isAdded() || surfaceView == null) return;
            Anatomy3DRenderer scene = surfaceView.getScene3d();
            if (scene != null) {
                scene.startTour();
            }
        }, 500);

        surfaceView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (hintTouchExplore != null) {
                    hintTouchExplore.setVisibility(View.GONE);
                }
                Anatomy3DRenderer scene = surfaceView.getScene3d();
                if (scene != null && scene.isTourRunning()) {
                    scene.stopTour();
                    if (overlayManager != null) overlayManager.hide();
                }
            }
            return false;
        });
    }

    private void bindViews(@NonNull View root) {
        hotspotInfoPanel = root.findViewById(R.id.hotspotInfoPanel);
        hotspotInfoTitle = root.findViewById(R.id.hotspotInfoTitle);
        hotspotInfoDescription = root.findViewById(R.id.hotspotInfoDescription);
        modelPlaceholder = root.findViewById(R.id.modelPlaceholder);
        layerNameText = root.findViewById(R.id.layerNameText);
        layerDescriptionText = root.findViewById(R.id.layerDescriptionText);
        infoOverlayPanel = root.findViewById(R.id.infoOverlay);
        structureNameText = root.findViewById(R.id.structureName);
        structureFunctionText = root.findViewById(R.id.structureFunction);
        structureHowItWorksText = root.findViewById(R.id.structureHowItWorks);
        structureCuriosityText = root.findViewById(R.id.structureCuriosity);
        hintTouchExplore = root.findViewById(R.id.hintTouchExplore);
    }

    private void setupSurface(@NonNull View root) {
        surfaceView = root.findViewById(R.id.anatomySurfaceView);

        filamentEngine = Engine.create();
        if (filamentEngine == null) {
            if (modelPlaceholder != null) {
                modelPlaceholder.setVisibility(View.VISIBLE);
                modelPlaceholder.setText(R.string.error_3d_unsupported);
            }
            return;
        }

        filamentRenderer = filamentEngine.createRenderer();
        surfaceView.createFilament(filamentEngine, filamentRenderer);

        surfaceView.setOnHotspotPickedListener(index -> {
            if (presenter != null) {
                presenter.selectHotspotByIndex(index);
            }
        });
    }

    private void setupRecyclerView(@NonNull View root) {
        hotspotsRecyclerView = root.findViewById(R.id.hotspotsRecyclerView);
        hotspotsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        hotspotAdapter = new HotspotAdapter(index -> {
            if (presenter != null) {
                presenter.selectHotspotByIndex(index);
            }
        });
        hotspotsRecyclerView.setAdapter(hotspotAdapter);
    }

    private void setupLayerButtons(@NonNull View root) {
        int[] buttonIds = {
                R.id.btnLayerSkin,
                R.id.btnLayerMuscle,
                R.id.btnLayerCirculatory,
                R.id.btnLayerOrgans
        };
        for (int i = 0; i < buttonIds.length; i++) {
            final int position = i;
            View btn = root.findViewById(buttonIds[i]);
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    if (presenter != null) presenter.onLayerChanged(position);
                });
            }
        }
    }

    private void setupTourOverlay() {
        if (infoOverlayPanel == null || structureNameText == null
            || structureFunctionText == null
            || structureHowItWorksText == null
            || structureCuriosityText == null) return;

        overlayManager = new TourOverlayManager(
            infoOverlayPanel,
            structureNameText,
            structureFunctionText,
            structureHowItWorksText,
            structureCuriosityText
        );

        surfaceView.post(() -> {
            if (!isAdded() || surfaceView == null) return;
            Anatomy3DRenderer scene = surfaceView.getScene3d();
            if (scene != null) {
                scene.setOnTourStructureChangedListener(
                    index -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (overlayManager != null) {
                                    overlayManager.showStructure(index);
                                }
                            });
                        }
                    }
                );
            }
        });
    }

    @Override
    public void showLayer(@NonNull AnatomyLayer layer) {
        Anatomy3DRenderer scene = surfaceView != null ? surfaceView.getScene3d() : null;
        if (scene != null) {
            scene.resetLayer(((int) layer.getId()));
            scene.buildSnakeBody();
            scene.buildHotspots(currentHotspots);
        }
        if (layerNameText != null) {
            layerNameText.setText(layer.getLayerName() != null ? layer.getLayerName() : "");
        }
        if (layerDescriptionText != null) {
            layerDescriptionText.setText(
                layer.getDescription() != null ? layer.getDescription() : "");
        }
        if (modelPlaceholder != null) {
            modelPlaceholder.setVisibility(View.GONE);
        }
    }

    @Override
    public void showHotspots(@NonNull List<AnatomyHotspot> hotspots) {
        this.currentHotspots = hotspots;
        if (hotspotAdapter == null) return;
        hotspotAdapter.setHotspots(hotspots);
        if (surfaceView != null) {
            surfaceView.setHotspots(hotspots);
            Anatomy3DRenderer scene = surfaceView.getScene3d();
            if (scene != null) {
                scene.buildHotspots(hotspots);
            }
        }
    }

    @Override
    public void showHotspotInfo(@Nullable AnatomyHotspot hotspot) {
        if (hotspotInfoPanel == null) return;
        if (hotspot == null) {
            hotspotInfoPanel.setVisibility(View.GONE);
            return;
        }
        hotspotInfoPanel.setVisibility(View.VISIBLE);
        hotspotInfoTitle.setText(
            hotspot.getHotspotName() != null ? hotspot.getHotspotName() : "");
        hotspotInfoDescription.setText(
            hotspot.getDescription() != null ? hotspot.getDescription() : "");

        if (overlayManager != null && hotspot.getHotspotName() != null) {
            for (int i = 0; i < TourOverlayManager.STRUCTURES.length; i++) {
                if (TourOverlayManager.STRUCTURES[i].name
                        .equalsIgnoreCase(hotspot.getHotspotName())) {
                    overlayManager.showStructure(i);
                    break;
                }
            }
        }
    }

    @Override
    public void hideHotspotInfo() {
        if (hotspotInfoPanel != null) {
            hotspotInfoPanel.setVisibility(View.GONE);
        }
    }

    @Override
    public void showModel(@NonNull String modelFileName) {
        if (modelPlaceholder != null) {
            modelPlaceholder.setVisibility(View.GONE);
        }
    }

    @Override
    public void showLoading() {
        if (modelPlaceholder != null) {
            modelPlaceholder.setVisibility(View.VISIBLE);
            modelPlaceholder.setText("Carregando...");
        }
    }

    @Override
    public void hideLoading() {
        if (modelPlaceholder != null) {
            modelPlaceholder.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError(@NonNull String message) {
        if (!isAdded() || getContext() == null) return;
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        if (surfaceView != null) {
            surfaceView.release();
            surfaceView = null;
        }
        if (filamentRenderer != null) {
            if (filamentEngine != null) {
                filamentEngine.destroyRenderer(filamentRenderer);
            }
            filamentRenderer = null;
        }
        if (filamentEngine != null) {
            filamentEngine.destroy();
            filamentEngine = null;
        }
        presenter = null;
        hotspotAdapter = null;
        hotspotsRecyclerView = null;
        hotspotInfoPanel = null;
        hotspotInfoTitle = null;
        hotspotInfoDescription = null;
        modelPlaceholder = null;
        layerNameText = null;
        layerDescriptionText = null;
        overlayManager = null;
        infoOverlayPanel = null;
        structureNameText = null;
        structureFunctionText = null;
        structureHowItWorksText = null;
        structureCuriosityText = null;
        currentHotspots = null;
        hintTouchExplore = null;
        super.onDestroyView();
    }
}
