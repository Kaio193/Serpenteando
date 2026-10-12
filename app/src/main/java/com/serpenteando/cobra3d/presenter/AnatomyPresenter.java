package com.serpenteando.cobra3d.presenter;

import android.content.Context;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.contract.AnatomyContract;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.model.entity.AnatomyHotspot;
import com.serpenteando.cobra3d.model.entity.AnatomyLayer;
import com.serpenteando.cobra3d.model.repository.AnatomyRepository;
import com.serpenteando.cobra3d.model.repository.ProgressRepository;

import java.util.List;

public class AnatomyPresenter implements AnatomyContract.Presenter {

    private final AnatomyContract.View view;
    private final Context context;
    private final AnatomyRepository anatomyRepository;
    private final ProgressRepository progressRepository;
    private List<AnatomyLayer> layers;
    private List<AnatomyHotspot> currentHotspots;
    private int currentLayerIndex;
    private boolean layerActuallySwitched;

    public AnatomyPresenter(AnatomyContract.View view, DatabaseHelper dbHelper, Context context) {
        this.view = view;
        this.context = context.getApplicationContext();
        this.anatomyRepository = new AnatomyRepository(dbHelper.getWritableDatabase());
        this.progressRepository = new ProgressRepository(dbHelper.getWritableDatabase());
        this.currentLayerIndex = 0;
    }

    @Override
    public void loadLayers() {
        view.showLoading();
        layers = anatomyRepository.getAllLayers();
        view.hideLoading();

        if (layers.isEmpty()) {
            view.showError(context.getString(R.string.error_no_layer));
            return;
        }

        selectLayer(layers.get(0).getLayerName());
        layerActuallySwitched = true;
    }

    @Override
    public void selectLayer(String layerName) {
        AnatomyLayer layer = anatomyRepository.getLayerByName(layerName);
        if (layer != null) {
            view.showLayer(layer);
            currentHotspots = anatomyRepository.getHotspotsByLayerId(layer.getId());
            view.showHotspots(currentHotspots);

            if (layer.getModelFileName() != null && !layer.getModelFileName().isEmpty()) {
                view.showModel(layer.getModelFileName());
            }

            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i).getLayerName().equals(layerName)) {
                    currentLayerIndex = i;
                    break;
                }
            }

            // Conta troca de camada real (botões OU bottom sheet), ignorando a primeira carga
            if (layerActuallySwitched) {
                progressRepository.incrementEvent("ANATOMY_LAYER");
            }
        }
    }

    @Override
    public void selectHotspot(long hotspotId) {
        AnatomyHotspot hotspot = anatomyRepository.getHotspotById(hotspotId);
        if (hotspot != null) {
            view.showHotspotInfo(hotspot);
            progressRepository.incrementEvent("ANATOMY_HOTSPOT");
        }
    }

    @Override
    public void selectHotspotByIndex(int index) {
        if (currentHotspots == null || index < 0 || index >= currentHotspots.size()) {
            return;
        }
        AnatomyHotspot hotspot = currentHotspots.get(index);
        if (hotspot != null) {
            view.showHotspotInfo(hotspot);
            progressRepository.incrementEvent("ANATOMY_HOTSPOT");
        }
    }

    @Override
    public void onLayerChanged(int position) {
        if (layers != null && position >= 0 && position < layers.size()) {
            currentLayerIndex = position;
            selectLayer(layers.get(position).getLayerName());
        }
    }
}
