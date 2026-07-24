package com.kaio.cobra3d.contract;

import com.kaio.cobra3d.model.entity.AnatomyHotspot;
import com.kaio.cobra3d.model.entity.AnatomyLayer;

import java.util.List;

public interface AnatomyContract {

    interface View {
        void showLayer(AnatomyLayer layer);
        void showHotspots(List<AnatomyHotspot> hotspots);
        void showHotspotInfo(AnatomyHotspot hotspot);
        void showModel(String modelFileName);
        void hideHotspotInfo();
        void showLoading();
        void hideLoading();
        void showError(String message);
    }

    interface Presenter {
        void loadLayers();
        void selectLayer(String layerName);
        void selectHotspot(long hotspotId);
        void selectHotspotByIndex(int index);
        void onLayerChanged(int position);
    }
}
