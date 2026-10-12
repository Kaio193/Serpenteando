package com.serpenteando.cobra3d.contract;

import com.serpenteando.cobra3d.model.entity.Mission;
import com.serpenteando.cobra3d.model.entity.MissionItem;

import java.util.List;

/**
 * Contrato MVP pra Tela de Missões.
 * [ALÉM DO TEI] Mesmo padrão dos outros contratos.
 */
public interface MissionContract {

    interface View {
        void showMission(Mission mission, List<MissionItem> items);
        void showMissionList(List<Mission> missions);
        void showResult(boolean success, int points, String message);
        void showProgressMessage(String message);
        void showLoading();
        void hideLoading();
        void showError(String message);
    }

    interface Presenter {
        void loadMissions();
        void loadMission(long missionId);
        void verifySelection(List<Boolean> selections);
        void onMissionCompleted(long missionId, int points);
    }
}
