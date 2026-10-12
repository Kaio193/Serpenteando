package com.serpenteando.cobra3d.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.adapter.MythTruthAdapter;
import com.serpenteando.cobra3d.contract.MythTruthContract;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.model.entity.MythTruth;
import com.serpenteando.cobra3d.presenter.MythTruthPresenter;

import java.util.List;

/**
 * [ALÉM DO TEI] Uma aba do ViewPager2 de Mitos e Verdades.
 *
 * É o mesmo padrão do ContentTabFragment (Conteúdo Educativo): cada aba é um
 * fragment que carrega só o seu filtro, e o ViewPager2 cria/destrói conforme
 * o usuário arrasta. Assim as duas telas funcionam do mesmo jeito.
 *
 *   "all"   → todos os itens
 *   "myth"  → só os mitos
 *   "truth" → só as verdades
 */
public class MythTabFragment extends Fragment implements MythTruthContract.View {

    private static final String ARG_FILTER = "filter";

    private MythTruthPresenter presenter;
    private RecyclerView recyclerView;
    private String filter = "all";

    public static MythTabFragment newInstance(String filter) {
        MythTabFragment fragment = new MythTabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_myth_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            String filtroRecebido = getArguments().getString(ARG_FILTER);
            if (filtroRecebido != null) filter = filtroRecebido;
        }

        presenter = new MythTruthPresenter(this, DatabaseHelper.getInstance(requireContext()));

        recyclerView = view.findViewById(R.id.mythRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        carregarFiltro();
    }

    // Carrega a lista de acordo com o filtro desta aba
    private void carregarFiltro() {
        if (presenter == null) return;
        switch (filter) {
            case "myth":
                presenter.loadMyths();
                break;
            case "truth":
                presenter.loadTruths();
                break;
            default:
                presenter.loadAll();
                break;
        }
    }

    @Override
    public void showMythsTruths(List<MythTruth> items) {
        if (!isAdded() || recyclerView == null) return;
        recyclerView.setAdapter(new MythTruthAdapter(items));
    }

    @Override
    public void showError(String message) {
        if (!isAdded() || getContext() == null) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter = null;
        recyclerView = null;
    }
}
