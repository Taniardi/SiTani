package edu.bluejack24_2.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import edu.bluejack24_2.myapplication.R;
import edu.bluejack24_2.myapplication.models.TodoItem;
import edu.bluejack24_2.myapplication.utils.FirebaseHelper;


public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private TextView welcomeTextView, userNameTextView;

    // Weather Views (Placeholder untuk tim kamu)
    private TextView temperatureTextView, descriptionTextView, locationTextView;
    private MaterialCardView weatherCard;
    private ProgressBar weatherProgressBar;

    // ToDo Views
    private TextView taskCountTextView, taskSummaryTextView;
    private MaterialCardView todoSummaryCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupBottomNavigation();

        // --- (Weather, Location, Profile) ---
        // setupLocationServices(); // TODO: Location
        // loadWeatherData();       // TODO: Weather
        loadUserData();             // TODO: Auth
        loadTodoSummary();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottomNavigationView);

        welcomeTextView = findViewById(R.id.welcomeTextView);
        userNameTextView = findViewById(R.id.userNameTextView);

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
                // Stay here
                return true;
            } else if (itemId == R.id.navigation_dashboard) {
                // Dashboard mengarah ke List ToDo lengkap
                startActivity(new Intent(MainActivity.this, ToDoActivity.class));
                return true;
            } else if (itemId == R.id.navigation_notifications) {
                // TODO: Notifikasi
                Toast.makeText(this, "Notification Feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // TODO: Profile
                // startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                Toast.makeText(this, "Profile Feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    // --- TO DO SUMMARY ---
    private void loadTodoSummary() {
        FirebaseUser firebaseUser = FirebaseHelper.getCurrentUser();

        // Jika User belum login (Testing Mode), kita bisa return atau pakai dummy data
        if (firebaseUser == null) {
            taskCountTextView.setText("Guest Mode");
            taskSummaryTextView.setText("Please login to see tasks");
            return;
            // Atau bypass pakai ID dummy seperti di ToDoActivity untuk testing
        }

        FirebaseHelper.getUserTodos(firebaseUser.getUid(), task -> {
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
                taskSummaryTextView.setText("Failed to load tasks");
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
            // Logic text plural/singular
            String taskText = pendingTasks > 1 ? "tasks" : "task";
            taskSummaryTextView.setText("You have " + pendingTasks + " pending " + taskText);
        }

        // Klik Card untuk pindah ke halaman ToDoActivity
        todoSummaryCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ToDoActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        // TODO: Tim Auth akan mengisi ini untuk mengambil Nama User dari Firestore
        FirebaseUser user = FirebaseHelper.getCurrentUser();
        if (user != null) {
            // String email = user.getEmail();
            // userNameTextView.setText(email); // Sementara pakai email dulu
            userNameTextView.setText("Welcome Back!");
        }
    }

    /* private void setupLocationServices() {

    }

    private void loadWeatherData() {

    }
    */

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data saat kembali dari halaman ToDoActivity
        loadTodoSummary();

        // Pastikan menu Home terpilih saat balik ke sini
        bottomNavigation.setSelectedItemId(R.id.navigation_home);
    }
}