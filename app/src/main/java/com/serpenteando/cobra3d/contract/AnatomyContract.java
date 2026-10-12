package com.serpenteando.cobra3d.contract;

import com.serpenteando.cobra3d.model.entity.AnatomyHotspot;
import com.serpenteando.cobra3d.model.entity.AnatomyLayer;

import java.util.List;

/**
 * Contrato MVP pra Tela de Anatomia 3D.
 * [ALÉM DO TEI] Inovador — não existe nos slides do prof.
 * Gerencia camadas, hotspots e modelo 3D.
 */
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
