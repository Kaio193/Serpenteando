package com.serpenteando.cobra3d.presenter;

import android.content.Context;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.contract.MissionContract;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.model.entity.Mission;
import com.serpenteando.cobra3d.model.entity.MissionItem;
import com.serpenteando.cobra3d.model.repository.MissionRepository;
import com.serpenteando.cobra3d.model.repository.ProgressRepository;

import java.util.List;

public class MissionPresenter implements MissionContract.Presenter {

    private final MissionContract.View view;
    private final Context context;
    private final MissionRepository missionRepository;
    private final ProgressRepository progressRepository;
    private List<MissionItem> currentItems;

    public MissionPresenter(MissionContract.View view, DatabaseHelper dbHelper, Context context) {
        this.view = view;
        this.context = context.getApplicationContext();
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
        boolean justCompleted = refreshProgress(missionId);
        Mission mission = missionRepository.getById(missionId);
        currentItems = missionRepository.getItemsByMissionId(missionId);
        view.hideLoading();

        if (mission == null || currentItems.isEmpty()) {
            view.showError(context.getString(R.string.error_mission_not_found));
            return;
        }

        view.showMission(mission, currentItems);
        // Missão completou SOZINHA ao bater as metas reais: celebra na hora
        if (justCompleted) {
            view.showResult(true, mission.getPoints(),
                    context.getString(R.string.mission_complete_congrats));
        }
    }

    // Calcula o progresso REAL das missões a partir dos contadores de ações do usuário.
    // Cada item bate a meta sozinho; quando todos os itens cumprem, a missão completa sozinha.
    private boolean refreshProgress(long missionId) {
        List<MissionItem> items = missionRepository.getItemsByMissionId(missionId);
        if (items == null || items.isEmpty()) return false;

        boolean nowComplete = true;
        for (MissionItem item : items) {
            boolean fulfilled;
            if (item.getTrigger() == null || item.getTrigger().isEmpty()) {
                fulfilled = item.isCorrect();
            } else {
                int current = progressRepository.getEventCount(item.getTrigger());
                fulfilled = current >= item.getTarget();
            }
            if (fulfilled != item.isCorrect()) {
                missionRepository.updateItemCorrect(item.getId(), fulfilled);
                item.setCorrect(fulfilled);
            }
            if (!fulfilled) nowComplete = false;
        }

        Mission mission = missionRepository.getById(missionId);
        if (mission != null && nowComplete && !mission.isCompleted()) {
            onMissionCompleted(mission.getId(), mission.getPoints());
            return true;
        }
        return false;
    }

    @Override
    public void verifySelection(List<Boolean> selections) {
        try {
            if (currentItems == null || currentItems.isEmpty()) {
                view.showError(context.getString(R.string.error_no_mission_loaded));
                return;
            }

            long missionId = currentItems.get(0).getMissionId();

            // Re-sincroniza o progresso real baseado nos triggers
            boolean justCompleted = refreshProgress(missionId);

            // Recarrega dados atualizados
            Mission mission = missionRepository.getById(missionId);
            currentItems = missionRepository.getItemsByMissionId(missionId);

            if (mission == null || currentItems == null || currentItems.isEmpty()) {
                view.showError(context.getString(R.string.error_mission_not_found));
                return;
            }

            view.showMission(mission, currentItems);

            // Celebra apenas a conclusão que ACABOU DE acontecer agora (não já premiada).
            // Isso evita conceder 500 pts de novo só por reabrir o app: se a missão
            // ja estava completa e ja premiou, o awarded_missions nao permite repetir.
            if (justCompleted) {
                view.showResult(true, mission.getPoints(),
                        context.getString(R.string.mission_complete_congrats));
            } else if (mission.isCompleted()) {
                // Missao ja completada (e ja premiada) — mostra a conclusao final
                // reaproveitando o dialog de recompensa, sem conceder pontos de novo.
                view.showAllMissionsCompleted();
            } else {
                // Missão em andamento — mostra progresso específico
                int fulfilled = 0;
                int total = currentItems.size();
                StringBuilder pending = new StringBuilder();

                for (MissionItem item : currentItems) {
                    if (item.isCorrect()) {
                        fulfilled++;
                    } else {
                        if (pending.length() > 0) pending.append("\n• ");
                        pending.append(item.getItemText());
                    }
                }

                // Mensagem traduzida (PT/EN/ES)
                String msg = context.getString(
                        R.string.mission_progress_message,
                        fulfilled, total, pending.toString());

                view.showProgressMessage(msg);
            }
        } catch (Exception e) {
            android.util.Log.e("MissionPresenter", "verifySelection error", e);
            view.showError(context.getString(R.string.error_verify_progress) + ": " + e.getMessage());
        }
    }

    @Override
    public void onMissionCompleted(long missionId, int points) {
        missionRepository.markCompleted(missionId);
        progressRepository.updateMissionCompleted(missionId, points);
    }

    /**
     * Troca a missão atual por outra.
     * Antes de carregar a nova missão, recalcula e persiste o progresso
     * dos itens da missão atual (sem marcar a missão como completada se
     * ainda não estiver completa).
     */
    @Override
    public void switchMission(long newMissionId) {
        long currentId = currentItems != null && !currentItems.isEmpty()
                ? currentItems.get(0).getMissionId() : -1L;

        if (currentId == newMissionId) {
            return;
        }

        // Salva o estado atual dos itens antes de trocar de missão.
        // Isso preserva o que o usuário já fez; a missão só completa quando
        // a lógica existente detecta que todas as metas foram batidas.
        if (currentId != -1L) {
            refreshProgress(currentId);
        }

        view.showLoading();
        currentItems = missionRepository.getItemsByMissionId(newMissionId);
        view.hideLoading();

        if (newMissionId <= 0 || currentItems == null || currentItems.isEmpty()) {
            view.showError(context.getString(R.string.error_mission_not_found));
            return;
        }

        view.showMission(missionRepository.getById(newMissionId), currentItems);
    }

    @Override
    public void showMissionChangeDialog() {
        List<Mission> missions = missionRepository.getAll();
        if (missions == null || missions.isEmpty()) {
            view.showError(context.getString(R.string.missions_empty));
            return;
        }

        view.showMissionList(missions);
    }
}