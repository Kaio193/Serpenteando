package com.kaio.cobra3d.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.adapter.MissionItemAdapter;
import com.kaio.cobra3d.contract.MissionContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Mission;
import com.kaio.cobra3d.model.entity.MissionItem;
import com.kaio.cobra3d.presenter.MissionPresenter;

import java.util.List;

public class MissionFragment extends Fragment implements MissionContract.View {

    private MissionPresenter presenter;
    private TextView missionTitle;
    private TextView missionDescription;
    private TextView missionScoreText;
    private TextView missionProgressText;
    private TextView missionItemCountText;
    private TextView verifyButton;
    private MissionItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new MissionPresenter(this, DatabaseHelper.getInstance(requireContext()));

        missionTitle = view.findViewById(R.id.missionTitleText);
        missionDescription = view.findViewById(R.id.missionDescText);
        missionScoreText = view.findViewById(R.id.missionScoreText);
        missionProgressText = view.findViewById(R.id.missionProgressText);
        missionItemCountText = view.findViewById(R.id.missionItemCountText);
        verifyButton = view.findViewById(R.id.verifyProgressButton);

        verifyButton.setOnClickListener(v -> {
            if (adapter != null && presenter != null) {
                presenter.verifySelection(adapter.getSelections());
            }
        });

        if (presenter != null) presenter.loadMissions();
    }

    @Override
    public void showMission(Mission mission, List<MissionItem> items) {
        if (!isAdded()) return;
        missionTitle.setText(mission.getTitle());
        missionDescription.setText(mission.getDescription());
        missionProgressText.setText("Progresso: " + mission.getPoints() + " pts");
        int completed = 0;
        for (MissionItem item : items) {
            if (item.isCorrect()) completed++;
        }
        missionItemCountText.setText(completed + "/" + items.size() + " itens concluídos");
        adapter = new MissionItemAdapter(items);
        verifyButton.setVisibility(View.VISIBLE);
        updateScore();
    }

    @Override
    public void showMissionList(List<Mission> missions) {
        if (!isAdded()) return;
        if (missions.isEmpty()) {
            missionTitle.setText(R.string.missions_empty);
            missionDescription.setText("");
            verifyButton.setVisibility(View.GONE);
            return;
        }

        String[] titles = new String[missions.size()];
        for (int i = 0; i < missions.size(); i++) {
            titles[i] = missions.get(i).getTitle() + " (" + missions.get(i).getPoints() + " pts)";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.missions_select)
                .setItems(titles, (dialog, which) -> {
                    if (!isAdded() || presenter == null) return;
                    presenter.loadMission(missions.get(which).getId());
                })
                .setNegativeButton("Voltar", (dialog, which) -> {
                    if (!isAdded()) return;
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .show();
    }

    @Override
    public void showResult(boolean success, int points, String message) {
        if (!isAdded()) return;
        if (adapter != null) adapter.setEnabled(false);
        verifyButton.setVisibility(View.GONE);
        updateScore();

        new AlertDialog.Builder(requireContext())
                .setTitle(success ? R.string.missions_complete_title : R.string.missions_retry_title)
                .setMessage(message + (success ? "\n+" + points + " pts" : ""))
                .setPositiveButton("OK", (dialog, which) -> {
                    if (!isAdded()) return;
                    if (success) {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        if (presenter != null) presenter.loadMissions();
                    }
                })
                .show();
    }

    private void updateScore() {
        if (missionScoreText == null || !isAdded()) return;
        try {
            com.kaio.cobra3d.model.repository.ProgressRepository repo =
                new com.kaio.cobra3d.model.repository.ProgressRepository(
                    DatabaseHelper.getInstance(requireContext()).getReadableDatabase());
            com.kaio.cobra3d.model.entity.UserProgress p = repo.getProgress();
            if (p != null) {
                missionScoreText.setText(p.getTotalPoints() + " pts");
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void showLoading() {}

    @Override
    public void hideLoading() {}

    @Override
    public void showError(String message) {
        if (!isAdded() || getContext() == null) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
