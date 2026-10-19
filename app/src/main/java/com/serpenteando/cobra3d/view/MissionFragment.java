package com.serpenteando.cobra3d.view;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.adapter.MissionItemAdapter;
import com.serpenteando.cobra3d.contract.MissionContract;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.model.entity.Mission;
import com.serpenteando.cobra3d.model.entity.MissionItem;
import com.serpenteando.cobra3d.presenter.MissionPresenter;
import com.serpenteando.cobra3d.util.PreferenceManager;
import com.serpenteando.cobra3d.util.PressSpring;

import java.util.List;

/**
 * Tela de Missões — sistema gamificado de 7 dias.
 *
 * O usuário completa tarefas (verificar serpente, ler conteúdo, etc.)
 * e ganha pontos. Missões são salvas no SQLite e sobrevivem ao fechar o app.
 *
 * [TEI Dialogs] Usa 3 dialogs customizados:
 * - Seleção de missão (dialog_mission_select)
 * - Sucesso (dialog_mission_complete com animação de troféu)
 * - Falha (dialog_mission_fail com opção de retry)
 *
 * [TEI Persistência] Progresso salvo no SQLite — mesmo conceito do slide 06,
 * só que com tabelas relacionadas (missions → mission_items).
 */
public class MissionFragment extends Fragment implements MissionContract.View {

    private MissionPresenter presenter;
    private TextView missionTitle;
    private TextView missionDescription;
    private TextView missionScoreText;
    private TextView missionProgressText;
    private TextView missionItemCountText;
    private TextView missionPercentText;
    private TextView verifyButton;
    private RecyclerView missionChecklist;
    private CircularProgressIndicator progressRing;
    private Dialog resultDialog;
    private MissionItemAdapter adapter;
    private long currentMissionId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // [TEI Persistência] DatabaseHelper = SQLiteOpenHelper do slide 06
        presenter = new MissionPresenter(this, DatabaseHelper.getInstance(requireContext()), requireContext());

        missionTitle = view.findViewById(R.id.missionTitleText);
        missionDescription = view.findViewById(R.id.missionDescText);
        missionScoreText = view.findViewById(R.id.missionScoreText);
        missionProgressText = view.findViewById(R.id.missionProgressText);
        missionItemCountText = view.findViewById(R.id.missionItemCountText);
        verifyButton = view.findViewById(R.id.verifyProgressButton);
        missionChecklist = view.findViewById(R.id.missionChecklist);
        missionChecklist.setNestedScrollingEnabled(false);
        progressRing = view.findViewById(R.id.missionProgressRing);
        missionPercentText = view.findViewById(R.id.missionPercentText);

        verifyButton.setOnClickListener(v -> {
            Log.d("Serpenteando", "verifyButton clicked, presenter=" + presenter + ", currentMissionId=" + currentMissionId);
            if (presenter != null) {
                presenter.verifySelection(null);
            } else {
                Log.e("Serpenteando", "Presenter is null!");
            }
        });
        PressSpring.attach(verifyButton);


        // Forçar sempre a "Ofensiva de 7 dias" (id 1)
        presenter.loadMission(1L);
    }

    /**
     * Mostra uma missão com seus itens.
     * Calcula progresso (X/Y itens concluídos) e atualiza a barra circular.
     */
    @Override
    public void showMission(Mission mission, List<MissionItem> items) {
        if (!isAdded() || mission == null) return;

        try {
            currentMissionId = mission.getId();
            if (missionTitle != null) missionTitle.setText(mission.getTitle());
            if (missionDescription != null) missionDescription.setText(mission.getDescription());

            // Conta quantos itens foram marcados como corretos
            int completed = 0;
            for (MissionItem item : items) {
                if (item.isCorrect()) completed++;
            }

            int total = items != null ? items.size() : 0;
            int percent = total > 0 ? (completed * 100 / total) : 0;

            // Atualiza o anel de progresso circular (CircularProgressIndicator)
            // Mostra % de conclusão da missão
            if (progressRing != null && total > 0) {
                progressRing.setProgress(percent);
            }
            if (missionPercentText != null) {
                missionPercentText.setText(percent + "%");
            }

            // Texto do dia: "Dia X de Y" — usa a sequência real de dias consecutivos.
            // O dia avança apenas quando o usuário conclui uma missão em um dia novo;
            // várias etapas no mesmo dia contam como um dia só.
            if (missionProgressText != null) {
                int totalDays = mission.getTotalDays();
                if (totalDays > 0) {
                    int effectiveStreak = 0;
                    try {
                        com.serpenteando.cobra3d.model.repository.ProgressRepository repo =
                                new com.serpenteando.cobra3d.model.repository.ProgressRepository(
                                        DatabaseHelper.getInstance(requireContext()).getReadableDatabase());
                        com.serpenteando.cobra3d.model.entity.UserProgress p = repo.getProgress();
                        if (p != null) {
                            // Sequência efetiva: só vale se o usuário concluiu missão hoje ou
                            // ontem; se ficou mais de um dia sem concluir, quebrou (0).
                            effectiveStreak = repo.getSequenciaEfetiva(p);
                        }
                    } catch (Exception ignored) {}
                    // O dia atual é a sequência efetiva, limitada ao total de dias da missão.
                    // Se não há sequência (streak 0), mostra Dia 0 de Y.
                    int day = Math.max(0, Math.min(effectiveStreak, totalDays));
                    missionProgressText.setText(getString(R.string.mission_day_counter, day, totalDays));
                    missionProgressText.setVisibility(View.VISIBLE);
                } else {
                    missionProgressText.setVisibility(View.GONE);
                }
            }

            // Contador pill: "3/4 itens concluídos" — usa string resource
            if (missionItemCountText != null) {
                missionItemCountText.setText(getString(R.string.mission_items_done, completed, total));
            }

            if (items != null) {
                adapter = new MissionItemAdapter(items);
                if (missionChecklist != null) missionChecklist.setAdapter(adapter);
            }

            if (verifyButton != null) verifyButton.setVisibility(View.VISIBLE);



            updateScore();
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.error_mission_load), Toast.LENGTH_SHORT).show();
            }
        }
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

        showMissionSelectDialog(missions);
    }

    /**
     * Abre o diálogo de escolha de missão para troca.
     * Usa a mesma lista de missões disponíveis do sistema atual.
     */
    @Override
    public void showMissionChangeDialog() {
        if (!isAdded() || presenter == null) return;
        presenter.loadMissions();
    }

    @Override
    public void showResult(boolean success, int points, String message) {
        Log.d("Serpenteando", "showResult: success=" + success + ", points=" + points + ", msg=" + message);
        if (!isAdded() || getContext() == null) {
            Log.w("Serpenteando", "showResult: fragment not added or no context");
            return;
        }

        try {
            // Atualiza visual dos itens no adapter
            if (adapter != null) {
                adapter.setEnabled(false);
                adapter.notifyDataSetChanged();
            }

            // Esconde o botão verificar
            if (verifyButton != null) {
                verifyButton.setVisibility(View.GONE);
            }

            // Atualiza pontuação
            updateScore();

            if (success) {
                showSuccessDialog(points, message);
            } else {
                showFailureDialog(message);
            }
        } catch (Exception e) {
            Log.e("Serpenteando", "showResult error", e);
            // Evita crash em qualquer erro inesperado
            if (getContext() != null) {                Toast.makeText(getContext(), getString(R.string.error_prefix) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }



        }
    }

    /**
     * Dialog de sucesso — mostra quando a missão é completada.
     * [TEI Dialogs] Diálogo customizado com layout próprio (slide 05.1)
     * Inclui animação de troféu (scale 0→1 com OvershootInterpolator)
     */
    private void showSuccessDialog(int points, String message) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_mission_complete);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        // Tocar fora fecha o "Missão Completa!" (pedido do Kaio, 08/10 — é o único
        // dialog que fecha assim; o do quiz NÃO fecha). O setCanceledOnTouchOutside
        // sozinho não basta: precisa também da FLAG_WATCH_OUTSIDE_TOUCH, senão o toque
        // de fora nunca chega no dialog (medido no device em 07/10).
        dialog.setCanceledOnTouchOutside(true);
        Window janelaMissao = dialog.getWindow();
        if (janelaMissao != null) {
            janelaMissao.addFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        }

        ImageView trophyIcon = dialog.findViewById(R.id.dialogTrophyIcon);
        trophyIcon.setScaleX(0f);
        trophyIcon.setScaleY(0f);

        ((TextView) dialog.findViewById(R.id.dialogTitle)).setText(R.string.missions_complete_title);
        ((TextView) dialog.findViewById(R.id.dialogPoints)).setText(getString(R.string.mission_points_format, points));
        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(message);

        dialog.setOnShowListener(d -> trophyIcon.animate().scaleX(1f).scaleY(1f).setDuration(400)
                .setInterpolator(new OvershootInterpolator()).start());

        dialog.findViewById(R.id.dialogOkButton).setOnClickListener(v -> {
            dialog.dismiss();
            if (!isAdded()) return;
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        resultDialog = dialog;
        dialog.show();
    }

    /**
     * Dialog de conclusão final — aparece quando o usuário já completou a missão
     * e clica em "Verificar progresso" de novo. Reutiliza o mesmo layout, troféu
     * e animação do dialog de recompensa, mas sem exibir pontos e sem alterar
     * a pontuação do usuário.
     */
    @Override
    public void showAllMissionsCompleted() {
        if (!isAdded() || getContext() == null) return;

        try {
            if (adapter != null) {
                adapter.setEnabled(false);
                adapter.notifyDataSetChanged();
            }
            if (verifyButton != null) {
                verifyButton.setVisibility(View.GONE);
            }
            updateScore();

            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.dialog_mission_complete);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCanceledOnTouchOutside(true);
            Window janelaConclusao = dialog.getWindow();
            if (janelaConclusao != null) {
                janelaConclusao.addFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
            }

            ImageView trophyIcon = dialog.findViewById(R.id.dialogTrophyIcon);
            trophyIcon.setScaleX(0f);
            trophyIcon.setScaleY(0f);

            ((TextView) dialog.findViewById(R.id.dialogTitle)).setText(R.string.missions_all_complete_title);
            dialog.findViewById(R.id.dialogPoints).setVisibility(View.GONE);
            ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(R.string.missions_all_complete_message);

            dialog.setOnShowListener(d -> trophyIcon.animate().scaleX(1f).scaleY(1f).setDuration(400)
                    .setInterpolator(new OvershootInterpolator()).start());

            dialog.findViewById(R.id.dialogOkButton).setOnClickListener(v -> dialog.dismiss());

            resultDialog = dialog;
            dialog.show();
        } catch (Exception e) {
            Log.e("Serpenteando", "Completion dialog error", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.error_prefix) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Dialog informativo de progresso — mostra quando o usuário clica em
     * "Verificar Progresso" e a missão ainda não está completa.
     * [TEI Dialogs] Dialog customizado com botão OK simples.
     */
    @Override
    public void showProgressMessage(String message) {
        if (!isAdded() || getContext() == null) return;

        try {
            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.dialog_mission_fail);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCanceledOnTouchOutside(true);

            TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
            if (dialogTitle != null) {
                dialogTitle.setText(R.string.mission_progress_title);
            }

            TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
            if (dialogMessage != null) {
                dialogMessage.setText(message);
                dialogMessage.setLineSpacing(0, 1.3f);
            }

            View okBtn = dialog.findViewById(R.id.dialogRetryButton);
            if (okBtn != null) {
                ((TextView) okBtn).setText(R.string.mission_progress_ok);
                okBtn.setOnClickListener(v -> dialog.dismiss());
            }

            dialog.show();
        } catch (Exception e) {
            if (getContext() != null) {
                Log.e("Serpenteando", "Progress dialog error", e);
                Toast.makeText(getContext(), getString(R.string.error_prefix) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Dialog de falha — mostra quando a missão não foi completada.
     * Tem botão de retry que recarrega a mesma missão.
     */
    private void showFailureDialog(String message) {
        if (!isAdded() || getContext() == null) return;

        try {
            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.dialog_mission_fail);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCanceledOnTouchOutside(true);

            TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
            if (dialogMessage != null) {
                dialogMessage.setText(message);
                dialogMessage.setLineSpacing(0, 1.3f);
            }

            View retryBtn = dialog.findViewById(R.id.dialogRetryButton);
            if (retryBtn != null) {
                retryBtn.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (!isAdded() || presenter == null) return;
                    if (currentMissionId != -1) {
                        presenter.loadMission(currentMissionId);
                    } else {
                        presenter.loadMissions();
                    }
                });
            }

            dialog.show();
        } catch (Exception e) {
            if (getContext() != null) {
                Log.e("Serpenteando", "Dialog error", e);                Toast.makeText(getContext(), getString(R.string.error_prefix) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }



        }
    }

    /**
     * Dialog de seleção de missão — mostra todas as missões disponíveis.
     * Cada missão aparece como um card com ícone, título e pontos.
     */
    private void showMissionSelectDialog(List<Mission> missions) {
        if (!isAdded() || getContext() == null) return;
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_mission_select);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(true);

        LinearLayout container = dialog.findViewById(R.id.missionListContainer);
        container.removeAllViews();

        // Descrições das missões traduzidas por idioma
        String[] descriptions = {
            getString(R.string.mission_select_desc_1),
            getString(R.string.mission_select_desc_2)
        };

        for (int i = 0; i < missions.size(); i++) {
            final Mission mission = missions.get(i);

            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.chip_inactive);
            card.setPadding(28, 24, 28, 24);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardParams);
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout titleRow = new LinearLayout(requireContext());
            titleRow.setOrientation(LinearLayout.HORIZONTAL);
            titleRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // O card da missão virou botão verde (Kaio, 07/10) — por isso os
            // textos dele são brancos (cascavel_on_button) pra continuar legíveis.
            TextView titleText = new TextView(requireContext());
            titleText.setText(mission.getTitle());
            titleText.setTextSize(16);
            titleText.setTypeface(null, android.graphics.Typeface.BOLD);
            titleText.setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_on_button));
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            titleParams.setMarginStart(16);
            titleText.setLayoutParams(titleParams);
            titleRow.addView(titleText);

            TextView ptsText = new TextView(requireContext());
            ptsText.setText(mission.getPoints() + " " + getString(R.string.mission_pts_label));
            ptsText.setTextSize(13);
            ptsText.setTypeface(null, android.graphics.Typeface.BOLD);
            ptsText.setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_on_button));
            titleRow.addView(ptsText);

            card.addView(titleRow);

            TextView descText = new TextView(requireContext());
            String desc = (i < descriptions.length) ? descriptions[i] : getString(R.string.mission_select_desc_default);
            descText.setText(desc);
            descText.setTextSize(13);
            descText.setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_on_button));
            LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            descParams.setMarginStart(38);
            descParams.topMargin = 6;
            descText.setLayoutParams(descParams);
            card.addView(descText);

            card.setOnClickListener(v -> {
                dialog.dismiss();
                if (!isAdded() || presenter == null) return;
                new PreferenceManager(requireContext()).setLastMissionId(mission.getId());
                presenter.loadMission(mission.getId());
            });

            container.addView(card);
        }

        dialog.findViewById(R.id.dialogBackButton).setOnClickListener(v -> {
            dialog.dismiss();
            if (!isAdded()) return;
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        dialog.show();
    }

    private void updateScore() {
        if (missionScoreText == null || !isAdded()) return;
        try {
            com.serpenteando.cobra3d.model.repository.ProgressRepository repo =
                new com.serpenteando.cobra3d.model.repository.ProgressRepository(
                    DatabaseHelper.getInstance(requireContext()).getReadableDatabase());
            com.serpenteando.cobra3d.model.entity.UserProgress p = repo.getProgress();
            if (p != null) {
                // Total exibido = pontos das missões concluídas (cada recompensa
                // registrada em awarded_missions) + questões certas nos quizzes.
                com.serpenteando.cobra3d.model.repository.MissionRepository missionRepo =
                        new com.serpenteando.cobra3d.model.repository.MissionRepository(
                                DatabaseHelper.getInstance(requireContext()).getReadableDatabase());
                int pontosMissoes = missionRepo.getAwardedPoints(p.getAwardedMissions());
                int total = pontosMissoes + p.getTotalQuestionsCorrect();
                missionScoreText.setText(total + " " + getString(R.string.mission_pts_label));
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (resultDialog != null && resultDialog.isShowing()) {
            resultDialog.dismiss();
        }
        resultDialog = null;
        missionChecklist = null;
        presenter = null;
        adapter = null;
        missionTitle = null;
        missionDescription = null;
        missionScoreText = null;
        missionProgressText = null;
        missionItemCountText = null;
        verifyButton = null;
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
