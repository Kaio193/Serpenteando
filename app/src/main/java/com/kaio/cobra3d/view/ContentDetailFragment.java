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

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.contract.ContentContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Content;
import com.kaio.cobra3d.presenter.ContentPresenter;

public class ContentDetailFragment extends Fragment implements ContentContract.View {

    private static final String ARG_CONTENT_ID = "contentId";

    private ContentPresenter presenter;
    private TextView titleText;
    private TextView bodyText;

    public static ContentDetailFragment newInstance(long contentId) {
        ContentDetailFragment fragment = new ContentDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CONTENT_ID, contentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new ContentPresenter(this, DatabaseHelper.getInstance(requireContext()));

        titleText = view.findViewById(R.id.titleText);
        bodyText = view.findViewById(R.id.bodyText);

        if (getArguments() != null && getArguments().containsKey(ARG_CONTENT_ID)) {
            long contentId = getArguments().getLong(ARG_CONTENT_ID);
            presenter.loadContentDetail(contentId);
        } else {
            titleText.setText("Erro");
            bodyText.setText("Nenhum conteudo encontrado.");
        }
    }

    @Override
    public void showContentList(java.util.List<Content> contents) {
    }

    @Override
    public void showContentDetail(Content content) {
        if (!isAdded()) return;
        titleText.setText(content.getTitle());
        bodyText.setText(content.getBody());
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
