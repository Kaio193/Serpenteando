package com.kaio.cobra3d.presenter;

import com.kaio.cobra3d.contract.MissionContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Mission;
import com.kaio.cobra3d.model.entity.MissionItem;
import com.kaio.cobra3d.model.repository.MissionRepository;
import com.kaio.cobra3d.model.repository.ProgressRepository;

import java.util.List;

public class MissionPresenter implements MissionContract.Presenter {

    private final MissionContract.View view;
    private final MissionRepository missionRepository;
    private final ProgressRepository progressRepository;
    private List<MissionItem> currentItems;

    public MissionPresenter(MissionContract.View view, DatabaseHelper dbHelper) {
        this.view = view;
        this.missionRepository = new MissionRepository(dbHelper.getWritableDatabase());
        this.progressRepository = new ProgressRepository(dbHelper.getWritableDatabase());
    }

    @Override
    public void loadMissions() {
        view.showLoading();
        List<Mission> missions = missionRepository.getAll();
        view.hideLoading();
        view.showMissionList(missions);
    }

    @Override
    public void loadMission(long missionId) {
        view.showLoading();
        Mission mission = missionRepository.getById(missionId);
        currentItems = missionRepository.getItemsByMissionId(missionId);
        view.hideLoading();

        if (mission == null || currentItems.isEmpty()) {
            view.showError("Missão não encontrada");
            return;
        }

        view.showMission(mission, currentItems);
    }

    @Override
    public void verifySelection(List<Boolean> selections) {
        if (currentItems == null || selections == null) return;

        boolean allCorrect = true;
        for (int i = 0; i < currentItems.size() && i < selections.size(); i++) {
            boolean selectedItem = selections.get(i);
            boolean correctItem = currentItems.get(i).isCorrect();
            if (selectedItem != correctItem) {
                allCorrect = false;
                break;
            }
        }

        Mission mission = missionRepository.getById(currentItems.get(0).getMissionId());
        if (allCorrect) {
            onMissionCompleted(mission.getId(), mission.getPoints());
            view.showResult(true, mission.getPoints(),
                    "Parabéns! Você completou a missão!");
        } else {
            view.showResult(false, 0,
                    "Revise suas seleções. Tente novamente!");
        }
    }

    @Override
    public void onMissionCompleted(long missionId, int points) {
        missionRepository.markCompleted(missionId);
        progressRepository.updateMissionCompleted(points);
    }
}
