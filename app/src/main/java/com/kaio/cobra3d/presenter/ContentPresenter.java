package com.kaio.cobra3d.presenter;

import android.content.Context;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.contract.ContentContract;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.model.entity.Content;
import com.kaio.cobra3d.model.repository.ContentRepository;

import java.util.List;

public class ContentPresenter implements ContentContract.Presenter {

    private final ContentContract.View view;
    private final Context context;
    private final ContentRepository contentRepository;

    public ContentPresenter(ContentContract.View view, DatabaseHelper dbHelper, Context context) {
        this.view = view;
        this.context = context.getApplicationContext();
        this.contentRepository = new ContentRepository(dbHelper.getWritableDatabase());
    }

    @Override
    public void loadContent(String category) {
        view.showLoading();
        List<Content> contents;
        if (category == null || category.isEmpty() || "todas".equalsIgnoreCase(category)) {
            contents = contentRepository.getAll();
        } else {
            contents = contentRepository.getByCategory(category);
        }
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
            view.showError(context.getString(R.string.error_no_content));
        }
    }
}
