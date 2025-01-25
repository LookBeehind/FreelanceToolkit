package com.oliviu.lab2;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Map;

public class HandleActivities {

    // Method to show Date and Time Picker
    @SuppressLint("SetTextI18n")
    public static void showDateTimePicker(Context context, TextView tvDeadline, String currentDeadline) {
        // Get the current date and time from the TextView or use today's date and time if empty
        Calendar calendar = Calendar.getInstance();
        final int[] year = {calendar.get(Calendar.YEAR)};
        final int[] month = {calendar.get(Calendar.MONTH)};
        final int[] day = {calendar.get(Calendar.DAY_OF_MONTH)};
        final int[] hour = {calendar.get(Calendar.HOUR_OF_DAY)};
        final int[] minute = {calendar.get(Calendar.MINUTE)};

        if (currentDeadline != null && !currentDeadline.isEmpty()) {
            // If deadline is already set, extract date and time
            String[] dateParts = currentDeadline.split(" ");
            String[] dateValues = dateParts[0].split("-");
            year[0] = Integer.parseInt(dateValues[0]);
            month[0] = Integer.parseInt(dateValues[1]) - 1; // Month is 0-based
            day[0] = Integer.parseInt(dateValues[2]);

            // Extract the time part (HH:MM)
            String[] timeValues = dateParts[1].split(":");
            hour[0] = Integer.parseInt(timeValues[0]);
            minute[0] = Integer.parseInt(timeValues[1]);
        }

        // Show the DatePickerDialog
        @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, yearSelected, monthSelected, daySelected) -> {
                    // Set the picked date
                    year[0] = yearSelected;
                    month[0] = monthSelected;
                    day[0] = daySelected;

                    // After selecting the date, show the TimePickerDialog
                    @SuppressLint("DefaultLocale") TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                            (timePicker, hourOfDay, minuteOfHour) -> {
                                // Set the picked time
                                hour[0] = hourOfDay;
                                minute[0] = minuteOfHour;

                                // Create Calendar instance for the selected date and time
                                Calendar selectedDateTime = Calendar.getInstance();
                                selectedDateTime.set(year[0], month[0], day[0], hourOfDay, minuteOfHour);

                                // Compare the selected date/time with the current date/time
                                if (selectedDateTime.before(calendar)) {
                                    // Show a Toast if the selected date/time is in the past
                                    Toast.makeText(context, "Please select a future date and time.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Update the TextView with both date and time
                                    tvDeadline.setText(year[0] + "-" + (month[0] + 1) + "-" + day[0] + " " + hourOfDay + ":" + String.format("%02d", minuteOfHour));
                                }
                            }, hour[0], minute[0], true);
                    timePickerDialog.show();
                }, year[0], month[0], day[0]);

        datePickerDialog.show();
    }

    // Method to handle Save and Update Button click for common logic
    public static void handleSaveOrUpdate(Context context, @NonNull EditText etProjectName, EditText etDescription,
                                          @NonNull TextView tvDeadline, String activityType, int position) {
        String projectName = etProjectName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String deadline = tvDeadline.getText().toString().trim();

        if (projectName.isEmpty() || description.isEmpty() || deadline.isEmpty() || deadline.equals("Select Deadline")) {
            Toast.makeText(context, "Name and deadline fields are required", Toast.LENGTH_SHORT).show();
        } else {
            // Create result intent with project name, description, and deadline
            Intent resultIntent = new Intent();
            resultIntent.putExtra("projectName", projectName);
            resultIntent.putExtra("description", description);
            resultIntent.putExtra("deadline", deadline);
            resultIntent.putExtra("position", position);

            if (activityType.equals("Add")) {
                ((AddActivity) context).setResult(android.app.Activity.RESULT_OK, resultIntent);
            } else if (activityType.equals("Update")) {
                ((UpdateActivity) context).setResult(android.app.Activity.RESULT_OK, resultIntent);
            }
            ((android.app.Activity) context).finish();
        }
    }

    public void setText(@NonNull Map<View, String> map) {
        for (Map.Entry<View, String> entry : map.entrySet()) {
            View view = entry.getKey();
            String value = entry.getValue();

            if (view instanceof EditText) {
                ((EditText) view).setText(value);
            } else if (view instanceof TextView) {
                ((TextView) view).setText(value);
            }
        }
    }

}
