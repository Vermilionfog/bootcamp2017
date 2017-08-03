package com.example.android.camera2basic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ProductDB {

    public static CreateProductHelper helper = null;
    public static SQLiteDatabase db = null;

    protected ProductDB(Context context) {
        // DB作成
        helper = new CreateProductHelper(context);
        db = helper.getWritableDatabase();
        Make_Table();

    }

    public void Make_Table(){
        db = helper.getWritableDatabase();
        try{
            // SQL文定義
            String sql
                    = "create table products (" +
                    "_id integer primary key autoincrement," +
                    "imageURL text," +
                    "review_title1 text," +
                    "review_text1 text," +
                    "review_title2 text," +
                    "review_text2 text," +
                    "review_title3 text," +
                    "review_text3 text," +
                    "rating real," +
                    "price integer," +
                    "name text," +
                    "favorite integer," +
                    "amazonURL text)";
            // SQL実行
            db.execSQL(sql);
            System.out.println( "テーブルできたよー" );
        }catch(Exception e){
            System.out.println( "もうテーブルありますよー" );
            Log.e("ERROR",e.toString());
        }
    }

    public void Insert_DB( Product product ){
        System.out.println( "登録します。" );
        // データ登録
        try{
            // トランザクション制御開始
            db = helper.getReadableDatabase();
            db.beginTransaction();

            // 登録データ設定
            ContentValues val = new ContentValues();

            if( product.imageURL == null ){
                val.put("imageURL", " " );
            }else {
                val.put("imageURL", product.imageURL );
            }

            if( product.reviews[0].title == null ){
                val.put("review_title1", " " );
            }else {
                val.put("review_title1", product.reviews[0].title );
            }

            if( product.reviews[0].text == null ){
                val.put("review_text1", " " );
            }else {
                val.put("review_text1", product.reviews[0].text );
            }

            if( product.reviews[1].title == null ){
                val.put("review_title2", " " );
            }else {
                val.put("review_title2", product.reviews[1].title );
            }

            if( product.reviews[1].text == null ){
                val.put("review_text2", " " );
            }else {
                val.put("review_text2", product.reviews[1].text );
            }

            if( product.reviews[2].title == null ){
                val.put("review_title3", " " );
            }else {
                val.put("review_title3", product.reviews[2].title );
            }

            if( product.reviews[2].text == null ){
                val.put("review_text3", " " );
            }else {
                val.put("review_text3", product.reviews[2].text );
            }

            if( product.rating == null ){
                val.put("rating", 0 );
            }else {
                val.put("rating", product.rating );
            }

            if( product.price == null ){
                val.put("price", 0 );
            }else {
                val.put("price", product.price );
            }

            if( product.name == null ){
                val.put("name", " " );
            }else {
                val.put("name", product.name );
            }

            if( product.favorite == null ){
                val.put("favorite", 0 );
            }else {
                val.put("favorite", product.favorite );
            }

            if( product.amazonURL == null ){
                val.put("amazonURL", " " );
            }else {
                val.put("amazonURL", product.amazonURL );
            }

            // データ登録
            db.insert("products", null, val);

            // コミット
            db.setTransactionSuccessful();

            // トランザクション制御終了
            db.endTransaction();

            // メッセージ設定
            System.out.println( "データ登録できました!!!" );
        }catch(Exception e){
            System.out.println( "データ登録失敗しました..." );
            Log.e("ERROR",e.toString());
        }
    }

    public void DeleteDB(Integer id)
    {
        String condition = "_id =" + id;
        db.delete("products",condition,null);
    }


    public void getProductById(int _id)
    {

        // 列名定義
        String columns[] = {"_id", "imageURL", "review_title1", "review_text1","review_title2", "review_text2","review_title3", "review_text3", "rating","price","name", "favorite", "amazonURL"};


        // SQL文定義
        String sql
                = "create table products (" +
                "_id integer primary key autoincrement," +
                "imageURL text," +
                "review_title1 text," +
                "review_text1 text," +
                "review_title2 text," +
                "review_text2 text," +
                "review_title3 text," +
                "review_text3 text," +
                "rating real," +
                "price integer," +
                "name text," +
                "favorite integer," +
                "amazonURL text)";

        // データ取得
        Cursor cursor = db.query(
                "products", columns, null, null, null, null, "_id");

    }

    public void getProductByName(String _name)
    {
//
//        // データ取得
//        Cursor cursor = db.query(
//                "products", columns, null, null, null, null, "_id");
//
//        // 取得したデータをテーブル明細部に設定
//        while(cursor.moveToNext()){
//
//            TableRow row = new TableRow(ProductDB.this);
//
//            TextView productidtxt = new TextView(ProductDB.this);
//            productidtxt.setGravity(Gravity.CENTER_HORIZONTAL);
//            productidtxt.setText(cursor.getString(0));
//
//            TextView nametxt = new TextView(ProductDB.this);
//            nametxt.setGravity(Gravity.CENTER_HORIZONTAL);
//            nametxt.setText(cursor.getString(1));
//
//            TextView pricetxt = new TextView(ProductDB.this);
//            pricetxt.setGravity(Gravity.CENTER_HORIZONTAL);
//            pricetxt.setText(cursor.getString(2));
//
//            row.addView(productidtxt);
//            row.addView(nametxt);
//            row.addView(pricetxt);
//            tablelayout.addView(row);
//
//            // メッセージ設定
//            System.out.println("表示できました。");
//        }
//    }
}}

