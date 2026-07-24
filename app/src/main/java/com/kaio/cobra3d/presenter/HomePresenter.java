package com.kaio.cobra3d.presenter;

import com.kaio.cobra3d.contract.HomeContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Tip;
import com.kaio.cobra3d.model.entity.UserProgress;
import com.kaio.cobra3d.model.repository.ProgressRepository;
import com.kaio.cobra3d.model.repository.TipRepository;

// [ALÉM DO TEI] MVP (Presenter) — isola a lógica de negócio da UI, facilita testes unitários
public class HomePresenter implements HomeContract.Presenter {

    private final HomeContract.View view;
    private final TipRepository tipRepository;
    // [ALÉM DO TEI] Repository Pattern — acesso a dados isolado; trocar fonte sem mudar o presenter
    private final ProgressRepository progressRepository;

    public HomePresenter(HomeContract.View view, DatabaseHelper dbHelper) {
        this.view = view;
        this.tipRepository = new TipRepository(dbHelper.getWritableDatabase());
        this.progressRepository = new ProgressRepository(dbHelper.getWritableDatabase());
    }

    @Override
    public void loadTip() {
        Tip tip = tipRepository.getRandom();
        if (tip != null) {
            view.showTip(tip);
        } else {
            view.showError("Nenhuma dica disponível");
        }
    }

    @Override
    public void loadProgress() {
        UserProgress progress = progressRepository.getProgress();
        view.showProgress(progress);
    }

    @Override
    public void onQuizClicked() {
        view.showQuiz();
    }

    @Override
    public void onMissionsClicked() {
        view.showMissions();
    }

    @Override
    public void onAnatomy3DClicked() {
        view.showAnatomy3D();
    }

    @Override
    public void onContentClicked(String category) {
        view.showContent(category);
    }

    @Override
    public void onMythsTruthsClicked() {
        view.showMythsTruths();
    }
}
