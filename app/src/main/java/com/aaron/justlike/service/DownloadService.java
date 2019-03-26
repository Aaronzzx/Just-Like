package com.aaron.justlike.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.aaron.justlike.common.ObserverImpl;
import com.aaron.justlike.library.aria.AriaApp;
import com.aaron.justlike.mvp.presenter.online.preview.PreviewPresenter;
import com.aaron.justlike.util.FileUtil;
import com.aaron.justlike.util.NotificationUtil;
import com.aaron.justlike.util.SystemUtil;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;

import java.io.File;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DownloadService extends IntentService {

    private String mPath;
    private String mPhotoName;
    private int mNotificationId;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String urls = intent.getStringExtra("urls");
        mPath = intent.getStringExtra("path");
        mPhotoName = mPath.substring(mPath.lastIndexOf("/") + 1);
        int mode = intent.getIntExtra("mode", 0);
        Aria.download(this).register();
        AriaApp.getInstance().startDownload(this, urls, mPath, mode);
        mNotificationId = SystemUtil.getRandomNum(10000);
        startForeground(mNotificationId, NotificationUtil.getNotification(this, "正在下载 " + mPhotoName, "0%", 1));
        Observable.create((ObservableOnSubscribe<String>) emitter -> emitter.onNext("开始下载"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ObserverImpl<String>() {
                    @Override
                    public void onNext(String s) {
                        Toast.makeText(DownloadService.this, s, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Called by Aria , 监听进度
     */
    @Download.onTaskRunning
    public void onDownloadProgress(DownloadTask task) {
        if (task == null || !task.getDownloadPath().equals(mPath + ".TEMP")) return;
        int progress = task.getPercent();
        NotificationUtil.createNotification(this, mNotificationId, "正在下载 " + mPhotoName, progress + "%", progress);
    }

    /**
     * Called by Aria , 监听是否完成
     */
    @Download.onTaskComplete
    public void onDownloadComplete(DownloadTask task) {
        if (task == null || !task.getDownloadPath().equals(mPath + ".TEMP")) return;
        stopForeground(true);
        NotificationUtil.createNotification(this, mNotificationId, mPhotoName, "下载完成", -1);
        File oldFile = new File(mPath + ".TEMP");
        Log.i("DownloadService", "onDownloadComplete: " + oldFile.renameTo(new File(mPath)));
        Map.Entry<String, Integer> entry = AriaApp.getInstance().getMode();
        if (entry != null) {
            if (entry.getValue() == PreviewPresenter.SET_WALLPAPER) {
                Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
                    emitter.onNext(0);
                    emitter.onNext(FileUtil.setWallpaper(DownloadService.this, mPath));
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new ObserverImpl<Integer>() {
                            @Override
                            public void onNext(Integer flag) {
                                if (flag == 0) {
                                    Toast.makeText(DownloadService.this, "稍等片刻", Toast.LENGTH_SHORT).show();
                                } else if (flag == 1) {
                                    Toast.makeText(DownloadService.this, "设置成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(DownloadService.this, "设置失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    /**
     * Called by Aria , 监听是否失败
     */
    @Download.onTaskFail
    public void onDownloadFailure(DownloadTask task) {
        if (task == null || !task.getDownloadPath().equals(mPath + ".TEMP")) return;
        stopForeground(true);
        NotificationUtil.createNotification(this, mNotificationId, mPhotoName, "下载失败", -1);
        File file = new File(task.getDownloadPath());
        if (file.exists()) {
            Log.i("DownloadService", "onDownloadFailure: " + file.delete());
        }
    }
}
