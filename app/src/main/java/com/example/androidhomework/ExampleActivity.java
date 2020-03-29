package com.example.androidhomework;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ExampleActivity extends AppCompatActivity {
    private EditText messageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        Intent intent = getIntent();
        String messageFromParentActivity = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (messageFromParentActivity != null && !messageFromParentActivity.isEmpty()) {
            TextView messageReceivedTextView = findViewById(R.id.text_view_message_received);
            messageReceivedTextView.setText(String.format(
                    "Message from previous activity is [%s]", messageFromParentActivity));
        }

        messageEditText = findViewById(R.id.edit_text_message);

        Button shareMessageButton = findViewById(R.id.button_share_message);
        shareMessageButton.setOnClickListener(v -> shareMessage());
    }

    private void shareMessage() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, messageEditText.getText().toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share message");
        startActivity(shareIntent);
    }
}
