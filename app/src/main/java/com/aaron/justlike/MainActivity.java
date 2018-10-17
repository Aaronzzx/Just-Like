package com.aaron.justlike;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;
import com.jaeger.library.StatusBarUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION = 1;
    private static final int CHOOSE_PHOTO = 2; // 定义打开文件管理器需要用的请求码，1 被申请权限用了，所以用2
    private static final int DELETE_PHOTO = 3;
    private static boolean isClick = false; // 点击状态
    private List<Image> mImageList = new ArrayList<>(); // 定义存放 Image 实例的 List 集合
    private ImageAdapter mAdapter; // 声明一个 Image 适配器
    private DrawerLayout mDrawerLayout;
    private static List<PhotoView> mPhotoViewList = new ArrayList<>();

    public static List<PhotoView> getPhotoViewList() {
        return mPhotoViewList;
    }

    public static void setPhotoViewList(List<PhotoView> photoViewList) {
        mPhotoViewList.clear(); // 防止程序通过返回键退出，再次打开时重复添加元素
        mPhotoViewList.addAll(photoViewList);
    }

    /**
     * 用于在 ImageAdapter 中判断图片是点击添加的还是
     * 自动缓存添加的。
     *
     * @return 返回点击状态
     */
    public static boolean isClick() {
        return isClick;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme); // 由于设置了启动页，需要在这里将主题改回来
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews(); // 初始化控件
        setStatusBar(); // 修改状态栏和导航栏
        requestWritePermission(); // 申请存储权限
        // 加载存储在程序外部缓存目录的图片
        FileUtils.getLocalCache(this, mImageList, mAdapter);
    }

    /**
     * 浮动按钮的点击事件
     *
     * @param v 传入的 View 实例
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                openAlbum();
                break;
        }
    }

    /**
     * 设置可以打开滑动菜单
     *
     * @param item item 传入的 View 实例
     * @return 返回 true 才执行
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    /**
     * 设置当滑动菜单打开时按返回键不会直接退出程序
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 在点击按钮后首先检查是否已赋予权限，没有则申请，有则直接打开文件管理器
     */
    private void requestWritePermission() {
        // 判断是否已经获得权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请读写存储的权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    /**
     * 申请权限后的回调方法，根据请求码判断是哪个权限的申请，然后根据传入的申请结果判断
     * 是否打开文件管理器，如果没赋予权限，程序将直接退出。
     *
     * @param requestCode  请求码
     * @param permissions  所申请的权限
     * @param grantResults 申请结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (!(grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED)) {
                    finish(); // 如果不授予权限，程序直接退出。
                }
                break;
        }
    }

    /**
     * 将打开文件管理器的代码封装在此方法内，方便调用和使代码简洁。
     */
    private void openAlbum() {
        // 参数为获取任意文件的值，由 setType() 方法限定获取范围
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // 设置需要访问的文件类型，避免用户选择其他类型文件导致程序出错
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    /**
     * startActivityForResult() 的回调方法
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        传回来的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    isClick = true; // 表示此次行为是用户所点击
                    Uri uri = data.getData(); // 获取返回的 URI
                    String path = FileUtils.getAbsolutePath(uri.getPath());

                    PhotoView photoView = new PhotoView(this);
                    Picasso.get()
                            .load(uri)
                            .resize(3000, 3000)
                            .onlyScaleDown()
                            .rotate(FileUtils.getBitmapDegree(FileUtils.getAbsolutePath(path)))
                            .centerInside()
                            .into(photoView);
                    mPhotoViewList.add(photoView);

                    // 通知适配器更新并将文件添加至缓存
                    mImageList.add(new Image(uri));
                    mAdapter.notifyDataSetChanged();
                    FileUtils.saveToCache(this, uri);
                }
                break;
            case DELETE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    int position = data.getIntExtra("position", 0);
                    String fileName = data.getStringExtra("fileName");
                    mImageList.remove(position);
                    mAdapter.notifyDataSetChanged();
                    FileUtils.deleteFile(this, fileName);
                }
                break;
        }
    }

    /**
     * 将控件的初始化代码封装在此方法中，方便调用并使代码简洁。
     */
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        /*
         * 让标题栏启用滑动菜单并设置图标
         */
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_drawer_menu);
        }
        FloatingActionButton fab = findViewById(R.id.fab); // 浮动按钮
        fab.setOnClickListener(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        // 将 RecyclerView 的布局风格改为网格类型,使用自定义的布局管理器，为了能修改滑动状态
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new ImageAdapter(this, mImageList);
        recyclerView.setAdapter(mAdapter);

        navView.setCheckedItem(R.id.nav_home_page);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void setStatusBar() {
        /*
         * 使用透明状态栏
         */
        StatusBarUtil
                .setColorNoTranslucentForDrawerLayout(this,
                        mDrawerLayout, getResources().getColor(R.color.colorPrimary));
    }
}