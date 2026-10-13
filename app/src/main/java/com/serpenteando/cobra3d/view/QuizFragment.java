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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.Locale;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.contract.QuizContract;
import com.serpenteando.cobra3d.presenter.QuizPresenter;
import com.serpenteando.cobra3d.util.PressSpring;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.model.entity.Question;

/**
 * Tela do Quiz — testa conhecimento sobre serpentes.
 *
 * Fluxo:
 * 1. Usuário entra → aparece dialog de dificuldade (Fácil/Médio/Difícil)
 * 2. Escolhe → quiz carrega 10 perguntas da dificuldade escolhida
 * 3. Cada pergunta: 4 opções → confirma → feedback (certo/errado) → explicação
 * 4. Após 10 perguntas → dialog de resultado (aprovado/reprovado)
 * 5. Se aprovado → dialog "Quer continuar?" (mostra só níveis pendentes)
 *
 * [TEI Dialogs] Usa AlertDialog.Builder (slide 05.1) + Dialog customizado
 * (glassmorphism) — exatamente o padrão do prof. Alexandre.
 *
 * [TEI Manipulando Views] Controla ~30 views dinamicamente: muda cor,
 * background, texto e visibilidade a cada resposta.
 */
public class QuizFragment extends Fragment implements QuizContract.View {

    // Níveis de dificuldade (1=Fácil, 2=Médio, 3=Difícil)
    private static final int DIFFICULTY_FACIL = 1;
    private static final int DIFFICULTY_MEDIO = 2;
    private static final int DIFFICULTY_DIFICIL = 3;

    // Cor de erro — vermelho suave, não agressivo
    private static final int COLOR_INCORRECT = Color.parseColor("#FF6B6B");

    private QuizPresenter presenter;

    private TextView scoreText;
    private TextView chipFacil, chipMedio, chipDificil;
    private View difficultySegment;
    private ProgressBar progressBar;
    private TextView questionCounterText;
    private TextView difficultyLabel;
    private TextView questionText;

    private final View[] optionContainers = new View[4];
    private final TextView[] optionTexts = new TextView[4];
    private final TextView[] optionLabels = new TextView[4];

    private View explanationCard;
    private TextView resultIcon, resultLabel, pointsBadge, explanationText;

    private TextView nextButton;
    private ProgressBar loadingIndicator;

    // Estado do quiz atual
    private Question currentQuestion;       // Pergunta sendo exibida agora
    private boolean answered = false;       // Já respondeu essa pergunta?
    private int selectedOptionIndex = -1;   // Qual opção clicou (-1 = nenhuma)
    private int scoreBeforeAnswer = 0;      // Pontos antes de responder (pra calcular delta)
    private boolean pendingIsCorrect = false;// Acertou ou errou (pra mostrar badge de pontos)
    private boolean isLoadingNext = false;   // Evita clique duplo no "Próxima"
    private Dialog difficultyDialog; // Dialog de dificuldade (dismiss ao navegar)
    private Dialog overlayDialog;    // Dialog do fim do quiz (resultado/continuar/confirmar)
    private boolean dificuldadeEscolhida = false; // Já escolheu nível neste dialog de entrada?

    /**
     * Deixa a barrinha do app clicável por baixo do dialog.
     *
     * FLAG_NOT_TOUCH_MODAL manda os toques de FORA da janela do dialog direto
     * pro app, então a barra de navegação continua funcionando mesmo com o
     * dialog na frente.
     *
     * O toque fora NÃO fecha o dialog: quem sai do quiz é a barrinha do app
     * (pedido do Kaio, 08/10 — só o dialog de Missões é que fecha ao tocar
     * fora). Isso volta ao item 6 do doc, que pedia o quiz SEM fechar ao tocar
     * fora.
     */
    private void manterBarraDoAppClicavel(Dialog dialog) {
        Window w = dialog.getWindow();
        if (w != null) {
            // FLAG_NOT_TOUCH_MODAL: os toques de FORA da janela do dialog vão pro app, então
            // a barrinha de baixo continua clicável — é por ela que o aluno sai do quiz.
            // FLAG_WATCH_OUTSIDE_TOUCH fica DESLIGADA de propósito (clearFlags): é ela que
            // faz o toque de fora fechar o dialog, e o quiz só fecha pela barrinha. (Medido
            // em 07/10: com a flag a janela sobe fl=d840022 e fecha; sem ela não faz nada.)
            w.clearFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
            w.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // [TEI Persistência] DatabaseHelper = SQLiteOpenHelper do slide 06
        // O QuizPresenter grava o progresso (quizzes/sequência) no SQLite
        DatabaseHelper bancoDados = DatabaseHelper.getInstance(requireContext());
        presenter = new QuizPresenter(this, bancoDados, requireContext());

        bindViews(view);
        setupDifficultySegment();
        setupOptionClicks();

        // Esconde todo o conteúdo do quiz até o usuário escolher a dificuldade
        hideQuizContent();

        // [TEI Dialogs] Dialog de seleção de dificuldade — padrão do slide 05.1
        // AlertDialog com as 3 opções sempre visíveis. A janela é limitada pra não
        // cobrir a barra de navegação (ela continua funcionando).
        showDifficultyDialog();

        view.findViewById(R.id.backButton).setOnClickListener(vista ->
                Navigation.findNavController(view).popBackStack());

        nextButton.setOnClickListener(vista -> {
            if (isLoadingNext) return;
            isLoadingNext = true;
            presenter.nextQuestion();
        });
    }

    private void bindViews(View view) {
        scoreText = view.findViewById(R.id.scoreText);

        chipFacil = view.findViewById(R.id.chipFacil);
        chipMedio = view.findViewById(R.id.chipMedio);
        chipDificil = view.findViewById(R.id.chipDificil);
        difficultySegment = view.findViewById(R.id.difficultySegment);

        progressBar = view.findViewById(R.id.progressBar);
        questionCounterText = view.findViewById(R.id.questionCounterText);

        difficultyLabel = view.findViewById(R.id.difficultyLabel);
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
        PressSpring.attach(nextButton);
        PressSpring.attach(view.findViewById(R.id.backButton));
        for (android.view.View opt : optionContainers) {
            if (opt != null) PressSpring.attach(opt);
        }
    }

    private void setupDifficultySegment() {
        chipFacil.setOnClickListener(vista -> selectDifficulty(DIFFICULTY_FACIL));
        chipMedio.setOnClickListener(vista -> selectDifficulty(DIFFICULTY_MEDIO));
        chipDificil.setOnClickListener(vista -> selectDifficulty(DIFFICULTY_DIFICIL));
    }

    /**
     * Seleciona a dificuldade e esconde a barra de seleção.
     * [TEI Manipulando Views] Muda background e cor das opções em tempo real
     * — mesmo conceito dos exercícios 02, só que com 3 chips.
     */

    /** Esconde tudo até o usuário escolher a dificuldade no dialog */
    private void hideQuizContent() {
        // Chips de dificuldade (Fácil/Médio/Difícil) — redundantes com o dialog
        if (difficultySegment != null) difficultySegment.setVisibility(View.GONE);
        // Conteúdo do quiz
        if (questionText != null) questionText.setVisibility(View.INVISIBLE);
        for (View opt : optionContainers) {
            if (opt != null) opt.setVisibility(View.INVISIBLE);
        }
        if (progressBar != null) progressBar.setVisibility(View.INVISIBLE);
        if (questionCounterText != null) questionCounterText.setVisibility(View.INVISIBLE);
        if (difficultyLabel != null) difficultyLabel.setVisibility(View.INVISIBLE);
        if (explanationCard != null) explanationCard.setVisibility(View.GONE);
        if (nextButton != null) nextButton.setVisibility(View.GONE);
    }

    /** Mostra o conteúdo do quiz após escolher a dificuldade */
    private void showQuizContent() {
        if (questionText != null) questionText.setVisibility(View.VISIBLE);
        for (View opt : optionContainers) {
            if (opt != null) opt.setVisibility(View.VISIBLE);
        }
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (questionCounterText != null) questionCounterText.setVisibility(View.VISIBLE);
        if (difficultyLabel != null) difficultyLabel.setVisibility(View.VISIBLE);
    }

    private void selectDifficulty(int difficulty) {
        if (!isAdded()) return;

        // Atualiza visual: chip selecionado fica verde, os outros ficam neutros
        chipFacil.setBackgroundResource(difficulty == DIFFICULTY_FACIL ? R.drawable.chip_active : R.drawable.chip_inactive);
        chipMedio.setBackgroundResource(difficulty == DIFFICULTY_MEDIO ? R.drawable.chip_active : R.drawable.chip_inactive);
        chipDificil.setBackgroundResource(difficulty == DIFFICULTY_DIFICIL ? R.drawable.chip_active : R.drawable.chip_inactive);

        // Texto branco nos dois estados (Kaio, 07/10): agora que o chip é verde
        // igual aos botões, o verde-claro do estado selecionado não dava
        // contraste. Quem marca o escolhido é o anel jade do chip_active.
        chipFacil.setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_on_button));
        chipMedio.setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_on_button));
        chipDificil.setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_on_button));

        // Esconde a barra — o usuário já escolheu no dialog, não precisa ver de novo
        if (difficultySegment != null) difficultySegment.setVisibility(View.GONE);

        // Mostra o conteúdo do quiz
        showQuizContent();

        // Carrega as 10 perguntas dessa dificuldade do banco
        presenter.loadQuestions(null, difficulty);
    }

    /**
     * Configura os cliques nas 4 opções (A, B, C, D).
     * Cada opção tem:
     * - TouchListener: efeito de pressão (scale 0.98 → 1.0)
     * - OnClickListener: abre dialog de confirmação antes de marcar
     *
     * [TEI Manipulando Views] setOnTouchListener + setOnClickListener —
     * mesma técnica dos exercícios 02, só que com animação.
     */
    private void setupOptionClicks() {
        for (int i = 0; i < optionContainers.length; i++) {
            final int index = i;

            // Efeito de pressão no toque (scale down/up)
            optionContainers[i].setOnTouchListener((vista, event) -> {
                if (answered) return false;
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    vista.animate().scaleX(0.98f).scaleY(0.98f).setDuration(90).start();
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP
                        || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                    vista.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                }
                return false;
            });

            // Ao clicar: mostra dialog de confirmação
            optionContainers[i].setOnClickListener(vista -> {
                if (answered) return;
                vista.animate().scaleX(1f).scaleY(1f).setDuration(120).start();

                // Feedback tátil ao tocar na alternativa
                vista.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                optionContainers[index].setBackgroundResource(R.drawable.option_selected);
                String chosen = optionTexts[index].getText().toString();
                showConfirmDialog(chosen, index);
            });
        }
    }

    @Override
    public void showQuestion(Question question, int current, int total) {
        if (!isAdded()) return;
        hideLoading();

        isLoadingNext = false;
        currentQuestion = question;
        answered = false;
        selectedOptionIndex = -1;
        pendingIsCorrect = false;

        resetOptionStyles();
        explanationCard.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        pointsBadge.setVisibility(View.GONE);

        questionCounterText.setText(getString(R.string.quiz_counter_format2, current, total));
        progressBar.setMax(total);
        // current em vez de current-1: na Questão 1 a barra mostra 1/total e o
        // jade neon aparece já na primeira tela (antes ficava em 0 = só o trilho
        // escuro, e o Gemini via a barra como "invisível/travada no vazio")
        progressBar.setProgress(current);

        questionText.setText(question.getQuestionText());
        optionTexts[0].setText(question.getOptionA());
        optionTexts[1].setText(question.getOptionB());
        optionTexts[2].setText(question.getOptionC());
        optionTexts[3].setText(question.getOptionD());

        updateDifficultyLabel(question.getDifficulty());
    }

    private void updateDifficultyLabel(int difficulty) {
        if (difficultyLabel != null) {
            int labelRes = difficulty >= 3 ? R.string.quiz_difficulty_label_3
                    : difficulty >= 2 ? R.string.quiz_difficulty_label_2
                    : R.string.quiz_difficulty_label_1;
            difficultyLabel.setText(labelRes);
        }
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
                pointsBadge.setText(getString(R.string.quiz_points_badge_format, delta));
                pointsBadge.setVisibility(View.VISIBLE);
            } else {
                pointsBadge.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void showExplanation(String explanation, boolean isCorrect) {
        if (!isAdded()) return;
        pendingIsCorrect = isCorrect;

        if (selectedOptionIndex >= 0) {
            optionContainers[selectedOptionIndex].setBackgroundResource(
                    isCorrect ? R.drawable.option_correct : R.drawable.option_incorrect);
            optionLabels[selectedOptionIndex].setText(isCorrect ? R.string.quiz_correct : R.string.quiz_incorrect);
            optionLabels[selectedOptionIndex].setVisibility(View.VISIBLE);
            optionLabels[selectedOptionIndex].setTextColor(
                    isCorrect ? ContextCompat.getColor(requireContext(), R.color.cascavel_jade_bright) : COLOR_INCORRECT);
        }

        if (!isCorrect && currentQuestion != null) {
            int correctIndex = currentQuestion.getCorrectAnswer();
            optionContainers[correctIndex].setBackgroundResource(R.drawable.option_correct);
            optionLabels[correctIndex].setText(R.string.quiz_correct);
            optionLabels[correctIndex].setVisibility(View.VISIBLE);
            optionLabels[correctIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_jade_bright));
        }

        for (View container : optionContainers) {
            container.setClickable(false);
        }

        resultIcon.setText(isCorrect ? "✓" : "✗");
        resultIcon.setTextColor(
                isCorrect ? ContextCompat.getColor(requireContext(), R.color.cascavel_jade_bright) : COLOR_INCORRECT);
        resultLabel.setText(isCorrect ? R.string.quiz_result_correct : R.string.quiz_result_incorrect);
        explanationText.setText(explanation);

        explanationCard.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showResult(int score, int total, boolean passed) {
        if (!isAdded()) return;
        // O contador do nível (QUIZ_FACIL/MEDIO/DIFICIL) é gravado pelo QuizPresenter ao
        // terminar o quiz. Antes era gravado AQUI de novo quando o aluno passava: o quiz
        // contava 2x e as missões ("3 quizzes do fácil") avançavam dobrado.
        showQuizResultDialog(score, total, passed);
    }

    /**
     * Dialog de confirmação antes de enviar resposta.
     * [TEI Dialogs] Dialog customizado com layout próprio (slide 05.1)
     * — mesmo padrão do prof. Alexandre, só que com visual glassmorphism.
     */
    private void showConfirmDialog(String chosenAnswer, int optionIndex) {
        if (!isAdded() || getContext() == null) return;
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_quiz_confirm);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        // Tocar fora NÃO fecha (pedido do Kaio, 08/10): quem sai do quiz é a barrinha do
        // app, que fica clicável por baixo (ver manterBarraDoAppClicavel). O botão
        // Cancelar continua devolvendo a alternativa ao estado anterior.
        dialog.setCanceledOnTouchOutside(false);
        manterBarraDoAppClicavel(dialog);
        overlayDialog = dialog;
        dialog.setOnDismissListener(d -> { if (overlayDialog == dialog) overlayDialog = null; });
        dialog.setOnCancelListener(d -> limparSelecaoDasOpcoes());

        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
        dialogMessage.setText(getString(R.string.quiz_confirm_message, chosenAnswer));

        dialog.findViewById(R.id.btnConfirm).setOnClickListener(vista -> {
            dialog.dismiss();
            if (!isAdded()) return;
            answered = true;
            if (difficultySegment != null) difficultySegment.setVisibility(View.GONE);
            selectedOptionIndex = optionIndex;
            scoreBeforeAnswer = presenter.getCurrentScore();
            presenter.selectAnswer(optionIndex);
        });

        dialog.findViewById(R.id.btnCancel).setOnClickListener(vista -> {
            dialog.dismiss();
            limparSelecaoDasOpcoes();
        });

        dialog.show();
    }

    /** Tira a marca de "escolhida" das 4 alternativas (cancelar/voltar atrás). */
    private void limparSelecaoDasOpcoes() {
        for (View container : optionContainers) {
            if (container != null) container.setBackgroundResource(R.drawable.option_default);
        }
        selectedOptionIndex = -1;
    }

    /**
     * Dialog de seleção de dificuldade (entrada do quiz).
     *
     * AlertDialog normal (como sempre foi), com as 3 opções sempre visíveis.
     * O tamanho do card não muda. A única diferença: a janela do dialog é
     * limitada até o topo da barra de navegação, então a navbar continua
     * clicável (trocar de aba fecha o dialog via onDestroyView).
     */
    private void showDifficultyDialog() {
        if (!isAdded() || getContext() == null) return;

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_difficulty);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        // Tocar fora NÃO fecha (pedido do Kaio, 08/10). Continua cancelável pelo botão
        // VOLTAR: aí sim desiste do quiz e volta pra tela anterior, em vez de deixar o
        // quiz em branco. Quem SAI pela barrinha do app não passa por aqui: é dismiss().
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        TextView dialogEasy = dialog.findViewById(R.id.dialogEasy);
        TextView dialogMedium = dialog.findViewById(R.id.dialogMedium);
        TextView dialogHard = dialog.findViewById(R.id.dialogHard);

        if (dialogEasy != null) dialogEasy.setOnClickListener(vista -> {
            dificuldadeEscolhida = true;
            dialog.dismiss();
            selectDifficulty(DIFFICULTY_FACIL);
        });
        if (dialogMedium != null) dialogMedium.setOnClickListener(vista -> {
            dificuldadeEscolhida = true;
            dialog.dismiss();
            selectDifficulty(DIFFICULTY_MEDIO);
        });
        if (dialogHard != null) dialogHard.setOnClickListener(vista -> {
            dificuldadeEscolhida = true;
            dialog.dismiss();
            selectDifficulty(DIFFICULTY_DIFICIL);
        });

        difficultyDialog = dialog;
        dificuldadeEscolhida = false;
        dialog.setOnDismissListener(d -> {
            if (difficultyDialog == dialog) difficultyDialog = null;
        });
        dialog.setOnCancelListener(d -> {
            if (dificuldadeEscolhida || !isAdded()) return;
            Navigation.findNavController(requireView()).popBackStack();
        });

        // AlertDialog padrão, centralizado na tela (como sempre foi).
        // A barrinha do app continua clicável (ver manterBarraDoAppClicavel).
        manterBarraDoAppClicavel(dialog);
        dialog.show();
    }

    /**
     * Dialog "Quer continuar?" — aparece após resultado do quiz.
     * Sempre mostra as 3 dificuldades (Fácil/Médio/Difícil) — não esconde
     * os níveis já concluídos.
     */
    private void showContinueDialog() {
        if (!isAdded() || getContext() == null) return;

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_difficulty);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        // Tocar fora NÃO fecha (pedido do Kaio, 08/10); a barrinha do app
        // continua clicável por baixo.
        dialog.setCanceledOnTouchOutside(false);
        manterBarraDoAppClicavel(dialog);
        overlayDialog = dialog;
        dialog.setOnDismissListener(d -> { if (overlayDialog == dialog) overlayDialog = null; });

        TextView dialogEasy = dialog.findViewById(R.id.dialogEasy);
        TextView dialogMedium = dialog.findViewById(R.id.dialogMedium);
        TextView dialogHard = dialog.findViewById(R.id.dialogHard);

        if (dialogEasy != null) dialogEasy.setOnClickListener(vista -> {
            dialog.dismiss();
            selectDifficulty(DIFFICULTY_FACIL);
        });
        if (dialogMedium != null) dialogMedium.setOnClickListener(vista -> {
            dialog.dismiss();
            selectDifficulty(DIFFICULTY_MEDIO);
        });
        if (dialogHard != null) dialogHard.setOnClickListener(vista -> {
            dialog.dismiss();
            selectDifficulty(DIFFICULTY_DIFICIL);
        });

        dialog.show();
    }

    /**
     * Dialog de resultado final do quiz (aprovado/reprovado).
     * [TEI Dialogs] Dialog customizado — exatamente o "diálogo customizado"
     * do slide 05.1 do prof. Alexandre.
     */
    private void showQuizResultDialog(int score, int total, boolean passed) {
        if (!isAdded() || getContext() == null) return;
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_quiz_result);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        // Tocar fora NÃO fecha (pedido do Kaio, 08/10); sai pela barrinha do app
        // ou pelo botão do próprio dialog.
        dialog.setCanceledOnTouchOutside(false);
        manterBarraDoAppClicavel(dialog);
        overlayDialog = dialog;
        dialog.setOnDismissListener(d -> { if (overlayDialog == dialog) overlayDialog = null; });

        TextView resultIcon = dialog.findViewById(R.id.dialogResultIcon);
        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        TextView dialogScore = dialog.findViewById(R.id.dialogScore);
        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);

        resultIcon.setText(passed ? "✓" : "✗");
        resultIcon.setTextColor(passed
                ? ContextCompat.getColor(requireContext(), R.color.cascavel_jade_bright)
                : ContextCompat.getColor(requireContext(), R.color.cascavel_wrong));

        dialogTitle.setText(passed ? R.string.quiz_result_passed_title : R.string.quiz_result_failed_title);
        dialogScore.setText(String.format(Locale.getDefault(), "%d / %d", score, total));
        dialogMessage.setText(passed ? R.string.quiz_result_passed_msg : R.string.quiz_result_failed_msg);

        dialog.findViewById(R.id.dialogCloseButton).setOnClickListener(vista -> {
            dialog.dismiss();
            // Apos fechar resultado, mostra dialog de continuar
            showContinueDialog();
        });

        dialog.show();
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
        if (!isAdded()) return;
        hideLoading();
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        // Fecha o dialog de dificuldade se o usuário trocou de aba pelo nav
        if (difficultyDialog != null) {
            difficultyDialog.dismiss();
            difficultyDialog = null;
        }
        // Mesma coisa pro dialog do fim do quiz (resultado/continuar): antes ele
        // ficava na frente da tela nova quando o usuário saía pela barra do app,
        // e a tela toda parecia congelada.
        if (overlayDialog != null) {
            overlayDialog.dismiss();
            overlayDialog = null;
        }
        super.onDestroyView();
        presenter = null;
        currentQuestion = null;
    }
}
