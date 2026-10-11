package com.kaio.cobra3d.contract;

import com.kaio.cobra3d.model.entity.Question;


/**
 * Contrato MVP pra Tela do Quiz.
 *
 * [ALÉM DO TEI] Mesma ideia do HomeContract:
 * - View: mostrar perguntas, score, resultado, loading, erro
 * - Presenter: carregar perguntas, selecionar resposta, próxima, finalizar
 */
public interface QuizContract {

    // O que a tela do quiz precisa conseguir fazer
    interface View {
        void showQuestion(Question question, int current, int total);
        void showScore(int score);
        void showResult(int score, int total, boolean passed);
        void showExplanation(String explanation, boolean isCorrect);
        void showLoading();
        void hideLoading();
        void showError(String message);
    }

    // O que o presenter do quiz precisa conseguir fazer
    interface Presenter {
        void loadQuestions(String category, int difficulty);
        void selectAnswer(int answerIndex);
        void nextQuestion();
        void finishQuiz();
        int getCurrentScore();
        int getTotalQuestions();
        int getCurrentDifficulty();
    }
}
