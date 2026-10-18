package com.serpenteando.cobra3d.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.contract.HomeContract;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.model.entity.Tip;
import com.serpenteando.cobra3d.model.entity.UserProgress;
import com.serpenteando.cobra3d.presenter.HomePresenter;
import com.serpenteando.cobra3d.util.PressSpring;

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
        view.findViewById(R.id.exploreQuizCard).setOnClickListener(vista -> presenter.onQuizClicked());
        view.findViewById(R.id.exploreMythsCard).setOnClickListener(vista -> presenter.onMythsTruthsClicked());
        view.findViewById(R.id.exploreAnatomyCard).setOnClickListener(vista -> presenter.onAnatomy3DClicked());
        view.findViewById(R.id.exploreContentCard).setOnClickListener(vista -> presenter.onContentClicked("todas"));

        // Efeito de pressão nos cards (spring animation)
        // Não é do slide do prof. — é um detalhe visual premium
        PressSpring.attach(view.findViewById(R.id.exploreQuizCard));
        PressSpring.attach(view.findViewById(R.id.exploreMythsCard));
        PressSpring.attach(view.findViewById(R.id.exploreAnatomyCard));
        PressSpring.attach(view.findViewById(R.id.exploreContentCard));

        // Enquadramento da arte do card de boas-vindas
        ajustarArteDoCard(view);

        // Carrega dados do banco: dica do dia + progresso do usuário
        presenter.loadTip();
        presenter.loadProgress();
    }

    /**
     * Ajusta o enquadramento da arte que fica atrás da frase do card de boas-vindas.
     *
     * A arte (barra clara no modo claro / barra escura no escuro) é mais larga que o
     * card, então o centerCrop corta os dois lados dela por igual — o desenho da cobra
     * fica no lado direito da arte e esse corte comia a ponta do desenho (Kaio, 12/10:
     * "as imagens estão deslocadas pra direita e parte delas fica cortada").
     *
     * Aqui a arte é desenhada com a mesma escala do centerCrop, só que andando pra
     * esquerda até a borda direita encostar na borda direita do card. Assim o corte sai
     * inteiro do lado esquerdo (que é fundo liso) e o desenho aparece completo.
     * A conta é feita no código, e não fixa no XML, porque a largura do card muda de
     * celular pra celular: em tela mais larga a sobra é menor, e assim nunca sobra
     * buraco do lado direito.
     */
    private void ajustarArteDoCard(View view) {
        ImageView arte = view.findViewById(R.id.heroBannerImage);

        // post: só dá pra medir o tamanho da imagem depois que a tela foi desenhada
        arte.post(() -> {
            Drawable desenho = arte.getDrawable();
            int larguraTela = arte.getWidth();
            int alturaTela = arte.getHeight();

            // Sem desenho (ou antes de medir) não faz nada: fica o centerCrop do XML
            if (desenho == null || larguraTela == 0 || alturaTela == 0) return;

            // Mesma escala que o centerCrop usa: a maior entre largura e altura
            float escala = Math.max((float) larguraTela / desenho.getIntrinsicWidth(),
                    (float) alturaTela / desenho.getIntrinsicHeight());

            // Quanto a arte passa do card em cada eixo (é o que o centerCrop corta)
            float sobraX = desenho.getIntrinsicWidth() * escala - larguraTela;
            float sobraY = desenho.getIntrinsicHeight() * escala - alturaTela;

            // Arte mais estreita que o card: não tem corte lateral, nada pra ajustar
            if (sobraX <= 4f) return;

            // A arte termina com uma ponta transparente (a clara tem ~9px, a escura nenhum).
            // Essa ponta precisa cair FORA do card, senão aparecem pixels do fundo do card
            // na borda direita — no modo claro virava uma faixinha clara de ~5px (print 12/10).
            float pontaTransparente = transparenciaNoFim(desenho, escala);
            float folga = pontaTransparente + 4f;   // 4px (~1,5dp) de segurança

            // Enquadramento do centerCrop (centralizado) andando pra esquerda, sem
            // deixar buraco na direita: a arte sai no máximo até encostar na borda
            float deslocamento = Math.max(0f, sobraX / 2f - folga);

            Matrix matriz = new Matrix();
            matriz.setScale(escala, escala);
            matriz.postTranslate(-sobraX / 2f - deslocamento, -sobraY / 2f);

            arte.setScaleType(ImageView.ScaleType.MATRIX);
            arte.setImageMatrix(matriz);
        });
    }

    /**
     * Mede quantos pixels desenhados a arte tem de transparência na ponta direita.
     * Lê uma linha do meio da imagem e conta, da direita pra esquerda, quantos pixels
     * estão transparentes (alpha < 250). Serve pra saber de quanto precisa ser a folga
     * de segurança — se a arte for trocada por outra com fade maior, a conta acompanha.
     */
    private float transparenciaNoFim(Drawable desenho, float escala) {
        if (!(desenho instanceof BitmapDrawable)) return 0f;
        try {
            Bitmap mapa = ((BitmapDrawable) desenho).getBitmap();
            int largura = mapa.getWidth();
            int larguraDrawable = desenho.getIntrinsicWidth();
            if (largura == 0 || larguraDrawable == 0) return 4f;

            int[] linha = new int[largura];
            mapa.getPixels(linha, 0, largura, 0, mapa.getHeight() / 2, largura, 1);

            int fim = largura - 1;
            while (fim > 0 && Color.alpha(linha[fim]) < 250) fim--;

            // O tamanho que o drawable usa pra desenhar não é o do bitmap: o Android
            // ajusta pela densidade da tela. Converte os pixels do bitmap pro tamanho
            // do drawable antes de aplicar a escala do card.
            float fator = (float) larguraDrawable / largura;
            return (largura - 1 - fim) * fator * escala;
        } catch (Exception e) {
            // Qualquer surpresa aqui só vira folga a mais: nunca quebra a tela
            return 4f;
        }
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
        // Card de pontuação: mostra apenas o total acumulado de questoes acertadas nos quizzes.
        // Cada acerto conta 1, somado ao total existente; sai e volta ao app nao zera.
        int totalAcertos = progress != null ? progress.getTotalQuestionsCorrect() : 0;
        int missions = progress != null ? progress.getMissionsCompleted() : 0;
        int streak = progress != null ? progress.getCurrentStreak() : 0;
        if (progressScoreText != null) progressScoreText.setText(String.valueOf(totalAcertos));
        if (progressMissionsText != null) progressMissionsText.setText(String.valueOf(missions));
        if (progressStreakText != null) progressStreakText.setText(String.valueOf(streak));
    }

    // Navegação entre telas usando Navigation Component
    // [TEI Intents] O prof. usa startActivity(new Intent(...)) no slide 05; aqui é
    // NavGraph, que é o padrão pra navegar entre Fragments (Activity + Fragments).

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
        // Usa Filament pra renderizar a cobra
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
