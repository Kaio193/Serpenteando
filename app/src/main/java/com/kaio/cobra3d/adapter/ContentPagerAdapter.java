package com.kaio.cobra3d.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.kaio.cobra3d.view.ContentTabFragment;

/**
 * [ALÉM DO TEI] Adapter do ViewPager2 — conecta as abas de conteúdo ao ViewPager.
 *
 * Cada aba (position) gera um ContentTabFragment filtrado por tipo:
 *   0 = Todas as serpentes
 *   1 = Peçonhentas
 *   2 = Não Peçonhentas
 *
 * Usa FragmentStateAdapter (padrão do ViewPager2) que cria e destrói
 * fragments conforme o usuário faz swipe, economizando memória.
 *
 * Funciona igual ao RecyclerView.Adapter mas pra fragments inteiros.
 */
public class ContentPagerAdapter extends FragmentStateAdapter {

    /** Quantidade de abas */
    private static final int TAB_COUNT = 3;

    public ContentPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // position 0 = todas, 1 = peçonhentas, 2 = não peçonhentas
        String filter;
        switch (position) {
            case 1: filter = "venomous"; break;
            case 2: filter = "non_venomous"; break;
            default: filter = "all"; break;
        }
        return ContentTabFragment.newInstance(filter);
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
