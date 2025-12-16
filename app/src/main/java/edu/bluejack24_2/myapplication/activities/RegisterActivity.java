package edu.bluejack24_2.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import edu.bluejack24_2.myapplication.R;
import edu.bluejack24_2.myapplication.models.User;
import edu.bluejack24_2.myapplication.utils.FirebaseHelper;

public class RegisterActivity extends AppCompatActivity {

    // Komponen UI yang disesuaikan dengan XML baru
    private TextInputLayout nameInputLayout, emailInputLayout, passwordInputLayout;
    private TextInputEditText nameEditText, emailEditText, passwordEditText;
    private MaterialButton btnContinue; // ID baru sesuai XML
    private TextView tvSignInLink;      // ID baru sesuai XML
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pastikan nama layout xml Anda benar di sini
        setContentView(R.layout.activity_register);

        initViews();
        setupClickListeners();
    }

    // Inisialisasi view berdasarkan ID di XML baru
    private void initViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // ID baru sesuai XML terakhir
        btnContinue = findViewById(R.id.btnContinue);
        tvSignInLink = findViewById(R.id.tvSignInLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Listener untuk tombol "Continue"
        btnContinue.setOnClickListener(v -> attemptRegister());

        // Listener untuk link "Sign in"
        tvSignInLink.setOnClickListener(v -> {
            // Kembali ke LoginActivity dan tutup activity ini
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            // Tambahkan flag agar tidak menumpuk activity di stack
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegister() {
        // Reset semua error sebelum validasi
        nameInputLayout.setError(null);
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        // Dapatkan nilai dari input fields
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Validasi Password
        // Pastikan R.string.error_password ada di strings.xml
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordInputLayout.setError(getString(R.string.error_password));
            focusView = passwordInputLayout;
            cancel = true;
        }

        // Validasi Email
        // Pastikan R.string.error_email ada di strings.xml
        if (TextUtils.isEmpty(email) || !isEmailValid(email)) {
            emailInputLayout.setError(getString(R.string.error_email));
            focusView = emailInputLayout;
            cancel = true;
        }

        // Validasi Nama (Tidak boleh kosong)
        // Pastikan R.string.error_fields ada di strings.xml
        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError(getString(R.string.error_fields));
            focusView = nameInputLayout;
            cancel = true;
        }

        if (cancel) {
            // Jika ada error, fokuskan ke field yang error pertama kali
            focusView.requestFocus();
        } else {
            // Jika validasi sukses, mulai proses registrasi
            showProgress(true);
            // Panggil fungsi register hanya dengan name, email, password
            registerUser(name, email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Fungsi register disederhanakan (menghapus parameter phone dan address)
    private void registerUser(String name, String email, String password) {
        // 1. Buat akun di Firebase Authentication
        FirebaseHelper.registerUser(email, password, task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = task.getResult().getUser();
                if (firebaseUser != null) {
                    // 2. Buat objek User untuk disimpan di Firestore
                    // PENTING: Asumsikan konstruktor User Anda sekarang bisa menerima null/kosong untuk phone dan address
                    // Jika tidak, Anda harus mengubah model User Anda dulu.
                    User user = new User(
                            firebaseUser.getUid(),
                            name,
                            email
                    );

                    // 3. Simpan data user tambahan ke Firestore
                    FirebaseHelper.saveUser(user, saveTask -> {
                        showProgress(false);
                        if (saveTask.isSuccessful()) {
                            // Registrasi dan penyimpanan data sukses
                            Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                            // Lanjut ke MainActivity
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Gagal menyimpan ke Firestore
                            String errorMsg = saveTask.getException() != null ? saveTask.getException().getMessage() : "Failed to save user data.";
                            showError(errorMsg);
                        }
                    });
                } else {
                    showProgress(false);
                    showError(getString(R.string.error_register));
                }
            } else {
                // Gagal membuat akun Auth
                showProgress(false);
                // Pastikan R.string.error_register ada di strings.xml
                String errorMessage = task.getException() != null ?
                        task.getException().getMessage() : getString(R.string.error_register);
                showError(errorMessage);
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        // Nonaktifkan semua input saat loading
        btnContinue.setEnabled(!show);
        setFieldsEnabled(!show);
        tvSignInLink.setEnabled(!show);
    }

    private void setFieldsEnabled(boolean enabled) {
        nameEditText.setEnabled(enabled);
        emailEditText.setEnabled(enabled);
        passwordEditText.setEnabled(enabled);
    }

    private void showError(String message) {
        if (message == null) return;

        if (message.toLowerCase().contains("email") || message.toLowerCase().contains("user")) {
            emailInputLayout.setError(message);
            emailEditText.requestFocus();
        } else if (message.toLowerCase().contains("password")) {
            passwordInputLayout.setError(message);
            passwordEditText.requestFocus();
        } else {
            // Error umum, tampilkan di name atau gunakan Toast
            nameInputLayout.setError(message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
}