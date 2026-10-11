package com.kaio.cobra3d.contract;

import com.kaio.cobra3d.model.entity.Tip;
import com.kaio.cobra3d.model.entity.UserProgress;

/**
 * Contrato MVP pra Tela Inicial.
 *
 * [ALÉM DO TEI] Padrão MVP (Model-View-Presenter):
 * - View: o que a tela PODE fazer (mostrar dica, navegar, mostrar erro)
 * - Presenter: o que a lógica PODE fazer (carregar dados, tratar cliques)
 *
 * O slide do prof. Alexandre usa Activity direta (tudo junto).
 * O MVP separa: a View não sabe como os dados são carregados,
 * e o Presenter não sabe como exibir na tela.
 * Vantagem: facilita testes unitários (mocka a View).
 */
public interface HomeContract {

    // O que a tela precisa conseguir fazer
    interface View {
        void showTip(Tip tip);
        void showProgress(UserProgress progress);
        void showContent(String category);
        void showQuiz();
        void showMissions();
        void showAnatomy3D();
        void showMythsTruths();
        void showError(String message);
    }

    // O que o presenter precisa conseguir fazer
    interface Presenter {
        void loadTip();
        void loadProgress();
        void onQuizClicked();
        void onMissionsClicked();
        void onAnatomy3DClicked();
        void onContentClicked(String category);
        void onMythsTruthsClicked();
    }
}
