package com.oliviu.lab2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AddActivity extends AppCompatActivity {
    private EditText etProjectName;
    private EditText etDescription;
    private TextView tvDeadline;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        etProjectName = findViewById(R.id.etProjectName);
        etDescription = findViewById(R.id.etDescription);
        tvDeadline = findViewById(R.id.tvDeadline);

        Button btnSave = findViewById(R.id.btnSave);

        // Initialize TextView with a default message
        tvDeadline.setText("Select Deadline");

        // DatePickerDialog onClick listener
        tvDeadline.setOnClickListener(v -> {
            HandleActivities.showDateTimePicker(AddActivity.this, tvDeadline, null);
        });

        // Save button click listener
        btnSave.setOnClickListener(v -> {
            HandleActivities.handleSaveOrUpdate(AddActivity.this, etProjectName, etDescription, tvDeadline, "Add", -1);
        });
    }
}
