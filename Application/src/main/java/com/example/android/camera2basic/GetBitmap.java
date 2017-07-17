package com.example.android.camera2basic;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class GetBitmap extends AsyncTask<String, Void, Bitmap> {

    private Activity cameraActivity;

    public GetBitmap(Activity activity) {

        // 呼び出し元のアクティビティ
        this.cameraActivity = activity;
    }

    // パラメータとして渡された画像URLの画像を取得し、onPostExecuteに処理を投げる
    @Override
    protected Bitmap doInBackground(String... imageURL) {
        URL url;
        InputStream istream;
        Bitmap bitmap;
        try {
            //画像のURLを直うち
            url = new URL(imageURL[0]);
            //インプットストリームで画像を読み込む
            istream = url.openStream();
            // 読み込んだファイルをビットマップに変換
            bitmap = BitmapFactory.decodeStream(istream);
            //インプットストリームを閉じる
            istream.close();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
            bitmap = null; //Nullだとエラーが出るので、認識できません、みたいな画像を入れるように後で変更
        }

        return bitmap;
    }
    /*
     * メインスレッドで実行する処理
     *
     *  @param param: doInBackground()から受け渡されるデータ
     */
    @Override
    protected void onPostExecute(Bitmap param) {
        ImageView productImage = (ImageView) cameraActivity.findViewById(R.id.productImage);
        productImage.setImageBitmap(param);
        return;
    }
}