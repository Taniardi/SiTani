package edu.bluejack24_2.myapplication.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import edu.bluejack24_2.myapplication.R;
import edu.bluejack24_2.myapplication.activities.MainActivity;
import edu.bluejack24_2.myapplication.utils.Constants;

public class WeatherNotificationService {
    private Context context;
    private NotificationManagerCompat notificationManager;

    public WeatherNotificationService(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Weather Channel
            NotificationChannel weatherChannel = new NotificationChannel(
                    Constants.WEATHER_CHANNEL_ID,
                    Constants.WEATHER_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            weatherChannel.setDescription("Daily weather updates and forecasts");

            // Todo Channel
            NotificationChannel todoChannel = new NotificationChannel(
                    Constants.TODO_CHANNEL_ID,
                    Constants.TODO_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            todoChannel.setDescription("Task reminders and to-do list notifications");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(weatherChannel);
            manager.createNotificationChannel(todoChannel);
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    public void showWeatherNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.WEATHER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_weather)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    public void showTodoNotification(String title, String message, int pendingTasks) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_todo", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.TODO_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_leaf)
                .setContentTitle(title)
                .setContentText(message)
                .setSubText(pendingTasks + " tasks pending")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(2, builder.build());
    }

    public void cancelWeatherNotification() {
        notificationManager.cancel(1);
    }

    public void cancelTodoNotification() {
        notificationManager.cancel(2);
    }
}