package com.example.android.camera2basic;

import android.content.Context;
import android.os.Handler;

import java.util.TimerTask;

/**
 * Created by Yoshiki on 2017/07/23.
 */

public class ReloadTimer extends TimerTask {
    private Handler handler;
    private Context context;

    public ReloadTimer(Context context) {
        handler = new Handler();
        this.context = context;

    }

    @Override
    public void run() {
//        ((CameraActivity) context).getObjects();
        ((CameraActivity) context).displayReload(false);
    }
}
