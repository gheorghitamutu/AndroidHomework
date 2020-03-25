package com.example.androidhomework;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

public class SettingsActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String CONTENT_VARIABLE_NAME = "content";
    public static final String SWITCH_VARIABLE_NAME = "saveSwitch";

    private TextView messageTextView;
    private EditText contentToSaveEditText;
    private Switch saveSwitch;

    private String content;
    private boolean switchValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        messageTextView = findViewById(R.id.textview);
        contentToSaveEditText = findViewById(R.id.edittext);
        Button applyTextButton = findViewById(R.id.apply_texr_button);
        Button saveButton = findViewById(R.id.save_button);
        saveSwitch = findViewById(R.id.switch1);

        applyTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageTextView.setText(contentToSaveEditText.getText().toString());
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        loadData();
        updateViews();
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CONTENT_VARIABLE_NAME, messageTextView.getText().toString());
        editor.putBoolean(SWITCH_VARIABLE_NAME, saveSwitch.isChecked());
        editor.apply();

        Toast.makeText(this, "Content has been saved!", Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        content = sharedPreferences.getString(CONTENT_VARIABLE_NAME, "");
        switchValue = sharedPreferences.getBoolean(SWITCH_VARIABLE_NAME, false);

        Toast.makeText(this, "Content has been loaded!", Toast.LENGTH_SHORT).show();
    }

    public void updateViews() {
        messageTextView.setText(content);
        saveSwitch.setChecked(switchValue);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
