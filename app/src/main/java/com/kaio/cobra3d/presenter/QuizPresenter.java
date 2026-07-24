package com.kaio.cobra3d.presenter;

import com.kaio.cobra3d.contract.QuizContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Question;
import com.kaio.cobra3d.model.repository.ProgressRepository;
import com.kaio.cobra3d.model.repository.QuestionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizPresenter implements QuizContract.Presenter {

    private final QuizContract.View view;
    private final QuestionRepository questionRepository;
    private final ProgressRepository progressRepository;
    private List<Question> questions;
    private int currentIndex;
    private int score;
    private int selectedAnswer;
    private boolean answered;

    public QuizPresenter(QuizContract.View view, DatabaseHelper dbHelper) {
        this.view = view;
        this.questionRepository = new QuestionRepository(dbHelper.getWritableDatabase());
        this.progressRepository = new ProgressRepository(dbHelper.getWritableDatabase());
        this.questions = new ArrayList<>();
        this.currentIndex = 0;
        this.score = 0;
        this.selectedAnswer = -1;
        this.answered = false;
    }

    @Override
    public void loadQuestions(String category, int difficulty) {
        view.showLoading();
        if (category == null || category.isEmpty()) {
            questions = questionRepository.getAll();
        } else if (difficulty <= 0) {
            questions = questionRepository.getByCategory(category);
        } else {
            questions = questionRepository.getByCategoryAndDifficulty(category, difficulty);
        }
        Collections.shuffle(questions);
        currentIndex = 0;
        score = 0;
        view.hideLoading();

        if (questions.isEmpty()) {
            view.showError("Nenhuma pergunta encontrada");
            return;
        }

        view.showQuestion(questions.get(currentIndex), currentIndex + 1, questions.size());
    }

    @Override
    public void selectAnswer(int answerIndex) {
        if (answered) return;
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
        currentIndex++;
        selectedAnswer = -1;
        answered = false;

        if (currentIndex >= questions.size()) {
            finishQuiz();
        } else {
            view.showQuestion(questions.get(currentIndex), currentIndex + 1, questions.size());
        }
    }

    @Override
    public void finishQuiz() {
        boolean passed = score >= (questions.size() * 0.6);
        progressRepository.updateQuizScore(score);
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
}
