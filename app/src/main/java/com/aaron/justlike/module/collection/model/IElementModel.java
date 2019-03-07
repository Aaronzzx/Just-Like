package com.aaron.justlike.module.collection.model;

import java.util.List;

public interface IElementModel<T> {

    void queryImage(String title, Callback<T> callback);

    void insertImage(String title, int size, List<String> list, Callback<T> callback);

    void deleteImage(String title, String path);

    interface Callback<T> {

        void onResponse(List<T> list);
    }
}