package com.serpenteando.cobra3d.view;

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

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.adapter.ContentPagerAdapter;

/**
 * Tela de Conteúdo Educativo — usa ViewPager2 + filtros estilo Mitos.
 *
 * [ALÉM DO TEI] ViewPager2 não existe nos slides do prof. Alexandre.
 * O usuário pode arrastar pra trocar entre abas (Todas, Peçonhentas, Não Peçonhentas)
 * ou clicar nos filtros.
 *
 * Funcionamento:
 * - 3 filtros TextView (Todas, Peçonhentas, Não Peçonhentas) — estilo Mitos
 * - ViewPager2 gerencia o swipe entre fragments
 * - FragmentStateAdapter cria/destrói fragments conforme necessário
 *
 * [TEI Intents] Navegação pra detail usa Navigation Component (não Intent)
 */
public class ContentListFragment extends Fragment {

    private ViewPager2 viewPager;
    private TextView filterAll, filterVenomous, filterNonVenomous;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Botão voltar
        view.findViewById(R.id.backButton).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        // ViewPager2
        viewPager = view.findViewById(R.id.contentViewPager);

        // Filtros
        filterAll = view.findViewById(R.id.filterAll);
        filterVenomous = view.findViewById(R.id.filterVenomous);
        filterNonVenomous = view.findViewById(R.id.filterNonVenomous);

        // Adapter do ViewPager2 — cria um fragment por aba
        ContentPagerAdapter adapter = new ContentPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        // Mantém as 3 abas vivas: trocar de aba não recarrega a lista
        viewPager.setOffscreenPageLimit(2);

        // SEM swipe horizontal (Kaio, 11/10 — item 17): o ViewPager2 continua
        // guardando as 3 abas e trocando quando o usuário toca no filtro, mas
        // o dedo não arrasta mais a lista. A rolagem vertical segue normal.
        viewPager.setUserInputEnabled(false);

        // Clique nos filtros → troca de aba no ViewPager2
        filterAll.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        filterVenomous.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        filterNonVenomous.setOnClickListener(v -> viewPager.setCurrentItem(2, true));

        // Atualiza visual do filtro selecionado quando troca de aba (swipe)
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
     * Kaio, 11/10 (itens 5 e 6): os TRÊS filtros (Todas / Peçonhentas / Não
     * Peçonhentas) usam o MESMO verde claro — o da classificação "Inofensiva"
     * — e o texto é BRANCO nos dois estados. Antes o selecionado ficava com
     * texto verde-claro sobre fundo verde, que era o que dava o aspecto
     * "apagado". Quem marca o filtro ativo agora é a borda do chip.
     */
    private void updateFilterSelection(int selected) {
        if (!isAdded()) return;

        TextView[] filters = {filterAll, filterVenomous, filterNonVenomous};
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
        filterVenomous = null;
        filterNonVenomous = null;
    }
}
