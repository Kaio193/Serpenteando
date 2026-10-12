package com.kaio.cobra3d.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.adapter.MythPagerAdapter;

/**
 * Tela de Mitos e Verdades — cabeçalho + filtros + ViewPager2.
 *
 * [ALÉM DO TEI] Mesmo padrão do Conteúdo Educativo (ContentListFragment):
 * o usuário pode ARRASTAR a lista pra trocar de filtro, ou tocar nos chips.
 *
 * Funcionamento:
 * - 3 filtros TextView (Todos, Mitos, Verdades)
 * - ViewPager2 gerencia o swipe entre as abas
 * - FragmentStateAdapter cria/destrói as abas conforme o arrasto
 *
 * [TEI Persistência] Cada aba lê do SQLite pelo seu próprio MythTruthPresenter.
 */
public class MythTruthFragment extends Fragment {

    private ViewPager2 viewPager;
    private TextView filterAll, filterMyths, filterTruths;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_myth_truth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Botão voltar
        view.findViewById(R.id.backButton).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        // ViewPager2 (swipe entre Todos / Mitos / Verdades)
        viewPager = view.findViewById(R.id.mythViewPager);

        // Filtros
        filterAll = view.findViewById(R.id.filterAll);
        filterMyths = view.findViewById(R.id.filterMyths);
        filterTruths = view.findViewById(R.id.filterTruths);

        // Adapter do ViewPager2 — cria um fragment por aba
        viewPager.setAdapter(new MythPagerAdapter(requireActivity()));

        // Mantém as 3 abas vivas: trocar de aba não recarrega a lista
        viewPager.setOffscreenPageLimit(2);

        // SEM swipe horizontal (Kaio, 11/10 — item 17): o ViewPager2 continua
        // existindo (é ele que guarda as 3 abas e troca na hora do toque no
        // chip), mas o dedo não arrasta mais a lista de um lado pro outro.
        // A rolagem VERTICAL da lista continua funcionando normalmente.
        viewPager.setUserInputEnabled(false);

        // Clique nos filtros → troca de aba no ViewPager2
        filterAll.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        filterMyths.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        filterTruths.setOnClickListener(v -> viewPager.setCurrentItem(2, true));

        // Atualiza o visual do filtro selecionado quando troca de aba (swipe)
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateFilterSelection(position);
            }
        });

        // Estado inicial
        updateFilterSelection(0);
    }

    /**
     * Atualiza visual dos filtros.
     *
     * Kaio, 11/10 (itens 5 e 6): os TRÊS filtros usam o mesmo verde claro
     * (o da classificação "Inofensiva") e o texto é BRANCO nos dois estados.
     * Antes o filtro selecionado recebia texto verde-claro em cima do fundo
     * verde — era isso que deixava o botão com cara de "apagado".
     * O que marca qual está ativo agora é só a borda do chip (chip_active),
     * não a cor do texto.
     */
    private void updateFilterSelection(int selected) {
        if (!isAdded() || getContext() == null) return;

        TextView[] filters = {filterAll, filterMyths, filterTruths};
        for (int i = 0; i < filters.length; i++) {
            if (filters[i] == null) continue;
            boolean isSelected = (i == selected);
            filters[i].setBackgroundResource(isSelected ? R.drawable.chip_active : R.drawable.chip_inactive);
            filters[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_on_button));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager = null;
        filterAll = null;
        filterMyths = null;
        filterTruths = null;
    }
}
