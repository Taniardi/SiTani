package edu.bluejack24_2.myapplication.models;

public class User {
    private String userId;
    private String name;
    private String email;
    // Field phone dan address tetap dipertahankan agar bisa diisi nanti di halaman "Edit Profile"
    // Saat registrasi awal, nilainya akan null.
    private String phone;
    private String address;
    private long createdAt;

    // Default Constructor (Wajib diperlukan oleh Firebase Firestore untuk deserialisasi data)
    public User() {
        // Required for Firebase
    }

    // --- KONSTRUKTOR UTAMA UNTUK REGISTRASI ---
    // Gunakan konstruktor ini di RegisterActivity.
    // Hanya menerima data yang wajib saat mendaftar.
    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        // Phone dan Address tidak diisi di sini, jadi akan bernilai null secara default.
        // Waktu pembuatan diset saat objek ini dibuat.
        this.createdAt = System.currentTimeMillis();
    }

    // (Opsional) Konstruktor lengkap jika suatu saat diperlukan untuk update data menyeluruh
    public User(String userId, String name, String email, String phone, String address) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        // Jika createdAt sudah ada (misal dari database), sebaiknya jangan ditimpa dengan waktu sekarang.
        // Jika ini objek baru, bisa gunakan: this.createdAt = System.currentTimeMillis();
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        // Menghindari return null jika ingin menampilkan string kosong di UI
        if (phone == null) return "";
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        // Menghindari return null jika ingin menampilkan string kosong di UI
        if (address == null) return "";
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}