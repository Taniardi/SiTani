package edu.bluejack24_2.myapplication.activities;

import edu.bluejack24_2.myapplication.utils.WeatherApiClient;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import edu.bluejack24_2.myapplication.R;
import edu.bluejack24_2.myapplication.models.TodoItem;
import edu.bluejack24_2.myapplication.models.WeatherResponse;
import edu.bluejack24_2.myapplication.utils.Constants; // Pastikan punya file ini untuk API KEY
import edu.bluejack24_2.myapplication.utils.FirebaseHelper;
import edu.bluejack24_2.myapplication.utils.WeatherApiClient; // Pastikan punya file ini
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private TextView welcomeTextView, userNameTextView;

    // Weather Views
    private TextView temperatureTextView, descriptionTextView, locationTextView;
    private MaterialCardView weatherCard;
    private ProgressBar weatherProgressBar;

    // ToDo Views
    private TextView taskCountTextView, taskSummaryTextView;
    private MaterialCardView todoSummaryCard;

    // Variabel global untuk menyimpan user yang sedang login
    private FirebaseUser currentUser;
    // Location & User Data
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- LANGKAH KRESIAL: CEK STATUS LOGIN ---
        currentUser = FirebaseHelper.getCurrentUser();

        if (currentUser == null) {
            // Jika user belum login, segera arahkan ke LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            // Gunakan flag ini agar user tidak bisa kembali ke MainActivity dengan tombol Back
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Hentikan MainActivity agar tidak lanjut memuat layout
            return; // Keluar dari onCreate
        }

        // Jika sampai di sini, berarti user SUDAH login.
        // Lanjutkan memuat tampilan.
        setContentView(R.layout.activity_main);

        initViews();
        setupBottomNavigation();

        // --- Init Services ---
        setupLocationServices();

        // --- Load Data ---
        loadWeatherData();
        loadUserData();
        loadTodoSummary(currentUser);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottomNavigationView);

        welcomeTextView = findViewById(R.id.welcomeTextView);
        userNameTextView = findViewById(R.id.userNameTextView);
        // Set teks loading sementara sambil menunggu data dari Firestore
        userNameTextView.setText("Loading...");

        // Weather IDs
        temperatureTextView = findViewById(R.id.temperatureTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        locationTextView = findViewById(R.id.locationTextView);
        weatherCard = findViewById(R.id.weatherCard);
        weatherProgressBar = findViewById(R.id.weatherProgressBar);

        // Todo IDs
        taskCountTextView = findViewById(R.id.taskCountTextView);
        taskSummaryTextView = findViewById(R.id.taskSummaryTextView);
        todoSummaryCard = findViewById(R.id.todoSummaryCard);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_dashboard) {
                startActivity(new Intent(MainActivity.this, ToDoActivity.class));
                return true;
            } else if (itemId == R.id.navigation_notifications) {
                Toast.makeText(this, "Notification Feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    // --- LOCATION SERVICES ---

    private void setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void loadWeatherData() {
        // Cek permission lokasi terlebih dahulu
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Showing default weather.", Toast.LENGTH_SHORT).show();
                loadWeatherForDefaultLocation();
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                loadWeatherForLocation(location.getLatitude(), location.getLongitude());
            } else {
                // Jika lokasi null (misal GPS mati), pakai default
                loadWeatherForDefaultLocation();
            }
        });
    }

    private void loadWeatherForDefaultLocation() {
        // Default Jakarta
        loadWeatherForLocation(-6.2088, 106.8456);
    }

    // --- WEATHER API LOGIC ---

    private void loadWeatherForLocation(double lat, double lon) {
        if (weatherProgressBar != null) weatherProgressBar.setVisibility(View.VISIBLE);

        String apiKey = Constants.OPENWEATHER_API_KEY;

        WeatherApiClient.getWeatherService()
                .getCurrentWeather(lat, lon, apiKey, "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (weatherProgressBar != null) weatherProgressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            updateWeatherUI(response.body());
                        } else {
                            locationTextView.setText("Weather Info Unavailable");
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        if (weatherProgressBar != null) weatherProgressBar.setVisibility(View.GONE);
                        locationTextView.setText("Network Error");
                        Toast.makeText(MainActivity.this, "Failed to load weather", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWeatherUI(WeatherResponse weather) {
        if (weather == null) return;

        // Update Location Name
        String cityName = weather.getName();
        locationTextView.setText(cityName != null ? cityName : "Unknown Location");

        // Update Temp
        if (weather.getMain() != null) {
            double temp = weather.getMain().getTemp();
            temperatureTextView.setText(String.format("%.1f°C", temp));
        }

        // Update Description
        if (weather.getWeather() != null && weather.getWeather().length > 0) {
            String desc = weather.getWeather()[0].getDescription();
            // Capitalize first letter
            if (desc != null && !desc.isEmpty()) {
                desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
            }
            descriptionTextView.setText(desc);
        }
    }

    // --- TO DO SUMMARY ---
    // Menerima parameter user yang sudah dipastikan login
    private void loadTodoSummary(FirebaseUser user) {
        // Kode "Guest Mode" dihapus karena tidak mungkin null di sini

        FirebaseHelper.getUserTodos(user.getUid(), task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    List<com.google.firebase.firestore.DocumentSnapshot> documents = querySnapshot.getDocuments();

                    int totalTasks = documents.size();
                    int pendingTasks = 0;

                    for (com.google.firebase.firestore.DocumentSnapshot doc : documents) {
                        TodoItem todoItem = doc.toObject(TodoItem.class);
                        if (todoItem != null && !todoItem.isCompleted()) {
                            pendingTasks++;
                        }
                    }
                    updateTodoSummary(totalTasks, pendingTasks);
                }
            } else {
                taskSummaryTextView.setText("Failed to load tasks summary");
            }
        });
    }

    private void updateTodoSummary(int totalTasks, int pendingTasks) {
        taskCountTextView.setText(totalTasks + " Tasks Total");

        if (totalTasks == 0) {
            taskSummaryTextView.setText("No tasks yet. Start being productive!");
        } else if (pendingTasks == 0) {
            taskSummaryTextView.setText("All tasks completed! Great job!");
        } else {
            String taskText = pendingTasks > 1 ? "tasks" : "task";
            taskSummaryTextView.setText("You have " + pendingTasks + " pending " + taskText);
        }

        todoSummaryCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ToDoActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseHelper.getCurrentUser();
        if (user != null) {
            userNameTextView.setText("Welcome Back!");
            // Nanti bisa fetch nama user dari Firestore 'users' collection di sini
        } else {
            userNameTextView.setText("Guest");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodoSummary(currentUser);
        bottomNavigation.setSelectedItemId(R.id.navigation_home);
    }
}