package com.aaron.justlike.fragment.online;

import android.view.Menu;
import android.view.MenuItem;

import com.aaron.justlike.http.unsplash.Order;
import com.aaron.justlike.http.unsplash.entity.Photo;
import com.aaron.justlike.mvp.presenter.online.IOnlinePresenter;
import com.aaron.justlike.mvp.presenter.online.OnlinePresenter;

public class RandomFragment extends PhotoFragment {

    private IOnlinePresenter<Photo> mPresenter;

    public RandomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
    }

    /**
     * 隐藏过滤图标，因为对于随机图片没作用
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.getItem(1);
        item.setVisible(false);
        item.setEnabled(false);
    }

    @Override
    public void requestPhotos(Order order, boolean isRefresh, boolean isFilter) {
        mPresenter.requestRandomPhotos(30);
    }

    @Override
    public void requestLoadMore(Order order) {
        mPresenter.requestLoadMoreRandom(30);
    }

    @Override
    public void attachPresenter() {
        if (mPresenter == null) {
            mPresenter = new OnlinePresenter();
        }
        mPresenter.attachView(this);
    }
}