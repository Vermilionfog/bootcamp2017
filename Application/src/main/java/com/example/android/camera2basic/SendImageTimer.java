package com.example.android.camera2basic;

import android.content.Context;
import android.os.Handler;

import java.util.TimerTask;

/**
 * Created by Yoshiki on 2017/07/10.
 */

public class SendImageTimer extends TimerTask {
    private Handler handler;
    private Context context;
    public Product result;

    public SendImageTimer(Context context) {
        handler = new Handler();
        this.context = context;

    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ((CameraActivity) context).sendImage();
            }
        });
    }

}