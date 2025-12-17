package edu.bluejack24_2.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;

import edu.bluejack24_2.myapplication.R;
import edu.bluejack24_2.myapplication.models.User;
import edu.bluejack24_2.myapplication.utils.FirebaseHelper;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private TextView userNameTextView, userEmailTextView;
    private ImageView profileImageView;
    private MaterialCardView editProfileCard, logoutCard;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = FirebaseHelper.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_profile);

        initViews();
        setupBottomNavigation();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigationView);
        userNameTextView = findViewById(R.id.userNameTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        profileImageView = findViewById(R.id.profileImageView);
        editProfileCard = findViewById(R.id.editProfileCard);
        logoutCard = findViewById(R.id.logoutCard);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navigation_profile);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_dashboard) {
                startActivity(new Intent(ProfileActivity.this, ToDoActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_notifications) {
                Toast.makeText(this, "Notification Feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        if (currentUser != null) {
            userEmailTextView.setText(currentUser.getEmail());

            FirebaseHelper.getUser(currentUser.getUid(), task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    User user = task.getResult().toObject(User.class);
                    if (user != null) {
                        if (user.getName() != null && !user.getName().isEmpty()) {
                            userNameTextView.setText(user.getName());
                        } else {
                            userNameTextView.setText("User");
                        }

                        if (!user.getProfileImageUrl().isEmpty()) {
                            Glide.with(this)
                                    .load(user.getProfileImageUrl())
                                    .circleCrop()
                                    .into(profileImageView);
                        }
                    }
                } else {
                    userNameTextView.setText("User");
                }
            });
        }
    }

    private void setupClickListeners() {
        editProfileCard.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        logoutCard.setOnClickListener(v -> {
            FirebaseHelper.logoutUser();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.navigation_profile);
        loadUserData();
    }
}
