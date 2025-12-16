package edu.bluejack24_2.myapplication.utils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import edu.bluejack24_2.myapplication.models.TodoItem;
// PENTING: Tambahkan import untuk User model Anda di sini
// Pastikan package-nya sesuai dengan lokasi file User.java Anda
import edu.bluejack24_2.myapplication.models.User;

public class FirebaseHelper {

    // Inisialisasi Firebase
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Nama Collection di Firestore
    private static final String TODOS_COLLECTION = "todos";
    private static final String USERS_COLLECTION = "users";

    // --- BAGIAN AUTH (STANDARD) ---
    public static FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public static void loginUser(String email, String password, OnCompleteListener<AuthResult> onCompleteListener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(onCompleteListener);
    }

    public static void registerUser(String email, String password, OnCompleteListener<AuthResult> onCompleteListener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(onCompleteListener);
    }

    public static void logoutUser() {
        mAuth.signOut();
    }

    // --- TO-DO (Bagian ini tidak diubah) ---
    public static void saveTodoItem(TodoItem todoItem, OnCompleteListener<Void> onCompleteListener) {
        db.collection(TODOS_COLLECTION)
                .document(todoItem.getId())
                .set(todoItem)
                .addOnCompleteListener(onCompleteListener);
    }

    public static void updateTodoItem(TodoItem todoItem, OnCompleteListener<Void> onCompleteListener) {
        db.collection(TODOS_COLLECTION)
                .document(todoItem.getId())
                .set(todoItem)
                .addOnCompleteListener(onCompleteListener);
    }

    public static void deleteTodoItem(String todoId, OnCompleteListener<Void> onCompleteListener) {
        db.collection(TODOS_COLLECTION)
                .document(todoId)
                .delete()
                .addOnCompleteListener(onCompleteListener);
    }

    public static void getUserTodos(String userId, OnCompleteListener<QuerySnapshot> onCompleteListener) {
        db.collection(TODOS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(onCompleteListener);
    }

    // --- BAGIAN USER DATA (KOMENTAR SUDAH DIHAPUS) ---

    // Method ini sekarang sudah aktif dan bisa dipanggil dari RegisterActivity
    public static void saveUser(User user, OnCompleteListener<Void> onCompleteListener) {
        db.collection(USERS_COLLECTION)
                // Menggunakan ID dari Auth sebagai ID dokumen agar sinkron
                .document(user.getUserId())
                .set(user)
                .addOnCompleteListener(onCompleteListener);
    }

    // Method ini juga diaktifkan (mungkin berguna nanti untuk mengambil profil)
    public static void getUser(String userId, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(onCompleteListener);
    }

}