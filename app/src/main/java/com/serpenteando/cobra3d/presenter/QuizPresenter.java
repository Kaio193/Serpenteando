package com.serpenteando.cobra3d.presenter;

import android.content.Context;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.contract.QuizContract;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.model.entity.Question;
import com.serpenteando.cobra3d.model.repository.ProgressRepository;
import com.serpenteando.cobra3d.model.repository.QuestionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Presenter do Quiz — cuida da lógica do jogo.
 *
 * [ALÉM DO TEI] Padrão MVP:
 * - Carrega perguntas do banco por dificuldade
 * - Embaralha (Collections.shuffle)
 * - Verifica resposta (certo/errado)
 * - Calcula score e salva progresso
 * - Finaliza quiz e mostra resultado
 *
 * [ALÉM DO TEI] Repository Pattern:
 * - QuestionRepository: busca perguntas no SQLite
 * - ProgressRepository: salva score e nível concluído
 */
public class QuizPresenter implements QuizContract.Presenter {

    private final QuizContract.View view;
    private final Context context;
    private final QuestionRepository questionRepository;
    private final ProgressRepository progressRepository;
    private List<Question> questions;  // Perguntas do quiz atual
    private int currentIndex;          // Índice da pergunta atual
    private int score;                 // Pontuação atual
    private int selectedAnswer;        // Opção escolhida (-1 = nenhuma)
    private boolean answered;          // Já respondeu essa pergunta?
    private int currentDifficulty;     // Dificuldade atual (1/2/3)
    private boolean finished;          // Quiz já fechado? (protege contra contar 2x)

    public QuizPresenter(QuizContract.View view, DatabaseHelper dbHelper, Context context) {
        this.view = view;
        this.context = context.getApplicationContext();
        this.questionRepository = new QuestionRepository(dbHelper.getWritableDatabase());
        this.progressRepository = new ProgressRepository(dbHelper.getWritableDatabase());
        this.questions = new ArrayList<>();
        this.currentIndex = 0;
        this.score = 0;
        this.selectedAnswer = -1;
        this.answered = false;
        this.finished = false;
    }

    /**
     * Carrega perguntas do banco baseado na dificuldade.
     * Embaralha pra não ser sempre a mesma ordem.
     */
    @Override
    public void loadQuestions(String category, int difficulty) {
        view.showLoading();
        if (category == null || category.isEmpty()) {
            questions = difficulty > 0
                    ? questionRepository.getByDifficulty(difficulty)
                    : questionRepository.getAll();
        } else if (difficulty <= 0) {
            questions = questionRepository.getByCategory(category);
        } else {
            questions = questionRepository.getByCategoryAndDifficulty(category, difficulty);
        }
        this.currentDifficulty = difficulty;
        Collections.shuffle(questions);  // Embaralha as perguntas
        currentIndex = 0;
        score = 0;
        finished = false;                // Quiz novo: libera fechar de novo
        view.hideLoading();

        if (questions.isEmpty()) {
            view.showError(context.getString(R.string.error_no_question));
            return;
        }

        view.showQuestion(questions.get(currentIndex), currentIndex + 1, questions.size());
    }

    /**
     * Processa a resposta do usuário.
     * Compara com a resposta correta e atualiza score.
     */
    @Override
    public void selectAnswer(int answerIndex) {
        if (answered || questions.isEmpty()) return;
        selectedAnswer = answerIndex;
        answered = true;

        Question current = questions.get(currentIndex);
        boolean isCorrect = (selectedAnswer == current.getCorrectAnswer());
        if (isCorrect) {
            score++;
        }
        view.showExplanation(current.getExplanation(), isCorrect);
        view.showScore(score);
    }

    @Override
    public void nextQuestion() {
        if (questions.isEmpty()) return;
        // Depois de terminado, "Próxima" não faz mais nada. Sem essa trava, um toque
        // por baixo do dialog de resultado chamava o finishQuiz() de novo: o quiz era
        // contado 2x no progresso e aparecia um segundo alerta (o "congelamento" do fim).
        if (finished) return;
        currentIndex++;
        selectedAnswer = -1;
        answered = false;

        if (currentIndex >= questions.size()) {
            finishQuiz();
        } else {
            view.showQuestion(questions.get(currentIndex), currentIndex + 1, questions.size());
        }
    }

    /**
     * Finaliza o quiz: salva progresso e mostra resultado.
     * Aprovado se acertou >= 60% das perguntas.
     * Salva o nível concluído pra não aparecer de novo no dialog.
     */
    @Override
    public void finishQuiz() {
        if (finished) return;   // Já salvou e já mostrou o resultado: não repete
        finished = true;
        boolean passed = !questions.isEmpty() && score >= (questions.size() * 0.6);
        // Recorde por nível: passa a dificuldade pra pontuação ser independente
        progressRepository.updateQuizScore(score, currentDifficulty);
        // Registra qual dificuldade foi completada
        String trigger;
        switch (currentDifficulty) {
            case 2: trigger = "QUIZ_MEDIO"; break;
            case 3: trigger = "QUIZ_DIFICIL"; break;
            default: trigger = "QUIZ_FACIL"; break;
        }
        progressRepository.incrementEvent(trigger);
        view.showResult(score, questions.size(), passed);
    }

    @Override
    public int getCurrentScore() {
        return score;
    }

    @Override
    public int getTotalQuestions() {
        return questions.size();
    }

    @Override
    public int getCurrentDifficulty() {
        return currentDifficulty;
    }
}
