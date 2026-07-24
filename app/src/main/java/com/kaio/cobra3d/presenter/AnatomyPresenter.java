package com.kaio.cobra3d.presenter;

import com.kaio.cobra3d.contract.AnatomyContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.AnatomyHotspot;
import com.kaio.cobra3d.model.entity.AnatomyLayer;
import com.kaio.cobra3d.model.repository.AnatomyRepository;

import java.util.List;

public class AnatomyPresenter implements AnatomyContract.Presenter {

    private final AnatomyContract.View view;
    private final AnatomyRepository anatomyRepository;
    private List<AnatomyLayer> layers;
    private List<AnatomyHotspot> currentHotspots;
    private int currentLayerIndex;

    public AnatomyPresenter(AnatomyContract.View view, DatabaseHelper dbHelper) {
        this.view = view;
        this.anatomyRepository = new AnatomyRepository(dbHelper.getWritableDatabase());
        this.currentLayerIndex = 0;
    }

    @Override
    public void loadLayers() {
        view.showLoading();
        layers = anatomyRepository.getAllLayers();
        view.hideLoading();

        if (layers.isEmpty()) {
            view.showError("Nenhuma camada encontrada");
            return;
        }

        selectLayer(layers.get(0).getLayerName());
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
        }
    }

    @Override
    public void selectHotspot(long hotspotId) {
        AnatomyHotspot hotspot = anatomyRepository.getHotspotById(hotspotId);
        if (hotspot != null) {
            view.showHotspotInfo(hotspot);
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
