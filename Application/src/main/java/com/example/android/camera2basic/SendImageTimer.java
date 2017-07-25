package com.example.android.camera2basic;

import android.content.Context;
import android.os.Handler;
import android.widget.ProgressBar;

import java.util.TimerTask;

/**
 * Created by Yoshiki on 2017/07/10.
 */

public class SendImageTimer extends TimerTask {
    private Handler handler;
    private Context context;
    public Product result;
    public ProgressBar sendImageProgress;
    public static int COUNT;
    public static int RUN_COUNT = 8000;

    public SendImageTimer(Context context) {
        System.out.println("SEND IMAGE TIMER");
        COUNT = 0;
        sendImageProgress = ((CameraActivity)context).sendImageProgress;
        sendImageProgress.setMax(RUN_COUNT/10);
        handler = new Handler();
        this.context = context;

    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if( !((CameraActivity)context).fixFlag) {
                    COUNT++;
                    sendImageProgress.setProgress(COUNT);

                    if ((COUNT) == sendImageProgress.getMax()) {
                        COUNT = 0;
                        long start = System.currentTimeMillis();
                        ExecuteCoreAPI exeCoreAPI = new ExecuteCoreAPI(((CameraActivity) context));
                        exeCoreAPI.execute();
                        long end = System.currentTimeMillis();
                        System.out.println("SendImageTimer#run " + (end - start) + "ms");
                    }
                }
            }
        });
    }

}