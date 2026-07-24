package com.kaio.cobra3d.contract;

import com.kaio.cobra3d.model.entity.Tip;
import com.kaio.cobra3d.model.entity.UserProgress;

// [ALÉM DO TEI] MVP — contrato separa responsabilidades: View não conhece a implementação do Presenter
public interface HomeContract {

    // [ALÉM DO TEI] Interface View — qualquer Fragment que implemente este contrato pode ser substituído por um mock em testes
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

    // [ALÉM DO TEI] Interface Presenter — a View invoca apenas métodos do contrato, sem acoplamento à implementação concreta
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
