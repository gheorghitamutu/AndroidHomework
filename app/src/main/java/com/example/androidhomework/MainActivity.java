package com.example.androidhomework;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<String> products = new ArrayList<>();

        for(int i = 0; i < 4; i++) {
            products.add(String.format(Locale.ENGLISH, "Product 0%d", i));
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, products);

        listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        // show the selected item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedItem = (String) listView.getItemAtPosition(position);
                final TextView productTextView = findViewById(R.id.product_view);
                productTextView.setText(clickedItem);
            }
        });
    }
}