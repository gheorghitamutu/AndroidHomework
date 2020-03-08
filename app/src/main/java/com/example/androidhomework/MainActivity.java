package com.example.androidhomework;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;


//https://stackoverflow.com/questions/13022677/save-state-of-activity-when-orientation-changes-android

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    final int productCount = 4;
    final private String productTextViewValueContent = "productTextViewValueContent";
    ListView listView;
    TextView productTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<String> products = new ArrayList<>();

        for (int i = 0; i < productCount; i++) {
            products.add(String.format(Locale.ENGLISH, "Product 0%d", i));
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, products);

        listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        // was this just rotated? check savedInstanceState value!
        productTextView = findViewById(R.id.product_view);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(productTextViewValueContent)) {
                String content = savedInstanceState.getString(productTextViewValueContent);
                productTextView.setText(content);
            }
        }

        // show the selected item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedItem = (String) listView.getItemAtPosition(position);
                productTextView.setText(clickedItem);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(productTextViewValueContent, productTextView.getText());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
}