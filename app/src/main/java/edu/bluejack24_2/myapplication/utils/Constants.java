package edu.bluejack24_2.myapplication.utils;

public class Constants {

    // API Configuration
    public static final String OPENWEATHER_API_BASE_URL = "https://api.openweathermap.org/data/2.5/";
    public static final String OPENWEATHER_API_KEY = "e2a9db18c308a778e64de919b4d90dcb"; // Replace with actual API key

    // Firebase Constants
    public static final String USERS_COLLECTION = "users";
    public static final String TODOS_COLLECTION = "todos";

    // SharedPreferences
    public static final String PREFS_NAME = "FarmerAppPrefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_NAME = "name";

    // Request Codes
    public static final int LOCATION_PERMISSION_REQUEST = 1001;

    // Weather Notification
    public static final String WEATHER_CHANNEL_ID = "weather_channel";
    public static final String WEATHER_CHANNEL_NAME = "Weather Updates";
    public static final String TODO_CHANNEL_ID = "todo_channel";
    public static final String TODO_CHANNEL_NAME = "Task Reminders";

    // Weather Icons (can be replaced with actual drawable resources)
    public static final int DEFAULT_WEATHER_ICON = android.R.drawable.ic_menu_info_details;
}