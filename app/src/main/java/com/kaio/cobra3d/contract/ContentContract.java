package com.kaio.cobra3d.contract;

import com.kaio.cobra3d.model.entity.Content;

import java.util.List;

public interface ContentContract {

    interface View {
        void showContentList(List<Content> contents);
        void showContentDetail(Content content);
        void showLoading();
        void hideLoading();
        void showError(String message);
    }

    interface Presenter {
        void loadContent(String category);
        void loadContentDetail(long contentId);
    }
}
