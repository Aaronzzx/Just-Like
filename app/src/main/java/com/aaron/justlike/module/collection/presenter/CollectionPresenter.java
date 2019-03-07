package com.aaron.justlike.module.collection.presenter;

import com.aaron.justlike.module.collection.entity.Album;
import com.aaron.justlike.module.collection.model.CollectionModel;
import com.aaron.justlike.module.collection.model.ICollectionModel;
import com.aaron.justlike.module.collection.view.ICollectionView;

import java.util.Collections;
import java.util.List;

public class CollectionPresenter implements ICollectionPresenter {

    private ICollectionView mView;
    private ICollectionModel<Album> mModel;

    @Override
    public void attachView(ICollectionView view) {
        mView = view;
        mModel = new CollectionModel();
    }

    @Override
    public void detachView() {
        mView = null;
        mModel = null;
    }

    @Override
    public void requestCollection(List<Album> albums) {
        mModel.queryCollection(new ICollectionModel.Callback<Album>() {
            @Override
            public void onResponse(List<Album> list) {
                if (albums.size() != list.size() || !albums.containsAll(list)) {
                    Collections.sort(list, (o1, o2) -> (int) (o2.getCreateAt() - o1.getCreateAt()));
                    mView.onShowImage(list);
                }
            }
        });
    }

    @Override
    public int saveCollection(List<String> list, String title) {
        mModel.insertCollection(list, title, new ICollectionModel.Callback<Album>() {
            @Override
            public void onFinish() {
            }
        });
        return 1;
    }

    @Override
    public void deleteCollection(String title) {
        mModel.deleteCollection(title);
    }
}