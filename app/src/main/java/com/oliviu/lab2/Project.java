package com.oliviu.lab2;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Project {
    private String projectName;
    private String deadline;
    private String description;

    public Project(String projectName, String deadline, String description) {
        this.projectName = projectName;
        this.deadline = deadline;
        this.description = description;
    }

    public String getProjectName() { return projectName; }

    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getDeadline() { return deadline; }

    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    // New method to convert deadline string to milliseconds
    public long getDeadlineInMillis() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date date = dateFormat.parse(deadline);  // Parsing the date string
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));  // or the time zone your date string is in
            if (date != null) {
                Log.d("DeadlineNotificationService", "Parsed deadline: " + date.toString());
                return date.getTime();  // Return the time in milliseconds
            }
        } catch (ParseException e) {
            Log.e("DeadlineNotificationService", "Error parsing deadline: " + deadline, e);
        }
        return 0;
    }
}
