package com.example.android.camera2basic;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by usr0301385 on 2017/07/25.
 */

public class FavoriteActivity extends Activity {

    ListView listView;

    private static Product[] products = {
            new Product(), new Product()
    };

    protected void onCreate(Bundle savedInstanseState)
    {
        super.onCreate((savedInstanseState));
        setContentView(R.layout.activity_favorite);

        listView = (ListView)findViewById(R.id.productList);

        ArrayAdapter<Product> arrayAdapter = new ArrayAdapter<Product>(this, android.R.layout.simple_list_item_1, products);

        System.out.println(arrayAdapter.getClass());
        System.out.println(listView.getClass());

        listView.setAdapter(arrayAdapter);
        ArrayList<Product> list = new ArrayList<>();
        MyAdapter myAdapter = new MyAdapter(this);

        myAdapter.setProductList(list);
        listView.setAdapter(myAdapter);

    }


}
