package com.oliviu.lab2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class UpdateActivity extends AppCompatActivity {
    private EditText etUpdateProjectName;
    private EditText etUpdateDescription;
    private TextView tvUpdateDeadline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);  // Move this to the top

        HandleActivities handleActivities = new HandleActivities();

        Button btnUpdate = findViewById(R.id.btnUpdate);  // Now btnUpdate will be initialized properly

        // Adding new fields
        etUpdateProjectName = findViewById(R.id.etUpdateProjectName);
        etUpdateDescription = findViewById(R.id.etUpdateDescription);
        tvUpdateDeadline = findViewById(R.id.tvUpdateDeadline);

        Intent intent = getIntent();
        String currentProjectName = intent.getStringExtra("projectName");
        String currentDescription = intent.getStringExtra("description");
        String currentDeadline = intent.getStringExtra("deadline");

        Map<View, String> viewDataMap = new HashMap<>();
        viewDataMap.put(etUpdateProjectName, currentProjectName);
        viewDataMap.put(etUpdateDescription, currentDescription);
        viewDataMap.put(tvUpdateDeadline, currentDeadline);

        handleActivities.setText(viewDataMap);

        // DatePickerDialog onClick listener
        tvUpdateDeadline.setOnClickListener(v -> {
            HandleActivities.showDateTimePicker(UpdateActivity.this, tvUpdateDeadline, currentDeadline);
        });

        // Update button click listener
        btnUpdate.setOnClickListener(v -> {
            HandleActivities.handleSaveOrUpdate(
                    UpdateActivity.this, etUpdateProjectName, etUpdateDescription,
                    tvUpdateDeadline, "Update",
                    intent.getIntExtra("position", -1)
            );
        });
    }
}


