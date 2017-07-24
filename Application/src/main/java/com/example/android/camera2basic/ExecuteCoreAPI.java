package com.example.android.camera2basic;

import android.os.AsyncTask;

/**
 * Created by usr0301385 on 2017/07/24.
 */

public class ExecuteCoreAPI extends AsyncTask<String, Void, Boolean> {
    private CameraActivity cameraActivity;
    private long start;
    private long end;

    public ExecuteCoreAPI(CameraActivity activity) {
        start = System.currentTimeMillis();
        // 呼び出し元のアクティビティ
        this.cameraActivity = activity;
    }

    // パラメータとして渡された画像URLの画像を取得し、onPostExecuteに処理を投げる
    @Override
    protected Boolean doInBackground(String... imageURL) {

        cameraActivity.sendImage();
        return true;
    }
    /*
     * メインスレッドで実行する処理
     *
     *  @param param: doInBackground()から受け渡されるデータ
     */
    @Override
    protected void onPostExecute(Boolean param) {
        end = System.currentTimeMillis();
        System.out.println("ExecuteCoreAPI " + (end-start) + "ms");
        return;
    }
}
