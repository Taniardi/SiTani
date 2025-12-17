package edu.bluejack24_2.myapplication.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import edu.bluejack24_2.myapplication.R;
import edu.bluejack24_2.myapplication.models.User;
import edu.bluejack24_2.myapplication.utils.FirebaseHelper;

public class EditProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView profileImageView;
    private TextInputEditText nameEditText, emailEditText, phoneEditText, addressEditText;
    private MaterialButton saveButton;
    private FirebaseUser currentUser;
    private User userData;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private android.widget.FrameLayout profileImageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = FirebaseHelper.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_edit_profile);

        setupImagePicker();
        initViews();
        setupToolbar();
        loadUserData();
        setupSaveButton();
        setupImageClick();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        profileImageView = findViewById(R.id.profileImageView);
        profileImageContainer = findViewById(R.id.profileImageContainer);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        saveButton = findViewById(R.id.saveButton);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .circleCrop()
                                    .into(profileImageView);
                        }
                    }
                });
    }

    private void setupImageClick() {
        profileImageContainer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    private void loadUserData() {
        if (currentUser != null) {
            emailEditText.setText(currentUser.getEmail());

            FirebaseHelper.getUser(currentUser.getUid(), task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    userData = task.getResult().toObject(User.class);
                    if (userData != null) {
                        nameEditText.setText(userData.getName());
                        phoneEditText.setText(userData.getPhone());
                        addressEditText.setText(userData.getAddress());

                        if (!userData.getProfileImageUrl().isEmpty()) {
                            Glide.with(this)
                                    .load(userData.getProfileImageUrl())
                                    .circleCrop()
                                    .into(profileImageView);
                        }
                    }
                } else {
                    userData = new User();
                    userData.setUserId(currentUser.getUid());
                    userData.setEmail(currentUser.getEmail());
                }
            });
        }
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();

            if (name.isEmpty()) {
                nameEditText.setError("Name is required");
                nameEditText.requestFocus();
                return;
            }

            if (userData == null) {
                userData = new User();
                userData.setUserId(currentUser.getUid());
                userData.setEmail(currentUser.getEmail());
                userData.setCreatedAt(System.currentTimeMillis());
            }

            userData.setName(name);
            userData.setPhone(phone);
            userData.setAddress(address);

            saveButton.setEnabled(false);
            saveButton.setText("Saving...");

            if (selectedImageUri != null) {
                convertImageToBase64AndSave();
            } else {
                saveUserData();
            }
        });
    }

    private void convertImageToBase64AndSave() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            int maxSize = 800;
            float scale = Math.min(((float)maxSize / bitmap.getWidth()), ((float)maxSize / bitmap.getHeight()));
            int newWidth = Math.round(bitmap.getWidth() * scale);
            int newHeight = Math.round(bitmap.getHeight() * scale);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            userData.setProfileImageUrl("data:image/jpeg;base64," + base64Image);
            saveUserData();

            bitmap.recycle();
            resizedBitmap.recycle();

        } catch (Exception e) {
            saveButton.setEnabled(true);
            saveButton.setText("Save Changes");
            Toast.makeText(this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserData() {
        FirebaseHelper.saveUser(userData, task -> {
            saveButton.setEnabled(true);
            saveButton.setText("Save Changes");

            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
