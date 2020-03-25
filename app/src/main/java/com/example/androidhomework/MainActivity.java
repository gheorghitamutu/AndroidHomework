package com.example.androidhomework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import static android.content.Intent.EXTRA_TEXT;

// https://stackoverflow.com/questions/13022677/save-state-of-activity-when-orientation-changes-android

public class MainActivity extends AppCompatActivity
        implements ExampleDialog.ExampleDialogListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MainActivity";
    final int productCount = 4;
    final private String productTextViewValueContent = "productTextViewValueContent";
    ListView listView;
    TextView productTextView;

    private static final String FILE_NAME = "shared_preferences_lab_05";
    TextView potentiallyVisibleTextView;
    EditText contentToSaveEditText;

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

        setupSharedPreferences();
        contentToSaveEditText = findViewById(R.id.edit_text);
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
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                ShowDialog(item.getTitle().toString());
                break;
            case R.id.item2:
                GoToActivityThroughIntent(item.getTitle().toString());
                break;
            case R.id.item3:
                this.finishActivity(0);
                break;
            case R.id.item4:
                openSettingsActivity();
                break;
        }
        return true;
    }

    public void GoToActivityThroughIntent(String item_name) {
        Intent intent = new Intent(this, ExampleActivity.class);
        intent.putExtra(EXTRA_TEXT, item_name);
        startActivity(intent);
    }

    public void ShowDialog(String message) {
        ExampleDialog exampleDialog = new ExampleDialog(message);
        exampleDialog.show(getSupportFragmentManager(), "Example dialog.");
    }

    // dialog listener implementation (implements ExampleDialog.ExampleDialogListener)
    @Override
    public void applyTexts(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void setTextVisible(boolean display_text) {
        if (display_text) {
            potentiallyVisibleTextView.setVisibility(View.VISIBLE);
        } else {
            potentiallyVisibleTextView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("display_text")) {
            setTextVisible(sharedPreferences.getBoolean("display_text", true));
        }
    }

    public void openSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void save(View v) {
        String text = contentToSaveEditText.getText().toString();
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(text.getBytes());

            contentToSaveEditText.getText().clear();

            Toast.makeText(
                    this,
                    String.format("Content saved to %s/%s", getFilesDir(), FILE_NAME),
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void load(View v) {
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }

            contentToSaveEditText.setText(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}