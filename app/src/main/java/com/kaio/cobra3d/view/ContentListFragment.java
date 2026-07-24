package com.kaio.cobra3d.view;

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

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.adapter.ContentAdapter;
import com.kaio.cobra3d.contract.ContentContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Content;
import com.kaio.cobra3d.presenter.ContentPresenter;

import java.util.List;

public class ContentListFragment extends Fragment implements ContentContract.View, ContentAdapter.OnItemClickListener {

    private static final String ARG_CATEGORY = "category";

    private ContentPresenter presenter;
    private RecyclerView recyclerView;
    private String category;

    public static ContentListFragment newInstance(String category) {
        ContentListFragment fragment = new ContentListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
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
        category = "todas";
        if (getArguments() != null) {
            String cat = getArguments().getString(ARG_CATEGORY);
            if (cat != null) category = cat;
        }

        presenter = new ContentPresenter(this, DatabaseHelper.getInstance(requireContext()));

        recyclerView = view.findViewById(R.id.contentRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        presenter.loadContent(category);
    }

    @Override
    public void showContentList(List<Content> contents) {
        ContentAdapter adapter = new ContentAdapter(contents, this);
        recyclerView.setAdapter(adapter);
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
    public void showLoading() {
    }

    @Override
    public void hideLoading() {
    }

    @Override
    public void showError(String message) {
        if (!isAdded() || getContext() == null) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
