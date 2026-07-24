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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.adapter.MythTruthAdapter;
import com.kaio.cobra3d.contract.MythTruthContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.MythTruth;
import com.kaio.cobra3d.presenter.MythTruthPresenter;

import java.util.List;

public class MythTruthFragment extends Fragment implements MythTruthContract.View {

    private MythTruthPresenter presenter;
    private RecyclerView recyclerView;
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

        presenter = new MythTruthPresenter(this, DatabaseHelper.getInstance(requireContext()));

        recyclerView = view.findViewById(R.id.mythRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        filterAll = view.findViewById(R.id.filterAll);
        filterMyths = view.findViewById(R.id.filterMyths);
        filterTruths = view.findViewById(R.id.filterTruths);

        filterAll.setOnClickListener(v -> selectFilter(filterAll, () -> { if (presenter != null) presenter.loadAll(); }));
        filterMyths.setOnClickListener(v -> selectFilter(filterMyths, () -> { if (presenter != null) presenter.loadMyths(); }));
        filterTruths.setOnClickListener(v -> selectFilter(filterTruths, () -> { if (presenter != null) presenter.loadTruths(); }));

        selectFilter(filterAll, () -> { if (presenter != null) presenter.loadAll(); });
    }

    private void selectFilter(TextView active, Runnable load) {
        if (!isAdded() || getContext() == null) return;

        // Reset all to inactive
        filterAll.setBackground(getResources().getDrawable(R.drawable.chip_inactive));
        filterMyths.setBackground(getResources().getDrawable(R.drawable.chip_inactive));
        filterTruths.setBackground(getResources().getDrawable(R.drawable.chip_inactive));
        filterAll.setTextColor(getResources().getColor(R.color.cascavel_on_surface_variant));
        filterMyths.setTextColor(getResources().getColor(R.color.cascavel_on_surface_variant));
        filterTruths.setTextColor(getResources().getColor(R.color.cascavel_on_surface_variant));

        // Set active
        active.setBackground(getResources().getDrawable(R.drawable.chip_active));
        active.setTextColor(getResources().getColor(android.R.color.black));

        load.run();
    }

    @Override
    public void showMythsTruths(List<MythTruth> items) {
        MythTruthAdapter adapter = new MythTruthAdapter(items);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void showError(String message) {
        if (!isAdded() || getContext() == null) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
