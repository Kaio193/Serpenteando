package com.kaio.cobra3d.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.Locale;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.contract.QuizContract;
import com.kaio.cobra3d.presenter.QuizPresenter;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Question;

public class QuizFragment extends Fragment implements QuizContract.View {

    private static final int DIFFICULTY_FACIL = 1;
    private static final int DIFFICULTY_MEDIO = 2;
    private static final int DIFFICULTY_DIFICIL = 3;

    private static final int COLOR_INCORRECT = Color.parseColor("#FF6B6B");

    private QuizPresenter presenter;

    private TextView scoreText;
    private TextView chipFacil, chipMedio, chipDificil;
    private ProgressBar progressBar;
    private TextView questionCounterText;
    private TextView star1, star2, star3;
    private TextView questionText;

    private final View[] optionContainers = new View[4];
    private final TextView[] optionTexts = new TextView[4];
    private final TextView[] optionLabels = new TextView[4];

    private View explanationCard;
    private TextView resultIcon, resultLabel, pointsBadge, explanationText;

    private TextView nextButton;
    private ProgressBar loadingIndicator;

    private Question currentQuestion;
    private boolean answered = false;
    private int selectedOptionIndex = -1;
    private int scoreBeforeAnswer = 0;
    private boolean pendingIsCorrect = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new QuizPresenter(this, DatabaseHelper.getInstance(requireContext()));

        bindViews(view);
        setupDifficultySegment();
        setupOptionClicks();

        view.findViewById(R.id.backButton).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        nextButton.setOnClickListener(v -> presenter.nextQuestion());

        selectDifficulty(DIFFICULTY_FACIL);
    }

    private void bindViews(View view) {
        scoreText = view.findViewById(R.id.scoreText);

        chipFacil = view.findViewById(R.id.chipFacil);
        chipMedio = view.findViewById(R.id.chipMedio);
        chipDificil = view.findViewById(R.id.chipDificil);

        progressBar = view.findViewById(R.id.progressBar);
        questionCounterText = view.findViewById(R.id.questionCounterText);

        star1 = view.findViewById(R.id.star1);
        star2 = view.findViewById(R.id.star2);
        star3 = view.findViewById(R.id.star3);
        questionText = view.findViewById(R.id.questionText);

        optionContainers[0] = view.findViewById(R.id.optionA);
        optionContainers[1] = view.findViewById(R.id.optionB);
        optionContainers[2] = view.findViewById(R.id.optionC);
        optionContainers[3] = view.findViewById(R.id.optionD);

        optionTexts[0] = view.findViewById(R.id.optionAText);
        optionTexts[1] = view.findViewById(R.id.optionBText);
        optionTexts[2] = view.findViewById(R.id.optionCText);
        optionTexts[3] = view.findViewById(R.id.optionDText);

        optionLabels[0] = view.findViewById(R.id.optionALabel);
        optionLabels[1] = view.findViewById(R.id.optionBLabel);
        optionLabels[2] = view.findViewById(R.id.optionCLabel);
        optionLabels[3] = view.findViewById(R.id.optionDLabel);

        explanationCard = view.findViewById(R.id.explanationCard);
        resultIcon = view.findViewById(R.id.resultIcon);
        resultLabel = view.findViewById(R.id.resultLabel);
        pointsBadge = view.findViewById(R.id.pointsBadge);
        explanationText = view.findViewById(R.id.explanationText);

        nextButton = view.findViewById(R.id.nextButton);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
    }

    private void setupDifficultySegment() {
        chipFacil.setOnClickListener(v -> selectDifficulty(DIFFICULTY_FACIL));
        chipMedio.setOnClickListener(v -> selectDifficulty(DIFFICULTY_MEDIO));
        chipDificil.setOnClickListener(v -> selectDifficulty(DIFFICULTY_DIFICIL));
    }

    private void selectDifficulty(int difficulty) {
        chipFacil.setBackgroundResource(difficulty == DIFFICULTY_FACIL ? R.drawable.chip_active : R.drawable.chip_inactive);
        chipMedio.setBackgroundResource(difficulty == DIFFICULTY_MEDIO ? R.drawable.chip_active : R.drawable.chip_inactive);
        chipDificil.setBackgroundResource(difficulty == DIFFICULTY_DIFICIL ? R.drawable.chip_active : R.drawable.chip_inactive);

        chipFacil.setTextColor(ContextCompat.getColor(requireContext(),
                difficulty == DIFFICULTY_FACIL ? R.color.cascavel_on_bg : R.color.cascavel_on_surface_variant));
        chipMedio.setTextColor(ContextCompat.getColor(requireContext(),
                difficulty == DIFFICULTY_MEDIO ? R.color.cascavel_on_bg : R.color.cascavel_on_surface_variant));
        chipDificil.setTextColor(ContextCompat.getColor(requireContext(),
                difficulty == DIFFICULTY_DIFICIL ? R.color.cascavel_on_bg : R.color.cascavel_on_surface_variant));

        presenter.loadQuestions(null, difficulty);
    }

    private void setupOptionClicks() {
        for (int i = 0; i < optionContainers.length; i++) {
            final int index = i;
            optionContainers[i].setOnClickListener(v -> {
                if (answered) return;
                answered = true;
                selectedOptionIndex = index;
                scoreBeforeAnswer = presenter.getCurrentScore();
                presenter.selectAnswer(index);
            });
        }
    }

    @Override
    public void showQuestion(Question question, int current, int total) {
        hideLoading();

        currentQuestion = question;
        answered = false;
        selectedOptionIndex = -1;
        pendingIsCorrect = false;

        resetOptionStyles();
        explanationCard.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);

        questionCounterText.setText(String.format(Locale.getDefault(), "Questão %d de %d", current, total));
        progressBar.setMax(total);
        progressBar.setProgress(current - 1);

        questionText.setText(question.getQuestionText());
        optionTexts[0].setText(question.getOptionA());
        optionTexts[1].setText(question.getOptionB());
        optionTexts[2].setText(question.getOptionC());
        optionTexts[3].setText(question.getOptionD());

        updateStars(question.getDifficulty());
    }

    private void updateStars(int difficulty) {
        int filledColor = ContextCompat.getColor(requireContext(), R.color.cascavel_gold);
        int emptyColor = ContextCompat.getColor(requireContext(), R.color.cascavel_on_surface_variant);

        star1.setTextColor(difficulty >= 1 ? filledColor : emptyColor);
        star2.setTextColor(difficulty >= 2 ? filledColor : emptyColor);
        star3.setTextColor(difficulty >= 3 ? filledColor : emptyColor);
    }

    private void resetOptionStyles() {
        for (int i = 0; i < optionContainers.length; i++) {
            optionContainers[i].setBackgroundResource(R.drawable.option_default);
            optionContainers[i].setClickable(true);
            optionLabels[i].setVisibility(View.GONE);
        }
    }

    @Override
    public void showScore(int score) {
        scoreText.setText(String.format(Locale.getDefault(), "%d pts", score));

        if (pendingIsCorrect) {
            int delta = score - scoreBeforeAnswer;
            if (delta > 0) {
                pointsBadge.setText(String.format(Locale.getDefault(), "+%d pts", delta));
                pointsBadge.setVisibility(View.VISIBLE);
            } else {
                pointsBadge.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void showExplanation(String explanation, boolean isCorrect) {
        pendingIsCorrect = isCorrect;

        if (selectedOptionIndex >= 0) {
            optionContainers[selectedOptionIndex].setBackgroundResource(
                    isCorrect ? R.drawable.option_correct : R.drawable.option_incorrect);
            optionLabels[selectedOptionIndex].setText(isCorrect ? "CORRECT" : "INCORRECT");
            optionLabels[selectedOptionIndex].setVisibility(View.VISIBLE);
            optionLabels[selectedOptionIndex].setTextColor(
                    isCorrect ? ContextCompat.getColor(requireContext(), R.color.cascavel_jade_bright) : COLOR_INCORRECT);
        }

        if (!isCorrect && currentQuestion != null) {
            int correctIndex = currentQuestion.getCorrectAnswer();
            optionContainers[correctIndex].setBackgroundResource(R.drawable.option_correct);
            optionLabels[correctIndex].setText("CORRECT");
            optionLabels[correctIndex].setVisibility(View.VISIBLE);
            optionLabels[correctIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_jade_bright));
        }

        for (View container : optionContainers) {
            container.setClickable(false);
        }

        resultIcon.setText(isCorrect ? "✓" : "✗");
        resultIcon.setTextColor(
                isCorrect ? ContextCompat.getColor(requireContext(), R.color.cascavel_jade_bright) : COLOR_INCORRECT);
        resultLabel.setText(isCorrect ? "Correto!" : "Incorreto");
        explanationText.setText(explanation);

        explanationCard.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showResult(int score, int total, boolean passed) {
        new AlertDialog.Builder(requireContext())
                .setTitle(passed ? "Parabéns!" : "Quase lá!")
                .setMessage(String.format(Locale.getDefault(),
                        "Você fez %d pontos em %d perguntas.\n%s",
                        score, total,
                        passed ? "Você passou de 60% de acerto!" : "Tenta de novo pra bater os 60% de acerto."))
                .setPositiveButton("Fechar", (dialog, which) -> {
                    dialog.dismiss();
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void showLoading() {
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        loadingIndicator.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        hideLoading();
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
