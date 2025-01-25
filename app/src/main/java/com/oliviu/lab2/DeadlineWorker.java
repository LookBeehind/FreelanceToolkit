package com.oliviu.lab2;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DeadlineWorker extends Worker {

    public DeadlineWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Call the method to check and send notifications
        DeadlineNotificationService service = new DeadlineNotificationService(getApplicationContext());
        service.checkAndSendNotifications();

        // Return success
        return Result.success();
    }
}
