package com.kaio.cobra3d.presenter;

import com.kaio.cobra3d.contract.ContentContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Content;
import com.kaio.cobra3d.model.repository.ContentRepository;

import java.util.List;

public class ContentPresenter implements ContentContract.Presenter {

    private final ContentContract.View view;
    private final ContentRepository contentRepository;

    public ContentPresenter(ContentContract.View view, DatabaseHelper dbHelper) {
        this.view = view;
        this.contentRepository = new ContentRepository(dbHelper.getWritableDatabase());
    }

    @Override
    public void loadContent(String category) {
        view.showLoading();
        List<Content> contents = contentRepository.getByCategory(category);
        view.hideLoading();
        view.showContentList(contents);
    }

    @Override
    public void loadContentDetail(long contentId) {
        view.showLoading();
        Content content = contentRepository.getById(contentId);
        view.hideLoading();

        if (content != null) {
            view.showContentDetail(content);
        } else {
            view.showError("Conteúdo não encontrado");
        }
    }
}
