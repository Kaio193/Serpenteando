package com.kaio.cobra3d.view;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.contract.HomeContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Tip;
import com.kaio.cobra3d.model.entity.UserProgress;
import com.kaio.cobra3d.presenter.HomePresenter;

public class HomeFragment extends Fragment implements HomeContract.View {

    private HomePresenter presenter;
    private TextView tipText;
    private TextView progressScoreText;
    private TextView progressMissionsText;
    private TextView progressStreakText;
    private TextView heroTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new HomePresenter(this, DatabaseHelper.getInstance(requireContext()));

        tipText = view.findViewById(R.id.tipText);
        progressScoreText = view.findViewById(R.id.progressScoreText);
        progressMissionsText = view.findViewById(R.id.progressMissionsText);
        progressStreakText = view.findViewById(R.id.progressStreakText);
        heroTitle = view.findViewById(R.id.tv_hero_title);
        if (heroTitle != null) {
            String fullText = heroTitle.getText().toString();
            SpannableString spannable = new SpannableString(fullText);
            int start = fullText.indexOf("serpentes");
            if (start != -1) {
                int jade = getResources().getColor(R.color.cascavel_jade, null);
                spannable.setSpan(new ForegroundColorSpan(jade), start, start + "serpentes".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                heroTitle.setText(spannable);
            }
        }

        // Explore cards
        view.findViewById(R.id.exploreQuizCard).setOnClickListener(v -> presenter.onQuizClicked());
        view.findViewById(R.id.exploreMythsCard).setOnClickListener(v -> presenter.onMythsTruthsClicked());
        view.findViewById(R.id.exploreAnatomyCard).setOnClickListener(null);

        // Category chips
        view.findViewById(R.id.chipCobras).setOnClickListener(v -> presenter.onContentClicked("cobras"));
        view.findViewById(R.id.chipAnfibios).setOnClickListener(v -> presenter.onContentClicked("anfibios"));
        view.findViewById(R.id.chipPeconhentos).setOnClickListener(v -> presenter.onContentClicked("peconhentos"));
        view.findViewById(R.id.chipNaoPeconhentos).setOnClickListener(v -> presenter.onContentClicked("nao_peconhentos"));
        view.findViewById(R.id.chipTodas).setOnClickListener(v -> presenter.onContentClicked("todas"));

        presenter.loadTip();
        presenter.loadProgress();
    }

    @Override
    public void showTip(Tip tip) {
        if (tip != null && tipText != null) {
            tipText.setText(tip.getTipText());
        }
    }

    @Override
    public void showProgress(UserProgress progress) {
        int score = progress != null ? progress.getQuizHighScore() : 0;
        int missions = progress != null ? progress.getMissionsCompleted() : 0;
        int streak = progress != null ? progress.getCurrentStreak() : 0;
        if (progressScoreText != null) progressScoreText.setText(String.valueOf(score));
        if (progressMissionsText != null) progressMissionsText.setText(String.valueOf(missions));
        if (progressStreakText != null) progressStreakText.setText(String.valueOf(streak));
    }

    @Override
    public void showContent(String category) {
        Bundle args = new Bundle();
        args.putString("category", category);
        Navigation.findNavController(requireView())
                .navigate(R.id.nav_content, args);
    }

    @Override
    public void showQuiz() {
        Navigation.findNavController(requireView())
                .navigate(R.id.nav_quiz);
    }

    @Override
    public void showMissions() {
        Navigation.findNavController(requireView())
                .navigate(R.id.nav_missions);
    }

    @Override
    public void showAnatomy3D() {
        Toast.makeText(getContext(), R.string.home_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMythsTruths() {
        Navigation.findNavController(requireView())
                .navigate(R.id.nav_myth_truth);
    }

    @Override
    public void showError(String message) {
        if (!isAdded() || getContext() == null) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter = null;
        tipText = null;
        progressScoreText = null;
        progressMissionsText = null;
        progressStreakText = null;
        heroTitle = null;
    }
}
