package com.aaron.justlike.collection;

import java.util.List;

public interface IElementPresenter<T> {

    void attachView(IElementView<T> view);

    void detachView();

    void requestImage(String title);

    void saveImage(String title, int size, List<String> list);

    void deleteImage(String title, String path);
}