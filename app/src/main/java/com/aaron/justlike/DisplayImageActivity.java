package com.aaron.justlike;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.text.ParseException;

public class DisplayImageActivity extends AppCompatActivity {

    private int mPosition;
    private String mFileName;
    private View mDecorView;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);
        /*
         * 启用标题栏的返回键
         */
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setHomeAsUpIndicator(R.mipmap.ic_back);
        }
        initContent();
    }

    /**
     * 按标题栏返回键直接终结 Activity
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    /**
     * 按导航栏返回键直接终结 Activity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * 使用透明状态栏并使状态栏字体颜色反转
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mDecorView = getWindow().getDecorView();
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    /**
     * 创建删除菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_display_image_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 创建菜单点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("确定删除图片吗？")
                        .setIcon(R.mipmap.ic_warn)
                        .setCancelable(false)
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.putExtra("position", mPosition);
                                intent.putExtra("fileName", mFileName);
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

    /**
     * 初始化界面
     */
    private void initContent() {
        /*
         * 获取从适配器序列化过来的 Image 对象，并取值
         */
        Image image = getIntent().getParcelableExtra("image");
        mPosition = getIntent().getIntExtra("position", 0);
        Uri imageUri = image.getUri(); // 获取 URI
        String path = imageUri.getPath(); // 获取原始路径
        String absolutePath = FileUtils.getAbsolutePath(path); // 获取绝对路径
        mFileName = absolutePath.substring(absolutePath.lastIndexOf("/"));

        ViewPager viewPager = findViewById(R.id.activity_display_image_vp);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setPageMargin(50);
        viewPager.setAdapter(new MyPagerAdapter(MainActivity.getPhotoViewList(), this));
        viewPager.setCurrentItem(mPosition);
        /*
         * 获取图片拍摄时间并将信息设置为标题栏标题
         */
        try {
            ExifInterface exifInterface = new ExifInterface(absolutePath);
            String dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            FileUtils.formatDateAndTime(mActionBar, dateTime);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}