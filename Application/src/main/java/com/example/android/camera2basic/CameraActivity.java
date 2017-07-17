/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2basic;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Activity {

    // 表示するデータ
    public Timer timer ;
    public Product displayProduct; //画面下部に表示する商品データ

    // データを乗せるTextViewやRatingBarなど
    public TextView review_1;
    public TextView review_2;
    public TextView review_3;

    public RatingBar rating;

    //public ImageView productImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }

        setEventListnerToFavoriteButton();
        fixMediaDir();
        sendImageTimer();
    }


    // TextViewなどをIDから取得
    public void getObjects()
    {
        review_1 = (TextView)findViewById(R.id.reviewView_1);
        review_2 = (TextView)findViewById(R.id.reviewView_2);
        review_3 = (TextView)findViewById(R.id.reviewView_3);

        rating = (RatingBar)findViewById(R.id.ratingBar);
        //productImage = (ImageView) findViewById(R.id.productImage);
    }


    // Timerを定義する。 実行内容はsendImageメソッド
    public void sendImageTimer(){
        timer = new Timer();
        TimerTask timerTask = new SendImageTimer(this);
        timer.scheduleAtFixedRate(timerTask, 0, 500);
    }

    //お気に入りボタンにイベントを追加設定する
    public void setEventListnerToFavoriteButton()
    {
        // お気に入りボタンをクリックした時、現在表示中の商品をお気に入り登録する
        // お気に入りボタンを表示を変更する
        // 後でお気に入り一覧を確認する為、DBに登録する。
    }


    // テスト用データを設定するメソッド
    public void setTestData()
    {
        displayProduct = new Product("", 0, new Review[3], 0f, ""); //テスト用データを格納する為、nullで作成を行う
        displayProduct.setDammyData();
    }


    // TextureViewに設定されている画像を引数として、APIに画像を送る関数を呼び出す
    public void sendImage()
    {

        //画像を保存し、実際にTextureViewから画像を取り出せるかテストを行った。
//        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
//            System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath());
//            TextureView texture = (TextureView) findViewById(R.id.texture);
//            saveBitmap(texture.getBitmap());
//        }

    }

    public void setResult()
    {
        // 本来はonCreateで呼ぶべき処理なのだが、そちらで呼ぶとオブジェクトが取得出来ない。onCreate後に行われる処理で何か定義されているものがある可能性アリ。暫定的にタイマーで処理を行う此方に記述する。
        getObjects();

        setImage();
        setReviews();
        setRating();
    }

    public void setImage()
    {
        GetBitmap task = new GetBitmap(this);
        task.execute(displayProduct.imageURL);
    }

    public void setReviews()
    {
        review_1.setText(displayProduct.reviews[0].title);
        review_2.setText(displayProduct.reviews[1].title);
        review_3.setText(displayProduct.reviews[2].title);
    }

    public void setRating()
    {
        rating.setRating(displayProduct.rating);
    }

    public void setPrice()
    {

    }



    public void saveBitmap(Bitmap bitmap)
    {
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "", "");
    }

    public void fixMediaDir() {
        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard != null) {
            File mediaDir = new File(sdcard, "DCIM/Camera");
            if (!mediaDir.exists()) {
                mediaDir.mkdirs();
            }
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }
}
