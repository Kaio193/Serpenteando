package com.serpenteando.cobra3d.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.adapter.ContentAdapter;
import com.serpenteando.cobra3d.contract.ContentContract;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.model.entity.Content;
import com.serpenteando.cobra3d.presenter.ContentPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * [ALÉM DO TEI] Fragment que mostra uma lista de conteúdo filtrado por tipo.
 *
 * Cada aba do ViewPager2 cria uma instância deste fragment com um filtro diferente:
 *   "all"         → todas as serpentes
 *   "venomous"    → só peçonhentas
 *   "non_venomous" → só não peçonhentas
 *
 * Funciona igual ao ContentListFragment original, mas sem o botão voltar
 * (já que o back está no fragment pai).
 */
public class ContentTabFragment extends Fragment implements ContentContract.View, ContentAdapter.OnItemClickListener {

    private static final String ARG_FILTER = "filter";

    private ContentPresenter presenter;
    private RecyclerView recyclerView;
    private String filter;

    public static ContentTabFragment newInstance(String filter) {
        ContentTabFragment fragment = new ContentTabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filter = "all";
        if (getArguments() != null) {
            String f = getArguments().getString(ARG_FILTER);
            if (f != null) filter = f;
        }

        presenter = new ContentPresenter(this, DatabaseHelper.getInstance(requireContext()), requireContext());

        recyclerView = view.findViewById(R.id.contentRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Esconde elementos que o fragment pai já tem (back button e title)
        View backButton = view.findViewById(R.id.backButton);
        if (backButton != null) backButton.setVisibility(View.GONE);

        // Título: o fragment pai (fragment_content_tabs) já mostra "Conteúdo
        // Educativo", então aqui ele tem que sumir — senão o texto aparece
        // duplicado na tela.
        View listTitle = view.findViewById(R.id.listTitle);
        if (listTitle != null) listTitle.setVisibility(View.GONE);

        presenter.loadContent("todas");
    }

    @Override
    public void showContentList(List<Content> contents) {
        // Filtra conteúdo baseado no tipo da aba
        List<Content> filtered = new ArrayList<>();
        for (Content c : contents) {
            switch (filter) {
                case "venomous":
                    if (c.isVenomous()) filtered.add(c);
                    break;
                case "non_venomous":
                    if (!c.isVenomous()) filtered.add(c);
                    break;
                default:
                    filtered.add(c);
                    break;
            }
        }

        ContentAdapter adapter = new ContentAdapter(filtered, this);
        recyclerView.setAdapter(adapter);

        if (filtered.isEmpty()) {
            Toast.makeText(getContext(), R.string.content_empty, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showContentDetail(Content content) {
        Bundle args = new Bundle();
        args.putLong("contentId", content.getId());
        args.putString("title", content.getTitle());
        Navigation.findNavController(requireView())
                .navigate(R.id.nav_content_detail, args);
    }

    @Override
    public void onItemClick(Content content) {
        if (presenter != null) presenter.loadContentDetail(content.getId());
    }

    @Override
    public void showLoading() {}

    @Override
    public void hideLoading() {}

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
