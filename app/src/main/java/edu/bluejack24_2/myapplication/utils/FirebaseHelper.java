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

public class FirebaseHelper {

    // Inisialisasi Firebase
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Nama Collection di Firestore (Hardcode disini biar ga butuh file Constants)
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

    // --- TO-DO ---

    // 1. Simpan (Create) Task Baru
    public static void saveTodoItem(TodoItem todoItem, OnCompleteListener<Void> onCompleteListener) {
        db.collection(TODOS_COLLECTION)
                .document(todoItem.getId())
                .set(todoItem)
                .addOnCompleteListener(onCompleteListener);
    }

    // 2. Update Task (Edit Judul/Deskripsi/Status Completed)
    public static void updateTodoItem(TodoItem todoItem, OnCompleteListener<Void> onCompleteListener) {
        db.collection(TODOS_COLLECTION)
                .document(todoItem.getId())
                .set(todoItem) // .set() akan menimpa data lama dengan ID yang sama (Update)
                .addOnCompleteListener(onCompleteListener);
    }

    // 3. Hapus Task
    public static void deleteTodoItem(String todoId, OnCompleteListener<Void> onCompleteListener) {
        db.collection(TODOS_COLLECTION)
                .document(todoId)
                .delete()
                .addOnCompleteListener(onCompleteListener);
    }

    // 4. Ambil Semua Task milik User Tertentu (Read)
    public static void getUserTodos(String userId, OnCompleteListener<QuerySnapshot> onCompleteListener) {
        db.collection(TODOS_COLLECTION)
                .whereEqualTo("userId", userId) // Filter hanya punya user yg login
                .orderBy("createdAt", Query.Direction.DESCENDING) // Urutkan dari yang terbaru
                .get()
                .addOnCompleteListener(onCompleteListener);
    }


    /*
    public static void saveUser(User user, OnCompleteListener<Void> onCompleteListener) {
        db.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .set(user)
                .addOnCompleteListener(onCompleteListener);
    }

    public static void getUser(String userId, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(onCompleteListener);
    }
    */
}