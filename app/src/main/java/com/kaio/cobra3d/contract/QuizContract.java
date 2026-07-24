package com.kaio.cobra3d.contract;

import com.kaio.cobra3d.model.entity.Question;

import java.util.List;

public interface QuizContract {

    interface View {
        void showQuestion(Question question, int current, int total);
        void showScore(int score);
        void showResult(int score, int total, boolean passed);
        void showExplanation(String explanation, boolean isCorrect);
        void showLoading();
        void hideLoading();
        void showError(String message);
    }

    interface Presenter {
        void loadQuestions(String category, int difficulty);
        void selectAnswer(int answerIndex);
        void nextQuestion();
        void finishQuiz();
        int getCurrentScore();
        int getTotalQuestions();
    }
}
