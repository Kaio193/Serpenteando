package com.kaio.cobra3d.view;

import android.graphics.Bitmap;
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

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.adapter.ContentAdapter;
import com.kaio.cobra3d.adapter.SnakeImageLoader;
import com.kaio.cobra3d.contract.ContentContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Content;
import com.kaio.cobra3d.presenter.ContentPresenter;

public class ContentDetailFragment extends Fragment implements ContentContract.View {

    private static final String ARG_CONTENT_ID = "contentId";

    private ContentPresenter presenter;
    private ImageView heroImage;
    private TextView titleText;
    private TextView scientificText;
    private TextView familyChip;
    private TextView categoryBadge;
    private TextView bodyText;
    private TextView habitatText;
    private TextView distributionText;
    private TextView dietText;
    private TextView reproductionText;
    private TextView curiosityText;
    private TextView habitatHeader;
    private TextView distributionHeader;
    private TextView dietHeader;
    private TextView reproductionHeader;
    private TextView curiosityHeader;

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

        presenter = new ContentPresenter(this, DatabaseHelper.getInstance(requireContext()), requireContext());

        heroImage = view.findViewById(R.id.heroImage);
        titleText = view.findViewById(R.id.titleText);
        scientificText = view.findViewById(R.id.scientificText);
        familyChip = view.findViewById(R.id.familyChip);
        categoryBadge = view.findViewById(R.id.categoryBadge);
        bodyText = view.findViewById(R.id.bodyText);
        habitatText = view.findViewById(R.id.habitatText);
        distributionText = view.findViewById(R.id.distributionText);
        dietText = view.findViewById(R.id.dietText);
        reproductionText = view.findViewById(R.id.reproductionText);
        curiosityText = view.findViewById(R.id.curiosityText);
        habitatHeader = view.findViewById(R.id.habitatHeader);
        distributionHeader = view.findViewById(R.id.distributionHeader);
        dietHeader = view.findViewById(R.id.dietHeader);
        reproductionHeader = view.findViewById(R.id.reproductionHeader);
        curiosityHeader = view.findViewById(R.id.curiosityHeader);

        view.findViewById(R.id.backButton).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        if (getArguments() != null && getArguments().containsKey(ARG_CONTENT_ID)) {
            long contentId = getArguments().getLong(ARG_CONTENT_ID);
            presenter.loadContentDetail(contentId);
        } else {
            titleText.setText(R.string.error);
            bodyText.setText(R.string.error_content_not_found);
        }
    }

    @Override
    public void showContentList(java.util.List<Content> contents) {
    }

    @Override
    public void showContentDetail(Content content) {
        if (!isAdded()) return;

        // Missão "Explorador da Fauna": abre ficha na enciclopédia = progresso real
        try {
            com.kaio.cobra3d.model.repository.ProgressRepository progressRepo =
                    new com.kaio.cobra3d.model.repository.ProgressRepository(
                            DatabaseHelper.getInstance(requireContext()).getWritableDatabase());
            if (getArguments() != null && getArguments().getLong(ARG_CONTENT_ID, -1) != -1) {
                progressRepo.incrementEvent("CONTENT_DETAIL");
            }
        } catch (Exception ignored) {}

        Bitmap bmp = SnakeImageLoader.load(requireContext(), content.getImageResName());
        if (bmp != null) {
            heroImage.setImageBitmap(bmp);
        } else {
            heroImage.setImageResource(R.drawable.ic_cobra);
        }

        titleText.setText(content.getTitle());
        ContentAdapter.bindBadge(requireContext(), content, categoryBadge);

        setText(scientificText, content.getScientificName());
        setText(familyChip, content.getFamily());
        setText(bodyText, content.getBody());
        setSection(habitatHeader, habitatText, content.getHabitat());
        setSection(distributionHeader, distributionText, content.getDistribution());
        setSection(dietHeader, dietText, content.getDiet());
        setSection(reproductionHeader, reproductionText, content.getReproduction());
        setSection(curiosityHeader, curiosityText, content.getCuriosity());
    }

    private void setText(TextView tv, String value) {
        tv.setText(value != null ? value : "");
    }

    private void setSection(TextView header, TextView text, String value) {
        if (value == null || value.isEmpty()) {
            header.setVisibility(View.GONE);
            text.setVisibility(View.GONE);
        } else {
            header.setVisibility(View.VISIBLE);
            text.setVisibility(View.VISIBLE);
            text.setText(value);
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter = null;
        heroImage = null;
        titleText = null;
        scientificText = null;
        familyChip = null;
        categoryBadge = null;
        bodyText = null;
        habitatText = null;
        distributionText = null;
        dietText = null;
        reproductionText = null;
        curiosityText = null;
    }
}