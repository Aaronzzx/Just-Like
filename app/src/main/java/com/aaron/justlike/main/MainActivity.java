package com.aaron.justlike.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.base.image.DefaultOption;
import com.aaron.base.image.ImageLoader;
import com.aaron.base.image.ScaleType;
import com.aaron.base.impl.OnClickListenerImpl;
import com.aaron.base.util.StatusBarUtils;
import com.aaron.justlike.R;
import com.aaron.justlike.collection.CollectionActivity;
import com.aaron.justlike.common.CommonActivity;
import com.aaron.justlike.common.bean.Image;
import com.aaron.justlike.common.event.DeleteEvent;
import com.aaron.justlike.common.impl.SquareItemDecoration;
import com.aaron.justlike.common.manager.ThemeManager;
import com.aaron.justlike.common.manager.UiManager;
import com.aaron.justlike.common.util.PopupWindowUtils;
import com.aaron.justlike.common.util.SelectorUtils;
import com.aaron.justlike.common.util.SystemUtil;
import com.aaron.justlike.common.widget.MyGridLayoutManager;
import com.aaron.justlike.online.home.OnlineActivity;
import com.aaron.justlike.others.about.AboutActivity;
import com.aaron.justlike.others.download.DownloadManagerActivity;
import com.aaron.justlike.others.theme.ThemeActivity;
import com.aaron.ui.widget.TopBar;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.BezierRadarHeader;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.ImageEngine;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends CommonActivity implements IMainContract.V<Image>, IMainCommunicable {

    private static final int REQUEST_SELECT_IMAGE = 1;

    private int mMatisseTheme;
    private int mColorAccent;
    private int mSortType;
    private boolean mIsAscending;
    private List<Image> mImageList = new ArrayList<>();

    private IMainContract.P<Image> mPresenter;
    private RecyclerView.Adapter mAdapter;

    private DrawerLayout mParentLayout;
    private NavigationView mNavView;
//    private Toolbar mToolbar;
    private TopBar mTopBar;
//    private SwipeRefreshLayout mSwipeRefresh;
    private SmartRefreshLayout mRefreshLayout;
    private BezierRadarHeader mRefreshHeader;
    private RecyclerView mRv;
    private FloatingActionButton mFabButton;
    private ImageView mNavHeaderImage;
    private View mEmptyView;

    private ActionBar mActionBar;

    private Drawable mIconDrawer;
    private Drawable mIconSort;
    private Drawable mIconAdd;

    private PopupWindow mPwMenu;
    private TextView mTvDate;
    private TextView mTvName;
    private TextView mTvSize;
    private LinearLayout mLlAscending;
    private CheckBox mCbAscending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.getInstance().setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        requestPermission();
        attachPresenter();
        initView();
        mPresenter.requestImage(mImageList, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRefreshLayout.finishRefresh(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mPresenter.detachView(); // 断开 OnlinePresenter
    }

    @Override
    public void onBackPressed() {
        if (mParentLayout.isDrawerOpen(GravityCompat.START)) {
            mParentLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
//        finish();
//        overridePendingTransition(0, R.anim.activity_slide_out);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Window window = getWindow();
        View decorView = window.getDecorView();
        if (hasFocus) {
            mNavView.setCheckedItem(R.id.nav_mine);
            ThemeManager.Theme theme = ThemeManager.getInstance().getCurrentTheme();
            if (theme == null || theme == ThemeManager.Theme.WHITE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    StatusBarUtils.setTransparent(this, true);
                } else {
//                    StatusBarUtil.setTranslucentForDrawerLayout(this, mParentLayout, 70);
                    StatusBarUtils.setTranslucent(this);
                }
//                mToolbar.setTitleTextColor(getResources().getColor(R.color.colorAccentWhite));
                mTopBar.setTextColor(getResources().getColor(R.color.colorAccentWhite));
                mActionBar.setHomeAsUpIndicator(mIconDrawer);
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                mTopBar.setTextColor(getResources().getColor(R.color.base_white));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        mParentLayout.openDrawer(GravityCompat.START);
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sort) {
            mPwMenu.showAsDropDown(mTopBar, 0, -ConvertUtils.dp2px(4), Gravity.BOTTOM | Gravity.END);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Matisse 选取图片后默认回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                List<String> selectedList = Matisse.obtainPathResult(data);
                mPresenter.addImage(mImageList, selectedList);
            }
        }
    }

    public int getColorAccent() {
        return mColorAccent;
    }

    /**
     * 接收 PreviewActivity 传过来的关于被删除图片的信息，并更新 UI
     */
    @Subscribe()
    public void onDeleteEvent(DeleteEvent event) {
        if (event.getEventType() == DeleteEvent.FROM_MAIN_ACTIVITY) {
            int position = event.getPosition();
            String path = event.getPath();
            mImageList.remove(position);
            mAdapter.notifyDataSetChanged();
            mPresenter.deleteImage(path, mImageList.isEmpty());
        }
    }

    @Override
    public void onDelete(String path, boolean isListEmpty) {
        mPresenter.deleteImage(path, isListEmpty);
    }

    @Override
    public void attachPresenter() {
        mPresenter = new MainPresenter(this);
    }

    @Override
    public void onShowImage(List<Image> imageList, int sortType, boolean ascendingOrder) {
        mImageList.clear();
        mImageList.addAll(imageList);
        runOnUiThread(() -> {
            mAdapter.notifyItemRangeChanged(0, mImageList.size());
            mRv.scrollToPosition(0);
        });
        mSortType = sortType;
        mIsAscending = ascendingOrder;
        initMenuItem();
    }

    @Override
    public void onShowAddImage(List<Image> list) {
        mImageList.addAll(0, list);
        runOnUiThread(() -> {
            mAdapter.notifyItemRangeInserted(0, list.size());
            mAdapter.notifyItemRangeChanged(list.size(), mImageList.size() - list.size());
            mRv.scrollToPosition(0);
        });
    }

    @Override
    public void onShowMessage(String args) {
        runOnUiThread(() -> {
            if (args != null) UiManager.showShort(args);
        });
    }

    @Override
    public void onShowEmptyView() {
        runOnUiThread(() -> mEmptyView.setVisibility(View.VISIBLE));
    }

    @Override
    public void onHideEmptyView() {
        runOnUiThread(() -> mEmptyView.setVisibility(View.GONE));
    }

    @Override
    public void onHideRefresh() {
        runOnUiThread(() -> mRefreshLayout.finishRefresh(1000));
    }

    private void initView() {
        // Part 1, find id
        mParentLayout = findViewById(R.id.drawer_layout_home_activity_main);
        mNavView = findViewById(R.id.navigation_view);
        View headerView = mNavView.getHeaderView(0);
        mNavHeaderImage = headerView.findViewById(R.id.nav_head_image);
        mTopBar = findViewById(R.id.toolbar_home_activity_main);
        mRefreshLayout = findViewById(R.id.swipe_refresh_home_activity_main);
        mRefreshHeader = findViewById(R.id.app_refresh_header);
        mRv = findViewById(R.id.rv);
        mFabButton = findViewById(R.id.fab_home_activity_main);
        mEmptyView = findViewById(R.id.empty_view);

        // Part 2
        mTopBar.setOnTapListener(v -> {
            // 查找当前屏幕内第一个可见的 View
            View firstVisibleItem = mRv.getChildAt(0);
            // 查找当前 View 在 RecyclerView 中处于哪个位置
            int itemPosition = mRv.getChildLayoutPosition(firstVisibleItem);
            if (itemPosition >= 48) {
                mRv.scrollToPosition(36);
            }
            mRv.smoothScrollToPosition(0);
        });
        mFabButton.setOnClickListener(new OnClickListenerImpl() {
            @Override
            public void onViewClick(View v, long interval) {
                openImageSelector();
            }
        });
        mNavView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.nav_home:
                    startActivityByNav(OnlineActivity.class);
                    break;
                case R.id.nav_mine:
                    mParentLayout.closeDrawers();
                    break;
                case R.id.nav_collection:
                    startActivityByNav(CollectionActivity.class);
                    break;
                case R.id.nav_download_manager:
                    startActivityByNav(DownloadManagerActivity.class);
                    break;
                // TODO 编写侧滑菜单设置项的逻辑
//            case R.id.nav_settings:
//
//                break;
                case R.id.nav_theme:
                    startActivityByNav(ThemeActivity.class);
                    break;
                case R.id.nav_about:
                    startActivityByNav(AboutActivity.class);
                    break;
            }
            return true;
        });
        mRefreshLayout.setOnRefreshListener(refreshLayout -> mPresenter.requestImage(mImageList, true));
//        ((DefaultItemAnimator) mRv.getItemAnimator()).setSupportsChangeAnimations(false);
        MyGridLayoutManager layoutManager = new MyGridLayoutManager(this, 3);
        mRv.setLayoutManager(layoutManager);
        mRv.addItemDecoration(new SquareItemDecoration.XItemDecoration());
        mRv.addItemDecoration(new SquareItemDecoration.YItemDecoration());
        mAdapter = new MainAdapter(mImageList);
        mRv.setAdapter(mAdapter);
        mRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    mFabButton.hide();
                } else if (dy < 0) {
                    mFabButton.show();
                }
            }
        });

        // Part 3, init status
        initPwMenu();
        initIconColor();
        initTheme();
        initToolbar();
        mRefreshHeader.setPrimaryColor(Color.WHITE);
        mRefreshHeader.setAccentColorId(mColorAccent);
    }

    private void initPwMenu() {
        View content = LayoutInflater.from(this).inflate(R.layout.app_pw_main_menu, null);
        mTvDate = content.findViewById(R.id.app_tv_date);
        mTvName = content.findViewById(R.id.app_tv_name);
        mTvSize = content.findViewById(R.id.app_tv_size);
        mLlAscending = content.findViewById(R.id.app_ll_ascending);
        mCbAscending = content.findViewById(R.id.app_cb);
        mPwMenu = PopupWindowUtils.create(this, content);
        mTvDate.setOnClickListener(v -> {
            setSort(MainPresenter.SORT_BY_DATE, mIsAscending);
            mPwMenu.dismiss();
        });
        mTvName.setOnClickListener(v -> {
            setSort(MainPresenter.SORT_BY_NAME, mIsAscending);
            mPwMenu.dismiss();
        });
        mTvSize.setOnClickListener(v -> {
            setSort(MainPresenter.SORT_BY_SIZE, mIsAscending);
            mPwMenu.dismiss();
        });
        mLlAscending.setOnClickListener(v -> {
            setSortByAscending(!mIsAscending);
            mPwMenu.dismiss();
        });
        mPwMenu.setAnimationStyle(R.style.AppPopupWindow);
        mPwMenu.setFocusable(true);
        mPwMenu.setOutsideTouchable(true);
        mPwMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void initIconColor() {
        mIconDrawer = getResources().getDrawable(R.drawable.ic_drawer_menu);
        mIconSort = getResources().getDrawable(R.drawable.ic_sort);
        mIconAdd = getResources().getDrawable(R.drawable.ic_add);
        if (ThemeManager.getInstance().getCurrentTheme() == null
                || ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.WHITE) {
            DrawableCompat.setTint(mIconDrawer, getResources().getColor(R.color.colorAccentWhite));
            DrawableCompat.setTint(mIconSort, getResources().getColor(R.color.colorAccentWhite));
            DrawableCompat.setTint(mIconAdd, getResources().getColor(R.color.colorAccentWhite));
            mFabButton.setBackgroundTintList(getColorStateListTest());
        } else {
            DrawableCompat.setTint(mIconDrawer, getResources().getColor(R.color.colorPrimaryWhite));
            DrawableCompat.setTint(mIconSort, getResources().getColor(R.color.colorPrimaryWhite));
            DrawableCompat.setTint(mIconAdd, getResources().getColor(R.color.colorPrimaryWhite));
        }
        mFabButton.setImageDrawable(mIconAdd);
    }

    private void initTheme() {
        ThemeManager.Theme theme = ThemeManager.getInstance().getCurrentTheme();
        if (theme == null) {
            mColorAccent = R.color.colorAccentWhite;
            mMatisseTheme = R.style.MatisseBlackTheme;
            mNavHeaderImage.setImageDrawable(getResources().getDrawable(R.drawable.theme_white));
            // 初次安装时由于有权限申请，此时没有获取到焦点，所以会有一刹那没变色，这里设置一下就好了
//            mToolbar.setTitleTextColor(getResources().getColor(R.color.colorAccentWhite));
            mTopBar.setTextColor(getResources().getColor(R.color.colorAccentWhite));
            return;
        }
        Drawable normal = new ColorDrawable(Color.WHITE);
        Drawable checked = getDrawable(R.drawable.app_bg_nav_checked);
        switch (theme) {
            case JUST_LIKE:
                mColorAccent = R.color.colorPrimary;
                mMatisseTheme = R.style.MatisseJustLikeTheme;
                mNavHeaderImage.setImageDrawable(getResources().getDrawable(R.drawable.theme_just_like));
                break;
            case WHITE:
            default:
                mColorAccent = R.color.colorAccentWhite;
                mMatisseTheme = R.style.MatisseBlackTheme;
                mNavHeaderImage.setImageDrawable(getResources().getDrawable(R.drawable.theme_white));
                break;
            case BLACK:
                mColorAccent = R.color.colorPrimaryBlack;
                mMatisseTheme = R.style.MatisseBlackTheme;
                mNavHeaderImage.setImageDrawable(getResources().getDrawable(R.drawable.theme_black));
                break;
            case GREY:
                mColorAccent = R.color.colorPrimaryGrey;
                mMatisseTheme = R.style.MatisseGreyTheme;
                mNavHeaderImage.setImageDrawable(getResources().getDrawable(R.drawable.theme_grey));
                break;
            case GREEN:
                mColorAccent = R.color.colorPrimaryGreen;
                mMatisseTheme = R.style.MatisseGreenTheme;
                mNavHeaderImage.setImageDrawable(getResources().getDrawable(R.drawable.theme_green));
                break;
            case RED:
                mColorAccent = R.color.colorPrimaryRed;
                mMatisseTheme = R.style.MatisseRedTheme;
                mNavHeaderImage.setImageDrawable(getResources().getDrawable(R.drawable.theme_red));
                break;
            case PINK:
                mColorAccent = R.color.colorPrimaryPink;
                mMatisseTheme = R.style.MatissePinkTheme;
                mNavHeaderImage.setImageDrawable(getResources().getDrawable(R.drawable.theme_pink));
                break;
            case BLUE:
                mColorAccent = R.color.colorPrimaryBlue;
                mMatisseTheme = R.style.MatisseBlueTheme;
                mNavHeaderImage.setImageDrawable(getResources().getDrawable(R.drawable.theme_blue));
                break;
            case PURPLE:
                mColorAccent = R.color.colorPrimaryPurple;
                mMatisseTheme = R.style.MatissePurpleTheme;
                mNavHeaderImage.setImageDrawable(getResources().getDrawable(R.drawable.theme_purple));
                break;
            case ORANGE:
                mColorAccent = R.color.colorPrimaryOrange;
                mMatisseTheme = R.style.MatisseBrownTheme;
                mNavHeaderImage.setImageDrawable(getResources().getDrawable(R.drawable.theme_orange));
                break;
        }
        if (theme == ThemeManager.Theme.WHITE) {
            checked.setTint(Color.BLACK);
        } else {
            checked.setTint(getResources().getColor(mColorAccent));
        }
        checked.setAlpha(20);
        Drawable selector = SelectorUtils.createCheckedSelector(this, normal, checked);
        mNavView.setItemBackground(selector);
    }

    private void initToolbar() {
        StatusBarUtils.setTransparent(this);
//        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_drawer_menu);
        }
    }

    private ColorStateList getColorStateListTest() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed} // pressed
        };
        int color = getResources().getColor(R.color.colorPrimaryWhite);
        int[] colors = new int[]{color, color, color, color};
        return new ColorStateList(states, colors);
    }

    private void initMenuItem() {
        mTvDate.setSelected(false);
        mTvName.setSelected(false);
        mTvSize.setSelected(false);
        switch (mSortType) {
            case MainPresenter.SORT_BY_DATE:
                mTvDate.setSelected(true);
                break;
            case MainPresenter.SORT_BY_NAME:
                mTvName.setSelected(true);
                break;
            case MainPresenter.SORT_BY_SIZE:
                mTvSize.setSelected(true);
                break;
        }
        mCbAscending.setChecked(mIsAscending);
    }

    /**
     * 设置 Popup 菜单排序类型状态
     */
    private void setSort(int sortType, boolean isAscending) {
        mPresenter.setSortType(sortType, isAscending);
        mTvDate.setSelected(false);
        mTvName.setSelected(false);
        mTvSize.setSelected(false);
        switch (sortType) {
            case MainPresenter.SORT_BY_DATE:
                mTvDate.setSelected(true);
                break;
            case MainPresenter.SORT_BY_NAME:
                mTvName.setSelected(true);
                break;
            case MainPresenter.SORT_BY_SIZE:
                mTvSize.setSelected(true);
                break;
        }
        mPresenter.requestImage(mImageList, false);
    }

    /**
     * 设置 Popup 菜单是否升序状态
     */
    private void setSortByAscending(boolean sortByAscending) {
        if (mTvDate.isSelected()) {
            mPresenter.setSortType(MainPresenter.SORT_BY_DATE, sortByAscending);
            mCbAscending.setChecked(sortByAscending);

        } else if (mTvName.isSelected()) {
            mPresenter.setSortType(MainPresenter.SORT_BY_NAME, sortByAscending);
            mCbAscending.setChecked(sortByAscending);

        } else {
            mPresenter.setSortType(MainPresenter.SORT_BY_SIZE, sortByAscending);
            mCbAscending.setChecked(sortByAscending);
        }
        mPresenter.requestImage(mImageList, false);
    }

    /**
     * 请求权限
     */
    private void requestPermission() {
        PermissionUtils.permission(PermissionConstants.STORAGE)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onDenied() {
                        UiManager.showShort("不开启权限将无法使用壁纸缓存功能");
                    }
                })
                .request();
    }

    /**
     * 点击菜单跳转 Activity --- for NavigationView
     */
    private void startActivityByNav(Class target) {
        mParentLayout.closeDrawers();
        mParentLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                Intent intent = new Intent(MainActivity.this, target);
                startActivity(intent);
//                overridePendingTransition(R.anim.activity_slide_in, android.R.anim.fade_out);
                mParentLayout.removeDrawerListener(this);
            }
        });
    }

    /**
     * 打开图片选择器
     */
    private void openImageSelector() {
        Matisse.from(this)
                .choose(MimeType.ofImage())
                .showSingleMediaType(true)
                .countable(true)
                .maxSelectable(9)
//                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(SystemUtil.dp2px(this, 120.0F))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new MatisseEngine())
                .autoHideToolbarOnSingleTap(true)
                .theme(mMatisseTheme)
                .forResult(REQUEST_SELECT_IMAGE);
    }

    public static class MatisseEngine implements ImageEngine {
        @Override
        public void loadThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
            ImageLoader.load(context, new DefaultOption.Builder(uri)
                    .asBitmap()
                    .placeholder(placeholder)
                    .scaleType(ScaleType.CENTER_CROP)
                    .override(resize)
                    .into(imageView));
        }

        @Override
        public void loadGifThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
            ImageLoader.load(context, new DefaultOption.Builder(uri)
                    .asGif()
                    .placeholder(placeholder)
                    .scaleType(ScaleType.CENTER_CROP)
                    .override(resize)
                    .into(imageView));
        }


        @Override
        public void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
            ImageLoader.load(context, new DefaultOption.Builder(uri)
                    .asBitmap()
                    .scaleType(ScaleType.CENTER_CROP)
                    .override(resizeX, resizeY)
                    .into(imageView));
        }

        @Override
        public void loadGifImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
            ImageLoader.load(context, new DefaultOption.Builder(uri)
                    .asGif()
                    .scaleType(ScaleType.CENTER_CROP)
                    .override(resizeX, resizeY)
                    .into(imageView));
        }

        @Override
        public boolean supportAnimatedGif() {
            return true;
        }
    }
}
