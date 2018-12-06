package com.aaron.justlike.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.aaron.justlike.R;
import com.aaron.justlike.adapter.MyPagerAdapter;
import com.aaron.justlike.util.AnimationUtil;
import com.aaron.justlike.util.FileUtils;
import com.aaron.justlike.util.SystemUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

public class MainImageActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_image);
        initContent();
    }

    public int getCurrentPosition() {
        return mPosition;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * 按标题栏返回键直接终结 Activity
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    /**
     * 按导航栏返回键直接终结 Activity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * 使用透明状态栏
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    /**
     * 创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_image_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 创建菜单点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_default_crop:
                cropImage("default");
                break;
            case R.id.action_free_crop:
                cropImage("free");
                break;
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("确定删除图片吗？")
                        .setCancelable(false)
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.putExtra("position", mPosition);
                                String fileName = MainActivity.getFileNameList().get(mPosition);
                                intent.putExtra("fileName", fileName);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case UCrop.REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    Uri resultUri = UCrop.getOutput(data);
                    FileUtils.setWallpaper(this, FileUtils.getPath(this, resultUri));
//                    PictureFileUtils.deleteCacheDirFile(this);
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    Toast.makeText(this, "设置失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 初始化界面
     */
    private void initContent() {
        // 获取从适配器序列化过来的值
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mPosition = bundle.getInt("position");
        }
        mToolbar = findViewById(R.id.activity_display_image_toolbar);
        setTitle();
        setSupportActionBar(mToolbar);
        // 启用标题栏的返回键
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        AnimationUtil.exitFullScreen(this, mToolbar, 300);

        mViewPager = findViewById(R.id.activity_display_image_vp);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setPageMargin(50);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(this, MainActivity.getImageList());
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(mPosition);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mPosition = mViewPager.getCurrentItem();
                setTitle();
            }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setTitle() {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(MainActivity.getImageList().get(mPosition).getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String originalDate = exif.getAttribute(ExifInterface.TAG_DATETIME);
        if (!TextUtils.isEmpty(originalDate)) {
            String[] dateArray = originalDate.split(" ");
            mToolbar.setTitle(dateArray[0]);
//            mToolbar.setSubtitle(dateArray[1].substring(0, 5));
        }
    }

    private void cropImage(String type) {
        String sourcePath = MainActivity.getImageList().get(mPosition).getPath();
        // 源文件位置
        Uri sourceUri = FileUtils.getUriFromPath(this, new File(sourcePath));
        File file = new File(getCacheDir(), "Cropped-Wallpaper.JPG");
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 需要输出的位置
        Uri destinationUri = Uri.fromFile(file);
        // 设置裁剪页面主题
        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        options.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        options.setActiveWidgetColor(getResources().getColor(R.color.colorPrimary));
        if (type.equals("default")) {
            // 获取设备分辨率
            int[] resolutionArray = SystemUtils.getResolution(getWindowManager());
            // 打开默认裁剪页面
            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(resolutionArray[0], resolutionArray[1])
                    .withOptions(options)
                    .start(this);
        } else if (type.equals("free")) {
            // 打开自由裁剪页面
            UCrop.of(sourceUri, destinationUri)
                    .withOptions(options)
                    .start(this);
        }
    }
}