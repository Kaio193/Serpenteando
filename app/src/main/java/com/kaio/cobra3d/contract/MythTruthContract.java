package com.kaio.cobra3d.contract;

import com.kaio.cobra3d.model.entity.MythTruth;

import java.util.List;

public interface MythTruthContract {

    interface View {
        void showMythsTruths(List<MythTruth> items);
        void showError(String message);
    }
}
