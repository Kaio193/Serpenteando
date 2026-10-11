package com.kaio.cobra3d.presenter;

import com.kaio.cobra3d.contract.HomeContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Tip;
import com.kaio.cobra3d.model.entity.UserProgress;
import com.kaio.cobra3d.model.repository.ProgressRepository;
import com.kaio.cobra3d.model.repository.TipRepository;

/**
 * Presenter da Tela Inicial — cuida da lógica de negócio.
 *
 * [ALÉM DO TEI] Padrão MVP:
 * - Carrega dica do dia do banco
 * - Carrega progresso do usuário (pontos, missões, streak)
 * - Redireciona pra outras telas quando o usuário clica nos cards
 *
 * [ALÉM DO TEI] Repository Pattern:
 * - TipRepository: busca dicas no SQLite
 * - ProgressRepository: busca/salva progresso no SQLite
 * - Isola acesso a dados — se mudar o banco, só muda o Repository
 */
public class HomePresenter implements HomeContract.Presenter {

    private final HomeContract.View view;
    private final TipRepository tipRepository;
    private final ProgressRepository progressRepository;

    public HomePresenter(HomeContract.View view, DatabaseHelper dbHelper) {
        this.view = view;
        this.tipRepository = new TipRepository(dbHelper.getWritableDatabase());
        this.progressRepository = new ProgressRepository(dbHelper.getWritableDatabase());
    }

    @Override
    public void loadTip() {
        Tip tip = tipRepository.getDailyTip();
        if (tip != null) {
            view.showTip(tip);
        }
        // Não mostra Toast quando não há dicas — o texto fica em branco
        // sem poluir a tela com mensagem de erro
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
