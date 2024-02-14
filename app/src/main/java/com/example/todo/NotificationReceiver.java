package com.example.todo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // This method will be called when the alarm is triggered
        Log.d("AlarmReceiver", "Alarm received");

        // Add code here to show a notification or perform any action you want
        // For example, you can use NotificationManager to show a notification
        showNotification(context, "Task Reminder", "Don't forget to complete your task!");
    }

    private void showNotification(Context context, String title, String content) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("task_channel", "Task Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_channel")
                .setSmallIcon(R.drawable.notif)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Notification notification = builder.build();

        // You can use a unique notification ID to update or cancel the notification later if needed
        int notificationId = 1;

        notificationManager.notify(notificationId, notification);
    }
}
