package com.serpenteando.cobra3d.presenter;

import com.serpenteando.cobra3d.contract.MythTruthContract;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.model.entity.MythTruth;
import com.serpenteando.cobra3d.model.repository.MythTruthRepository;

import java.util.List;

public class MythTruthPresenter {

    private final MythTruthContract.View view;
    private final MythTruthRepository repository;

    public MythTruthPresenter(MythTruthContract.View view, DatabaseHelper dbHelper) {
        this.view = view;
        this.repository = new MythTruthRepository(dbHelper.getWritableDatabase());
    }

    public void loadAll() {
        List<MythTruth> items = repository.getAll();
        view.showMythsTruths(items);
    }

    public void loadMyths() {
        List<MythTruth> myths = repository.getMyths();
        view.showMythsTruths(myths);
    }

    public void loadTruths() {
        List<MythTruth> truths = repository.getTruths();
        view.showMythsTruths(truths);
    }
}
