package com.example.androidhomework;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

// https://stackoverflow.com/questions/33320373/mainactivity-must-implement-alertpositivelistener

public class ExampleDialog extends AppCompatDialogFragment {
    private EditText messageEditText;
    private String messagePassed;
    private ExampleDialogListener listener;

    ExampleDialog(String messagePassed) {
        this.messagePassed = messagePassed;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getContext(), R.layout.dialog, null);

        messageEditText = view.findViewById(R.id.edit_message);

        builder.setView(view)
                .setTitle("Send")
                .setNegativeButton("cancel", (dialog, which) -> {
                    // pass - it is automatically closed & destroyed, no further action needed
                }).setPositiveButton("ok", (dialog, which) -> {
            String message = messageEditText.getText().toString();
            listener.applyTexts(message);
        });

        TextView textMessage = view.findViewById(R.id.dialog_message);
        textMessage.setText(messagePassed);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ExampleDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    String.format("%s must implement ExampleDialogListener!", context.toString()));
        }
    }

    public interface ExampleDialogListener {
        void applyTexts(String message);
    }
}
