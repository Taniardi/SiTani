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
import com.google.firebase.firestore.DocumentSnapshot;
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

    // Variabel global untuk menyimpan user yang sedang login
    private FirebaseUser currentUser;

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

        // --- (Weather, Location, Profile) ---
        // setupLocationServices(); // TODO: Location
        // loadWeatherData();       // TODO: Weather

        // Kita kirim currentUser ke method ini agar tidak perlu memanggil getCurrentUser() berulang kali
        loadUserData(currentUser);
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
                // TODO: Profile Activity (Tempat untuk Logout nanti)
                // startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                Toast.makeText(this, "Profile Feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
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

    // --- LOAD USER DATA (Mengambil Nama dari Firestore) ---
    private void loadUserData(FirebaseUser user) {
        // Menggunakan method getUser yang sudah di-uncomment di FirebaseHelper
        FirebaseHelper.getUser(user.getUid(), task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Ambil field "name" dari dokumen Firestore
                    String name = document.getString("name");
                    if (name != null && !name.isEmpty()) {
                        userNameTextView.setText(name);
                    } else {
                        // Jika field name kosong, gunakan email sebagai fallback
                        userNameTextView.setText(user.getEmail());
                    }
                } else {
                    // Dokumen tidak ditemukan (jarang terjadi jika alur register benar)
                    userNameTextView.setText("Welcome User!");
                }
            } else {
                // Gagal mengambil data (misal koneksi internet mati)
                Toast.makeText(MainActivity.this, "Failed to fetch profile name", Toast.LENGTH_SHORT).show();
                userNameTextView.setText("Welcome!");
            }
        });
    }

    /* private void setupLocationServices() {

    }

    private void loadWeatherData() {

    }
    */

    @Override
    protected void onResume() {
        super.onResume();
        // Cek lagi user saat resume, untuk berjaga-jaga
        currentUser = FirebaseHelper.getCurrentUser();

        if (currentUser != null) {
            // Refresh data ToDo saat kembali dari halaman ToDoActivity
            loadTodoSummary(currentUser);

            // Pastikan menu Home terpilih saat balik ke sini
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        } else {
            // Jika tiba-tiba sesi habis saat resume, panggil onCreate lagi untuk diredirect
            recreate();
        }
    }
}