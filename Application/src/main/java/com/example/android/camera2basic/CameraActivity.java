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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.TextureView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends FragmentActivity {

    // 表示するデータ
    public Timer sendImageTimer ;
    public Timer displayReloadTimer;
    public Product displayProduct; //画面下部に表示する商品データ

    //カメラの映像を表示するTextureView
    public TextureView cameraView;

    // データを乗せるTextViewやRatingBarなど
    public TextView reviewTitle_1;
    public TextView reviewTitle_2;
    public TextView reviewTitle_3;
    public TextView reviewText_1;
    public TextView reviewText_2;
    public TextView reviewText_3;

    public RatingBar rating;
    public TextView price;
    public TextView name;

    // 各種ボタン
    public ImageButton favoriteButton;
    public ImageButton amazonButton;
    public ToggleButton fixButton;
    public ImageButton moveFavoriteList;

    public Boolean initFlag = false; //ビューの初期化が行われていればTrueに。非同期処理の関係で、初期化の可否を確認するフラグが必要であった為追加。
    public Boolean fixFlag = false; // 更新せず固定するフラグ

    //デバッグ用
    public TextView recognitionText;
    public ImageView sendImageView;

    // タイマーの残り時間確認
    public ProgressBar sendImageProgress;

    // APIの実行結果
    private String APIResult = "";

    // Product DB 作成
    public static ProductDB productDB;
    ViewPager viewPager;
    //public ImageView productImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        make_db();
        setContentView(R.layout.activity_camera);

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }

//        viewPager = (ViewPager)findViewById(R.id.viewPager);
//        viewPager.setAdapter(new MyFragmentStatePagerAdapter(getSupportFragmentManager()));
        // カスタム PagerAdapter を生成
//        MyPagerAdapter adapter = new MyPagerAdapter(this);
//        adapter.add(Color.BLACK);
//        adapter.add(Color.RED);
//        adapter.add(Color.GREEN);
//        adapter.add(Color.BLUE);
//        adapter.add(Color.CYAN);
//        adapter.add(Color.MAGENTA);
//        adapter.add(Color.YELLOW);
//
//        // ViewPager を生成
//        ViewPager viewPager = new ViewPager(this);
//        viewPager.setAdapter(adapter);

//        setContentView(viewPager);


        sendImageTimer = new Timer();
        displayReloadTimer = new Timer();
        fixMediaDir();
        displayProduct = new Product(this);

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        getObjects();
        sendImageTimer();
    }



    // TextViewなどをIDから取得、恐らくFragmentが追加されて後に実行しないとエラーが発生する
    public void getObjects()
    {
        // オブジェクトを取得し保存
        cameraView = (TextureView) findViewById(R.id.texture);
        reviewTitle_1 = (TextView) findViewById(R.id.reviewTitle_1);
        reviewTitle_2 = (TextView) findViewById(R.id.reviewTitle_2);
        reviewTitle_3 = (TextView) findViewById(R.id.reviewTitle_3);
        reviewText_1 = (TextView) findViewById(R.id.reviewText_1);
        reviewText_2 = (TextView) findViewById(R.id.reviewText_2);
        reviewText_3 = (TextView) findViewById(R.id.reviewText_3);
        rating = (RatingBar) findViewById(R.id.ratingBar);
        price = (TextView) findViewById(R.id.price);
        name = (TextView) findViewById(R.id.name);

        favoriteButton = (ImageButton) findViewById(R.id.favoriteButton);
        amazonButton = (ImageButton) findViewById(R.id.linkToAmazon);
        fixButton = (ToggleButton) findViewById(R.id.fixButton);
        moveFavoriteList = (ImageButton) findViewById(R.id.moveFavoriteList);

        //デバッグ用
        recognitionText = (TextView)findViewById(R.id.recognitionText);
        sendImageView = (ImageView)findViewById(R.id.sendImageView);

        //タイマーの進捗
        sendImageProgress = (ProgressBar)findViewById(R.id.sendImageProgress);

        // 行数指定などの細かい設定
        setTextViewOption(name, TextUtils.TruncateAt.MARQUEE);
        setTextViewOption(reviewTitle_1, TextUtils.TruncateAt.END);
        setTextViewOption(reviewTitle_2, TextUtils.TruncateAt.END);
        setTextViewOption(reviewTitle_3, TextUtils.TruncateAt.END);

        // イベント設定、これらも本来はOnCreateでやる処理だが、そちらだとオブジェクトが取得出来ない為こちらで定義。
        setEventListnerTolinkToAmazonButton();
        setEventListnerToFavoriteButton();
        setEventListnerToFixButton();
        setEventListnerTolinkToMoveFavoriteListButton();

        initFlag = true;
    }

    private void setEventListnerTolinkToMoveFavoriteListButton() {
        moveFavoriteList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(getApplication(), FavoriteActivity.class);
            startActivity(intent);
            }
        });
    }


    // Timerを定義する。 実行内容はsendImageメソッド
    public void sendImageTimer(){
        TimerTask timerTask = new SendImageTimer(this);
        //ProgressBarとの兼ね合いでここは固定で10msec単位、 実行感覚はSendImageTimerクラスの中で決める。
        sendImageTimer.scheduleAtFixedRate(timerTask, 0, 10);
    }

    public void setResultTimer(){
        TimerTask timerTask = new ReloadTimer(this);
        displayReloadTimer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    //お気に入りボタンにイベントを追加設定する
    public void setEventListnerToFavoriteButton()
    {
        favoriteButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view)
            {
                // お気に入りの状態を変更する
                displayProduct.favorite = !displayProduct.favorite;

                if(displayProduct.favorite)
                {
                    //お気に入りに追加する
                    productDB.Insert_DB(displayProduct);
                    favoriteButton.setImageResource(R.drawable.favorite_on);
                }
                else
                {
                    //お気に入りから削除する
                    favoriteButton.setImageResource(R.drawable.favorite_off);
                }
            }
        });

    }

    // 詳細ボタンを押した時の処理
    public void setEventListnerTolinkToAmazonButton()
    {
        amazonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("AmazonURL :" + displayProduct.amazonURL);
                Uri uri = Uri.parse(displayProduct.amazonURL);
                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i);
            }
        });
    }

    public void setEventListnerToFixButton()
    {

        fixButton.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fixFlag = fixButton.isChecked();
            }
        });
    }

    // FixButtonの状態を変更する
    public void changeDisplayFixButton()
    {

    }




    // テスト用データを設定するメソッド
    public void setTestData()
    {
//        String test_xml = "<?xml version=\"1.0\" ?><ItemSearchResponse xmlns=\"http://webservices.amazon.com/AWSECommerceService/2013-08-01\"><OperationRequest><HTTPHeaders><Header Name=\"UserAgent\" Value=\"Python-urllib/3.5\"></Header></HTTPHeaders><RequestId>c174984d-8272-46db-80c9-55c094ec756b</RequestId><Arguments><Argument Name=\"AWSAccessKeyId\" Value=\"AKIAJPDXZXBB46X637HA\"></Argument><Argument Name=\"AssociateTag\" Value=\"iatlex-20\"></Argument><Argument Name=\"Keywords\" Value=\"python\"></Argument><Argument Name=\"Operation\" Value=\"ItemSearch\"></Argument><Argument Name=\"ResponseGroup\" Value=\"Medium,Reviews\"></Argument><Argument Name=\"SearchIndex\" Value=\"All\"></Argument><Argument Name=\"Service\" Value=\"AWSECommerceService\"></Argument><Argument Name=\"Timestamp\" Value=\"2017-07-20T14:50:20Z\"></Argument><Argument Name=\"Version\" Value=\"2013-08-01\"></Argument><Argument Name=\"Signature\" Value=\"MGxR0ULPgcBjqsc+HKE2kBFH2qq/GfjG3rtP4IgGKD4=\"></Argument></Arguments><RequestProcessingTime>0.1585142690000000</RequestProcessingTime></OperationRequest><Items><Request><IsValid>True</IsValid><ItemSearchRequest><Keywords>python</Keywords><ResponseGroup>Medium</ResponseGroup><ResponseGroup>Reviews</ResponseGroup><SearchIndex>All</SearchIndex></ItemSearchRequest></Request><TotalResults>25788</TotalResults><TotalPages>2579</TotalPages><MoreSearchResultsUrl>https://www.amazon.co.jp/gp/search?linkCode=xm2&amp;SubscriptionId=AKIAJPDXZXBB46X637HA&amp;keywords=python&amp;tag=iatlex-20&amp;creative=5143&amp;url=search-alias%3Daws-amazon-aps&amp;camp=2025</MoreSearchResultsUrl><Item><ASIN>487311778X</ASIN><DetailPageURL>https://www.amazon.co.jp/%E9%80%80%E5%B1%88%E3%81%AA%E3%81%93%E3%81%A8%E3%81%AFPython%E3%81%AB%E3%82%84%E3%82%89%E3%81%9B%E3%82%88%E3%81%86-%E2%80%95%E3%83%8E%E3%83%B3%E3%83%97%E3%83%AD%E3%82%B0%E3%83%A9%E3%83%9E%E3%83%BC%E3%81%AB%E3%82%82%E3%81%A7%E3%81%8D%E3%82%8B%E8%87%AA%E5%8B%95%E5%8C%96%E5%87%A6%E7%90%86%E3%83%97%E3%83%AD%E3%82%B0%E3%83%A9%E3%83%9F%E3%83%B3%E3%82%B0-Al-Sweigart/dp/487311778X?SubscriptionId=AKIAJPDXZXBB46X637HA&amp;tag=iatlex-20&amp;linkCode=xm2&amp;camp=2025&amp;creative=165953&amp;creativeASIN=487311778X</DetailPageURL><ItemLinks><ItemLink><Description>Add To Wishlist</Description><URL>https://www.amazon.co.jp/gp/registry/wishlist/add-item.html?asin.0=487311778X&amp;SubscriptionId=AKIAJPDXZXBB46X637HA&amp;tag=iatlex-20&amp;linkCode=xm2&amp;camp=2025&amp;creative=5143&amp;creativeASIN=487311778X</URL></ItemLink><ItemLink><Description>Tell A Friend</Description><URL>https://www.amazon.co.jp/gp/pdp/taf/487311778X?SubscriptionId=AKIAJPDXZXBB46X637HA&amp;tag=iatlex-20&amp;linkCode=xm2&amp;camp=2025&amp;creative=5143&amp;creativeASIN=487311778X</URL></ItemLink><ItemLink><Description>All Customer Reviews</Description><URL>https://www.amazon.co.jp/review/product/487311778X?SubscriptionId=AKIAJPDXZXBB46X637HA&amp;tag=iatlex-20&amp;linkCode=xm2&amp;camp=2025&amp;creative=5143&amp;creativeASIN=487311778X</URL></ItemLink><ItemLink><Description>All Offers</Description><URL>https://www.amazon.co.jp/gp/offer-listing/487311778X?SubscriptionId=AKIAJPDXZXBB46X637HA&amp;tag=iatlex-20&amp;linkCode=xm2&amp;camp=2025&amp;creative=5143&amp;creativeASIN=487311778X</URL></ItemLink></ItemLinks><SalesRank>587</SalesRank><SmallImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL75_.jpg</URL><Height Units=\"pixels\">75</Height><Width Units=\"pixels\">53</Width></SmallImage><MediumImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL160_.jpg</URL><Height Units=\"pixels\">160</Height><Width Units=\"pixels\">113</Width></MediumImage><LargeImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL.jpg</URL><Height Units=\"pixels\">500</Height><Width Units=\"pixels\">354</Width></LargeImage><ImageSets><ImageSet Category=\"primary\"><SwatchImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL30_.jpg</URL><Height Units=\"pixels\">30</Height><Width Units=\"pixels\">21</Width></SwatchImage><SmallImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL75_.jpg</URL><Height Units=\"pixels\">75</Height><Width Units=\"pixels\">53</Width></SmallImage><ThumbnailImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL75_.jpg</URL><Height Units=\"pixels\">75</Height><Width Units=\"pixels\">53</Width></ThumbnailImage><TinyImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL110_.jpg</URL><Height Units=\"pixels\">110</Height><Width Units=\"pixels\">78</Width></TinyImage><MediumImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL160_.jpg</URL><Height Units=\"pixels\">160</Height><Width Units=\"pixels\">113</Width></MediumImage><LargeImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL.jpg</URL><Height Units=\"pixels\">500</Height><Width Units=\"pixels\">354</Width></LargeImage><HiResImage><URL>https://images-fe.ssl-images-amazon.com/images/I/81i-G8YToFL.jpg</URL><Height Units=\"pixels\">2480</Height><Width Units=\"pixels\">1754</Width></HiResImage></ImageSet></ImageSets><ItemAttributes><Author>Al Sweigart</Author><Binding>単行本（ソフトカバー）</Binding><Creator Role=\"翻訳\">相川 愛三</Creator><EAN>9784873117782</EAN><EANList><EANListElement>9784873117782</EANListElement></EANList><IsAdultProduct>0</IsAdultProduct><ISBN>487311778X</ISBN><Label>オライリージャパン</Label><Languages><Language><Name>日本語</Name><Type>Published</Type></Language></Languages><Manufacturer>オライリージャパン</Manufacturer><NumberOfPages>616</NumberOfPages><PackageDimensions><Height Units=\"100分の1インチ\">134</Height><Length Units=\"100分の1インチ\">858</Length><Weight Units=\"100分の1ポンド\">168</Weight><Width Units=\"100分の1インチ\">591</Width></PackageDimensions><ProductGroup>Book</ProductGroup><ProductTypeName>ABIS_BOOK</ProductTypeName><PublicationDate>2017-06-03</PublicationDate><Publisher>オライリージャパン</Publisher><Studio>オライリージャパン</Studio><Title>退屈なことはPythonにやらせよう ―ノンプログラマーにもできる自動化処理プログラミング</Title></ItemAttributes><OfferSummary><LowestNewPrice><Amount>3996</Amount><CurrencyCode>JPY</CurrencyCode><FormattedPrice>￥ 3,996</FormattedPrice></LowestNewPrice><LowestUsedPrice><Amount>4395</Amount><CurrencyCode>JPY</CurrencyCode><FormattedPrice>￥ 4,395</FormattedPrice></LowestUsedPrice><TotalNew>6</TotalNew><TotalUsed>6</TotalUsed><TotalCollectible>0</TotalCollectible><TotalRefurbished>0</TotalRefurbished></OfferSummary><CustomerReviews><IFrameURL>https://www.amazon.jp/reviews/iframe?akid=AKIAJPDXZXBB46X637HA&amp;alinkCode=xm2&amp;asin=487311778X&amp;atag=iatlex-20&amp;exp=2017-07-21T05%3A50%3A20Z&amp;v=2&amp;sig=Nmf%252FvQq4smLhFB9dNaWVpeYm2BBHQmFmEQleMa9I4jY%253D</IFrameURL><HasReviews>true</HasReviews></CustomerReviews></Item></Items></ItemSearchResponse>";
        String test_xml = "<?xml version=\"1.0\" ?><ItemSearchResponse xmlns=\"http://webservices.amazon.com/AWSECommerceService/2013-08-01\"><OperationRequest><HTTPHeaders><Header Name=\"UserAgent\" Value=\"Python-urllib/3.5\"></Header></HTTPHeaders><RequestId>0b98c2e3-5f79-4354-9a4f-6f87581f7553</RequestId><Arguments><Argument Name=\"AWSAccessKeyId\" Value=\"AKIAJPDXZXBB46X637HA\"></Argument><Argument Name=\"AssociateTag\" Value=\"iatlex-20\"></Argument><Argument Name=\"Keywords\" Value=\"python\"></Argument><Argument Name=\"Operation\" Value=\"ItemSearch\"></Argument><Argument Name=\"ResponseGroup\" Value=\"Medium,Reviews\"></Argument><Argument Name=\"SearchIndex\" Value=\"All\"></Argument><Argument Name=\"Service\" Value=\"AWSECommerceService\"></Argument><Argument Name=\"Timestamp\" Value=\"2017-07-21T12:31:35Z\"></Argument><Argument Name=\"Version\" Value=\"2013-08-01\"></Argument><Argument Name=\"Signature\" Value=\"CLXdgdBSLrGSqc0VDq9fCGRlRSzojfr0FT7cx07vHLg=\"></Argument></Arguments><RequestProcessingTime>0.1863752980000000</RequestProcessingTime></OperationRequest><Items><Request><IsValid>True</IsValid><ItemSearchRequest><Keywords>python</Keywords><ResponseGroup>Medium</ResponseGroup><ResponseGroup>Reviews</ResponseGroup><SearchIndex>All</SearchIndex></ItemSearchRequest></Request><TotalResults>25815</TotalResults><TotalPages>2582</TotalPages><MoreSearchResultsUrl>https://www.amazon.co.jp/gp/search?linkCode=xm2&amp;SubscriptionId=AKIAJPDXZXBB46X637HA&amp;keywords=python&amp;tag=iatlex-20&amp;creative=5143&amp;url=search-alias%3Daws-amazon-aps&amp;camp=2025</MoreSearchResultsUrl><Item><ASIN>487311778X</ASIN><DetailPageURL>https://www.amazon.co.jp/%E9%80%80%E5%B1%88%E3%81%AA%E3%81%93%E3%81%A8%E3%81%AFPython%E3%81%AB%E3%82%84%E3%82%89%E3%81%9B%E3%82%88%E3%81%86-%E2%80%95%E3%83%8E%E3%83%B3%E3%83%97%E3%83%AD%E3%82%B0%E3%83%A9%E3%83%9E%E3%83%BC%E3%81%AB%E3%82%82%E3%81%A7%E3%81%8D%E3%82%8B%E8%87%AA%E5%8B%95%E5%8C%96%E5%87%A6%E7%90%86%E3%83%97%E3%83%AD%E3%82%B0%E3%83%A9%E3%83%9F%E3%83%B3%E3%82%B0-Al-Sweigart/dp/487311778X?SubscriptionId=AKIAJPDXZXBB46X637HA&amp;tag=iatlex-20&amp;linkCode=xm2&amp;camp=2025&amp;creative=165953&amp;creativeASIN=487311778X</DetailPageURL><ItemLinks><ItemLink><Description>Add To Wishlist</Description><URL>https://www.amazon.co.jp/gp/registry/wishlist/add-item.html?asin.0=487311778X&amp;SubscriptionId=AKIAJPDXZXBB46X637HA&amp;tag=iatlex-20&amp;linkCode=xm2&amp;camp=2025&amp;creative=5143&amp;creativeASIN=487311778X</URL></ItemLink><ItemLink><Description>Tell A Friend</Description><URL>https://www.amazon.co.jp/gp/pdp/taf/487311778X?SubscriptionId=AKIAJPDXZXBB46X637HA&amp;tag=iatlex-20&amp;linkCode=xm2&amp;camp=2025&amp;creative=5143&amp;creativeASIN=487311778X</URL></ItemLink><ItemLink><Description>All Customer Reviews</Description><URL>https://www.amazon.co.jp/review/product/487311778X?SubscriptionId=AKIAJPDXZXBB46X637HA&amp;tag=iatlex-20&amp;linkCode=xm2&amp;camp=2025&amp;creative=5143&amp;creativeASIN=487311778X</URL></ItemLink><ItemLink><Description>All Offers</Description><URL>https://www.amazon.co.jp/gp/offer-listing/487311778X?SubscriptionId=AKIAJPDXZXBB46X637HA&amp;tag=iatlex-20&amp;linkCode=xm2&amp;camp=2025&amp;creative=5143&amp;creativeASIN=487311778X</URL></ItemLink></ItemLinks><SalesRank>611</SalesRank><SmallImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL75_.jpg</URL><Height Units=\"pixels\">75</Height><Width Units=\"pixels\">53</Width></SmallImage><MediumImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL160_.jpg</URL><Height Units=\"pixels\">160</Height><Width Units=\"pixels\">113</Width></MediumImage><LargeImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL.jpg</URL><Height Units=\"pixels\">500</Height><Width Units=\"pixels\">354</Width></LargeImage><ImageSets><ImageSet Category=\"primary\"><SwatchImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL30_.jpg</URL><Height Units=\"pixels\">30</Height><Width Units=\"pixels\">21</Width></SwatchImage><SmallImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL75_.jpg</URL><Height Units=\"pixels\">75</Height><Width Units=\"pixels\">53</Width></SmallImage><ThumbnailImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL75_.jpg</URL><Height Units=\"pixels\">75</Height><Width Units=\"pixels\">53</Width></ThumbnailImage><TinyImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL110_.jpg</URL><Height Units=\"pixels\">110</Height><Width Units=\"pixels\">78</Width></TinyImage><MediumImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL._SL160_.jpg</URL><Height Units=\"pixels\">160</Height><Width Units=\"pixels\">113</Width></MediumImage><LargeImage><URL>https://images-fe.ssl-images-amazon.com/images/I/51hk%2B5bKNrL.jpg</URL><Height Units=\"pixels\">500</Height><Width Units=\"pixels\">354</Width></LargeImage><HiResImage><URL>https://images-fe.ssl-images-amazon.com/images/I/81i-G8YToFL.jpg</URL><Height Units=\"pixels\">2480</Height><Width Units=\"pixels\">1754</Width></HiResImage></ImageSet></ImageSets><ItemAttributes><Author>Al Sweigart</Author><Binding>単行本（ソフトカバー）</Binding><Creator Role=\"翻訳\">相川 愛三</Creator><EAN>9784873117782</EAN><EANList><EANListElement>9784873117782</EANListElement></EANList><IsAdultProduct>0</IsAdultProduct><ISBN>487311778X</ISBN><Label>オライリージャパン</Label><Languages><Language><Name>日本語</Name><Type>Published</Type></Language></Languages><Manufacturer>オライリージャパン</Manufacturer><NumberOfPages>616</NumberOfPages><PackageDimensions><Height Units=\"100分の1インチ\">134</Height><Length Units=\"100分の1インチ\">858</Length><Weight Units=\"100分の1ポンド\">168</Weight><Width Units=\"100分の1インチ\">591</Width></PackageDimensions><ProductGroup>Book</ProductGroup><ProductTypeName>ABIS_BOOK</ProductTypeName><PublicationDate>2017-06-03</PublicationDate><Publisher>オライリージャパン</Publisher><Studio>オライリージャパン</Studio><Title>退屈なことはPythonにやらせよう ―ノンプログラマーにもできる自動化処理プログラミング</Title></ItemAttributes><OfferSummary><LowestNewPrice><Amount>3996</Amount><CurrencyCode>JPY</CurrencyCode><FormattedPrice>￥ 3,996</FormattedPrice></LowestNewPrice><LowestUsedPrice><Amount>4395</Amount><CurrencyCode>JPY</CurrencyCode><FormattedPrice>￥ 4,395</FormattedPrice></LowestUsedPrice><TotalNew>6</TotalNew><TotalUsed>6</TotalUsed><TotalCollectible>0</TotalCollectible><TotalRefurbished>0</TotalRefurbished></OfferSummary><CustomerReviews><IFrameURL>https://www.amazon.jp/reviews/iframe?akid=AKIAJPDXZXBB46X637HA&amp;alinkCode=xm2&amp;asin=487311778X&amp;atag=iatlex-20&amp;exp=2017-07-22T03%3A31%3A34Z&amp;v=2&amp;sig=0hzO5lqEenBwCElNBrGMjh7mB3HuBXT2TbM0gkWP8N0%253D</IFrameURL><HasReviews>true</HasReviews></CustomerReviews></Item></Items></ItemSearchResponse>";
//        displayProduct = new Product(); //テスト用データを格納する為、nullで作成を行う
        displayProduct.getProductForXML(test_xml, false);
//        displayProduct.setDammyData();
    }

    public void setDisplayData(Boolean flag)
    {
        System.out.println("SET DISPLAY DATA");
        System.out.println(APIResult);
        displayProduct.getProductForXML(APIResult, flag);
    }

    // 引数で受け取ったテキストビューを一行表示にし、省略方法を指定する
    public void setTextViewOption(TextView target, TextUtils.TruncateAt truncate)
    {
        target.setSingleLine();
        target.setFocusableInTouchMode(true);
        target.setEllipsize(truncate);
        target.getPaint().setUnderlineText(true);
    }

    public void setVerticalScrollOption(TextView target)
    {
        target.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    // TextureViewに設定されている画像を引数として、APIに画像を送る関数を呼び出す
    public void sendImage()
    {
        long start = System.currentTimeMillis();
        if(initFlag && (!fixFlag) && cameraView.getBitmap() != null)
        {
            long convert_start = System.currentTimeMillis();

            // 本番用、カメラからbmp取得
            Bitmap bmp = cameraView.getBitmap();

            // 作られたサムネイルから横幅とそれに応じた高さを指定
            int dispWidth = bmp.getWidth();
            int dispHeight = bmp.getHeight();
            Matrix matrix = new Matrix();
            matrix.postScale(0.2f, 0.2f);
            // 元のサイズでBitmap作成
            bmp = Bitmap.createBitmap(bmp, 0, 0, dispWidth, dispHeight, matrix, true);
            String img_base64 = BMP_to_Base64(bmp);

            // APIクラス
//            setSendImageView(bmp);
            Core_API APIs = new Core_API(this);
            APIs.execute(img_base64);
        }

        long end = System.currentTimeMillis();
        System.out.println("SendImage " + (end-start) + "ms");
    }

    public void setSendImageView(Bitmap bmp)
    {
        final Bitmap view_bmp = bmp;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendImageView.setImageBitmap(view_bmp);
                // mTextView.setVisibility(View.VISIBLE);
            }
        });
    }


    public void displayReload(boolean flag)
    {
        System.out.println("DISPLAY RELOAD");
        if(initFlag)
        {
            setDisplayData(flag);
            setResult();
        }
    }


    public void setResult()
    {
        long start = System.currentTimeMillis();
        // 以前確認していた商品と同一名でなければ、表示されるデータを更新する。
        // この処理を挟まない場合
        if(!name.getText().equals(displayProduct.name))
        {
            System.out.println("SET RESULT 1");
            setPrice();
            setName();
            resetButton();
            setImage();
            System.out.println("SET RESULT 2");
        }
        // ReviewとRatingは非同期処理の関係で、データを取得出来るタイミングが僅かに異なる為別に分ける。
        // 分けない場合、レビューは更新されていないが、名称は更新されているという状態になり
        if(!reviewTitle_1.getText().equals(displayProduct.reviews[0].title))
        {
            System.out.println("SET RESULT 3");
            setRating();
            setReviews();
            System.out.println("SET RESULT 4");
        }
        long end = System.currentTimeMillis();
        System.out.println("SetResult " + (end-start) + "ms");

    }

    public void setImage()
    {
        GetBitmap task = new GetBitmap(this);
        task.execute(displayProduct.imageURL);
    }

    public void setReviews()
    {
        reviewTitle_1.setText(displayProduct.reviews[0].title);
        reviewTitle_2.setText(displayProduct.reviews[1].title);
        reviewTitle_3.setText(displayProduct.reviews[2].title);
        reviewText_1.setText(displayProduct.reviews[0].text);
        reviewText_2.setText(displayProduct.reviews[1].text);
        reviewText_3.setText(displayProduct.reviews[2].text);
    }

    public void setRating()
    {
        rating.setRating(displayProduct.rating);
    }

    public void setPrice()
    {
        price.setText("¥" + displayProduct.price);
    }

    public void setName() {
        System.out.println(displayProduct.name);
        name.setText(displayProduct.name);
    }

    public void resetButton()
    {
        // URLが何も入っていない時、Amazonへのリンクを非アクティブにする
        if(displayProduct.amazonURL.equals("")) {
            amazonButton.setActivated(false);
        }
        else
        {
            amazonButton.setActivated(true);
        }
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

    public static String BMP_to_Base64( Bitmap bmp ){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 0, baos);
        byte[] bytes = baos.toByteArray();
        String img_base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        return img_base64;
    }

    public void setAPIResult(String result)
    {
        APIResult = result;
    }

    public void setRecognitionText(String text)
    {
        recognitionText.setText(text);
    }

    // DB関連の処理
    public void make_db() {
        productDB = new ProductDB(getApplicationContext());
    };

}
