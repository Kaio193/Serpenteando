package com.kaio.cobra3d.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.kaio.cobra3d.view.MythTabFragment;

/**
 * [ALÉM DO TEI] Adapter do ViewPager2 da tela de Mitos e Verdades.
 *
 * Mesmo padrão do ContentPagerAdapter (Conteúdo Educativo):
 *   0 = Todos
 *   1 = Mitos
 *   2 = Verdades
 *
 * FragmentStateAdapter cria e destrói os fragments conforme o swipe, então
 * trocar de aba não acumula memória.
 */
public class MythPagerAdapter extends FragmentStateAdapter {

    /** Quantidade de abas */
    private static final int TAB_COUNT = 3;

    public MythPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String filter;
        switch (position) {
            case 1: filter = "myth"; break;
            case 2: filter = "truth"; break;
            default: filter = "all"; break;
        }
        return MythTabFragment.newInstance(filter);
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
