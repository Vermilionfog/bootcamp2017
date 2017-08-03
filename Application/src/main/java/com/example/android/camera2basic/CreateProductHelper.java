package com.example.android.camera2basic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CreateProductHelper extends SQLiteOpenHelper{

    // Constructor
    public CreateProductHelper( Context con ){
        // Setting DB Name Here
        super(con,"dbsample011",null,1);//
    }

    @Override
    public void onCreate(SQLiteDatabase db){
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion){
    }

}

