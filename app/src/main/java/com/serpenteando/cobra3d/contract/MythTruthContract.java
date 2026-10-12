package com.serpenteando.cobra3d.contract;

import com.serpenteando.cobra3d.model.entity.MythTruth;

import java.util.List;

/**
 * Contrato MVP pra Tela de Mitos e Verdades.
 * [ALÉM DO TEI] Lista de cards com frase + explicação.
 */
public interface MythTruthContract {

    interface View {
        void showMythsTruths(List<MythTruth> items);
        void showError(String message);
    }
}
