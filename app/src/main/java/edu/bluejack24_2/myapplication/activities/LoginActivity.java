package edu.bluejack24_2.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import edu.bluejack24_2.myapplication.R;
import edu.bluejack24_2.myapplication.utils.FirebaseHelper;

public class LoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton btnContinue; // ID berubah dari XML sebelumnya
    private TextView tvSignUpLink;      // ID berubah dari XML sebelumnya
    private TextView tvForgotPassword;  // Elemen baru
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pastikan nama layout xml Anda benar di sini
        setContentView(R.layout.activity_login);

        initViews();
        setupClickListeners();
        checkCurrentUser();
    }

    // Inisialisasi view berdasarkan ID di XML baru
    private void initViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // ID baru sesuai XML terakhir
        btnContinue = findViewById(R.id.btnContinue);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Listener untuk tombol "Continue"
        btnContinue.setOnClickListener(v -> attemptLogin());

        // Listener untuk link "Sign Up"
        tvSignUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            // Opsi: Hapus finish() jika ingin user bisa kembali ke login dengan tombol back
            // finish();
        });

        // Listener baru untuk link "Forgot Password"
        tvForgotPassword.setOnClickListener(v -> {
            // TODO: Implementasi logika lupa password di sini
            // Misalnya, buka ForgotPasswordActivity
            Toast.makeText(LoginActivity.this, "Fitur Lupa Password belum diimplementasikan", Toast.LENGTH_SHORT).show();
            // Contoh intent:
            // Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            // startActivity(intent);
        });
    }

    // --- Bagian di bawah ini tidak banyak berubah dari kode asli Anda ---

    private void checkCurrentUser() {
        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
        if (currentUser != null) {
            goToMainActivity();
        }
    }

    private void attemptLogin() {
        // Reset errors
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        // Get values
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        // Pastikan R.string.error_password ada di strings.xml
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordInputLayout.setError(getString(R.string.error_password));
            focusView = passwordInputLayout;
            cancel = true;
        }

        // Check for a valid email
        // Pastikan R.string.error_email ada di strings.xml
        if (TextUtils.isEmpty(email) || !isEmailValid(email)) {
            emailInputLayout.setError(getString(R.string.error_email));
            focusView = emailInputLayout;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            loginUser(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void loginUser(String email, String password) {
        FirebaseHelper.loginUser(email, password, task -> {
            showProgress(false);
            if (task.isSuccessful() && task.getResult() != null) {
                // Login successful
                goToMainActivity();
            } else {
                // Login failed
                // Pastikan R.string.error_login ada di strings.xml
                String errorMessage = task.getException() != null ?
                        task.getException().getMessage() : getString(R.string.error_login);
                showError(errorMessage);
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        // Nonaktifkan semua input saat loading agar user tidak spam klik
        btnContinue.setEnabled(!show);
        emailEditText.setEnabled(!show);
        passwordEditText.setEnabled(!show);
        tvSignUpLink.setEnabled(!show);
        tvForgotPassword.setEnabled(!show);
    }

    private void showError(String message) {
        if (message == null) return;

        if (message.toLowerCase().contains("password")) {
            passwordInputLayout.setError(message);
            passwordEditText.requestFocus();
        } else if (message.toLowerCase().contains("email") || message.toLowerCase().contains("user")) {
            emailInputLayout.setError(message);
            emailEditText.requestFocus();
        } else {
            // Error umum, tampilkan di email input atau gunakan Toast/Snackbar
            emailInputLayout.setError(message);
            emailEditText.requestFocus();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
}