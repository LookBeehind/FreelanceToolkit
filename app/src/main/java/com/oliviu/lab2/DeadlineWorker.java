package com.oliviu.lab2;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class DeadlineWorker extends Worker {

    public DeadlineWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("DeadlineWorker", "Worker started");

        // Perform your task (e.g., sending a notification)
        DeadlineNotificationService service = new DeadlineNotificationService(getApplicationContext());
        service.checkAndSendNotifications();

        // Reschedule after completion
        scheduleNextWork();

        return Result.success();
    }

    private void scheduleNextWork() {
        OneTimeWorkRequest nextWork = new OneTimeWorkRequest.Builder(DeadlineWorker.class)
                .setInitialDelay(1, TimeUnit.MINUTES) // Delay before next execution
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(nextWork);
    }
}
