package edu.bluejack24_2.myapplication.notifications;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.RequiresPermission;

import edu.bluejack24_2.myapplication.R;

public class WeatherReceiver extends BroadcastReceiver {
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                case Intent.ACTION_MY_PACKAGE_REPLACED:
                    // Start notification service
                    startNotificationService(context);
                    break;
                case Intent.ACTION_TIME_TICK:
                    // Check if it's a good time to show notifications (e.g., 8 AM)
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);

                    if (hour == 8) { // Show weather notification at 8 AM
                        showDailyWeatherNotification(context);
                    }
                    break;
            }
        }
    }

    private void startNotificationService(Context context) {
        Intent serviceIntent = new Intent(context, WeatherNotificationService.class);
        // You would typically start a background service here
        // For simplicity, we're just using the notification class directly
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void showDailyWeatherNotification(Context context) {
        WeatherNotificationService notificationService = new WeatherNotificationService(context);
        notificationService.showWeatherNotification(
                context.getString(R.string.weather_notification_title),
                context.getString(R.string.weather_notification_text)
        );
    }
}