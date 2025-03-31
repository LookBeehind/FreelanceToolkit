package com.oliviu.lab2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Project> projectList;
    private ArrayAdapter<Project> adapter;
    private boolean isMultiSelectMode = false;
    private final Set<Integer> selectedItems = new HashSet<>();
    private Button btnDeleteSelected;

    private EditText etSearch;
    private ListView lvProjects;

    private final ActivityResultLauncher<Intent> addActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                String projectName = result.getData().getStringExtra("projectName");
                                String description = result.getData().getStringExtra("description");
                                String deadline = result.getData().getStringExtra("deadline");
                                assert projectName != null;
                                handleSaveOrUpdate(projectName, description, deadline, -1); // -1 indicates a new project
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> updateActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                String updatedProjectName = result.getData().getStringExtra("projectName");
                                String updatedDescription = result.getData().getStringExtra("description");
                                String updatedDeadline = result.getData().getStringExtra("deadline");
                                int position = result.getData().getIntExtra("position", -1);
                                if (position != -1) {
                                    assert updatedProjectName != null;
                                    handleSaveOrUpdate(updatedProjectName, updatedDescription, updatedDeadline, position);
                                }
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check and request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            } else {
                createNotificationChannel();
                scheduleDeadlineWorker(); // Schedule the Worker if permission is already granted
            }
        } else {
            createNotificationChannel();
            scheduleDeadlineWorker(); // Schedule the Worker for older Android versions
        }

        // Initialize the project list
        projectList = new ArrayList<>();

        // Initialize adapter with custom layout
        adapter = new ArrayAdapter<Project>(this, R.layout.list_item_project, projectList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_project, parent, false);
                }

                Project project = getItem(position);
                TextView tvProjectName = convertView.findViewById(R.id.tvProjectName);
                TextView tvDescription = convertView.findViewById(R.id.tvDescription);
                TextView tvDeadline = convertView.findViewById(R.id.tvDeadline);
                CheckBox checkBox = convertView.findViewById(R.id.checkbox);

                assert project != null;
                tvProjectName.setText(project.getProjectName());
                tvDescription.setText(project.getDescription());
                tvDeadline.setText(project.getDeadline());

                // Show checkbox in multi-select mode
                if (isMultiSelectMode) {
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setChecked(selectedItems.contains(position));
                } else {
                    checkBox.setVisibility(View.GONE);
                }

                // Handle checkbox toggle
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedItems.add(position);
                    } else {
                        selectedItems.remove(position);
                    }
                });

                return convertView;
            }
        };

        // Bind the adapter to the ListView
        ListView lvProjects = findViewById(R.id.lvProjects);
        lvProjects.setAdapter(adapter);

        // Load projects from JSON file after initializing adapter
        loadProjectsFromJsonFile("");

        // Button click listener to navigate to AddActivity
        Button btnAddProject = findViewById(R.id.btnAddProject);
        btnAddProject.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            addActivityLauncher.launch(intent);
        });

        // Set item click listener to update a project
        lvProjects.setOnItemClickListener((parent, view, position, id) -> {
            if (isMultiSelectMode) {
                if (selectedItems.contains(position)) {
                    selectedItems.remove(position);
                } else {
                    selectedItems.add(position);
                }

                adapter.notifyDataSetChanged();
            } else {
                // Open UpdateActivity if not in multi-select mode
                Project selectedProject = projectList.get(position);
                Intent intent = new Intent(MainActivity.this, UpdateActivity.class);
                intent.putExtra("projectName", selectedProject.getProjectName());
                intent.putExtra("description", selectedProject.getDescription());
                intent.putExtra("deadline", selectedProject.getDeadline());
                intent.putExtra("position", position);
                updateActivityLauncher.launch(intent);
            }
        });

        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        btnDeleteSelected.setVisibility(View.GONE);

        btnDeleteSelected.setOnClickListener(v -> {
            // Remove selected items from the list
            List<Project> itemsToRemove = new ArrayList<>();
            for (int position : selectedItems) {
                itemsToRemove.add(projectList.get(position));
            }
            projectList.removeAll(itemsToRemove);
            selectedItems.clear();
            isMultiSelectMode = false;

            adapter.notifyDataSetChanged();
            btnDeleteSelected.setVisibility(View.GONE);
            saveProjectsToJsonFile();
        });

        lvProjects.setOnItemLongClickListener((parent, view, position, id) -> {
            if (!isMultiSelectMode) {
                isMultiSelectMode = true;
                selectedItems.clear();
                selectedItems.add(position);

                adapter.notifyDataSetChanged();
                btnDeleteSelected.setVisibility(View.VISIBLE);
            }
            return true;
        });

        etSearch = findViewById(R.id.etSearch);
        ImageButton btnSearch = findViewById(R.id.searchButton);

        // Set click listener for the search button
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearch();
            }
        });

    }

    private void handleSaveOrUpdate(String projectName, String description, String deadline, int position) {
        if (projectName.isEmpty() || description.isEmpty() || deadline.isEmpty()) {
            Toast.makeText(this, "Name and deadline fields are required", Toast.LENGTH_SHORT).show();
        } else {
            if (position == -1) {
                Project newProject = new Project(projectName, deadline, description);
                projectList.add(newProject);
            } else {
                Project existingProject = projectList.get(position);
                existingProject.setProjectName(projectName);
                existingProject.setDescription(description);
                existingProject.setDeadline(deadline);
            }
            adapter.notifyDataSetChanged();
            saveProjectsToJsonFile();
        }
    }

    public void saveProjectsToJsonFile() {
        File file = new File(getExternalFilesDir(null), "projects.json");

        Gson gson = new Gson();
        String json = gson.toJson(projectList);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
            Log.d("MainActivity", "Projects saved to file: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("saveProjectsToJsonFile", String.valueOf(e));
            Toast.makeText(this, "Error saving projects to file", Toast.LENGTH_SHORT).show();
        }
    }

    public void onSearch() {
        String query = etSearch.getText().toString().trim();
        loadProjectsFromJsonFile(query);
    }

    public void loadProjectsFromJsonFile(String query) {
        File file = new File(getExternalFilesDir(null), "projects.json");

        if (file.exists()) {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(file)) {
                Type projectListType = new TypeToken<ArrayList<Project>>() {}.getType();
                ArrayList<Project> loadedProjects = gson.fromJson(reader, projectListType); // Deserialize JSON

                // Filter projects if a query is provided
                ArrayList<Project> filteredProjects = new ArrayList<>();
                if (query != null && !query.isEmpty()) {
                    for (Project project : loadedProjects) {
                        // Assuming Project class has a getName() method for filtering
                        if (project.getProjectName().toLowerCase().contains(query.toLowerCase())) {
                            filteredProjects.add(project);
                        }
                    }
                } else {
                    filteredProjects.addAll(loadedProjects);
                }

                // Update the project list and refresh the adapter
                projectList.clear();
                projectList.addAll(filteredProjects);
                adapter.notifyDataSetChanged();

                Log.d("MainActivity", "Projects loaded successfully with filter: " + query);
            } catch (IOException e) {
                Log.e("loadProjectsFromJsonFile", "Error reading JSON file: " + e);
                Toast.makeText(this, "Error loading projects from file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("MainActivity", "No saved projects found. File does not exist.");
            Toast.makeText(this, "No saved projects found", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleDeadlineWorker() {
        WorkManager.getInstance(this).cancelAllWork();

        WorkManager.getInstance(this)
                .enqueue(OneTimeWorkRequest.from(DeadlineWorker.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) { // POST_NOTIFICATIONS request code
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Notification permission granted.");
                scheduleDeadlineWorker();
            } else {
                Log.e("MainActivity", "Notification permission denied.");
            }
        }

    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "project_deadline_channel";
            String channelName = "Project Deadlines";
            String channelDescription = "Notifications for approaching project deadlines";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
