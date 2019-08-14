package com.aaron.justlike.online;

public interface IElementView<T> {

    void onShowProgress();

    void onHideProgress();

    void onShowLoading();

    void onHideLoading();

    void onShowErrorView();

    void onHideErrorView();

    void onShowMessage(String msg);

    void onShowPhotos(T t);

    void onShowMore(T t);
}