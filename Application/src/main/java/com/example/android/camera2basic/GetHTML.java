package com.example.android.camera2basic;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class GetHTML extends AsyncTask<String, Void, String> {

    private Product product;

    public GetHTML(Product product) {
        // 呼び出し元のアクティビティ
        this.product = product;
    }

    // パラメータとして渡されたURLのHTMLを取得し、onPostExecuteに処理を投げる
    @Override
    protected String doInBackground(String... page_url) {
        URL url;
        try {

            //アクセスしたいページpage_url
            url = new URL(page_url[0]);
            URLConnection conn = url.openConnection();
            System.out.println("GetHTML doInBackground Start");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            System.out.println("GH 1");
            StringBuffer response = new StringBuffer();
            System.out.println("GH 2");
            String line;
            System.out.println("GH 3");
            // ここは一瞬
            while ((line = in.readLine()) != null) {
                response.append(line + "\n");
            }
            System.out.println("GH 4");
            in.close();
            System.out.println("GetHTML doInBackground End");
            return response.toString();
        }
        catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
            return "";
        }
    }
    /*
     * メインスレッドで実行する処理
     *
     *  @param param: doInBackground()から受け渡されるデータ
     */
    @Override
    protected void onPostExecute(String html) {

        product.iFrameHTML = html;
        System.out.println("SET DATA BY HTML START");
        product.setDataByHtml();
        System.out.println("SET DATA BY HTML END");
        product.activity.displayReload(false);
        return;
    }
}