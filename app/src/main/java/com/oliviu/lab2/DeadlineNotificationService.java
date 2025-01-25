package com.oliviu.lab2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class DeadlineNotificationService {

    private final Context context;

    public DeadlineNotificationService(Context context) {
        this.context = context.getApplicationContext(); // Use application context to avoid memory leaks
    }

    public void checkAndSendNotifications() {
        // Load projects from JSON
        ArrayList<Project> projectList = loadProjectsFromJsonFile();

        if (projectList == null || projectList.isEmpty()) {
            Log.e("DeadlineNotificationService", "No projects found or failed to load.");
            return;
        }

        // Get the current time
        long currentTime = System.currentTimeMillis();
        for (Project project : projectList) {
            // Check if the project deadline is within the next hour
            long deadlineTime = project.getDeadlineInMillis(); // Assuming this method exists in Project

            if (deadlineTime - currentTime <= 60 * 60 * 1000 && deadlineTime > currentTime) { // 1 hour in millis
                Log.d("DeadlineNotificationService", "Sending notification for project: " + project.getProjectName());
                sendNotification(project);
            }
        }
    }

    private ArrayList<Project> loadProjectsFromJsonFile() {
        ArrayList<Project> projectList = null;
        try (FileReader reader = new FileReader(new File(context.getExternalFilesDir(null), "projects.json"))) {
            Gson gson = new Gson();
            Type projectListType = new TypeToken<ArrayList<Project>>() {}.getType();
            projectList = gson.fromJson(reader, projectListType);
        } catch (IOException e) {
            Log.e("DeadlineNotificationService", "Error loading projects", e);
        }

        if (projectList == null) {
            Log.e("DeadlineNotificationService", "Project list is null");
        } else {
            Log.d("DeadlineNotificationService", "Loaded " + projectList.size() + " projects.");
        }

        return projectList;
    }

    private void sendNotification(Project project) {
        // Check if the POST_NOTIFICATIONS permission is granted
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("DeadlineNotificationService", "Notification permission not granted.");
            return;
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "project_deadline_channel")
                .setSmallIcon(R.drawable.ic_notification) // Replace with your app's icon
                .setContentTitle("Deadline Approaching: " + project.getProjectName())
                .setContentText("Your project deadline is approaching in less than an hour.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Send the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Log.d("sendNotification", "Trying to display notification for: " + project.getProjectName());
        notificationManager.notify((int) project.getDeadlineInMillis(), builder.build()); // Use unique ID for each notification
    }
}
