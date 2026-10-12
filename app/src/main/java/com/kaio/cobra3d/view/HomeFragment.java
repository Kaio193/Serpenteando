package com.kaio.cobra3d.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.contract.HomeContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Tip;
import com.kaio.cobra3d.model.entity.UserProgress;
import com.kaio.cobra3d.presenter.HomePresenter;
import com.kaio.cobra3d.util.PressSpring;

/**
 * Tela inicial do Serpenteando.
 *
 * Mostra: saudação, pontuação, missões, sequência, cards de navegação e dica do dia.
 * Usa MVP (HomeContract.View + HomePresenter) — padrão diferente do slide do
 * prof. Alexandre (que usa Activity direta), mas mesma ideia: separar lógica da tela.
 *
 * [TEI Manipulando Views] findViewById + setText + setOnClickListener — mesmas
 * técnicas dos exercícios 02, só que em Fragment em vez de Activity.
 */
public class HomeFragment extends Fragment implements HomeContract.View {

    // Presenter cuida da lógica (carregar dica, progresso, navegação)
    private HomePresenter presenter;

    // Views da tela
    private TextView tipText;              // Texto da dica do dia
    private TextView progressScoreText;    // Pontuação total
    private TextView progressMissionsText; // Missões concluídas
    private TextView progressStreakText;   // Sequência de dias

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cria o presenter com referência a esta view e ao banco de dados
        // [TEI Persistência] DatabaseHelper é o SQLiteOpenHelper do slide 06
        presenter = new HomePresenter(this, DatabaseHelper.getInstance(requireContext()));

        tipText = view.findViewById(R.id.tipText);
        progressScoreText = view.findViewById(R.id.progressScoreText);
        progressMissionsText = view.findViewById(R.id.progressMissionsText);
        progressStreakText = view.findViewById(R.id.progressStreakText);

        // Cards de navegação — cada um leva pra uma tela diferente
        // Usa Navigation Component (não Intent como no slide 05 do prof.)
        // O prof. usa startActivity; aqui usamos NavGraph porque o app tem várias telas
        view.findViewById(R.id.exploreQuizCard).setOnClickListener(v -> presenter.onQuizClicked());
        view.findViewById(R.id.exploreMythsCard).setOnClickListener(v -> presenter.onMythsTruthsClicked());
        view.findViewById(R.id.exploreAnatomyCard).setOnClickListener(v -> presenter.onAnatomy3DClicked());
        view.findViewById(R.id.exploreContentCard).setOnClickListener(v -> presenter.onContentClicked("todas"));

        // Efeito de pressão nos cards (spring animation)
        // Não é do slide do prof. — é um detalhe visual premium
        PressSpring.attach(view.findViewById(R.id.exploreQuizCard));
        PressSpring.attach(view.findViewById(R.id.exploreMythsCard));
        PressSpring.attach(view.findViewById(R.id.exploreAnatomyCard));
        PressSpring.attach(view.findViewById(R.id.exploreContentCard));

        // Carrega dados do banco: dica do dia + progresso do usuário
        presenter.loadTip();
        presenter.loadProgress();
    }

    /**
     * Relê o progresso toda vez que a Home volta a aparecer na tela.
     *
     * O onViewCreated só roda uma vez: se o usuário concluísse uma missão ou um
     * quiz e voltasse pra Home, os números (pontos, missões, sequência) ficavam
     * os antigos — mesmo com tudo já gravado no SQLite. Agora a Home relê o banco
     * no onResume, que é chamado exatamente nessa volta.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.loadProgress();
        }
    }

    @Override
    public void showTip(Tip tip) {
        if (tip != null && tipText != null) {
            tipText.setText(tip.getTipText());
        }
    }

    @Override
    public void showProgress(UserProgress progress) {
        int score = progress != null ? progress.getQuizHighScore() : 0;
        int missions = progress != null ? progress.getMissionsCompleted() : 0;
        int streak = progress != null ? progress.getCurrentStreak() : 0;
        if (progressScoreText != null) progressScoreText.setText(String.valueOf(score));
        if (progressMissionsText != null) progressMissionsText.setText(String.valueOf(missions));
        if (progressStreakText != null) progressStreakText.setText(String.valueOf(streak));
    }

    // Navegação entre telas usando Navigation Component
    // [TEI Intents] No slide 05, o prof. usa startActivity(new Intent(...))
    // Aqui usamos NavGraph porque é o padrão moderno pra navegar entre Fragments
    // (Activity única + vários Fragments = melhor performance)

    @Override
    public void showContent(String category) {
        Bundle args = new Bundle();
        args.putString("category", category);
        Navigation.findNavController(requireView())
                .navigate(R.id.nav_content, args);
    }

    @Override
    public void showQuiz() {
        Navigation.findNavController(requireView())
                .navigate(R.id.nav_quiz);
    }

    @Override
    public void showMissions() {
        Navigation.findNavController(requireView())
                .navigate(R.id.nav_missions);
    }

    @Override
    public void showAnatomy3D() {
        // Tela do 3D — inovadora, o professor vai perguntar!
        // Usa Filament (engine 3D do Google) pra renderizar a cobra
        Navigation.findNavController(requireView())
                .navigate(R.id.nav_anatomy);
    }

    @Override
    public void showMythsTruths() {
        Navigation.findNavController(requireView())
                .navigate(R.id.nav_myth_truth);
    }

    @Override
    public void showError(String message) {
        if (!isAdded() || getContext() == null) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Limpa referências quando a tela é destruída.
     * IMPORTANTE: sem isso, o app trava (memory leak) — o Fragment tenta
     * acessar views que já não existem mais.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter = null;
        tipText = null;
        progressScoreText = null;
        progressMissionsText = null;
        progressStreakText = null;
    }
}
